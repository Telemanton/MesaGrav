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

@RestController
@RequestMapping("/export")
public class DataExportController {

    private final MqttListener mqttListener;
    
    //////////////////////////////////////////////////////////////////////////////////////////// 
    /// DataExportController class documentation
    ////////////////////////////////////////////////////////////////////////////////////////////
    /// The DataExportController class is responsible for handling the recording and exporting of sensor data in CSV format.
    /// It provides two main endpoints:
    /// - POST /export/start: Starts the recording of sensor data. It captures snapshots of the current sensor data every 250 milliseconds and stores them in memory until the recording is stopped.
    /// - GET /export/download: Stops the recording and generates a CSV file with the recorded
    /// data. The CSV file is then sent to the user as a downloadable response. Additionally, the same CSV content is saved in the database for historical records.
    ///
    @Autowired
    private RegistroService registroService;

    private boolean isRecording = false;
    private final List<String[]> recordedData = Collections.synchronizedList(new ArrayList<>());
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
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
            
            // Every 250ms, captures a snapshot which contains the actual data in the MqttListener and stores it in the "recordedData" list within the RAM.
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
        row[6] = formatDecimal(weight.getWeightValue());
        
        double currentSpeed = (speed.getSpeedValue() != null) ? speed.getSpeedValue() : 0.0;
        row[7] = formatDecimal(currentSpeed);
        row[8] = formatDecimal(calculateAcceleration(currentSpeed));

        for (int i = 1; i <= 3; i++) {
            Sensor3Data f = flows.get(i);
            row[8 + i] = (f != null) ? formatDecimal(f.getFlowRate()) : "0,0";
        }
        
        recordedData.add(row);
    }

    private double calculateAcceleration(double currentSpeed) {
        synchronized (recordedData) {
            if (recordedData.isEmpty()) return 0.0;
            try {
                String lastRow[] = recordedData.get(recordedData.size() - 1);
                double lastSpeed = Double.parseDouble(lastRow[7].replace(',', '.'));
                return (currentSpeed - lastSpeed) / DELTA_T;
            } catch (Exception e) {
                return 0.0;
            }
        }
    }

    private String formatDecimal(Object value) {
        if (value == null) return "0,0";
        return String.valueOf(value).replace('.', ',');
    }

    //////////////////////////////////////////////////////////////////////////////////////////// 

    @GetMapping("/download")
    public ResponseEntity<byte[]> stopAndDownload(HttpSession session) {
        // 1. Detenemos la grabación inmediatamente
        isRecording = false;
        
        if (session.getAttribute("currentUser") == null) return ResponseEntity.status(403).build();

        // 2. Generamos el contenido del CSV primero
        StringBuilder csv = new StringBuilder();
        csv.append("Fecha_Hora_MS;Pitch;Roll;Vibracion_Hz;Motor_Load;Dropper_Load;Peso_Kg;Velocidad_RPM;Aceleracion_RPM_s2;Caudal_1;Caudal_2;Caudal_3\n");
        
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