package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;

public class SFZdouble {

    // 读取文件内容并返回字符串
    public static String readFileAndOutput(String filePath) {
        StringBuilder content = new StringBuilder();
        // 创建 BufferedReader 来读取文件内容
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 循环读取每一行，直到文件结束
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n"); // 读取每行并追加到内容字符串
            }
        } catch (IOException e) {
            // 捕获异常并打印错误信息
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return content.toString(); // 返回文件的内容
    }

    public static void main(String[] args) {
        // 设置文件路径
        String filePath = "C:\\Users\\潘强\\Desktop\\data.txt"; // 请根据实际文件路径修改

        // 读取文件并获取文件内容
        String jsonString = readFileAndOutput(filePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;  // 如果读取失败，终止程序
        }

        // 解析 JSON 数据
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode hitsNode = rootNode.path("hits").path("hits");

            // 创建一个 Excel 文件
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("License Data");

            // 创建 Excel 表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("createdDate");
            headerRow.createCell(1).setCellValue("licenseCode");
            headerRow.createCell(2).setCellValue("issueDate");
            headerRow.createCell(3).setCellValue("holderIdentityNum1");
            headerRow.createCell(4).setCellValue("holderIdentityNum2");
            headerRow.createCell(5).setCellValue("idCode");

            // 遍历 JSON 数据并写入 Excel 文件
            Iterator<JsonNode> elements = hitsNode.elements();
            int rowIndex = 1; // Excel 中从第 2 行开始写数据
            while (elements.hasNext()) {
                JsonNode hit = elements.next();
                JsonNode sourceNode = hit.path("_source");

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sourceNode.path("createdDate").asText());
                row.createCell(1).setCellValue(sourceNode.path("licenseCode").asText());
                row.createCell(2).setCellValue(sourceNode.path("issueDate").asText());

                // "holderIdentityNum" 是一个数组，这里取最多两个元素
                JsonNode holderIdentityNumNode = sourceNode.path("holderIdentityNum");
                if (holderIdentityNumNode.isArray()) {
                    // 如果数组不为空，写入第一个元素
                    if (holderIdentityNumNode.size() > 0) {
                        row.createCell(3).setCellValue(holderIdentityNumNode.get(0).asText());
                    } else {
                        row.createCell(3).setCellValue(""); // 空值
                    }
                    // 如果数组中有第二个元素，写入第二个元素
                    if (holderIdentityNumNode.size() > 1) {
                        row.createCell(4).setCellValue(holderIdentityNumNode.get(1).asText());
                    } else {
                        row.createCell(4).setCellValue(""); // 空值
                    }
                }

                row.createCell(5).setCellValue(sourceNode.path("idCode").asText());
            }

            // 将 Excel 数据写入文件
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\license_data.xlsx")) {
                workbook.write(fileOut);
            } catch (IOException e) {
                System.err.println("写入 Excel 文件时发生错误: " + e.getMessage());
            } finally {
                // 关闭工作簿
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.err.println("关闭工作簿时发生错误: " + e.getMessage());
                }
            }

            System.out.println("Excel 文件生成成功！");
        } catch (IOException e) {
            System.err.println("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }
}
