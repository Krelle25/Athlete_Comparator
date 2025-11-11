package org.example.athlete_comparator;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for customizing Spring MVC behavior.

 * This configuration tells Spring Boot where to find static resources like
 * CSS stylesheets and JavaScript files. Without this, requests to URLs like
 * /stylesheet/styles.css or /js/app.js wouldn't know where to look.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers resource handlers to serve static files.
     * Maps URL patterns to actual file locations in the classpath (src/main/resources).
     *
     * @param registry The ResourceHandlerRegistry to configure
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // When browser requests /stylesheet/styles.css, serve it from src/main/resources/stylesheet/
        registry.addResourceHandler("/stylesheet/**")
                .addResourceLocations("classpath:/stylesheet/");

        // When browser requests /js/app.js, serve it from src/main/resources/js/
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/js/");
    }
}
