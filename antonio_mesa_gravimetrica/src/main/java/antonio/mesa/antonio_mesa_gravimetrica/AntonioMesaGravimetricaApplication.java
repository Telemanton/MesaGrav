package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
AntonioMesaGravimetricaApplication is the main entry point of the Spring Boot application. 
It is annotated with @SpringBootApplication, which is a convenience annotation that combines

This annotation indicates that this class serves as the configuration and bootstrap for the application.
*/
@SpringBootApplication
public class AntonioMesaGravimetricaApplication {

    public static void main(String[] args) {
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        
        SpringApplication.run(AntonioMesaGravimetricaApplication.class, args);
    }

}