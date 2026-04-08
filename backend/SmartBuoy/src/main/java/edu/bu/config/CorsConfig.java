package edu.bu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Global CORS configuration. Allowed origins are injected from the cors.allowed-origins property. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  /** Creates CorsConfig with allowed origins from application properties. */
  public CorsConfig(@Value("${cors.allowed-origins}") String allowedOriginsValue) {
    this.allowedOrigins = allowedOriginsValue.split(",");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
