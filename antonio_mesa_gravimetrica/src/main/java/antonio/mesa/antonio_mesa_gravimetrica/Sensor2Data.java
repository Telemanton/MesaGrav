package antonio.mesa.antonio_mesa_gravimetrica;
// Final de carrera sensor de frecuencia

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

public class Sensor2Data {
    private double frecuency;

    public Sensor2Data() {}
    public Sensor2Data(float frecuency) {this.frecuency = frecuency;}
    
    
    public double getFrecuency() { return frecuency;}
    public void setFrecuency(float frecuency) { this.frecuency = frecuency;}
}
