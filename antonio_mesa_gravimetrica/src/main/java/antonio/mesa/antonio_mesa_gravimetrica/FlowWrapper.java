package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper class to map the JSON structure sent by the ESP32.
 * The JSON format expected is: { "sensores": [ { "id": 1, "flowRate": 0.0 }, ... ] }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowWrapper {
    
    private List<Sensor3Data> sensores;

    // Default constructor for Jackson
    public FlowWrapper() {}

    public List<Sensor3Data> getSensores() {
        return sensores;
    }

    public void setSensores(List<Sensor3Data> sensores) {
        this.sensores = sensores;
    }
}