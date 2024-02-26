package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.generator.DataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Slf4j
class TestDataGenerationTest {

    @Autowired
    private DataGenerator dataGenerator;

    public static List<Arguments> prepareDataSizes() {
        List<Arguments> arguments = new ArrayList<>();

        for (int i = 1; i <= 100000000; i *= 100) {
            for (int j = 1; j <= 10; j++) {
                arguments.add(Arguments.of(i * j));
            }
        }

        return arguments;
    }

    @ParameterizedTest
    @MethodSource("prepareDataSizes")
    void generateTestData(int dataSize) {
        assertDoesNotThrow(() -> dataGenerator.generateDataFile(dataSize));
    }
}
