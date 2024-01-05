package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.service.ProductLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication
@Slf4j
public class BigDataLoadingExampleApplication implements CommandLineRunner {

    private final ProductLoaderService service;

    @Override
    public void run(String... args) {
        log.info("Beginning processing with streams");
        Long streamsStart = System.currentTimeMillis();
        service.readAllProductsAndSaveAllStreamImplementation("test-data/generated_output_1000000.csv");
        Long streamsFinish = System.currentTimeMillis();
        log.info("Finished processing with streams. Time taken: [{}ms]", streamsFinish - streamsStart);

        log.info("Beginning processing without streams");
        Long nonStreamStart = System.currentTimeMillis();
        service.readAllProductsAndSaveAll("test-data/generated_output_1000000.csv");
        Long nonStreamFinish = System.currentTimeMillis();
        log.info("Finished processing without streams. Time taken: [{}ms]", nonStreamFinish - nonStreamStart);
    }

    public static void main(String[] args) {
        SpringApplication.run(BigDataLoadingExampleApplication.class,
                              args);
    }
}
