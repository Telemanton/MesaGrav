package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MqttListener {

    private static final String BROKER_URL = "tcp://192.168.1.128:1883"; // IP del broker MQTT
    private static final String CLIENT_ID = "spring-mqtt-server";
    private static final String TOPIC = "sensor/adxl345";

    private final AtomicReference<SensorData> lastData = new AtomicReference<>(new SensorData());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SensorData getLastData() {
        return lastData.get();
    }

    @PostConstruct
    public void init() {
        try {
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("✅ Conectado al broker MQTT: " + BROKER_URL);

            client.subscribe(TOPIC, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("📩 Mensaje recibido MQTT: " + payload);

                try {
                    SensorData data = objectMapper.readValue(payload, SensorData.class);
                    lastData.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando JSON MQTT: " + e.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}