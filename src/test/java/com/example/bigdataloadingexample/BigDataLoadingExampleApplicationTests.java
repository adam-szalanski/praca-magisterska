package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.repository.ProductRepository;
import com.example.bigdataloadingexample.service.ProductLoadingService;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
@Slf4j
class BigDataLoadingExampleApplicationTests {

    private static final String TEST_DATA_DIRECTORY = "test-data/";
    private static final String TEST_OUTPUT_DIRECTORY = "test-output/";

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductLoadingService service;

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

    @BeforeEach
    @AfterEach
    void truncateTable() {
        log.info("Truncating products table");
        productRepository.truncateTable();
    }

    @ParameterizedTest
    @ValueSource(strings = {"generated_output_1000000.csv", "generated_output_2000000.csv", "generated_output_3000000" +
            ".csv", "generated_output_4000000.csv", "generated_output_5000000.csv", "generated_output_6000000.csv",
            "generated_output_7000000.csv", "generated_output_8000000.csv", "generated_output_9000000.csv",
            "generated_output_11000000.csv", "generated_output_21000000.csv", "generated_output_31000000.csv",
            "generated_output_41000000.csv", "generated_output_51000000.csv", "generated_output_61000000.csv",
            "generated_output_71000000.csv", "generated_output_81000000.csv", "generated_output_91000000.csv"})
    void process_file(String fileName) {
        String filePath = TEST_DATA_DIRECTORY + fileName;

        String streamsRecordingFileName = fileName + ".streams";
        Recording streamsRecording = startRecording(streamsRecordingFileName);
        processWithStreams(filePath);
        stopRecording(streamsRecording);
        analyzeRecording(streamsRecordingFileName);

        truncateTable();

        String arrayListsRecordingFileName = fileName + ".arrayLists";
        Recording arrayListsRecording = startRecording(arrayListsRecordingFileName);
        processWithoutStreams(filePath);
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

    private void processWithStreams(String filePath) {
        log.info("Beginning processing with streams");
        Long start = System.currentTimeMillis();
        service.readAllProductsAndSaveAllStreamImplementation(filePath);
        Long finish = System.currentTimeMillis();
        log.info("Finished processing with streams. Time taken: {}", convertTimeToString(finish - start));
    }

    private void processWithoutStreams(String filePath) {
        log.info("Beginning processing without streams");
        Long start = System.currentTimeMillis();
        service.readAllProductsAndSaveAll(filePath);
        Long finish = System.currentTimeMillis();
        log.info("Finished processing without streams. Time taken: {}", convertTimeToString(finish - start));
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
