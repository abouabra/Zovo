package me.abouabra.zovo;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZovoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZovoApplication.class, args);
    }
}
