package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
//                                          SensorController class documentation
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
/**
 * The SensorController class is a REST controller that handles HTTP requests related to sensor data.
 * It provides an endpoint to retrieve the latest data from all sensors of the system.
 * * The controller uses an instance of MqttListener to access the latest sensor data, which is stored in memory.
 * When a GET request is made to the "/sensor" endpoint, the controller returns a JSON response containing the latest data from all sensors.
 * * This class is very important , it is the responsable of serving as interface between the frontend and the backend for retrieving sensor data in real-time.
 * Once the data is retrieved from the MqttListener, it is returned as a JSON response to the frontend
 * which follows the next structure:
 * {
 * "adxl": { ... },
 * "frecuency_sensor": { ... },
 * "engine_gauge": { ... },
 * "caudales": { ... },
 * "dropper_gauge": { ... },
 * "weight": { ... },
 * "speed": { ... }
 * }  
*/

@RestController
@RequestMapping("/sensor")
public class SensorController {

    private final MqttListener mqttListener;

    // Global tare reference value stored in memory (accessible across recording threads and UI polling cycles)
    private static float valorTara = 0.0f;

    // Constructor for SensorController, which takes an instance of MqttListener as a parameter
    // this is used to access the latest sensor data stored in memory by the MqttListener
    public SensorController(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    @GetMapping
    public Map<String, Object> getLastData() {
        Map<String, Object> response = new HashMap<>();

    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
        // Retrieve the latest data from the MqttListener and put it in the response map
        response.put("adxl", mqttListener.getLastSensorData());
        response.put("frecuency_sensor", mqttListener.getLastSensor2Data());
        response.put("engine_gauge", mqttListener.getLastSensor4Data());
        response.put("caudales", mqttListener.getAllFlowData());
        response.put("dropper_gauge", mqttListener.getLastSensor5Data());
        
        // --- LIVE TARE WEIGHT PROCESSING ---
        // Fetch raw weight data from MQTT in real-time
        Sensor6Data rawWeightData = mqttListener.getLastSensor6Data();
        Sensor6Data processedWeight = new Sensor6Data();
        
        if (rawWeightData != null && rawWeightData.getWeightValue() != null) {
            float rawWeight = rawWeightData.getWeightValue().floatValue();
            // Subtract the global software tare offset
            float netWeight = rawWeight - valorTara;
            
            // Prevent minor mechanical scale bounce or noise from reading below absolute zero
            if (netWeight < 0.0f) {
                netWeight = 0.0f;
            }
            processedWeight.setWeightValue((double) netWeight);
        } else {
            processedWeight.setWeightValue(0.0);
        }
        
        // Send the real-time calculated net weight instead of raw values to the JS dashboard UI
        response.put("weight", processedWeight);
        response.put("speed", mqttListener.getLastSensor7Data());
        // Put the new topics in the response map here, for example:
        // response.put("new topic name", mqttListener.getLastNewTopicData()); 
        // *
        // You can add as many topics as you want, just make sure to implement the corresponding getter method in MqttListener and subscribe to the topic in the init() method of MqttListener.
    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
        return response;
    }

    /**
     * Endpoint to capture and save the current scale weight snapshot as a baseline offset (Tare).
     * Accessible by the frontend button using a POST request asynchronously.
     */
    @PostMapping("/tare")
    public ResponseEntity<String> aplicarTare(HttpSession session) {
        // Enforce user authorization security context check
        if (session.getAttribute("currentUser") == null) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Sensor6Data currentWeightData = mqttListener.getLastSensor6Data();
        if (currentWeightData != null && currentWeightData.getWeightValue() != null) {
            // Lock down the current raw reading value as the new baseline
            valorTara = currentWeightData.getWeightValue().floatValue();
            System.out.println("[TARE CONTROLLER] Global tare baseline set at: " + valorTara + " g");
            return ResponseEntity.ok("Tare baseline set to: " + valorTara);
        } else {
            // Reset to default zero index if sensor connection isn't alive or is unreadable
            valorTara = 0.0f;
            return ResponseEntity.ok("Sensor reading unrecoverable. Resetting tare offset index to 0.");
        }
    }

    /**
     * Public cross-reference static getter allowing DataExportController thread 
     * snapshots to extract and write identical net weight attributes to the physical CSV sheet logs.
     */
    public static float getValorTara() {
        return valorTara;
    }
}