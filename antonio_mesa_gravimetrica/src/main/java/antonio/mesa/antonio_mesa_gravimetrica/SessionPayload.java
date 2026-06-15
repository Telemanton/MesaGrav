package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.Map;

/* ===================================
SessionPayload class documentation
===================================
The SessionPayload class represents the data structure for holding all sensor data collected from the MQTT listener.

The sense of this class is to serve as a container for all the different types of sensor data 
(SensorData, Sensor2Data, Sensor3Data, etc.) that are collected during a session. 
It includes fields for each type of sensor data, as well as a timestamp to record when the data was collected.

The SessionPayload class is used to encapsulate the sensor data for further processing or transmission.
 */
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