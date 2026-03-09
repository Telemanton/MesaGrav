package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////           
//                                          DataExportController class
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * This controller handles the data logging and export functionality.
 * It allows users to record real-time sensor data and download it as a CSV file.
 */
@RestController
@RequestMapping("/export")
public class DataExportController {

    private final MqttListener mqttListener;
    private boolean isRecording = false;
    // Usamos una lista sincronizada para evitar errores si el hilo de lectura y el de escritura coinciden
    private final List<String[]> recordedData = Collections.synchronizedList(new ArrayList<>());

    public DataExportController(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////           
    //                                          RECORDING CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Starts the data recording process. 
     * It launches a background thread that captures sensor data every 1000ms.
     */
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
                        Thread.sleep(1000); 
                    } catch (InterruptedException e) { 
                        Thread.currentThread().interrupt(); 
                        break; 
                    }
                }
            }).start();
        }
        return ResponseEntity.ok("Recording started");
    }

    /**
     * Captures the current state of all sensors from MqttListener and adds a row to the list.
     */
    private void captureSnapshot() {
        SensorData adxl = mqttListener.getLastSensorData();
        Sensor2Data freq = mqttListener.getLastSensor2Data();
        Map<Integer, Sensor3Data> flows = mqttListener.getAllFlowData();

        // Estructura: Timestamp, X, Y, Z, Pitch, Roll, Frecuencia, Flow1...Flow12
        String[] row = new String[19];
        row[0] = String.valueOf(System.currentTimeMillis());
        row[1] = String.valueOf(adxl.getX());
        row[2] = String.valueOf(adxl.getY());
        row[3] = String.valueOf(adxl.getZ());
        row[4] = String.valueOf(adxl.getPitch());
        row[5] = String.valueOf(adxl.getRoll());
        row[6] = String.valueOf(freq.getFrecuency());
        
        for (int i = 1; i <= 12; i++) {
            Sensor3Data f = flows.get(i);
            row[6 + i] = (f != null) ? String.valueOf(f.getFlowRate()) : "0.0";
        }
        recordedData.add(row);
    }

    /**
     * Stops the recording and returns the accumulated data as a CSV file.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> stopAndDownload(HttpSession session) {
        isRecording = false;
        
        if (session.getAttribute("currentUser") == null) {
            return ResponseEntity.status(403).build();
        }

        StringBuilder csv = new StringBuilder();
        // Cabecera (He usado punto y coma para mejor compatibilidad con Excel en español)
        csv.append("Timestamp;Eje_X;Eje_Y;Eje_Z;Pitch;Roll;Frecuencia;S1;S2;S3;S4;S5;S6;S7;S8;S9;S10;S11;S12\n");
        
        synchronized (recordedData) {
            for (String[] row : recordedData) {
                csv.append(String.join(";", row)).append("\n");
            }
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ensayo_mesa_" + System.currentTimeMillis() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}