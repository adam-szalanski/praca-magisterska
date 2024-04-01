package com.example.bigdataloadingexample.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ExcelDataArchiver {

    private static final int VERTICAL_ROW_OFFSET = 2;
    private static final int HORIZONTAL_ROW_OFFSET = 2;
    private final ExcelStyleProvider excelStyleProvider;
    @Value("${excel.data.filename:wyniki_testow.xlsx}")
    private String filename;

    public void updateExcelFile(TestDataDto testData) throws IOException {
        int lastTestNumber = getLastTestNumber();
        int newTestNumber = lastTestNumber + 1;

        WorkbookDto workBookDto = prepareWorkbook();

        appendTestData(testData, workBookDto.sheet(), newTestNumber, workBookDto.workbook());

        excelStyleProvider.autoSizeColumns(workBookDto.sheet(), 8, HORIZONTAL_ROW_OFFSET);

        saveWorkbook(workBookDto.file(), workBookDto.workbook());

    }

    private WorkbookDto prepareWorkbook() throws IOException {
        Workbook workbook;
        Sheet sheet;
        File file = new File(filename);

        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Wyniki Testów");
            createTableHeaders(sheet, workbook);
        }
        else {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
                sheet = workbook.getSheetAt(0);
            }
        }
        return new WorkbookDto(workbook, sheet, file);
    }

    private void saveWorkbook(File file, Workbook workbook) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }

    private void appendTestData(TestDataDto testData, Sheet sheet, int newTestNumber, Workbook workbook) {
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);
        CreationHelper workbookCreationHelper = workbook.getCreationHelper();

        CellStyle testNumberCellStyle = excelStyleProvider.prepareCellStyleBorderLeftBottomThickBorderTopRightThin(
                workbook);
        Cell testNumberCell = newRow.createCell(HORIZONTAL_ROW_OFFSET);
        testNumberCell.setCellValue(newTestNumber);
        testNumberCell.setCellStyle(testNumberCellStyle);

        CellStyle fileSizeCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        Cell fileSizeCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 1);
        fileSizeCell.setCellValue(Integer.parseInt(testData.getFileSize()));
        fileSizeCell.setCellStyle(fileSizeCellStyle);

        CellStyle jpaCpuUsageCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jpaCpuUsageCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                   .getFormat("0.000%"));
        Cell jpaCpuUsageCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 2);
        jpaCpuUsageCell.setCellValue(testData.getCpuLoadMethodJpa());
        jpaCpuUsageCell.setCellStyle(jpaCpuUsageCellStyle);

        CellStyle jpaMemoryUsageCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jpaMemoryUsageCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                      .getFormat("0.00 \"MB\""));
        Cell jpaMemoryUsageCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 3);
        jpaMemoryUsageCell.setCellValue(testData.getRamUsageMethodJpa());
        jpaMemoryUsageCell.setCellStyle(jpaMemoryUsageCellStyle);

        CellStyle jpaDurationCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jpaDurationCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                   .getFormat("0 \"ms\""));
        Cell jpaDurationCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 4);
        jpaDurationCell.setCellValue(testData.getDurationMethodJpa());
        jpaDurationCell.setCellStyle(jpaDurationCellStyle);

        CellStyle jdbcCpuUsageCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jdbcCpuUsageCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                    .getFormat("0.000%"));
        Cell jdbcCpuUsageCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 5);
        jdbcCpuUsageCell.setCellValue(testData.getCpuLoadMethodJdbc());
        jdbcCpuUsageCell.setCellStyle(jdbcCpuUsageCellStyle);

        CellStyle jdbcMemoryUsageCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jdbcMemoryUsageCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                       .getFormat("0.00 \"MB\""));
        Cell jdbcMemoryUsageCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 6);
        jdbcMemoryUsageCell.setCellValue(testData.getRamUsageMethodJdbc());
        jdbcMemoryUsageCell.setCellStyle(jdbcMemoryUsageCellStyle);

        CellStyle jdbcDurationCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopRightThin(workbook);
        jdbcDurationCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                                    .getFormat("0 \"ms\""));
        Cell jdbcDurationCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 7);
        jdbcDurationCell.setCellValue(testData.getDurationMethodJdbc());
        jdbcDurationCell.setCellStyle(jdbcDurationCellStyle);

        CellStyle dateCellStyle = excelStyleProvider.prepareCellStyleBorderRightBottomThickBorderLeftTopThin(workbook);
        dateCellStyle.setAlignment(HorizontalAlignment.RIGHT);
        String excelFormatPattern = DateFormatConverter.convert(Locale.getDefault(), "yyyy-MM-dd HH:mm:ss");
        dateCellStyle.setDataFormat(workbookCreationHelper.createDataFormat()
                                            .getFormat(excelFormatPattern));
        Cell dateCell = newRow.createCell(HORIZONTAL_ROW_OFFSET + 8);
        dateCell.setCellStyle(dateCellStyle);
        dateCell.setCellValue(testData.getTestDate());
    }

    private int getLastTestNumber() throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return 0;
        }

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 2) {
                return 0;
            }
            Row lastRow = sheet.getRow(lastRowNum);
            Cell lastTestNumCell = lastRow.getCell(HORIZONTAL_ROW_OFFSET);
            if (lastTestNumCell != null && lastTestNumCell.getCellType() == CellType.NUMERIC) {
                return (int) lastTestNumCell.getNumericCellValue();
            }
            return 0;
        }
    }

    private void createTableHeaders(Sheet sheet, Workbook workbook) {
        CellStyle secondRowHeaderStyle = excelStyleProvider.prepareCellStyleBroderLeftTopRightBottomThick(workbook);
        CellStyle leftCellStyle = excelStyleProvider.prepareCellStyleBorderLeftTopThick(workbook);
        CellStyle middleCellStyle = excelStyleProvider.prepareCellStyleBorderTopBottomThick(workbook);
        CellStyle rightCellStyle = excelStyleProvider.prepareCellStyleBorderTopRightThick(workbook);

        Row firstHeaderRow = sheet.createRow(VERTICAL_ROW_OFFSET);
        methodHeader(sheet, firstHeaderRow, leftCellStyle, middleCellStyle, rightCellStyle, "Metoda JPA",
                     HORIZONTAL_ROW_OFFSET + 2);
        methodHeader(sheet, firstHeaderRow, leftCellStyle, middleCellStyle, rightCellStyle, "Metoda JDBC",
                     HORIZONTAL_ROW_OFFSET + 5);

        Row secondHeaderRow = sheet.createRow(VERTICAL_ROW_OFFSET + 1);
        createSecondRowHeader(secondHeaderRow, 0, "Numer Pomiaru", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 1, "Liczba wierszy pliku", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 2, "Obciążenie CPU (%)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 3, "Użycie RAM (MB)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 4, "Czas Trwania (ms)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 5, "Obciążenie CPU (%)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 6, "Użycie RAM (MB)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 7, "Czas Trwania (ms)", secondRowHeaderStyle);
        createSecondRowHeader(secondHeaderRow, 8, "Data Wykonania Pomiaru", secondRowHeaderStyle);
    }

    private void createSecondRowHeader(Row secondHeaderRow, int x, String cellValue, CellStyle secondRowHeaderStyle) {
        Cell secondCell = secondHeaderRow.createCell(HORIZONTAL_ROW_OFFSET + x);
        secondCell.setCellValue(cellValue);
        secondCell.setCellStyle(secondRowHeaderStyle);
    }

    private void methodHeader(Sheet sheet, Row firstHeaderRow, CellStyle leftCellStyle, CellStyle middleCellStyle,
                              CellStyle rightCellStyle, String method, int startIndex) {

        Cell leftCell = firstHeaderRow.createCell(startIndex);
        leftCell.setCellStyle(leftCellStyle);
        leftCell.setCellValue(method);

        Cell middleCell = firstHeaderRow.createCell(startIndex + 1);
        middleCell.setCellStyle(middleCellStyle);

        Cell rightCell = firstHeaderRow.createCell(startIndex + 2);
        rightCell.setCellStyle(rightCellStyle);

        String cellRangeStart = getCharForNumber(startIndex + 1);
        String cellRangeStop = getCharForNumber(startIndex + 1 + 2);

        sheet.addMergedRegion(CellRangeAddress.valueOf(
                "%s%d:%s%d".formatted(cellRangeStart, VERTICAL_ROW_OFFSET + 1, cellRangeStop,
                                      VERTICAL_ROW_OFFSET + 1)));
    }

    private String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
    }

    private record WorkbookDto(Workbook workbook, Sheet sheet, File file) {

    }

}
