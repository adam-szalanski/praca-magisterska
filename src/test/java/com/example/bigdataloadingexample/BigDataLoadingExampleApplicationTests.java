package com.example.bigdataloadingexample;

import com.example.bigdataloadingexample.excel.ExcelDataArchiver;
import com.example.bigdataloadingexample.excel.TestDataDto;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest
@Slf4j
class BigDataLoadingExampleApplicationTests {

    private static final String TEST_DATA_DIRECTORY = "test-data/";
    private static final String TEST_OUTPUT_DIRECTORY = "test-output/";
    private static final String JDK_CPULOAD = "jdk.CPULoad";
    private static final String JDK_GCHEAP_SUMMARY = "jdk.PhysicalMemory";
    private static final String CPU_LOAD_READ_PARAMETER = "machineTotal";
    private static final String MEMORY_USED_READ_PARAMETER = "usedSize";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer;
    private static final String DB_NAME = "praca-magisterska";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = USERNAME;
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
    @Autowired
    private ExcelDataArchiver excelDataArchiver;

    static {
        PostgreSQLContainerProvider postgreSQLContainerProvider = new PostgreSQLContainerProvider();
        postgreSQLContainer = (PostgreSQLContainer<?>) postgreSQLContainerProvider.newInstance("latest")
                .withInitScript("db/init-db.sql")
                .withDatabaseName(DB_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                     () -> String.format("jdbc:postgresql://localhost:%d/%s", postgreSQLContainer.getFirstMappedPort(),
                                         DB_NAME));
        registry.add("spring.datasource.username", () -> USERNAME);
        registry.add("spring.datasource.password", () -> PASSWORD);
    }

    public static String convertTimeToString(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        long milliseconds = millis % 1000;

        return String.format("%02d h %02d min %02d sec %03d ms", hours, minutes, seconds, milliseconds);
    }

    public static String convertCpuUsageToString(float cpuUsage) {
        return String.format("%.2f%%", getCpuUsagePercentage(cpuUsage));
    }

    public static String convertMemoryUsageToString(long memoryBytes) {
        float memoryInMB = getMemoryInMB(memoryBytes);
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
        TestDataDto testDataDto = prepareDataDto(fileName);

        Stream<String> jdbcData = fileParser.streamProductsFromCsv(filePath);
        String streamsRecordingFileName = fileName + ".jdbc";
        Recording streamsRecording = startRecording(streamsRecordingFileName);
        processWithJdbc(jdbcData);
        stopRecording(streamsRecording);
        analyzeRecording(TestedMethod.JDBC, testDataDto, streamsRecordingFileName);
        jdbcData.close();

        truncateTable();

        Stream<String> jpaData = fileParser.streamProductsFromCsv(filePath);
        String arrayListsRecordingFileName = fileName + ".jpa";
        Recording arrayListsRecording = startRecording(arrayListsRecordingFileName);
        processWithJpa(jpaData);
        stopRecording(arrayListsRecording);
        analyzeRecording(TestedMethod.JPA, testDataDto, arrayListsRecordingFileName);
        jpaData.close();

        testDataDto.setTestDate(LocalDateTime.now()
                                        .format(DATE_TIME_FORMATTER));
        try {
            excelDataArchiver.updateExcelFile(testDataDto);
        } catch (IOException e) {
            log.error("Failed to update excel file with following data: [{}]", testDataDto, e);
        }

    }

    private TestDataDto prepareDataDto(String fileName) {
        TestDataDto testDataDto = new TestDataDto();
        testDataDto.setFileSize(fileName.replace("generated_data_", Strings.EMPTY)
                                        .replace(".csv", Strings.EMPTY));
        return testDataDto;
    }

    @SneakyThrows
    private Recording startRecording(String fileName) {
        Path recordingPath = Path.of(TEST_OUTPUT_DIRECTORY + fileName);
        Recording recording = new Recording();
        recording.enable(JDK_CPULOAD)
                .withoutThreshold();
        recording.enable(JDK_GCHEAP_SUMMARY)
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
        jdbcStrategy.process(data);
        log.info("Finished processing with JdbcTemplate");
    }

    private void processWithJpa(Stream<String> data) {
        log.info("Beginning processing with JPA");
        jpaStrategy.process(data);
        log.info("Finished processing with JPA");
    }

    void analyzeRecording(TestedMethod testedMethod, TestDataDto testDataDto, String fileName) {
        Path recordingFilePath = Path.of(TEST_OUTPUT_DIRECTORY + fileName);
        float maxCpuUsage = 0F;
        long maxMemoryUsed = 0L;
        long milliseconds;
        Instant startTime = null, endTime = null;
        Duration duration;
        try (RecordingFile recordingFile = new RecordingFile(recordingFilePath)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent recordedEvent = recordingFile.readEvent();
                if (startTime == null) {
                    startTime = recordedEvent.getStartTime();
                }
                endTime = recordedEvent.getEndTime();
                switch (recordedEvent.getEventType()
                        .getName()) {
                    case JDK_CPULOAD: {
                        float recordedCpuUsage = recordedEvent.getFloat(CPU_LOAD_READ_PARAMETER);
                        maxCpuUsage = Math.max(recordedCpuUsage, maxCpuUsage);
                        break;
                    }
                    case JDK_GCHEAP_SUMMARY: {
                        long recordedMemoryUsage = recordedEvent.getLong(MEMORY_USED_READ_PARAMETER);
                        maxMemoryUsed = Math.max(maxMemoryUsed, recordedMemoryUsage);
                        break;
                    }
                }
            }
            duration = Duration.between(startTime, endTime);
            milliseconds = duration.toMillis();
            log.info("Maximum CPU usage: {}", convertCpuUsageToString(maxCpuUsage));
            log.info("Maximum memory used: {}", convertMemoryUsageToString(maxMemoryUsed));
            log.info("Time taken: {}", convertTimeToString(milliseconds));
            switch (testedMethod) {
                case JPA -> {
                    testDataDto.setDurationMethodJpa(milliseconds);
                    testDataDto.setCpuLoadMethodJpa(getCpuUsagePercentage(maxCpuUsage));
                    testDataDto.setRamUsageMethodJpa(getMemoryInMB(maxMemoryUsed));
                }
                case JDBC -> {
                    testDataDto.setDurationMethodJdbc(milliseconds);
                    testDataDto.setCpuLoadMethodJdbc(getCpuUsagePercentage(maxCpuUsage));
                    testDataDto.setRamUsageMethodJdbc(getMemoryInMB(maxMemoryUsed));
                }
            }
        } catch (IOException e) {
            log.error("Recording file not found for [{}]", fileName);
        }
    }

    private static float getCpuUsagePercentage(float cpuUsage) {
        return cpuUsage * 100;
    }

    private static float getMemoryInMB(long memoryBytes) {
        return memoryBytes / (1024f * 1024f);
    }

    private enum TestedMethod {
        JDBC, JPA
    }
}
