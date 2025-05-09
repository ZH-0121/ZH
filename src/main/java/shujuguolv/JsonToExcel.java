package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/*
* 读取TXT中的数据，将KEY和doc_count输出到excel中
* */
public class JsonToExcel {
    public static void main(String[] args) {
        String jsonFilePath = "C:\\Users\\潘强\\Desktop\\data.txt";
        String excelFilePath = "C:\\Users\\潘强\\Desktop\\全量已签发.xlsx";

        try {
            // 读取 JSON 文件
            String jsonContent = readFile(jsonFilePath);

            // 解析 JSON 数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            JsonNode buckets = rootNode.path("aggregations").path("group_by_implementCode").path("buckets");

            // 创建 Excel 工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Key", "Doc Count"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (JsonNode bucket : buckets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(bucket.path("key").asText());
                row.createCell(1).setCellValue(bucket.path("doc_count").asInt());
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入 Excel 文件
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            System.out.println("数据已成功写入 Excel 文件: " + excelFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte[] data = new byte[(int) new File(filePath).length()];
            fis.read(data);
            return new String(data, "UTF-8");
        }
    }
}
