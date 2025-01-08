package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ExcelToJson {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\潘强\\Desktop\\license_code.xlsx";  // 修改为你表格文件的路径
        String outputJsonFilePath = "C:\\Users\\潘强\\Desktop\\license_code.json";  // 输出 JSON 的文件路径

        try {
            List<Map<String, String>> result = readExcelAndConvertToJson(excelFilePath);
            String jsonString = convertToJson(result);
            saveJsonToFile(jsonString, outputJsonFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取 Excel 文件并将数据转换为 JSON 格式
    private static List<Map<String, String>> readExcelAndConvertToJson(String excelFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(new File(excelFilePath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);  // 获取第一个工作表
        List<Map<String, String>> dataList = new ArrayList<>();

        // 从第二行开始读取（假设第一行为标题）
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                String licenseCode = row.getCell(0).getStringCellValue();  // 第一列
                String holderIdentityNum1 = row.getCell(1).getStringCellValue();  // 第二列
                String holderIdentityNum2 = row.getCell(2).getStringCellValue();  // 第三列

                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("license_code", licenseCode);
                dataMap.put("holderIdentityNum1", holderIdentityNum1);
                dataMap.put("holderIdentityNum2", holderIdentityNum2);
                dataList.add(dataMap);
            }
        }

        workbook.close();
        fis.close();
        return dataList;
    }

    // 将数据列表转换为 JSON 字符串
    private static String convertToJson(List<Map<String, String>> dataList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(dataList);
    }

    // 将 JSON 字符串保存到指定的文件中
    private static void saveJsonToFile(String jsonString, String outputFilePath) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(outputFilePath));
        fileWriter.write(jsonString);
        fileWriter.close();
        System.out.println("JSON 数据已保存到文件: " + outputFilePath);
    }
}
