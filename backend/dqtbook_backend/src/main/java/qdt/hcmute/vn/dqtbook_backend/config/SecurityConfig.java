package qdt.hcmute.vn.dqtbook_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Dòng này ra lệnh cho Spring Security: "Hãy tìm một bean cấu hình CORS có sẵn và sử dụng nó"
            // Bean này đang nằm trong file CorsConfig.java của bạn.
            .cors(Customizer.withDefaults())
            
            // Tắt CSRF
            .csrf(csrf -> csrf.disable())
            
            // Cho phép tất cả các request đi qua, việc kiểm tra sẽ do SessionInterceptor đảm nhiệm
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}