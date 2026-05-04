package antonio.mesa.antonio_mesa_gravimetrica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

