package qdt.hcmute.vn.dqtbook_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                .addPathPatterns("/**")              // áp dụng cho tất cả các request
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/users/register",
                        "/error",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/static/**",
                        "/ws/**",       // Cho phép WebSocket đi qua mà không cần session
                        "/views/**",
                        "/home",
                        "/"
                ); // bỏ qua các path không cần check session
    }
}
