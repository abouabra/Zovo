package me.abouabra.zovo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The ZovoApplication class serves as the entry point for the Spring Boot application.
 * <p>
 * It enables asynchronous processing using the {@code @EnableAsync} annotation and
 * initializes the application using the {@code @SpringBootApplication} annotation.
 */
@EnableAsync
@SpringBootApplication
public class ZovoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZovoApplication.class, args);
    }
}
