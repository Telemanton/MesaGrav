package antonio.mesa.antonio_mesa_gravimetrica;

/* Sensor7Data class documentation
================================
The Sensor7Data class represents the data structure for the seventh sensor, which measures speed values.
The Sensor7Data class is used to encapsulate the speed data collected.
 */
public class Sensor7Data {
    // CAMBIO CRÍTICO: Tipo de dato primitivo float (32 bits), idéntico al ESP32
    private float speed; 

    // Constructor por defecto
    public Sensor7Data() {}

    // Constructor parametrizado (opcional, pero buena práctica)
    public Sensor7Data(float speed) {
        this.speed = speed;
    }

    // Getter y Setter alineados a float puro sin objetos envoltorios (Wrappers)
    public float getSpeedValue() { 
        return speed; 
    }
    
    public void setSpeedValue(float speed) { 
        this.speed = speed; 
    }
}