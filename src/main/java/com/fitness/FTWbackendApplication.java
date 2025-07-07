package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FTWbackendApplication
{
    public static void main(String[] args) {
        SpringApplication.run(FTWbackendApplication.class, args);
    }
}
