package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
//                                          SensorController class documentation
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
/**
 * The SensorController class is a REST controller that handles HTTP requests related to sensor data.
 * It provides an endpoint to retrieve the latest data from all sensors of the system.
 * 
 * The controller uses an instance of MqttListener to access the latest sensor data, which is stored in memory.
 * When a GET request is made to the "/sensor" endpoint, the controller returns a JSON response containing the latest data from all sensors.
 * 
 * This class is very important , it is the responsable of serving as interface between the frontend and the backend for retrieving sensor data in real-time.
 */

@RestController
@RequestMapping("/sensor")
public class SensorController {

    private final MqttListener mqttListener;

    // Constructor for SensorController, which takes an instance of MqttListener as a parameter
    public SensorController(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    /*
    Returns the latest data from all sensors in a JSON format whenever a GET request to "/sensor" endpoint by the frontend (JAVA SCRIPT)
    To check the data structure of the response, you can use the following example:
    {
        "adxl": {
            "x": 0.0,
            "y": 0.0,
            "z": 0.0,
            "pitch": 0.0,
            "roll": 0.0
        },
        "frecuency sensor": {
            "frecuency": 0.0
        }
    }

    Revise MqttListener.java for more details about the data structure of the response. 
    Revise mesa-gravimetrica.html code for more details about how the data is used in the frontend.

    */ 
    @GetMapping
    public Map<String, Object> getLastData() {
        Map<String, Object> response = new HashMap<>();

    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
        // Retrieve the latest data from the MqttListener and put it in the response map
        response.put("adxl", mqttListener.getLastSensorData());
        response.put("frecuency sensor", mqttListener.getLastSensor2Data());
        // Put the new topics in the response map here, for example:
        // response.put("new topic name", mqttListener.getLastNewTopicData()); 
        // *
        // You can add as many topics as you want, just make sure to implement the corresponding getter method in MqttListener and subscribe to the topic in the init() method of MqttListener.
    //////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
        return response;
    }
}
