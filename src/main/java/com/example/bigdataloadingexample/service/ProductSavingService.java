package com.example.bigdataloadingexample.service;

import com.example.bigdataloadingexample.model.Product;
import com.example.bigdataloadingexample.repository.ProductsJDBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSavingService {

    private static final String INSERT_TEMPLATE = """
            insert into products ("name","category","creation_date","author","release_date","publisher","review_score")
            values
            """;
    private static final String NAME = "$name";
    private static final String CATEGORY = "$category";
    private static final String CREATION_DATE = "$creation_date";
    private static final String AUTHOR = "$author";
    private static final String RELEASE_DATE = "$release_date";
    private static final String PUBLISHER = "$publisher";
    private static final String REVIEW_SCORE = "$review_score";
    private static final String VALUE_TEMPLATE = """
            ('$name','$category','$creation_date','$author','$release_date','$publisher','$review_score')""";
    private static final int BATCH_SIZE = 5000;
    private final ProductsJDBCRepository productsJDBCRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void batchSaveNonStream(List<Product> products) {
        String currentQuery = INSERT_TEMPLATE;
        int currentBatchCount = 0;
        int currentBatchNumber = 0;
        for (Product product : products) {
            if (currentBatchCount != BATCH_SIZE
                    &&
                    product != products.getLast()) {
                if (currentBatchCount != 0) {
                    currentQuery = currentQuery.concat(",");
                }
                currentQuery = currentQuery.concat(mapProductToValuesString(product));
                currentBatchCount++;
            }
            else {
                currentQuery = currentQuery.concat(";");
                log.debug("Saving batch number {} with array list", ++currentBatchNumber);
                productsJDBCRepository.executeQuery(currentQuery);
                currentQuery = INSERT_TEMPLATE + mapProductToValuesString(product);
                currentBatchCount = 1;
            }
        }
    }

    public void batchSaveStream(Stream<Product> products) {
        final String[] currentQuery = {INSERT_TEMPLATE};
        final int[] currentBatchCount = {0};
        final int[] currentBatchNumber = {0};
        products.forEach(product -> {
                             if (currentBatchCount[0] != BATCH_SIZE) {
                                 if (currentBatchCount[0] != 0) {
                                     currentQuery[0] = currentQuery[0].concat(",");
                                 }
                                 currentQuery[0] = currentQuery[0].concat(mapProductToValuesString(product));
                                 currentBatchCount[0]++;
                             }
                             else {
                                 currentQuery[0] = currentQuery[0].concat(";");
                                 log.debug("Saving batch number {} with streams!", ++currentBatchNumber[0]);
                                 productsJDBCRepository.executeQuery(currentQuery[0]);
                                 currentQuery[0] = INSERT_TEMPLATE + mapProductToValuesString(product);
                                 currentBatchCount[0] = 1;
                             }
                         }
                        );

    }

    private String mapProductToValuesString(Product product) {
        return VALUE_TEMPLATE
                .replace(NAME, product.getName())
                .replace(CATEGORY, product.getCategory())
                .replace(CREATION_DATE, product.getCreationDate()
                        .format(dateTimeFormatter))
                .replace(AUTHOR, product.getAuthor())
                .replace(RELEASE_DATE, product.getReleaseDate()
                        .format(dateTimeFormatter))
                .replace(PUBLISHER, product.getPublisher())
                .replace(REVIEW_SCORE, product.getReviewScore()
                        .toString());
    }
}
