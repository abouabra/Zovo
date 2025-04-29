package me.abouabra.zovo.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * WebConfig is a configuration class that defines the configuration related to web requests
 * in a Spring application. It provides a bean for request logging to log details of HTTP requests
 * entering the application.
 * <p>
 * The request logging functionality is provided by a CommonsRequestLoggingFilter,
 * which is configured to include specific details about each request, such as client information,
 * query string, and headers. This class helps in debugging and monitoring HTTP requests efficiently.
 * <p>
 * It is annotated with @Configuration, making it part of the Spring container for dependency injection.
 */
@Configuration
public class WebConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);  // Log client IP
        filter.setIncludeQueryString(true); // Log query string
        filter.setIncludeHeaders(true);     // Log request headers
        filter.setIncludePayload(false);    // Do not log the request body by default
        filter.setAfterMessagePrefix("REQUEST DATA: "); // Customize log prefix
        return filter;
    }
}

