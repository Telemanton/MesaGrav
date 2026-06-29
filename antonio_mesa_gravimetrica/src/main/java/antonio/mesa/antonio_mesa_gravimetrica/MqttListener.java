package antonio.mesa.antonio_mesa_gravimetrica;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MqttListener {

    private static final String BROKER_URL = "tcp://10.3.141.1:1883";
    private static final String CLIENT_ID = "spring-mqtt-client";

    private static final String TOPIC_ADXL = "sensor/adxl345";
    private static final String TOPIC_FRECUENCIA = "sensor/frecuency";
    private static final String TOPIC_FLOW = "sensor/flow";
    private static final String TOPIC_ENGINE_GAUGE = "sensor/engine_gauge";
    private static final String TOPIC_DROPPER_GAUGE = "sensor/dropper_gauge";
    private static final String TOPIC_MOTOR_ENERGY = "tele/tasmota-motor/SENSOR";
    private static final String TOPIC_PLATFORM_ENERGY = "tele/tasmota-platform/SENSOR";
    private static final String TOPIC_WEIGHT = "sensor/weight";
    private static final String TOPIC_SPEED = "sensor/speed";

    private final AtomicReference<SensorData> lastSensorData = new AtomicReference<>(new SensorData());
    private final AtomicReference<Sensor2Data> lastSensor2Data = new AtomicReference<>(new Sensor2Data());
    private final AtomicReference<Sensor4Data> lastSensor4Data = new AtomicReference<>(new Sensor4Data());
    private final AtomicReference<Sensor5Data> lastSensor5Data = new AtomicReference<>(new Sensor5Data());
    private final AtomicReference<Sensor6Data> lastSensor6Data = new AtomicReference<>(new Sensor6Data());
    private final AtomicReference<Sensor7Data> lastSensor7Data = new AtomicReference<>(new Sensor7Data());
    private final AtomicReference<Sensor8Data> lastSensor8Data = new AtomicReference<>(new Sensor8Data());
    private final AtomicReference<Sensor8Data> lastSensor8_2_Data = new AtomicReference<>(new Sensor8Data());

    private final ObjectMapper objectMapper = new ObjectMapper(); 
    private final Map<Integer, Sensor3Data> flowSensorsMap = new java.util.concurrent.ConcurrentHashMap<>();

    private MqttClient client;

    public Map<Integer, Sensor3Data> getAllFlowData() {
        return flowSensorsMap;
    }

    public SensorData getLastSensorData() { return lastSensorData.get(); }
    public Sensor2Data getLastSensor2Data() { return lastSensor2Data.get(); }
    public Sensor4Data getLastSensor4Data() { return lastSensor4Data.get(); }
    public Sensor5Data getLastSensor5Data() { return lastSensor5Data.get(); }
    public Sensor6Data getLastSensor6Data() { return lastSensor6Data.get(); }
    public Sensor7Data getLastSensor7Data() { return lastSensor7Data.get(); }
    public Sensor3Data getFlowDataById(Integer id) { return flowSensorsMap.get(id); }
    public Sensor8Data getLastSensor8Data() { return lastSensor8Data.get(); }
    public Sensor8Data getLastSensor8_2_Data() { return lastSensor8_2_Data.get(); }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("Conectado al broker MQTT: " + BROKER_URL);

            // 1. ADXL345
            client.subscribe(TOPIC_ADXL, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    SensorData data = objectMapper.readValue(payload, SensorData.class);
                    lastSensorData.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando ADXL345: " + e.getMessage());
                }
            });

            // 2. Frecuencia
            client.subscribe(TOPIC_FRECUENCIA, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    Sensor2Data data = objectMapper.readValue(payload, Sensor2Data.class);
                    lastSensor2Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Frecuencia: " + e.getMessage());
                }
            });

            // 3. CAUDALÍMETROS (Movido arriba para garantizar su suscripción)
            client.subscribe(TOPIC_FLOW, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("MqttListener -> Datos de caudal recibidos: " + payload);
                try {
                    FlowWrapper wrapper = objectMapper.readValue(payload, FlowWrapper.class);
                    if (wrapper != null && wrapper.getSensores() != null) {
                        for (Sensor3Data sensor : wrapper.getSensores()) {
                            if (sensor.getId() != null) {
                                flowSensorsMap.put(sensor.getId(), sensor);
                            }
                        }
                        System.out.println("OK: Mapa de caudales actualizado. Total sensores: " + flowSensorsMap.size());
                    }
                } catch (Exception e) {
                    System.err.println("Error parseando Flow de caudales: " + e.getMessage());
                }
            });

            // 4. Motor Energy (Tasmota JSON)
            client.subscribe(TOPIC_MOTOR_ENERGY, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    JsonNode rootNode = objectMapper.readTree(payload);
                    Sensor8Data data = new Sensor8Data();
                    data.setActivePower(rootNode.path("ENERGY").path("Power").floatValue());
                    data.setApparentPower(rootNode.path("ENERGY").path("ApparentPower").floatValue());
                    data.setReactivePower(rootNode.path("ENERGY").path("ReactivePower").floatValue());
                    data.setPowerFactor(rootNode.path("ENERGY").path("Factor").floatValue());
                    data.setVoltage(rootNode.path("ENERGY").path("Voltage").floatValue());
                    data.setCurrent(rootNode.path("ENERGY").path("Current").floatValue());
                    data.setESP32Temperature(rootNode.path("ESP32").path("Temperature").floatValue());
                    lastSensor8Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Motor Energy: " + e.getMessage());
                }
            });

            // 5. Platform Energy (Tasmota JSON)
            client.subscribe(TOPIC_PLATFORM_ENERGY, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    JsonNode rootNode = objectMapper.readTree(payload);
                    Sensor8Data data = new Sensor8Data();
                    data.setActivePower(rootNode.path("ENERGY").path("Power").floatValue());
                    data.setApparentPower(rootNode.path("ENERGY").path("ApparentPower").floatValue());
                    data.setReactivePower(rootNode.path("ENERGY").path("ReactivePower").floatValue());
                    data.setPowerFactor(rootNode.path("ENERGY").path("Factor").floatValue());
                    data.setVoltage(rootNode.path("ENERGY").path("Voltage").floatValue());
                    data.setCurrent(rootNode.path("ENERGY").path("Current").floatValue());
                    data.setESP32Temperature(rootNode.path("ESP32").path("Temperature").floatValue());
                    lastSensor8_2_Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Platform Energy: " + e.getMessage());
                }
            });

            // 6. Engine Gauge (Texto plano número)
            client.subscribe(TOPIC_ENGINE_GAUGE, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8).trim();
                try {
                    float valor = Float.parseFloat(payload);
                    Sensor4Data data = new Sensor4Data();
                    data.setGaugeValue(valor);
                    lastSensor4Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Gauge Motor: " + e.getMessage());
                }
            });

            // 7. Dropper Gauge 
            client.subscribe(TOPIC_DROPPER_GAUGE, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    float valor = Float.parseFloat(payload);
                    
                    Sensor5Data data = new Sensor5Data();
                    data.setDropperValue(valor);
                    lastSensor5Data.set(data);
                    
                    System.out.println("OK: Dropper Gauge extraído: " + valor + " W");
                } catch (Exception e) {
                    System.err.println("Error parseando Dropper Gauge JSON: " + e.getMessage());
                }
            });

            // 8. Weight
            client.subscribe(TOPIC_WEIGHT, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    float valor = Float.parseFloat(payload.trim());
                    Sensor6Data data = new Sensor6Data();
                    data.setWeightValue(valor);
                    lastSensor6Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Weight: " + e.getMessage());
                }
            });

            // 9. Speed
            client.subscribe(TOPIC_SPEED, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    float valor = Float.parseFloat(payload.trim());
                    Sensor7Data data = new Sensor7Data();
                    data.setSpeedValue(valor);
                    lastSensor7Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Speed: " + e.getMessage());
                }
            });

        } catch (MqttException e) {
            System.err.println("Fallo crítico al iniciar conexiones del MqttListener");
            e.printStackTrace();
        }
    }
}