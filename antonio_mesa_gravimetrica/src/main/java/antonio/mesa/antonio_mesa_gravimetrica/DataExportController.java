package antonio.mesa.antonio_mesa_gravimetrica;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

// This controller class is specialized for handling the recording and exporting of sensors data to a CSV file. 
// It provides two main endpoints: one to start the recording process and another to stop it and download the recorded data as a CSV file. The recorded data is also saved in a database for historical reference.

@RestController
@RequestMapping("/export")
public class DataExportController {

    private final MqttListener mqttListener;
    
    /**
     * Class registroService:
     * Is a service that handles the interaction with the database, 
     * specifically for saving the recorded data in the historical record.
     */
    @Autowired
    private RegistroService registroService;

    private boolean isRecording = false;
    private final List<String[]> recordedData = Collections.synchronizedList(new ArrayList<>());
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // DELTA_T is the time interval in seconds between each recorded snapshot, which is used to calculate the acceleration based on the change in speed over time.
    private final double DELTA_T = 0.25;

    public DataExportController(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startRecording(HttpSession session) {
        if (session.getAttribute("currentUser") == null) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        if (!isRecording) {
            recordedData.clear();
            isRecording = true;
            
            // Every DELTA_T seconds, captures a snapshot which contains the actual data in the MqttListener and stores it in the "recordedData" list within the RAM.
            // This process continues until the user stops the recording.
            new Thread(() -> {
                while (isRecording) {
                    captureSnapshot();
                    try { 
                        Thread.sleep(250); 
                    } catch (InterruptedException e) { 
                        Thread.currentThread().interrupt(); 
                        break; 
                    }
                }
            }).start();
        }
        return ResponseEntity.ok("Recording started");
    }

    private void captureSnapshot() {
        SensorData adxl = mqttListener.getLastSensorData();
        Sensor2Data freq = mqttListener.getLastSensor2Data();
        
        // 1. CORRECCIÓN AQUÍ: Recibimos el Mapa nativo con clave Integer desde MqttListener
        Map<Integer, Sensor3Data> flows = mqttListener.getAllFlowData();
        
        Sensor4Data engine = mqttListener.getLastSensor4Data();
        Sensor5Data dropper = mqttListener.getLastSensor5Data();
        Sensor6Data weight = mqttListener.getLastSensor6Data();
        Sensor7Data speed = mqttListener.getLastSensor7Data();

        String[] row = new String[12]; 
        
        row[0] = LocalDateTime.now().format(timeFormatter);
        row[1] = formatDecimal(adxl.getPitch());
        row[2] = formatDecimal(adxl.getRoll());
        row[3] = formatDecimal(freq.getFrecuency());
        row[4] = formatDecimal(engine.getGaugeValue());
        row[5] = formatDecimal(dropper.getDropperValue());

        // === COLA DE TARA COMPARTIDA SIN ERRORES DE COMPILACIÓN ===
        float pesoNetoTarado = 0.0f;
        if (weight != null && weight.getWeightValue() != null) {
            float pesoCrudo = weight.getWeightValue().floatValue();
            
            // Extraemos la tara global del SensorController de forma estática
            pesoNetoTarado = pesoCrudo - SensorController.getValorTara(); 
            if (pesoNetoTarado < 0.0f) {
                pesoNetoTarado = 0.0f;
            }
        }
        row[6] = formatDecimal(pesoNetoTarado);
        
        row[7] = formatDecimal(speed.getSpeedValue());
        row[8] = formatDecimal(calculateAcceleration(speed.getSpeedValue()));

        // 2. CORRECCIÓN AQUÍ: Buscamos en el mapa usando el tipo primitivo int/Integer directamente
        for (int i = 1; i <= 3; i++) {
            Sensor3Data f = flows.get(i); // Buscamos usando el número entero directamente
            row[8 + i] = (f != null) ? formatDecimal(f.getFlowRate()) : "0,0";
        }
        
        recordedData.add(row);
    }

    private float calculateAcceleration(float currentSpeed) {
        synchronized (recordedData) {
            if (recordedData.isEmpty()) return 0.0f;
            try {
                String[] lastRow = recordedData.get(recordedData.size() - 1);
                float lastSpeed = Float.parseFloat(lastRow[7].replace(',', '.'));
                return (float) ((currentSpeed - lastSpeed) / DELTA_T);
            } catch (Exception e) {
                return 0.0f;
            }
        }
    }

    private String formatDecimal(Object value) {
        if (value == null) return "0,0";
        return String.valueOf(value).replace('.', ',');
    }

    //////////////////////////////////////////////////////////////////////////////////////////// de aquí hasta arriba congelado

    @GetMapping("/download")
    public ResponseEntity<byte[]> stopAndDownload(HttpSession session) {
        // 1. Detenemos la grabación inmediatamente
        isRecording = false;
        
        if (session.getAttribute("currentUser") == null) return ResponseEntity.status(403).build();

        // 2. Generamos el contenido del CSV primero
        StringBuilder csv = new StringBuilder();
        csv.append("Fecha_Hora_MS;Pitch;Roll;Vibracion_Hz;Consumo_Motor_W;Consumo_Total_W;Peso_entrada_g;Velocidad_RPM;Aceleracion_RPM_s2;Caudal_1;Caudal_2;Caudal_3\n");
        
        String csvFinal;
        synchronized (recordedData) {
            for (String[] row : recordedData) {
                csv.append(String.join(";", row)).append("\n");
            }
            csvFinal = csv.toString();
            // Limpiamos los datos después de generar el String para el siguiente ensayo
            recordedData.clear();
        }

        // 3. Guardamos EXACTAMENTE lo mismo que se descarga en la BBDD
        try {
            // Pasamos el csvFinal al servicio para asegurar integridad absoluta
            registroService.guardarEnHistorico(csvFinal);
            System.out.println("✅ Histórico blindado en BBDD correctamente.");
        } catch (Exception e) {
            System.err.println("❌ Error al guardar histórico en BBDD: " + e.getMessage());
        }

        // 4. Enviamos el archivo al usuario
        byte[] bytes = csvFinal.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ensayo_mesa_" + System.currentTimeMillis() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}