package shujuguolv;



/*
 * ES批量导数据，把txt中的数据转换到Excel中
 * */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ESJsonToExcel {

    static class LicenseData {
        String licenseCode;
        String idCode;
        String createdDate;
        String expiryDate;

        public LicenseData(String licenseCode, String idCode,String createdDate,String expiryDate) {
            this.licenseCode = licenseCode;
            this.idCode = idCode;
            this.createdDate = createdDate;
            this.expiryDate = expiryDate;
        }
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
        try {
            // 1. 读取输入文件
            File inputFile = new File("C:\\Users\\潘强\\Desktop\\export_10000760100002888X110000_guoqi.txt");
            List<LicenseData> dataList = new ArrayList<>();

            // 2. 创建JSON解析器
            ObjectMapper mapper = new ObjectMapper();

            try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    // 3. 解析每行JSON
                    JsonNode rootNode = mapper.readTree(line);
                    JsonNode hits = rootNode.path("hits").path("hits");

                    // 4. 遍历hits数组
                    for (JsonNode hit : hits) {
                        JsonNode source = hit.path("_source");
                        String licenseCode = source.path("licenseCode").asText();
                        String idCode = source.path("idCode").asText();

                        // 处理holderIdentityNum数组
                     /*   JsonNode idNumNode = source.path("holderIdentityNum");
                        String holderIdentityNum = idNumNode.isArray() && idNumNode.size() > 0
                                ? idNumNode.get(0).asText()
                                : "";*/
                        String createdDate = source.path("createdDate").asText();
                        String expiryDate = source.path("expiryDate").asText();

                        dataList.add(new LicenseData(licenseCode, idCode,createdDate,expiryDate));
                    }
                }
            }

            // 5. 创建Excel文件
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("License Data");

                // 创建表头
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("License Code");
                headerRow.createCell(1).setCellValue("idCode");
                headerRow.createCell(2).setCellValue("createdDate");
                headerRow.createCell(3).setCellValue("expiryDate");

                // 填充数据
                int rowNum = 1;
                for (LicenseData data : dataList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(data.licenseCode);
                    row.createCell(1).setCellValue(data.idCode);
                    row.createCell(2).setCellValue(convertToChinaTime(data.createdDate));
                    row.createCell(3).setCellValue(convertToChinaTime(data.expiryDate));
                }

                // 自动调整列宽
                for (int i = 0; i < 2; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入文件
                try (FileOutputStream fos = new FileOutputStream("C:\\Users\\潘强\\Desktop\\身份证过期数据.xlsx")) {
                    workbook.write(fos);
                }
            }

            System.out.println("Excel文件生成成功！共处理 " + dataList.size() + " 条数据");

        } catch (Exception e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}