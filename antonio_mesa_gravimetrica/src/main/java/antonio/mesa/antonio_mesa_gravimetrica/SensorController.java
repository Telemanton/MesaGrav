package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sensor")
public class SensorController {

    private final MqttListener mqttListener;

    public SensorController(MqttListener mqttListener) {
        this.mqttListener = mqttListener;
    }

    // Devuelve los últimos datos de ambos sensores
    @GetMapping
    public Map<String, Object> getLastData() {
        Map<String, Object> response = new HashMap<>();
        response.put("adxl", mqttListener.getLastSensorData());
        response.put("frecuencia", mqttListener.getLastSensor2Data());
        return response;
    }
}
