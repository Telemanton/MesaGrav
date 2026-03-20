package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // IMPORTANTE: Nuevo import
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/export")
public class DataExportController {

    private final MqttListener mqttListener;
    private boolean isRecording = false;
    private final List<String[]> recordedData = Collections.synchronizedList(new ArrayList<>());
    
    // Definimos el formato: Año-Mes-Día Hora:Min:Seg.Milisegundos
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
                        Thread.sleep(250); // Muestreo cada 0.25s (4Hz)
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

        String[] row = new String[11]; 
        
        // --- CAMBIO AQUÍ: Tiempo legible con milisegundos ---
        row[0] = LocalDateTime.now().format(timeFormatter);
        
        row[1] = formatDecimal(adxl.getX());
        row[2] = formatDecimal(adxl.getY());
        row[3] = formatDecimal(adxl.getZ());
        row[4] = formatDecimal(adxl.getPitch());
        row[5] = formatDecimal(adxl.getRoll());
        row[6] = formatDecimal(freq.getFrecuency());
        row[7] = formatDecimal(engine.getGaugeValue());
        
        for (int i = 1; i <= 3; i++) {
            Sensor3Data f = flows.get(i);
            row[7 + i] = (f != null) ? formatDecimal(f.getFlowRate()) : "0,0";
        }
        recordedData.add(row);
    }

    private String formatDecimal(Object value) {
        if (value == null) return "0,0";
        return String.valueOf(value).replace('.', ',');
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> stopAndDownload(HttpSession session) {
        isRecording = false;
        
        if (session.getAttribute("currentUser") == null) {
            return ResponseEntity.status(403).build();
        }

        StringBuilder csv = new StringBuilder();
        // Cabecera clara
        csv.append("Fecha_Hora_MS;Eje_X;Eje_Y;Eje_Z;Pitch;Roll;Vibracion_Hz;Motor_Load;Caudal_1;Caudal_2;Caudal_3\n");
        
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