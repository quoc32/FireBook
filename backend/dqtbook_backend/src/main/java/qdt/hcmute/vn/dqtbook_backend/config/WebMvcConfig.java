package qdt.hcmute.vn.dqtbook_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Map tài nguyên tĩnh đúng với các URL đang dùng trong HTML:
 * - /static/**           -> classpath:/static/
 * - /img/**              -> classpath:/static/img/
 * - /favicon.ico/.png    -> classpath:/static/
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");

        registry
            .addResourceHandler("/img/**")
            .addResourceLocations("classpath:/static/img/");

        registry
            .addResourceHandler("/favicon.ico", "/favicon.png")
            .addResourceLocations("classpath:/static/favicon.ico", "classpath:/static/favicon.png");
    }
}