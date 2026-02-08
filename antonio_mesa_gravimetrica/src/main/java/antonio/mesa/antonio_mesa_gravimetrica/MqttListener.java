package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MqttListener {

    private static final String BROKER_URL = "tcp://192.168.1.130:1883";
    private static final String CLIENT_ID = "spring-mqtt-server";
    private static final String TOPIC_ADXL = "sensor/adxl345";
    private static final String TOPIC_FRECUENCIA = "sensor/frecuencia";

    private final AtomicReference<SensorData> lastSensorData = new AtomicReference<>(new SensorData());
    private final AtomicReference<Sensor2Data> lastSensor2Data = new AtomicReference<>(new Sensor2Data());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MqttClient client;

    public SensorData getLastSensorData() { return lastSensorData.get(); }
    public Sensor2Data getLastSensor2Data() { return lastSensor2Data.get(); }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("Conectado al broker MQTT: " + BROKER_URL);

            // Suscribirse a los dos topics
            client.subscribe(TOPIC_ADXL, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("ADXL345 recibido: " + payload);
                try {
                    SensorData data = objectMapper.readValue(payload, SensorData.class);
                    lastSensorData.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando ADXL345: " + e.getMessage());
                }
            });

            client.subscribe(TOPIC_FRECUENCIA, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Frecuencia recibido: " + payload);
                try {
                    Sensor2Data data = objectMapper.readValue(payload, Sensor2Data.class);
                    lastSensor2Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Frecuencia: " + e.getMessage());
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
