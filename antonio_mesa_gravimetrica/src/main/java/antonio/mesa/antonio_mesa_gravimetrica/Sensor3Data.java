package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Sensor2Data class documentation
/**
 * The Sensor2Data class represents the data structure for the second sensor, which measures frequency.
 * 
 * The Sensor2Data class is used to encapsulate the frequency data collected from the second sensor in the application.
 */

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
// Ignores any unknown properties in the JSON payload to prevent errors during deserialization if the payload contains extra fields that are not defined in this class.
                @JsonIgnoreProperties(ignoreUnknown = true)
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////

public class Sensor3Data {
    private Integer id;
    private Double flowRate;

    public Sensor3Data() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getFlowRate() { return flowRate; }
    public void setFlowRate(Double flowRate) { this.flowRate = flowRate; }
}

