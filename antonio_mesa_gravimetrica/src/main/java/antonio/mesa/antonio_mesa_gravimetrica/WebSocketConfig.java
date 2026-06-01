package antonio.mesa.antonio_mesa_gravimetrica;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
/* WebSocket configuration 
- The WebSocketConfig class is responsible for configuring the WebSocket message broker in the application.
- It enables a simple in-memory message broker and sets the application destination prefix for sending messages.
- It also registers the STOMP endpoint for WebSocket communication,allowing clients to connect to the "/ws" endpoint with SockJS fallback options.
*/
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Configure the message broker to enable a simple in-memory broker and set the application destination prefix
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    // Register the STOMP endpoint for WebSocket communication,
    // by allowing clients to connect to the "/ws" endpoint with SockJS fallback options
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
