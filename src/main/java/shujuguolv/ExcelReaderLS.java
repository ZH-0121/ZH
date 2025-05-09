package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;

/**
 * 读取txt文件中的JSON数据，提取指定字段并写入Excel
 */
public class ExcelReaderLS {

    // 读取文件内容并返回字符串
    public static String readFileAndOutput(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line); // 去除换行符，确保JSON完整性（原数据可能包含换行影响解析）
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return content.toString();
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\input.txt"; // 输入文件路径，请根据实际修改
        String outputPath = "C:\\Users\\潘强\\Desktop\\license_data11111.xlsx"; // 输出文件路径

        String jsonString = readFileAndOutput(filePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode rowsNode = rootNode.path("data").path("rows"); // 正确路径为data.rows

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("证书数据");

            // 创建表头，按需求顺序定义字段
            String[] headers = {
                    "certificateTypeId",
                    "certificateType",
                    "updateTime",
                    "certificateTypeCode",
                    "certificateDefineAuthorityName",
                    "certificateDefineAuthorityCode",
                    "certificateHolderCategory",
                    "validityRange",
                    "certificateDefineDeptLevel",
                    "certificateIssueDeptLevel",
                    "status",
                    "standardFileId",
                    "certificateTemplateId",
                    "certificateUploadMode",
                    "issueDate"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Iterator<JsonNode> elements = rowsNode.elements();
            int rowIndex = 1;

            while (elements.hasNext()) {
                JsonNode rowNode = elements.next();
                Row dataRow = sheet.createRow(rowIndex++);

                // 按顺序提取每个字段，处理可能的null值（使用asText()自动处理null为空字符串）
                dataRow.createCell(0).setCellValue(rowNode.path("certificateTypeId").asText());
                dataRow.createCell(1).setCellValue(rowNode.path("certificateType").asText());
                dataRow.createCell(2).setCellValue(rowNode.path("updateTime").asText());
                dataRow.createCell(3).setCellValue(rowNode.path("certificateTypeCode").asText());
                dataRow.createCell(4).setCellValue(rowNode.path("certificateDefineAuthorityName").asText());
                dataRow.createCell(5).setCellValue(rowNode.path("certificateDefineAuthorityCode").asText());
                dataRow.createCell(6).setCellValue(rowNode.path("certificateHolderCategory").asText());
                dataRow.createCell(7).setCellValue(rowNode.path("validityRange").asText());
                dataRow.createCell(8).setCellValue(rowNode.path("certificateDefineDeptLevel").asText());
                dataRow.createCell(9).setCellValue(rowNode.path("certificateIssueDeptLevel").asText());
                dataRow.createCell(10).setCellValue(rowNode.path("status").asText());
                dataRow.createCell(11).setCellValue(rowNode.path("standardFileId").asText());
                dataRow.createCell(12).setCellValue(rowNode.path("certificateTemplateId").asText());
                dataRow.createCell(13).setCellValue(rowNode.path("certificateUploadMode").asText());
                dataRow.createCell(14).setCellValue(rowNode.path("issueDate").asText());
            }

            // 写入Excel文件
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
            System.out.println("Excel文件生成成功，路径：" + outputPath);

        } catch (IOException e) {
            System.err.println("解析或写入文件时发生错误: " + e.getMessage());
        }
    }
}