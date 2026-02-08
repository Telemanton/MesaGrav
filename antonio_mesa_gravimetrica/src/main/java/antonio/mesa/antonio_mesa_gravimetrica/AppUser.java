package antonio.mesa.antonio_mesa_gravimetrica;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank 
    @Size(min = 3, max = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;  // ← SOLO esto va a PostgreSQL

    @Transient  // ← NO VA A BBDD, solo formulario
    @Size(min = 6)
    private String password;  // ← Temporal para crear usuarios

    @Column(nullable = false, length = 100)
    @NotBlank 
    @Size(max = 100)
    private String name;

    @Column(nullable = false, length = 100)
    @NotBlank 
    @Size(max = 100)
    private String surname;

    @Column(nullable = false, length = 100, unique = true)
    @NotBlank 
    @Email 
    @Size(max = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructores
    public AppUser() {}

    public AppUser(String username, String passwordHash, String name, String surname, 
                   String email, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
    }

    // Getters y Setters COMPLETOS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // ← password SOLO para formularios
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "AppUser{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                '}';
    }
}
