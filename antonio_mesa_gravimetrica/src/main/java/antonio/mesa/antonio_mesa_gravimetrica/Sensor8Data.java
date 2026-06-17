package antonio.mesa.antonio_mesa_gravimetrica;

public class Sensor8Data {
    private float activePower;
    private float apparentPower;
    private float reactivePower;
    private float powerFactor;
    private float voltage;
    private float current;
    private float ESP32Temperature;

    // Getters and Setters
    public float getActivePower() {
        return activePower;
    }
    public void setActivePower(float activePower) {
        this.activePower = activePower;
    }
    public float getApparentPower() {
        return apparentPower;
    }
    public void setApparentPower(float apparentPower) {
        this.apparentPower = apparentPower;
    }
    public float getReactivePower() {
        return reactivePower;
    }
    public void setReactivePower(float reactivePower) {
        this.reactivePower = reactivePower;
    }
    public float getPowerFactor() {
        return powerFactor;
    }
    public void setPowerFactor(float powerFactor) {
        this.powerFactor = powerFactor;
    }
    public float getVoltage() {
        return voltage;
    }
    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }
    public float getCurrent() {
        return current;
    }
    public void setCurrent(float current) {
        this.current = current;
    }
    public float getESP32Temperature() {
        return ESP32Temperature;
    }
    public void setESP32Temperature(float ESP32Temperature) {
        this.ESP32Temperature = ESP32Temperature;
    }
    

}