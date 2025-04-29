package me.abouabra.zovo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * <p>The entry point for the Zovo application. This class initializes and starts the Spring Boot application.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li><b>@EnableAsync:</b> Enables asynchronous processing in the application.</li>
 *   <li><b>@EnableScheduling:</b> Enables Spring's scheduling capabilities.</li>
 *   <li><b>@SpringBootApplication:</b> Indicates this is a Spring Boot application.</li>
 * </ul>
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ZovoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZovoApplication.class, args);
    }
}
