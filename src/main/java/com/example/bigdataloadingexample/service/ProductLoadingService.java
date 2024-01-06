package com.example.bigdataloadingexample.service;

import com.example.bigdataloadingexample.model.Product;
import com.example.bigdataloadingexample.reader.FileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductLoadingService {

    private final FileParser fileParser;
    private final ProductSavingService productSavingService;

    public void readAllProductsAndSaveAll(String fileName) {
        log.debug("Attempting to read file: [{}]",
                  fileName);
        List<Product> products = fileParser.readProductsFromCsv(fileName);
        log.debug("Attempting to save product list of {} records",
                  products.size());
        productSavingService.batchSaveNonStream(products);
        log.debug("Products saved successfully");
    }

    public void readAllProductsAndSaveAllStreamImplementation(String fileName) {
        log.debug("Attempting to read file: [{}] and save product records stream",
                  fileName);
        productSavingService.batchSaveStream(fileParser.streamProductsFromCsv(fileName));
        log.debug("Products saved successfully");
    }
}
