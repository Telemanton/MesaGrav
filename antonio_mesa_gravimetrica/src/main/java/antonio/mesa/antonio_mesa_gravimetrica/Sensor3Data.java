package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
Sensor3Data class documentation
================================
The Sensor3Data class represents the data structure for the third sensor, which measures flow rate.
The Sensor3Data class is used to encapsulate the flow rate data collected from the flow sensors in the application.
There are 3 of them.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sensor3Data {
    private Integer id;
    private Double flowRate;

    public Sensor3Data() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getFlowRate() { return flowRate; }
    public void setFlowRate(Double flowRate) { this.flowRate = flowRate; }
}

