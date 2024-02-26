package com.example.bigdataloadingexample.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Component
public class DataGenerator {

    private static final String DATA_DIRECTORY = "test-data/";
    private static final Random random = new Random();
    private static final String[] categories = {"Book", "Game", "Music", "Movie"};
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public void generateDataFile(int lines) {
        String fileName = DATA_DIRECTORY + "generated_data_%d.csv".formatted(lines);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Name,Category,CreationDate,Author,ReleaseDate,Publisher,ReviewScore");
            writer.newLine();

            for (int i = 1; i <= lines; i++) {
                writer.write(generateDataLine(i));
                writer.newLine();
            }

        } catch (IOException e) {
            log.error("Unable to write to file: {}", fileName, e);
        }
    }

    private String generateDataLine(int id) {
        String name = "Product_" + id;
        String category = categories[random.nextInt(categories.length)];
        LocalDate creationDate = LocalDate.now().minusDays(random.nextInt(365 * 26));
        String author = "Author_" + (random.nextInt(1000) + 1);
        LocalDate releaseDate = creationDate.plusDays(random.nextInt(365 * 26));
        String publisher = "Publisher_" + (random.nextInt(500) + 1);
        double reviewScore = 1 + (10 - 1) * random.nextDouble();

        return String.format("%s,%s,%s,%s,%s,%s,%.2f",
                             name,
                             category,
                             creationDate.format(dateFormatter),
                             author,
                             releaseDate.format(dateFormatter),
                             publisher,
                             reviewScore);
    }

}
