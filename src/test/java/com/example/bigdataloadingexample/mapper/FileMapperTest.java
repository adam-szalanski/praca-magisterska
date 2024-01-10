package com.example.bigdataloadingexample.mapper;

import com.example.bigdataloadingexample.model.Product;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileMapperTest {

    private final FileMapper fileMapper = new FileMapperImpl();

    @Test
    void maps_test_data() {
        String name = "test-name";
        String category = "test-category";
        LocalDate creationDate = LocalDate.now();
        String author = "test-author";
        LocalDate releaseDate = LocalDate.now();
        String publisher = "test-publisher";
        Double reviewScore = 5D;

        Product mappedProduct = fileMapper.toProductModel(name,
                                                          category,
                                                          creationDate,
                                                          author,
                                                          releaseDate,
                                                          publisher,
                                                          reviewScore);

        assertEquals(name,
                     mappedProduct.getName());
        assertEquals(category,
                     mappedProduct.getCategory());
        assertEquals(creationDate,
                     mappedProduct.getCreationDate());
        assertEquals(author,
                     mappedProduct.getAuthor());
        assertEquals(releaseDate,
                     mappedProduct.getReleaseDate());
        assertEquals(publisher,
                     mappedProduct.getPublisher());
        assertEquals(reviewScore,
                     mappedProduct.getReviewScore());
    }
}