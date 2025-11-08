package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Sensor ADXL345

//Ignora propiedades desconocidas en el JSON entrante, ya que son de otros sensores
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorData {
    private float x;
    private float y;
    private float z;
    private float pitch;
    private float roll;

    public SensorData() {}

    public SensorData(float x, float y, float z, float pitch, float roll) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.roll = roll;
    }

    // Getters y setters
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getZ() { return z; }
    public void setZ(float z) { this.z = z; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public float getRoll() { return roll; }
    public void setRoll(float roll) { this.roll = roll; }
}