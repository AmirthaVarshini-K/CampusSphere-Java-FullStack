package com.campussphere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CampusSphereApplication {

    private static final Logger log = LoggerFactory.getLogger(CampusSphereApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CampusSphereApplication.class, args);
    }

    @Bean
    CommandLineRunner startupLogger(@Value("${campus-sphere.app-name}") String appName) {
        return args -> log.info("{} started with profile-aware configuration support.", appName);
    }
}
