package com.example.bigdataloadingexample.processing;

import com.example.bigdataloadingexample.mapper.FileMapper;
import com.example.bigdataloadingexample.model.Product;
import com.example.bigdataloadingexample.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Qualifier("jpaStrategy")
@Component
public class HibernateJpaProcessingStrategy implements ProcessingStrategy {

    private final FileMapper fileMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void process(Stream<String> data) {
        Stream<Product> products = data.map(fileMapper::toProduct);
        products.forEach(productRepository::save);
    }

}
