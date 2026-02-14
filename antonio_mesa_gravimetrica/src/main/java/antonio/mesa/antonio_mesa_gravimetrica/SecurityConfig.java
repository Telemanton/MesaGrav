package antonio.mesa.antonio_mesa_gravimetrica;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/user-logging", 
                "/create-user", "/save-user",     
                "/create-admin", "/save-admin",    
                "/admin-home", "/admin-users", 
                "/edit-user/**", "/delete-user/**",
                "/mesa-gravimetrica",
                "/css/**", "/js/**", "/webjars/**").permitAll()
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf.disable())
        .formLogin(form -> form.disable())

        .logout(logout -> logout
            .logoutUrl("/logout") // La URL que dispara el proceso
            .logoutSuccessUrl("/") // ¡AQUÍ indicas a dónde ir al terminar!
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        );
    
    return http.build();
}


}


