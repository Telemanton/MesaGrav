package antonio.mesa.antonio_mesa_gravimetrica;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private boolean isRecording = false;
    private final List<String[]> recordedData = Collections.synchronizedList(new ArrayList<>());
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Intervalo de tiempo coincide con Thread.sleep(250) -> 0.25 segundos
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
        
        // --- CÁLCULO DE ACELERACIÓN ---
        row[8] = formatDecimal(calculateAcceleration(currentSpeed));

        for (int i = 1; i <= 3; i++) {
            Sensor3Data f = flows.get(i);
            row[8 + i] = (f != null) ? formatDecimal(f.getFlowRate()) : "0,0";
        }
        
        recordedData.add(row);
    }

    private double calculateAcceleration(double currentSpeed) {
        synchronized (recordedData) {
            if (recordedData.isEmpty()) {
                return 0.0; // En el primer punto la aceleración es 0
            }
            
            try {
                // Obtenemos la velocidad del registro anterior (índice 7 del array de strings)
                String lastRow[] = recordedData.get(recordedData.size() - 1);
                double lastSpeed = Double.parseDouble(lastRow[7].replace(',', '.'));
                
                // Aceleración = (V_final - V_inicial) / Tiempo
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

    @GetMapping("/download")
    public ResponseEntity<byte[]> stopAndDownload(HttpSession session) {
        isRecording = false;
        if (session.getAttribute("currentUser") == null) return ResponseEntity.status(403).build();

        StringBuilder csv = new StringBuilder();
        // Cabecera actualizada con Aceleración
        csv.append("Fecha_Hora_MS;Pitch;Roll;Vibracion_Hz;Motor_Load;Dropper_Load;Peso_Kg;Velocidad_RPM;Aceleracion_RPM_s2;Caudal_1;Caudal_2;Caudal_3\n");
        
        synchronized (recordedData) {
            for (String[] row : recordedData) {
                csv.append(String.join(";", row)).append("\n");
            }
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ensayo_aceleracion_" + System.currentTimeMillis() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}