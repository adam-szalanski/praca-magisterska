package com.example.bigdataloadingexample.reader;

import com.example.bigdataloadingexample.mapper.FileMapper;
import com.example.bigdataloadingexample.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileParser {

    private final FileMapper fileMapper;

    public List<Product> readProductsFromCsv(String fileName) {
        List<Product> products = new ArrayList<>();
        try (Reader in = new FileReader(fileName);
             BufferedReader reader = new BufferedReader(in)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    products.add(mapLineToProduct(line));
                }
                else {
                    firstLine = false;
                }
            }
        } catch (Exception e) {
            log.error("File parsing failed",
                      e);
        }
        return products;
    }

    @SneakyThrows
    public Stream<Product> streamProductsFromCsv(String fileName) {
        Path filePath = Path.of(fileName);
        Stream<String> lineStream = Files.lines(filePath);
        return lineStream
                .skip(1)
                .map(this::mapLineToProduct);
    }

    private Product mapLineToProduct(String line) {
        String[] values = line.split(",");
        return fileMapper.toProductModel(
                values[0],
                values[1],
                LocalDate.parse(values[2]),
                values[3],
                LocalDate.parse(values[4]),
                values[5],
                Double.parseDouble(values[6]));
    }
}
