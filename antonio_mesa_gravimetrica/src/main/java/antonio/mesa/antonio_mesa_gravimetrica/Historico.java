package antonio.mesa.antonio_mesa_gravimetrica;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "dispositivo_id")
    private String dispositivoId = "Mesa de concentración gravimétrica";

    @Column(name = "datos_compactados", columnDefinition = "TEXT")
    private String datosCompactados;

    @Column(name = "checksum", length = 64)
    private String checksum;

    // Constructor vacío (Obligatorio para JPA)
    public Historico() {
    }

    // Constructor para guardar nuevos registros
    public Historico(String datosCompactados, String checksum) {
        this.datosCompactados = datosCompactados;
        this.checksum = checksum;
        this.dispositivoId = "Mesa de concentración gravimétrica";
        this.fechaRegistro = LocalDateTime.now(); // Asignamos la fecha al crear el objeto
    }

    // --- GETTERS Y SETTERS (Indispensables para Thymeleaf) ---

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