package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
@OpenAPIDefinition(
        info = @Info(
                title       = "Fitness Trainer API",
                version     = "1.0.0",
                description = "First stable version of API"
        )
)
@SpringBootApplication
@EnableScheduling
public class FTWbackendApplication
{
    public static void main(String[] args) {
        SpringApplication.run(FTWbackendApplication.class, args);
    }
}
