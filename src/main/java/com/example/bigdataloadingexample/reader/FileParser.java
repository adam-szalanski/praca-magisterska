package com.example.bigdataloadingexample.reader;

import com.example.bigdataloadingexample.exceptions.UnableToReadFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileParser {

    public Stream<String> streamProductsFromCsv(String fileName) throws UnableToReadFileException {
        Path filePath = Path.of(fileName);
        try {
            Stream<String> lineStream = Files.lines(filePath);

            return lineStream.skip(1);
        } catch (IOException e) {
            log.error("Unable to read file: [{}]", fileName);
            throw new UnableToReadFileException("Unable to read file: " + fileName);
        }
    }
}
