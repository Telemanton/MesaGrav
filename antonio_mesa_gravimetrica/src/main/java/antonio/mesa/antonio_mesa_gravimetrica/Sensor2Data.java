package antonio.mesa.antonio_mesa_gravimetrica;

// Sensor2Data class documentation
/**
 * The Sensor2Data class represents the data structure for the second sensor, which measures frequency.
 * 
 * The Sensor2Data class is used to encapsulate the frequency data collected from the second sensor in the application.
 */

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////

public class Sensor2Data {
    private float frecuency;

    // Default constructor and parameterized constructor for Sensor2Data class
    public Sensor2Data() {}
    public Sensor2Data(float frecuency) {this.frecuency = frecuency;}
    
    
    public float getFrecuency() { return frecuency;}
    public void setFrecuency(float frecuency) { this.frecuency = frecuency;}
}
