package com.example.bigdataloadingexample.reader;

import com.example.bigdataloadingexample.mapper.FileMapper;
import com.example.bigdataloadingexample.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileParser {

    private final FileMapper fileMapper;

    public List<Product> readProductsFromCsv(String fileName) {
        List<Product> products = new ArrayList<>();
        try (Reader in = new FileReader(fileName)) {
            Iterable<CSVRecord> records = CSVFormat.Builder
                    .create()
                    .setHeader("Name",
                               "Category",
                               "Creation Date",
                               "Author",
                               "Release Date",
                               "Publisher",
                               "Review Score")
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(in);
            for (CSVRecord csvRecord : records) {
                Product product = fileMapper.toProductModel(csvRecord.get("Name"),
                                                            csvRecord.get("Category"),
                                                            LocalDate.parse(csvRecord.get("Creation Date")),
                                                            csvRecord.get("Author"),
                                                            LocalDate.parse(csvRecord.get("Release Date")),
                                                            csvRecord.get("Publisher"),
                                                            Double.parseDouble(csvRecord.get("Review Score")));
                products.add(product);
                log.debug("Product list size: {}",
                          products.size());
            }
        } catch (Exception e) {
            log.error("File parsing failed",
                      e);
        }
        return products;
    }
}
