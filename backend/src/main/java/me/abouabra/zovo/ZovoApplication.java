package me.abouabra.zovo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * The {@code ZovoApplication} serves as the entry point for the Spring Boot application.
 * <p>
 * It enables asynchronous processing and scheduling using Spring annotations.
 * This class also configures and starts the application.
 */
@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ZovoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZovoApplication.class, args);
        log.info("Zovo application started successfully");
    }
}
