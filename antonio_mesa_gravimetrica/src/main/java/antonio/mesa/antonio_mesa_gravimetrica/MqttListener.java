package antonio.mesa.antonio_mesa_gravimetrica;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MqttListener {

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
/// MQTT Broker configuration
    private static final String BROKER_URL = "tcp://10.3.141.75:1883";
    private static final String CLIENT_ID = "spring-mqtt-client";

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
/// MQTT Topics
    private static final String TOPIC_ADXL = "sensor/adxl345";
    private static final String TOPIC_FRECUENCIA = "sensor/frecuency";
    private static final String TOPIC_FLOW = "sensor/flow";
    private static final String TOPIC_ENGINE_GAUGE = "sensor/engine_gauge";
    private static final String TOPIC_DROPPER_GAUGE = "sensor/dropper_gauge";
    private static final String TOPIC_WEIGHT = "sensor/weight";
    private static final String TOPIC_SPEED = "sensor/speed"; 
    // Add more topics here as needed, for example:
    // private static final String TOPIC_NEW = "sensor/newtopic";
   
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
    private final AtomicReference<SensorData> lastSensorData = new AtomicReference<>(new SensorData());
    private final AtomicReference<Sensor2Data> lastSensor2Data = new AtomicReference<>(new Sensor2Data());
    private final AtomicReference<Sensor4Data> lastSensor4Data = new AtomicReference<>(new Sensor4Data());
    private final AtomicReference<Sensor5Data> lastSensor5Data = new AtomicReference<>(new Sensor5Data());
    private final AtomicReference<Sensor6Data> lastSensor6Data = new AtomicReference<>(new Sensor6Data());
    private final AtomicReference<Sensor7Data> lastSensor7Data = new AtomicReference<>(new Sensor7Data());
    // Add more AtomicReferences for new topics here, for example:
    // private final AtomicReference<NewTopicData> lastNewTopicData = new AtomicReference
  
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, Sensor3Data> flowSensorsMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private MqttClient client;

    public Map<Integer, Sensor3Data> getAllFlowData() { return flowSensorsMap; }

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
    public SensorData getLastSensorData() { return lastSensorData.get(); }
    public Sensor2Data getLastSensor2Data() { return lastSensor2Data.get(); }
    public Sensor4Data getLastSensor4Data() { return lastSensor4Data.get(); }
    public Sensor5Data getLastSensor5Data() { return lastSensor5Data.get(); }
    public Sensor6Data getLastSensor6Data() { return lastSensor6Data.get(); }
    public Sensor7Data getLastSensor7Data() { return lastSensor7Data.get(); }
    public Sensor3Data getFlowDataById(Integer id) { return flowSensorsMap.get(id); }
    
    
    // Add more getter methods for new topics here, for example:
    // public NewTopicData getLastNewTopicData() { return lastNewTopicData.get(); }

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////

    @PostConstruct // This method will be called after the bean is initialized, and it will set up the MQTT client and subscriptions.
    public void init() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("Conectado al broker MQTT: " + BROKER_URL);

            ////////////////////////////////////////////////////////////////////       
            // Subscriptions to MQTT topics and handleling incoming messages  // 
            ////////////////////////////////////////////////////////////////////
            /// Each subscription listens to a specific topic and processes incoming messages by parsing the JSON payload into the corresponding data class
            
            // ADXL345 sensor data suscription
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

            // Frecuency sensor data suscription
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

            // Engine gauge sensor data suscription
            client.subscribe(TOPIC_ENGINE_GAUGE, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Gauge recibido: " + payload);
                try {
                Double valor = Double.parseDouble(payload.trim());
                Sensor4Data data = new Sensor4Data();
                data.setGaugeValue(valor);
                lastSensor4Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Gauge: " + e.getMessage());
                }
            });

            // Dropper gauge sensor data suscription
            client.subscribe(TOPIC_DROPPER_GAUGE, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Gauge recibido: " + payload);
                try {
                Double valor = Double.parseDouble(payload.trim());
                Sensor5Data data = new Sensor5Data();
                data.setDropperValue(valor);
                lastSensor5Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Gauge: " + e.getMessage());
                }
            });

            // Weight sensor data suscription
            client.subscribe(TOPIC_WEIGHT, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Weight recibido: " + payload);
                try {
                Double valor = Double.parseDouble(payload.trim());
                Sensor6Data data = new Sensor6Data();
                data.setWeightValue(valor);
                lastSensor6Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Weight: " + e.getMessage());
                }
            });

            // Speed sensor data suscription
            client.subscribe(TOPIC_SPEED, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                System.out.println("Speed recibido: " + payload);
                try {
                Double valor = Double.parseDouble(payload.trim());
                Sensor7Data data = new Sensor7Data();
                data.setSpeedValue(valor);
                lastSensor7Data.set(data);
                } catch (Exception e) {
                    System.err.println("Error parseando Speed: " + e.getMessage());
                }
            });

            // Flow sensor data suscription
            client.subscribe(TOPIC_FLOW, (topic, message) -> {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                try {
                    FlowWrapper wrapper = objectMapper.readValue(payload, FlowWrapper.class);
                    if (wrapper.getSensores() != null) {
                        for (Sensor3Data sensor : wrapper.getSensores()) {
                            flowSensorsMap.put(sensor.getId(), sensor);
                        }

                        // Envía el objeto completo al canal que escuchará el HTML
                        messagingTemplate.convertAndSend("/topic/flow", wrapper);
                    }

                    System.out.println("Actualizados " + wrapper.getSensores().size() + " sensores de caudal.");

                } catch (Exception e) {
                    System.err.println("Error parseando Flow: " + e.getMessage());
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
    }
}
