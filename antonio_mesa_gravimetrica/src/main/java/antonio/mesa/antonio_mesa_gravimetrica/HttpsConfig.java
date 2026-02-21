package antonio.mesa.antonio_mesa_gravimetrica;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// HttpsConfig class documentation
/**
 * This is the necessary HttpsConfig for configuring the application in order to use HTTPS for secure communication.
 * It sets up a servlet container that listens on both HTTP and HTTPS ports, and redirects all HTTP traffic to HTTPS.
 * Whit the propose of ensuring that data transmitted between the client and server will be encrypted and secure.
 * The configuration includes:
 * - A servlet container factory that creates a Tomcat server with the necessary security constraints to enforce HTTPS.
 * - An additional connector that listens on the standard HTTP port (80) and redirects traffic to the HTTPS port (443).
 * 
 */
@Configuration
public class HttpsConfig {

    @Bean 
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            /**
             * Configures security constraints for the application context to enforce HTTPS.
             * 
             * This method sets up a security constraint that requires all HTTP requests to be 
             * transmitted over a secure channel (HTTPS). The "/*" pattern ensures this constraint 
             * applies to every URL endpoint in the application.
             * 
             * The "/*" pattern is a standard URL pattern matching syntax where:
             * - "/" represents the root context
             * - "*" is a wildcard that matches any character sequence
             * - Together "/*" means "all paths at any depth within the application"
             * 
             * @param context the Tomcat Context object to which the security constraint will be applied
             * 
             * @note Tomcat is the embedded servlet container used by Spring Boot, and this method allows to customize its configuration to enforce HTTPS for the entire application.
             */
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint(); 
                securityConstraint.setUserConstraint("CONFIDENTIAL"); // This means that all requests shall be made over a secure channel (HTTPS)
                SecurityCollection collection = new SecurityCollection(); 
                collection.addPattern("/*"); 
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };

        // Add an additional connector to listen on HTTP port and redirect to HTTPS
        tomcat.addAdditionalTomcatConnectors(getHttpConnector()); 

        return tomcat; // Return the configured Tomcat servlet
    }

    // Connector configuration for HTTP to HTTPS redirection
    /**
     * Configures an additional HTTP connector that listens on port 80 and redirects all traffic to HTTPS on port 443.
     * 
     * This method creates a new Connector instance with the following settings:
     * - Scheme: "http" (indicating that this connector will handle HTTP traffic)
     * - Port: 80 (the standard port for HTTP)
     * - Secure: false (indicating that this connector does not handle secure traffic)
     * - Redirect Port: 443 (the standard port for HTTPS, where traffic will be redirected)
     * 
     * @return a Connector object configured for HTTP to HTTPS redirection
     */
    private Connector getHttpConnector() {
        Connector connector = new Connector(); 
        connector.setScheme("http");
        connector.setPort(80); // Escucha en el 80 normal
        connector.setSecure(false);
        connector.setRedirectPort(443); // Lo manda al 443 seguro
        return connector;
    }
}
