package qdt.hcmute.vn.dqtbook_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Định nghĩa các tiền tố cho các kênh nhắn tin
        // Kênh '/topic' cho các thông báo chung (ví dụ: tin nhắn trong nhóm chat)
        // Kênh '/queue' cho các tin nhắn riêng tư (ví dụ: tin nhắn 1-1)
        config.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các endpoint mà client sẽ gửi tin nhắn đến
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint '/ws' mà client sẽ kết nối đến
        // Cho phép CORS từ tất cả các nguồn (*) để dễ phát triển
        // Trong môi trường sản xuất, bạn nên giới hạn những nguồn được phép
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Hỗ trợ SockJS cho các trình duyệt không hỗ trợ WebSocket natively
    }
}