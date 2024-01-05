package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.service.ProductLoaderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BigDataLoadingExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BigDataLoadingExampleApplication.class,
                              args);
    }

    @Bean
    CommandLineRunner runner(ProductLoaderService service) {
        return ignored -> service.readAllProductsAndSaveAll("test-data/generated_output_1000000.csv");
    }
}
