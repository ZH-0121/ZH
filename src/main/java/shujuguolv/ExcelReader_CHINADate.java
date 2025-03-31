package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

public class ExcelReader_CHINADate {

    // 读取文件内容并返回字符串
    public static String readFileAndOutput(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return content.toString();
    }

    // 将ISO时间转换为中国时区时间
    public static String convertToChinaTime(String isoTime) {
        try {
            Instant instant = Instant.parse(isoTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Shanghai"));
            return formatter.format(instant);
        } catch (DateTimeParseException e) {
            System.err.println("日期格式转换错误: " + isoTime);
            return isoTime; // 返回原字符串或处理异常
        }
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\sfz\\data21.txt";
        String jsonString = readFileAndOutput(filePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode hitsNode = rootNode.path("hits").path("hits");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("License Data");

            // 创建表头
            Row headerRow = sheet.createRow(0);
           // headerRow.createCell(0).setCellValue("createdDate");

//            headerRow.createCell(0).setCellValue("issueDate");
//            headerRow.createCell(1).setCellValue("holderIdentityNum");
//              headerRow.createCell(2).setCellValue("licenseCode");
          //  headerRow.createCell(4).setCellValue("idCode");
          //  headerRow.createCell(5).setCellValue("expiryDate");
          //  headerRow.createCell(6).setCellValue("holderName");

            Iterator<JsonNode> elements = hitsNode.elements();
            int rowIndex = 0;
            while (elements.hasNext()) {
                JsonNode hit = elements.next();
                JsonNode sourceNode = hit.path("_source");
                Row row = sheet.createRow(rowIndex++);

                // issueDate
                String issueDate = sourceNode.path("issueDate").asText("");
                row.createCell(0).setCellValue(convertToChinaTime(issueDate));
                // holderIdentityNum
                JsonNode holderIdentityNum = sourceNode.path("holderIdentityNum");
                if (holderIdentityNum.isArray() && !holderIdentityNum.isEmpty()) {
                    row.createCell(1).setCellValue(holderIdentityNum.get(0).asText(""));
                } else {
                    row.createCell(1).setCellValue("");
                }
                // licenseCode
                row.createCell(2).setCellValue(sourceNode.path("licenseCode").asText(""));



/*
                // createdDate
                String createdDate = sourceNode.path("createdDate").asText("");
                row.createCell(0).setCellValue(convertToChinaTime(createdDate));

                // licenseCode
                row.createCell(1).setCellValue(sourceNode.path("licenseCode").asText(""));

                // issueDate
                String issueDate = sourceNode.path("issueDate").asText("");
                row.createCell(2).setCellValue(convertToChinaTime(issueDate));

                // holderIdentityNum
                JsonNode holderIdentityNum = sourceNode.path("holderIdentityNum");
                if (holderIdentityNum.isArray() && !holderIdentityNum.isEmpty()) {
                    row.createCell(3).setCellValue(holderIdentityNum.get(0).asText(""));
                } else {
                    row.createCell(3).setCellValue("");
                }

                // idCode
                row.createCell(4).setCellValue(sourceNode.path("idCode").asText(""));

                // expiryDate
                String expiryDate = sourceNode.path("expiryDate").asText("");
                row.createCell(5).setCellValue(convertToChinaTime(expiryDate));

                // holderName
                JsonNode holderName = sourceNode.path("holderName");
                if (holderName.isArray() && !holderName.isEmpty()) {
                    row.createCell(6).setCellValue(holderName.get(0).asText(""));
                } else {
                    row.createCell(6).setCellValue("");
                }*/
            }

            // 写入Excel文件
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\sfz\\license_data21.xlsx")) {
                workbook.write(fileOut);
                System.out.println("Excel 文件生成成功！");
            } catch (IOException e) {
                System.err.println("写入 Excel 文件时发生错误: " + e.getMessage());
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.err.println("关闭工作簿时发生错误: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }
}