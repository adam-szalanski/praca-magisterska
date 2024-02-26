package com.example.bigdataloadingexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestBigDataLoadingExampleApplication {

    public static void main(String[] args) {
        SpringApplication.from(BigDataLoadingExampleApplication::main).with(TestBigDataLoadingExampleApplication.class).run(args);
    }

    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
    }

}
