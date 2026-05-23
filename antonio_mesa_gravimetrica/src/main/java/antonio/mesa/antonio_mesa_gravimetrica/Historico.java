package antonio.mesa.antonio_mesa_gravimetrica;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "historico")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "dispositivo_id")
    private String dispositivoId = "Mesa de concentración gravimétrica"; // to be changed in future implementations 

    @Column(name = "datos_compactados", columnDefinition = "TEXT")
    private String datosCompactados;

    @Column(name = "checksum", length = 64)
    private String checksum;

    public Historico() {
    }

    public Historico(String datosCompactados, String checksum) {
        this.datosCompactados = datosCompactados;
        this.checksum = checksum;
        this.dispositivoId = "Mesa de concentración gravimétrica"; // to be changed in future implementations
        this.fechaRegistro = LocalDateTime.now(); // Asignamos la fecha al crear el objeto
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDispositivoId() {
        return dispositivoId;
    }

    public void setDispositivoId(String dispositivoId) {
        this.dispositivoId = dispositivoId;
    }

    public String getDatosCompactados() {
        return datosCompactados;
    }

    public void setDatosCompactados(String datosCompactados) {
        this.datosCompactados = datosCompactados;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}