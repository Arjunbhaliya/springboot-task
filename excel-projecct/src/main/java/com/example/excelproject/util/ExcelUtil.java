package com.example.excelproject.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import java.io.FileOutputStream;
import java.util.Date;

public class ExcelUtil {

    public static void saveFile(Workbook workbook, String path) {
        try (FileOutputStream fileOut = new FileOutputStream(path)) {
            workbook.write(fileOut);
            workbook.close();
            System.out.println("Excel file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createHeader(Sheet sheet, String[] headers) {

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    public static void createData(Sheet sheet, Object[][] data) {

        for (int i = 0; i < data.length; i++) {
            Row  dataRow = sheet.createRow(i+1);
            for(int j=0 ; j<data[i].length;j++){
                Cell cell = dataRow.createCell(j);
                Object value = data[i][j];

                if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        }
    }

    public static CellStyle createHeaderStyle(XSSFWorkbook workbook, String fontStyle , int size , boolean bold){
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setFontName(fontStyle);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) size);
        font.setBold(bold);
        headerStyle.setFont(font);

        return headerStyle;
    }

    public static CellStyle createTextStyle(XSSFWorkbook workbook){
        Font textFont = workbook.createFont();
        textFont.setFontHeightInPoints((short) 11);
        textFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setFont(textFont);
        textStyle.setAlignment(HorizontalAlignment.LEFT);

        return  textStyle;
    }

    public static CellStyle createCurrencyStyle(XSSFWorkbook workbook){
        DataFormat format = workbook.createDataFormat();
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        return currencyStyle;
    }

    public static CellStyle createDateStyle(XSSFWorkbook workbook){
        DataFormat format = workbook.createDataFormat();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("dd-MM-yyyy"));
        return dateStyle;
    }

    public static void createFormula (Sheet sheet,int row , int col, String formula ){
        Row formulaRow = sheet.createRow(row);
        formulaRow.createCell(col).setCellFormula(formula);
    }

    public static void createTable(XSSFWorkbook workbook, XSSFSheet sheet, String areaReference , String [] columnNames , String tableName ){
        AreaReference reference = new AreaReference(
                areaReference, workbook.getSpreadsheetVersion()
        );

        XSSFTable table = sheet.createTable(reference);

        CTTable ctTable = table.getCTTable();
        ctTable.setRef(areaReference);

        ctTable.setId(1);
        ctTable.setName(tableName);
        ctTable.setDisplayName(tableName);

        ctTable.addNewAutoFilter().setRef(areaReference);

        CTTableStyleInfo style = ctTable.addNewTableStyleInfo();
        style.setName("TableStyleMedium9");
        style.setShowColumnStripes(false);
        style.setShowRowStripes(true);

        CTTableColumns columnDef = ctTable.addNewTableColumns();
        columnDef.setCount(columnNames.length);

        for (int i = 0; i < columnNames.length; i++) {
            CTTableColumn column = columnDef.addNewTableColumn();
            column.setId(i + 1);
            column.setName(columnNames[i]);
        }
    }
}
