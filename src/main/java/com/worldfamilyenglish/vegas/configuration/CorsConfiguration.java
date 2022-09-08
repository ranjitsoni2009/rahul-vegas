package com.worldfamilyenglish.vegas.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        //TODO: provide proper injected cors clients from an environment variable.
       registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "127.0.0.1:300", "https://wfevegasbackofficedev.z7.web.core.windows.net", "https://wfevegasbackofficeprod.z7.web.core.windows.net")
               .allowedMethods("HEAD", "GET", "PUT", "POST", "OPTIONS", "DELETE", "PATCH");
    }
}