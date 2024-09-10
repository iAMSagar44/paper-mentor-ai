package dev.sagar.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig  {
    private final Environment environment;
    @Autowired
    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);
        String allowedOrigins = environment.getProperty("API_ALLOW_ORIGINS");
        LOGGER.debug("The allowed origins are:: {}", allowedOrigins);
        return new WebMvcConfigurer() {
            final String [] origins = allowedOrigins != null ? allowedOrigins.split(";") : new String[0];
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods("*");
            }
        };
    }

}
