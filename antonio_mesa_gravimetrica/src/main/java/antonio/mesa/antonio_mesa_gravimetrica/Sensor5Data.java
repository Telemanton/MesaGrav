package antonio.mesa.antonio_mesa_gravimetrica;

/*
================================
Sensor5Data class documentation
================================
The Sensor5Data class represents the data structure for the measured voltage provenient from the dropper control circuit.
The Sensor5Data class is used to encapsulate the dropper value data collected.
*/
public class Sensor5Data {
    private Double dropperValue;

    public Sensor5Data() {}

    public Double getDropperValue() { return dropperValue; }
    public void setDropperValue(Double dropperValue) { this.dropperValue = dropperValue; }
}
