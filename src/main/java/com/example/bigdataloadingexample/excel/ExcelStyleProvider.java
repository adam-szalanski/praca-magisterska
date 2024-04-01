package com.example.bigdataloadingexample.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelStyleProvider {

    private static final int COLUMN_WIDTH_OFFSET = 5 * 256;

    public CellStyle prepareCellStyleBroderLeftTopRightBottomThick(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THICK);
        cellStyle.setBorderLeft(BorderStyle.THICK);
        cellStyle.setBorderRight(BorderStyle.THICK);
        cellStyle.setBorderBottom(BorderStyle.THICK);
        return cellStyle;
    }

    public CellStyle prepareCellStyleBorderTopBottomThick(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THICK);
        cellStyle.setBorderBottom(BorderStyle.THICK);
        return cellStyle;
    }

    public CellStyle prepareCellStyleBorderLeftTopThick(Workbook workbook) {
        CellStyle cellStyle = prepareCellStyleBorderTopBottomThick(workbook);
        cellStyle.setBorderLeft(BorderStyle.THICK);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    public CellStyle prepareCellStyleBorderTopRightThick(Workbook workbook) {
        CellStyle cellStyle = prepareCellStyleBorderTopBottomThick(workbook);
        cellStyle.setBorderRight(BorderStyle.THICK);
        return cellStyle;
    }

    public void autoSizeColumns(Sheet sheet, int numberOfCollumns, int horizontalRowOffset) {
        for (int i = horizontalRowOffset; i <= horizontalRowOffset + numberOfCollumns; i++) {
            sheet.autoSizeColumn(i);
            int manuallyAdjustedColumnWidth = sheet.getColumnWidth(i) + COLUMN_WIDTH_OFFSET;
            sheet.setColumnWidth(i, manuallyAdjustedColumnWidth);
        }
    }

    public CellStyle prepareCellStyleBorderLeftBottomThickBorderTopRightThin(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderLeft(BorderStyle.THICK);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        return cellStyle;
    }

    public CellStyle prepareCellStyleBorderRightBottomThickBorderLeftTopThin(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderRight(BorderStyle.THICK);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        return cellStyle;
    }

    public CellStyle prepareCellStyleBorderLeftTopRightThin(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        return cellStyle;
    }
}
