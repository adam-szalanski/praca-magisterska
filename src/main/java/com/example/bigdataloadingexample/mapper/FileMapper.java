package com.example.bigdataloadingexample.mapper;

import com.example.bigdataloadingexample.config.DefaultMapstructConfig;
import com.example.bigdataloadingexample.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(config = DefaultMapstructConfig.class)
public interface FileMapper {

    @Mapping(target = ".", source = "fileLine")
    default Product toProduct(String fileLine) {
        String[] values = fileLine.split(",");
        return toProductModel(values[0], values[1], LocalDate.parse(values[2]), values[3], LocalDate.parse(values[4]),
                              values[5], Double.parseDouble(values[6]));
    }

    @Mapping(target = "id", ignore = true)
    Product toProductModel(String name, String category, LocalDate creationDate, String author, LocalDate releaseDate,
                           String publisher, Double reviewScore);
}
