// src/main/java/com/example/festival_management/config/WebConfig.java
package com.example.festival_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
 // apla dhlwnoume ta static paths (an theleis kan to afineis keno,
        // giati to Spring Boot ta servicearei idi apo /static, /public klp)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
       registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/");

    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // ώστε το "/" να σερβίρει index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
