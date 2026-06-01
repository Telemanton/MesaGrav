package antonio.mesa.antonio_mesa_gravimetrica;

/*
Sensor6Data class documentation
================================
The Sensor6Data class represents the data structure for the sensor, which measures weight values.
The Sensor6Data class is used to encapsulate the weight data collected.
*/
public class Sensor6Data {
    private Double weightValue;

    public Sensor6Data() {}

    public Double getWeightValue() { return weightValue; }
    public void setWeightValue(Double weightValue) { this.weightValue = weightValue; }
}
