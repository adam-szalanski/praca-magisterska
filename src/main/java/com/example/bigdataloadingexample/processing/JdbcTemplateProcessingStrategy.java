package com.example.bigdataloadingexample.processing;

import com.example.bigdataloadingexample.repository.ProductsJDBCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Qualifier("jdbcStrategy")
@Component
public class JdbcTemplateProcessingStrategy implements ProcessingStrategy {

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
    private static final String SEPARATOR = "','";
    private static final String VALUE_TEMPLATE =
            "('" + NAME + SEPARATOR + CATEGORY + SEPARATOR + CREATION_DATE + SEPARATOR + AUTHOR + SEPARATOR + RELEASE_DATE + SEPARATOR + PUBLISHER + SEPARATOR + REVIEW_SCORE + "')";
    private static final int BATCH_SIZE = 5000;

    private final ProductsJDBCRepository productsJDBCRepository;

    @Override
    public void process(Stream<String> data) {
        batchSaveStream(data);
    }


    public void batchSaveStream(Stream<String> productsStringStream) {
        String[] currentQuery = {INSERT_TEMPLATE};
        int[] currentBatchCount = {0};
        int[] currentBatchNumber = {0};
        productsStringStream.forEach(productString -> {
            if (currentBatchCount[0] != BATCH_SIZE) {
                if (currentBatchCount[0] != 0) {
                    currentQuery[0] = currentQuery[0].concat(",");
                }
                currentQuery[0] = currentQuery[0].concat(mapProductStringToValuesString(productString));
                currentBatchCount[0]++;
            }
            else {
                currentQuery[0] = currentQuery[0].concat(";");
                log.debug("Saving batch number {} with streams!", ++currentBatchNumber[0]);
                productsJDBCRepository.executeQuery(currentQuery[0]);
                currentQuery[0] = INSERT_TEMPLATE + mapProductStringToValuesString(productString);
                currentBatchCount[0] = 1;
            }
        });

    }

    private String mapProductStringToValuesString(String fileLine) {
        String[] values = fileLine.split(",");
        return VALUE_TEMPLATE.replace(NAME, values[0])
                .replace(CATEGORY, values[1])
                .replace(CREATION_DATE, values[2])
                .replace(AUTHOR, values[3])
                .replace(RELEASE_DATE, values[4])
                .replace(PUBLISHER, values[5])
                .replace(REVIEW_SCORE, values[6]);
    }
}
