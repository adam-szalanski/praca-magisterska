package com.example.bigdataloadingexample.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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
public class ExcelDataArchiver {

    @Value("${excel.data.filename:wyniki_testow.xlsx}")
    private String filename;

    public void updateExcelFile(TestDataDto testData) throws IOException {
        int lastTestNumber = getLastTestNumber();
        int newTestNumber = lastTestNumber + 1;

        Workbook workbook;
        Sheet sheet;
        File file = new File(filename);

        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Wyniki Testów");
            createHeader(sheet, workbook);
        }
        else {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
                sheet = workbook.getSheetAt(0);
            }
        }

        // Dodawanie nowego wiersza z danymi testu
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);
        newRow.createCell(0)
                .setCellValue(newTestNumber);
        newRow.createCell(1)
                .setCellValue(testData.getFileSize());
        newRow.createCell(2)
                .setCellValue(testData.getCpuLoadMethodJpa());
        newRow.createCell(3)
                .setCellValue(testData.getRamUsageMethodJpa());
        newRow.createCell(4)
                .setCellValue(testData.getDurationMethodJpa());
        newRow.createCell(5)
                .setCellValue(testData.getCpuLoadMethodJdbc());
        newRow.createCell(6)
                .setCellValue(testData.getRamUsageMethodJdbc());
        newRow.createCell(7)
                .setCellValue(testData.getDurationMethodJdbc());
        CellStyle dateCellStyle = workbook.createCellStyle();
        String excelFormatPattern = DateFormatConverter.convert(Locale.getDefault(), "yyyy-MM-dd HH:mm:ss");
        dateCellStyle.setDataFormat(workbook.getCreationHelper()
                                            .createDataFormat()
                                            .getFormat(excelFormatPattern));
        Cell dateCell = newRow.createCell(8);
        dateCell.setCellStyle(dateCellStyle);
        dateCell.setCellValue(testData.getTestDate());

        // Zapisywanie zmian do pliku
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
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
            Cell lastTestNumCell = lastRow.getCell(0);
            if (lastTestNumCell != null && lastTestNumCell.getCellType() == CellType.NUMERIC) {
                return (int) lastTestNumCell.getNumericCellValue();
            }
            return 0;
        }
    }

    private void createHeader(Sheet sheet, Workbook workbook) {
        Row firstHeaderRow = sheet.createRow(0);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THICK);
        cellStyle.setBorderLeft(BorderStyle.THICK);
        cellStyle.setBorderRight(BorderStyle.THICK);
        Cell firstHeaderRowFirstCell = firstHeaderRow.createCell(2);
        firstHeaderRowFirstCell.setCellValue("Metoda JPA");
        sheet.addMergedRegion(CellRangeAddress.valueOf("C1:E1"));
        firstHeaderRowFirstCell.setCellStyle(cellStyle);
        Cell firstHeaderRowSecondCell = firstHeaderRow.createCell(5);
        firstHeaderRowSecondCell.setCellValue("Metoda JDBC");
        sheet.addMergedRegion(CellRangeAddress.valueOf("F1:H1"));
        firstHeaderRowSecondCell.setCellStyle(cellStyle);
        Row secondHeaderRow = sheet.createRow(1);
        secondHeaderRow.createCell(0)
                .setCellValue("Numer Testu");
        secondHeaderRow.createCell(1)
                .setCellValue("Rozmiar Pliku");
        secondHeaderRow.createCell(2)
                .setCellValue("Obciążenie CPU (%)");
        secondHeaderRow.createCell(3)
                .setCellValue("Użycie RAM (MB)");
        secondHeaderRow.createCell(4)
                .setCellValue("Czas Trwania (ms)");
        secondHeaderRow.createCell(5)
                .setCellValue("Obciążenie CPU (%)");
        secondHeaderRow.createCell(6)
                .setCellValue("Użycie RAM (MB)");
        secondHeaderRow.createCell(7)
                .setCellValue("Czas Trwania (ms)");
        secondHeaderRow.createCell(8)
                .setCellValue("Data Wykonania Testu");
    }
}
