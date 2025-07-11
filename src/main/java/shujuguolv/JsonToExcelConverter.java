package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class JsonToExcelConverter {

    public static void main(String[] args) {
        try {
            // 1. 读取文件内容
            String jsonContent = readFile("C:\\Users\\潘强\\Desktop\\data.txt");

            // 2. 解析JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);

            // 3. 创建Excel工作簿
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("License Data");

                // 创建表头样式
                CellStyle headerStyle = createHeaderStyle(workbook);

                // 4. 写入表头
                Row headerRow = sheet.createRow(0);
                createCell(headerRow, 0, "Name", headerStyle);
                createCell(headerRow, 1, "License Code", headerStyle);
                createCell(headerRow, 2, "ID Code", headerStyle);

                // 5. 处理数据
                JsonNode dataArray = rootNode.path("data");
                int rowNum = 1;
                for (JsonNode item : dataArray) {
                    Row row = sheet.createRow(rowNum++);

                    String name = item.path("name").asText("");
                    String licenseCode = item.path("license_code").asText("");
                    String idCode = item.path("id_code").asText("");

                    createCell(row, 0, name, null);
                    createCell(row, 1, licenseCode, null);
                    createCell(row, 2, idCode, null);
                }

                // 6. 自动调整列宽
                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 7. 保存文件
                try (FileOutputStream fos = new FileOutputStream("C:\\Users\\潘强\\Desktop\\output.xlsx")) {
                    workbook.write(fos);
                }
            }

            System.out.println("Excel文件生成成功！");

        } catch (Exception e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}