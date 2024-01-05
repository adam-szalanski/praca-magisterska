package com.example.bigdataloadingexample.mapper;

import com.example.bigdataloadingexample.config.DefaultMapstructConfig;
import com.example.bigdataloadingexample.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(config = DefaultMapstructConfig.class)
public interface FileMapper {

    @Mapping(target = "id", ignore = true)
    Product toProductModel(String name, String category, LocalDate creationDate, String author, LocalDate releaseDate,
                           String publisher, Double reviewScore);
}
