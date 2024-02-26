package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.exceptions.UnableToReadFileException;
import com.example.bigdataloadingexample.processing.ProcessingStrategy;
import com.example.bigdataloadingexample.reader.FileParser;
import com.example.bigdataloadingexample.repository.ProductRepository;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest
@Slf4j
class BigDataLoadingExampleApplicationTests {

    private static final String TEST_DATA_DIRECTORY = "test-data/";
    private static final String TEST_OUTPUT_DIRECTORY = "test-output/";

    @Autowired
    private ProductRepository productRepository;
    @Qualifier("jpaStrategy")
    @Autowired
    private ProcessingStrategy jpaStrategy;
    @Qualifier("jdbcStrategy")
    @Autowired
    private ProcessingStrategy jdbcStrategy;
    @Autowired
    private FileParser fileParser;

    public static String convertTimeToString(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        long milliseconds = millis % 1000;

        return String.format("%02d h %02d min %02d sec %03d ms", hours, minutes, seconds, milliseconds);
    }

    public static String convertCpuUsageToString(float cpuUsage) {
        return String.format("%.2f%%", cpuUsage * 100);
    }

    public static String convertMemoryUsageToString(long memoryBytes) {
        float memoryInMB = memoryBytes / (1024f * 1024f);
        return String.format("%.2f MB", memoryInMB);
    }

    public static Stream<Arguments> list_available_test_files() {
        Optional<File[]> testFilesNames = Optional.ofNullable(new File(TEST_DATA_DIRECTORY).listFiles());
        return Stream.of(testFilesNames.orElseThrow())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(fileName -> fileName.startsWith("generated_data_") && fileName.endsWith(".csv"))
                .sorted((o1, o2) -> {
                    Integer o1Value = Integer.parseInt(o1.replace("generated_data_", Strings.EMPTY)
                                                               .replace(".csv", Strings.EMPTY));
                    Integer o2Value = Integer.parseInt(o2.replace("generated_data_", Strings.EMPTY)
                                                               .replace(".csv", Strings.EMPTY));
                    return o1Value.compareTo(o2Value);
                })
                .map(Arguments::of);
    }

    @BeforeEach
    @AfterEach
    void truncateTable() {
        log.info("Truncating products table");
        productRepository.truncateTable();
    }

    @ParameterizedTest
    @MethodSource("list_available_test_files")
    void process_file(String fileName) throws UnableToReadFileException {
        String filePath = TEST_DATA_DIRECTORY + fileName;

        Stream<String> jdbcData = fileParser.streamProductsFromCsv(filePath);
        String streamsRecordingFileName = fileName + ".jdbc";
        Recording streamsRecording = startRecording(streamsRecordingFileName);
        processWithJdbc(jdbcData);
        stopRecording(streamsRecording);
        analyzeRecording(streamsRecordingFileName);

        truncateTable();

        Stream<String> jpaData = fileParser.streamProductsFromCsv(filePath);
        String arrayListsRecordingFileName = fileName + ".jpa";
        Recording arrayListsRecording = startRecording(arrayListsRecordingFileName);
        processWithJpa(jpaData);
        stopRecording(arrayListsRecording);
        analyzeRecording(arrayListsRecordingFileName);
    }

    @SneakyThrows
    private Recording startRecording(String fileName) {
        Path recordingPath = Path.of(TEST_OUTPUT_DIRECTORY + fileName);
        Recording recording = new Recording();
        recording.enable("jdk.CPULoad")
                .withoutThreshold();
        recording.enable("jdk.GCHeapSummary")
                .withoutThreshold();
        recording.setName(fileName);
        recording.setToDisk(true);
        recording.setDestination(recordingPath);
        recording.start();
        return recording;
    }

    @SneakyThrows
    private void stopRecording(Recording recording) {
        recording.dump(recording.getDestination());
        recording.stop();
    }

    private void processWithJdbc(Stream<String> data) {
        log.info("Beginning processing with JdbcTemplate");
        Long start = System.currentTimeMillis();
        jdbcStrategy.process(data);
        Long finish = System.currentTimeMillis();
        log.info("Finished processing with JdbcTemplate. Time taken: {}", convertTimeToString(finish - start));
    }

    private void processWithJpa(Stream<String> data) {
        log.info("Beginning processing with JPA");
        Long start = System.currentTimeMillis();
        jpaStrategy.process(data);
        Long finish = System.currentTimeMillis();
        log.info("Finished processing with JPA. Time taken: {}", convertTimeToString(finish - start));
    }

    void analyzeRecording(String fileName) {
        Path recordingFilePath = Path.of(TEST_OUTPUT_DIRECTORY + fileName);
        float maxCpuUsage = 0F;
        long maxMemoryUsed = 0L;
        try (RecordingFile recordingFile = new RecordingFile(recordingFilePath)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent recordedEvent = recordingFile.readEvent();
                if (recordedEvent.getEventType()
                        .getName()
                        .equals("jdk.CPULoad")) {
                    float recordedCpuUsage = recordedEvent.getFloat("jvmUser");
                    maxCpuUsage = Math.max(recordedCpuUsage, maxCpuUsage);
                }
                if (recordedEvent.getEventType()
                        .getName()
                        .equals("jdk.GCHeapSummary")) {
                    long recordedMemoryUsage = recordedEvent.getLong("heapUsed");
                    maxMemoryUsed = Math.max(maxMemoryUsed, recordedMemoryUsage);
                }
            }
            log.info("Maximum CPU usage: {}", convertCpuUsageToString(maxCpuUsage));
            log.info("Maximum memory used: {}", convertMemoryUsageToString(maxMemoryUsed));
        } catch (IOException e) {
            log.error("Recording file not found for [{}]", fileName);
        }
    }
}
