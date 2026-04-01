package com.example.excelproject.services;


import com.example.excelproject.modal.Product;
import com.example.excelproject.util.ExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

@Service
public class MyService {

    public void generateSimpleExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("My First Sheet");

            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Hello Excel");

            ExcelUtil.saveFile(workbook, "output-file/simple.xlsx");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateExcelWithMultiSheet() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet userSheet = workbook.createSheet("Users");
            Sheet productSheet = workbook.createSheet("Products");
            Sheet summarySheet = workbook.createSheet("Summary");

            ExcelUtil.createHeader(userSheet, new String[]{"Name", "Age"});
            ExcelUtil.createData(userSheet, new Object[][]{
                    {"Arjun", "23"},
                    {"Karan", "24"}
            });

            String[] pHeader = {"ProductId", "ProductName"};
            Object[][] pData = {{"1", "Tv"}, {"2", "Laptop"}};
            ExcelUtil.createHeader(productSheet, pHeader);
            ExcelUtil.createData(productSheet, pData);

            String[] sHeader = {"Total Users", "Total Products"};
            Object[][] sData = {{"2", "2"}, {"3", "3"}};
            ExcelUtil.createHeader(summarySheet, sHeader);
            ExcelUtil.createData(summarySheet, sData);

            workbook.setSheetOrder("Summary", 0);
            workbook.setSheetOrder("Users", 1);
            workbook.setSheetOrder("Products", 2);
            workbook.setActiveSheet(2);

            ExcelUtil.saveFile(workbook, "output-file/excel-with-multi-sheet.xlsx");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void generateExcelWithTable() {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet productsSheet = workbook.createSheet("Products");

            String[] columnNames = {"ProductID", "ProductName", "ProductPrice"};

            Object[][] data = {{1, "watch", 200}, {2, "T-shirt", 500}, {3, "Bottle", 150}};

            ExcelUtil.createHeader(productsSheet, columnNames);
            ExcelUtil.createData(productsSheet, data);

            ExcelUtil.createTable(workbook,productsSheet,"A1:C4",columnNames,"ProductTable");

            ExcelUtil.saveFile(workbook, "output-file/excel-with-table.xlsx");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void generateExcelWithStyledHeader(int size, String fileName) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Persons");
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 2000);

            Row header = sheet.createRow(0);

            CellStyle headerStyle = ExcelUtil.createHeaderStyle(workbook, "Arial", size, true);

            Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Name");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(1);
            headerCell.setCellValue("Age");
            headerCell.setCellStyle(headerStyle);

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);

            Row row = sheet.createRow(2);
            Cell cell = row.createCell(0);
            cell.setCellValue("John Smith");
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(20);
            cell.setCellStyle(style);

            ExcelUtil.saveFile(workbook, "output-file/excel-with-" + fileName + ".xlsx");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void generateExcelWithStyledData() {

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = ExcelUtil.createHeaderStyle(workbook, "Arial", 14, true);

            CellStyle textStyle = ExcelUtil.createTextStyle(workbook);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle currencyStyle = ExcelUtil.createCurrencyStyle(workbook);

            CellStyle dateStyle = ExcelUtil.createDateStyle(workbook);

            String[] columns = {"ID", "Name", "Price", "Created Date"};

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            sheet.createFreezePane(1, 1, 1, 1);
            Object[][] data = {
                    {1, "Pen", 10.5, new Date()},
                    {2, "Book", 50.0, new Date()},
                    {3, "Bag", 500.99, new Date()},
                    {4, "Laptop", 55000.75, new Date()}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);

                Cell idCell = row.createCell(0);
                idCell.setCellValue((Integer) data[i][0]);
                idCell.setCellStyle(numberStyle);

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue((String) data[i][1]);
                nameCell.setCellStyle(textStyle);

                Cell priceCell = row.createCell(2);
                priceCell.setCellValue((Double) data[i][2]);
                priceCell.setCellStyle(currencyStyle);

                Cell dateCell = row.createCell(3);
                dateCell.setCellValue((Date) data[i][3]);
                dateCell.setCellStyle(dateStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ExcelUtil.saveFile(workbook, "output-file/excel-with-font-color-styled-data.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void generateExcelWithSortedData(String columName) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet prodcuts = workbook.createSheet("Prodcuts");

            ArrayList<Product> productList = new ArrayList<>();

            productList.add(new Product(1, "Tv", 56000));
            productList.add(new Product(2, "Book", 50));
            productList.add(new Product(3, "Laptop", 55000));

            if (columName.equalsIgnoreCase("price")) {
                productList.sort(Comparator.comparingDouble(p -> p.price));
            }
            else if (columName.equalsIgnoreCase("name")) {
                productList.sort(Comparator.comparing(p -> p.name));
            }
            else if (columName.equalsIgnoreCase("id")) {
                productList.sort(Comparator.comparingInt(p -> p.id));
            }

            Object[][] data = new Object[productList.size()][3];
            for (int i = 0; i < productList.size(); i++) {
                Product p = productList.get(i);
                data[i] = (new Object[]{p.id, p.name, p.price});
            }

            String[] headers = {"ID", "Name", "Price"};
            ExcelUtil.createHeader(prodcuts, headers);
            ExcelUtil.createData(prodcuts, data);

            ExcelUtil.saveFile(workbook, "output-file/excel-with-sorted-by-"+columName+".xlsx");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateExcelWithFormulas(){
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Formulas");

            String [] header = {"num1" ,"num2"};
            Object [][] data = new Object[][]{{20,10},{40,50} ,{70,90}};

            ExcelUtil.createHeader(sheet, header);
            ExcelUtil.createData(sheet, data);

            ExcelUtil.createFormula(sheet,4,0,"SUM(A2:A4)");
            ExcelUtil.createFormula(sheet,5,0,"COUNT(A2:A4)");
            ExcelUtil.createFormula(sheet,6,0,"AVERAGE(A2:A4)");

            ExcelUtil.createFormula(sheet,7,0,"A2 - B2");
            ExcelUtil.createFormula(sheet,8,0,"A2 * B2");
            ExcelUtil.createFormula(sheet,9,0,"A2 / B2");

            ExcelUtil.createFormula(sheet,10,0,"A2 & \" - \" & B2");
            ExcelUtil.createFormula(sheet,11,0,"If(B3>30 ,\"High\",\"low\")");
            ExcelUtil.createFormula(sheet,12,0,"ROUND(B4,2)");

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            ExcelUtil.saveFile(workbook, "output-file/all-formula.xlsx");

        }catch (Exception e){
        throw new RuntimeException(e);}
    }

    public  void generateExcelTableWithFormulas(){
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet productsSheet = workbook.createSheet("Products");

            String[] columnNames = {"ProductID", "ProductName", "ProductPrice"};

            Object[][] data = { {1, "Pen", 10},
                    {2, "Book", 50},
                    {3, "Bag", 500}};

            ExcelUtil.createHeader(productsSheet, columnNames);
            ExcelUtil.createData(productsSheet, data);

            ExcelUtil.createTable(workbook,productsSheet,"A1:C4",columnNames,"ProductTable");

            int lastRowIndex = data.length + 2 ;

            XSSFRow lastRow = productsSheet.createRow(lastRowIndex);
            lastRow.createCell(1).setCellValue("Total");
            XSSFCell totalCell = lastRow.createCell(2);
            totalCell.setCellFormula("SUM(C2:C4)");
            CellStyle currencyStyle = ExcelUtil.createCurrencyStyle(workbook);
            totalCell.setCellStyle(currencyStyle);

            ExcelUtil.saveFile(workbook, "output-file/excel-with-formula-table.xlsx");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void readExcelAndInsertRow(){
        try(FileInputStream fis = new FileInputStream("output-file/excel-with-font-color-styled-data.xlsx");
            Workbook workbook = new XSSFWorkbook(fis)){
            int startRow = 1;
            int rowNumber = 1;

            Sheet sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();
            if (lastRow < startRow) {
                sheet.createRow(startRow);
            }

            sheet.shiftRows(startRow, lastRow, rowNumber, true, true);
            sheet.createRow(startRow);

            ExcelUtil.saveFile(workbook, "output-file/excel-with-inserted-row.xlsx");

        }catch(Exception e){
            throw new RuntimeException(e);
        }


    }
}
