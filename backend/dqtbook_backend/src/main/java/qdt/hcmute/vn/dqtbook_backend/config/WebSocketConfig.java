package qdt.hcmute.vn.dqtbook_backend.config;

import com.sun.security.auth.UserPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOriginPatterns("http://localhost:8080", "http://localhost:5173")
                .withSockJS();
    }

    /**
     * Ưu tiên userId từ HttpSession; nếu không có thì fallback lấy từ query param ?uid=
     * Principal.name = userId -> convertAndSendToUser(userId, ...) sẽ hoạt động.
     */
    public static class UserHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                var httpReq = servletRequest.getServletRequest();
                HttpSession session = httpReq.getSession(false);
                Object sid = (session != null) ? session.getAttribute("userId") : null;
                String uidParam = httpReq.getParameter("uid");
                String userId = (sid != null) ? String.valueOf(sid)
                        : (uidParam != null && !uidParam.isBlank() ? uidParam : null);
                if (userId != null) return new UserPrincipal(userId);
            }
            return null;
        }
    }
}