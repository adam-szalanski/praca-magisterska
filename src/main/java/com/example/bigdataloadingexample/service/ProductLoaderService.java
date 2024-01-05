package com.example.bigdataloadingexample.service;

import com.example.bigdataloadingexample.model.Product;
import com.example.bigdataloadingexample.reader.FileParser;
import com.example.bigdataloadingexample.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductLoaderService {

    private final FileParser fileParser;
    private final ProductRepository productRepository;

    public void readAllProductsAndSaveAll(String fileName) {
        log.debug("Attempting to read file: [{}]",
                  fileName);
        List<Product> products = fileParser.readProductsFromCsv(fileName);
        log.debug("Attempting to save product list of {} records",
                  products.size());
        productRepository.saveAll(products);
        log.debug("Products saved successfully");
    }
}
