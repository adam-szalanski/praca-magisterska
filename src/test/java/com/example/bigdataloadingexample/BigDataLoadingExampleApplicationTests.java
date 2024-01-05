package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.repository.ProductRepository;
import com.example.bigdataloadingexample.service.ProductLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class BigDataLoadingExampleApplicationTests {

    private static final String TEST_DATA_DIRECTORY = "test-data/";

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductLoaderService service;

    @BeforeEach
    @AfterEach
    void truncateTable() {
        productRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"generated_output_1000000.csv", "generated_output_101000000.csv",
            "generated_output_201000000.csv", "generated_output_301000000.csv", "generated_output_401000000.csv",
            "generated_output_501000000.csv", "generated_output_601000000.csv", "generated_output_701000000.csv",
            "generated_output_801000000.csv", "generated_output_901000000.csv"})
    void process_file(String fileName) {
        String filePath = TEST_DATA_DIRECTORY + fileName;

        processWithStreams(filePath);
        truncateTable();
        processWithoutStreams(filePath);
    }

    private void processWithStreams(String filePath) {
        log.info("Beginning processing with streams");
        Long streamsStart = System.currentTimeMillis();
        service.readAllProductsAndSaveAllStreamImplementation(filePath);
        Long streamsFinish = System.currentTimeMillis();
        log.info("Finished processing with streams. Time taken: [{}ms]", streamsFinish - streamsStart);
    }

    private void processWithoutStreams(String filePath) {
        log.info("Beginning processing without streams");
        Long streamsStart = System.currentTimeMillis();
        service.readAllProductsAndSaveAllStreamImplementation(filePath);
        Long streamsFinish = System.currentTimeMillis();
        log.info("Finished processing without streams. Time taken: [{}ms]", streamsFinish - streamsStart);
    }

}
