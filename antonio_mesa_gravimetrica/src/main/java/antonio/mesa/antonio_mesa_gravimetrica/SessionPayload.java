package antonio.mesa.antonio_mesa_gravimetrica;
import java.util.Map;


// This class is a simple POJO (Plain Old Java Object) that serves as a data structure to hold the sensor data collected from the MQTT listener.
public class SessionPayload {
    public SensorData adxl;
    public Sensor2Data frecuencia;
    public Map<Integer, Sensor3Data> caudales;
    public Sensor4Data engine;
    public Sensor5Data dropper;
    public Sensor6Data weight;
    public Sensor7Data speed;
    public long timestamp = System.currentTimeMillis();
}