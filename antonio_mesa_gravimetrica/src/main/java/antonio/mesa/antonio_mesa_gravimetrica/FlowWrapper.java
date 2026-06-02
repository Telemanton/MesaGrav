package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper class to map the JSON structure sent by the controller.
 * The JSON format expected is: 
 * {
 *  "sensores": 
 * [
 *  { "id": 1, "flowRate": 0.0 } 
 *  { "id": 2, "flowRate": 0.0 }
 *  { "id": 3, "flowRate": 0.0 }
 * ] }
 * 
 * Jackson is a popular Java library used for converting Java objects to JSON and vice versa.
 * As well as that it provides the feature of preventing deserialization, such as ignoring unknown properties in the JSON input.
 * for example, if the JSON input contains properties that do not match any fields in the Java class, Jackson will ignore those properties instead of throwing an error.
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowWrapper {
    
    // Type List<Sensor3Data> to hold the list of sensor data received from the controller.
    private List<Sensor3Data> sensores;

    public FlowWrapper() {}

    public List<Sensor3Data> getSensores() {
        return sensores;
    }

    public void setSensores(List<Sensor3Data> sensores) {
        this.sensores = sensores;
    }
}