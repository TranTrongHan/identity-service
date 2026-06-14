package com.luketran.identity.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.luketran.identity")
@EnableJpaRepositories(basePackages = "com.luketran.identity.infrastructure.persistence.jpa")
@EntityScan(basePackages = "com.luketran.identity.infrastructure.persistence.entities")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
