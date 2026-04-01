package com.example.excelproject;

import com.example.excelproject.services.MyService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExcelProjectApplicationTests {

    public  final MyService myService;
    ExcelProjectApplicationTests(MyService myService) {
        this.myService = myService;
    }

    @Test
    void simpleExcel() throws IOException, InvalidFormatException {
        myService.generateSimpleExcel();
        File file = new File("output-file/simple.xlsx");
        assertTrue(file.exists());

        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheet("My First Sheet");

        assertNotNull(sheet);

        Row row = sheet.getRow(0);
        assertEquals("Hello Excel", row.getCell(0).getStringCellValue());

        workbook.close();
    }

    @Test
    void ExcelWithMultiSheet() throws IOException, InvalidFormatException {
        myService.generateExcelWithMultiSheet();
        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-multi-sheet.xlsx"));

        assertEquals(3, workbook.getNumberOfSheets());

        assertNotNull(workbook.getSheet("Users"));
        assertNotNull(workbook.getSheet("Products"));
        assertNotNull(workbook.getSheet("Summary"));

        workbook.close();
    }

    @Test
    void ExcelWithTable() throws IOException, InvalidFormatException {
        myService.generateExcelWithTable();
        XSSFWorkbook workbook = new XSSFWorkbook(new File("output-file/excel-with-table.xlsx"));
        XSSFSheet sheet = workbook.getSheet("Products");

        assertNotNull(sheet);

        List<XSSFTable> tables = sheet.getTables();
        assertEquals(1, tables.size());

        assertEquals("ProductTable", tables.get(0).getName());

        workbook.close();
    }

    @Test
    void ExcelWithSmallStyledHeader() throws IOException, InvalidFormatException {
        myService.generateExcelWithStyledHeader(10 , "small-styled-header");
        XSSFWorkbook workbook = new XSSFWorkbook(new File("output-file/excel-with-table.xlsx"));
        XSSFSheet sheet = workbook.getSheet("Products");

        assertNotNull(sheet);

        List<XSSFTable> tables = sheet.getTables();
        assertEquals(1, tables.size());

        assertEquals("ProductTable", tables.get(0).getName());

        workbook.close();
    }

    @Test
    void ExcelWithLargeStyledHeader() throws IOException, InvalidFormatException {
        myService.generateExcelWithStyledHeader(16, "large-styled-header");
        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-large-styled-header.xlsx"));
        Sheet sheet = workbook.getSheet("Persons");

        Cell cell = sheet.getRow(0).getCell(0);

        Font font = workbook.getFontAt(cell.getCellStyle().getFontIndex());

        assertEquals(16, font.getFontHeightInPoints());

        workbook.close();
    }

    @Test
    void ExcelWithDataStyle() throws IOException, InvalidFormatException {
        myService.generateExcelWithStyledData();
        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-font-color-styled-data.xlsx"));
        Sheet sheet = workbook.getSheet("Products");

        Cell cell = sheet.getRow(1).getCell(2);

        assertNotNull(cell.getCellStyle());

        workbook.close();

    }

    @Test
    void generateExcelSortedByPrice() throws IOException, InvalidFormatException {
        myService.generateExcelWithSortedData("price");
        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-sorted-by-price.xlsx"));
        Sheet sheet = workbook.getSheet("Prodcuts");

        double first = sheet.getRow(1).getCell(2).getNumericCellValue();
        double second = sheet.getRow(2).getCell(2).getNumericCellValue();

        assertTrue(first <= second);

        workbook.close();
    }

    @Test
    void generateExcelSortedByID() throws IOException {
        myService.generateExcelWithSortedData("id");
        Workbook workbook = new XSSFWorkbook("output-file/excel-with-sorted-by-id.xlsx");
        Sheet sheet = workbook.getSheetAt(0);

        assertEquals("Tv", sheet.getRow(1).getCell(1).getStringCellValue());
        assertEquals("Book", sheet.getRow(2).getCell(1).getStringCellValue());
        assertEquals("Laptop", sheet.getRow(3).getCell(1).getStringCellValue());

        workbook.close();
    }

    @Test
    void generateExcelSortedByName() throws IOException, InvalidFormatException {
        myService.generateExcelWithSortedData("name");
        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-sorted-by-name.xlsx"));
        Sheet sheet = workbook.getSheet("Prodcuts");

        String first = sheet.getRow(1).getCell(1).getStringCellValue();
        String second = sheet.getRow(2).getCell(1).getStringCellValue();

        assertTrue(first.compareTo(second) <= 0);

        workbook.close();
    }

    @Test
    void generateExcelWithFormulas() throws IOException, InvalidFormatException {
        myService.generateExcelWithFormulas();
        Workbook workbook = new XSSFWorkbook(new File("output-file/all-formula.xlsx"));
        Sheet sheet = workbook.getSheetAt(0);

        Cell cell = sheet.getRow(4).getCell(0);

        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals("SUM(A2:A4)", cell.getCellFormula());

        workbook.close();
    }

    @Test
    void generateExcelWithTableFormulas() throws IOException, InvalidFormatException {
        myService.generateExcelTableWithFormulas();
        Workbook workbook = new     XSSFWorkbook(new File("output-file/excel-with-formula-table.xlsx"));
        Sheet sheet = workbook.getSheet("Products");

        Cell cell = sheet.getRow(5).getCell(2);

        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals("SUM(C2:C4)", cell.getCellFormula());

        workbook.close();
    }

    @Test
    void insertRowInExcel() throws IOException, InvalidFormatException {
        myService.readExcelAndInsertRow();

        Workbook workbook = new XSSFWorkbook(new File("output-file/excel-with-inserted-row.xlsx"));
        Sheet sheet = workbook.getSheetAt(0);
        Cell cell= sheet.getRow(2).getCell(1);

        assertEquals("Pen",cell.toString());
    }

}
