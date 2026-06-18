package antonio.mesa.antonio_mesa_gravimetrica;

public class Sensor5Data {
    private float dropperValue;

    // EL METODO TIENE QUE LLAMARSE ASÍ EXACTAMENTE Y SER PUBLIC
    public float getDropperValue() {
        return this.dropperValue;
    }

    public void setDropperValue(float dropperValue) {
        this.dropperValue = dropperValue;
    }
}