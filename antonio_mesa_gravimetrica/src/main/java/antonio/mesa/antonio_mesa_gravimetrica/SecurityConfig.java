package antonio.mesa.antonio_mesa_gravimetrica;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
//                                           SecurityConfig class documentation 
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
/**
 * The SecurityConfig class is responsible for configuring the security settings of the application.
 * It defines the password encoding mechanism and the security filter chain that controls access to different endpoints.
 * 
 * The password encoder used is BCryptPasswordEncoder, which provides a one way hashing algorithm for storing passwords securely, ofuscating view even for administrators.
 * 
 * The security filter chain is configured to allow access to specific endpoints related to user logging.
 * All other requests require authentication.
 * 
 * The logout configuration specifies that when a user logs out, they will be redirected to the home page ("/"), their session will be invalidated, and the JSESSIONID cookie will be deleted to ensure a clean logout process.
 */

//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
     
                        @Configuration // Indicates that this class contains Spring configuration
                        @EnableWebSecurity // Enables Spring Security for the application
                
//////////////////////////////////////////////////////////          //////////////////////////////////////////////////////////
public class SecurityConfig {

    @Bean 
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for the application to control access to different endpoints
     * based on authentication and authorization rules.
     *
     * <p>Security Configuration Details:
     * <ul>
     *   <li>Public endpoints (permitAll): Authorization page and URL for login process</li>
     *   <li>All other endpoints require authentication</li>
     *   <li>CSRF protection is disabled</li>
     *   <li>Default form login is disabled to allow custom login handling</li>
     *   <li>Logout functionality redirects to home page and invalidates session</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object used to configure security settings
     *
     * @note form.disable() does NOT disable logout on page revisit. It disables Spring's automatic login form page.
     *       After logout, the user's session is invalidated and cookies are deleted, so they cannot access
     *       protected resources without re-authenticating. The disable() only removes the default /login page.
     */
    @Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http // HTTP Protocol configuration
        .authorizeHttpRequests(auth -> auth // auth section means that we are configuring authorization rules for HTTP requests
            .requestMatchers("/", "/user-logging", "/css/**", "/js/**", "/webjars/**").permitAll()
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf.disable()) // Disable CSRF protection for simplicity (not recommended for production)
        .formLogin(form -> form.disable()) // Disable default form login (In order to handle it manually)

        .logout(logout -> logout
            .logoutUrl("/logout") // URL trigger for logout
            .logoutSuccessUrl("/") // Here indicates that after logout, the user will be redirected to the home page ("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID") // Ensure that the session is invalidated and cookies are deleted upon logout
        );
    
    return http.build(); 
}


}


