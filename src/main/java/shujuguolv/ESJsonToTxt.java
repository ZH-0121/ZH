package shujuguolv;
/*
 * ES批量导数据，把txt中的数据转换到txt中
 * */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;

public class ESJsonToTxt {

/*    static class LicenseData {
        String licenseCode;
        String holderIdentityNum;
        String createdDate;
        String  idCode;
        String holderName;
        String issueDate;

        public LicenseData(String licenseCode, String holderIdentityNum, String createdDate,String idCode,String holderName,String issueDate) {
            this.licenseCode = licenseCode;
            this.holderIdentityNum = holderIdentityNum;
            this.createdDate = createdDate;
            this.holderName = holderName;
            this.issueDate = issueDate;
            this.idCode = idCode;
        }
    }*/

    // 将ISO时间转换为中国时区时间
    public static String convertToChinaTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "";
        try {
            Instant instant = Instant.parse(isoTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Shanghai"));
            return formatter.format(instant);
        } catch (DateTimeParseException e) {
            System.err.printf("日期格式转换错误 (原始值: %s): %s%n", isoTime, e.getMessage());
            return isoTime; // 返回原字符串或处理异常
        }
    }

    public static void main(String[] args) {
        // 输入输出文件路径
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\export_10000710100002888X110000.txt";
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\行驶证.txt";

        // 计数器和性能指标
        AtomicInteger totalProcessed = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            // 写入CSV表头
//            writer.write("LicenseCode,HolderIdentityNum,holderName,createdDate,issueDate,idCode\n");
//            writer.write("HolderIdentityNum,holderName,issueDate,idCode\n");
            writer.write("LicenseCode,createdDate,idCode,HolderIdentityNum\n");
//            writer.write("licenseCode\n");
            ObjectMapper mapper = new ObjectMapper();
            String line;

            // 逐行处理输入文件
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    // 解析JSON行
                    JsonNode rootNode = mapper.readTree(line);

                    // 检查是否包含hits数组
                    JsonNode hitsNode = rootNode.path("hits").path("hits");
                    if (!hitsNode.isArray()) continue;

                    // 处理每个hit
                    for (JsonNode hit : (ArrayNode) hitsNode) {
                        JsonNode source = hit.path("_source");

                        // 提取字段
                        String licenseCode = source.path("licenseCode").asText();
//                        String idCode = source.path("idCode").asText();
/*                        JsonNode idNumNode = source.path("holderIdentityNum");
                        String holderIdentityNum = idNumNode.isArray() && idNumNode.size() > 0
                                ? idNumNode.get(0).asText()
                                : "";*/
//
//                        String formattedCreatedDate = convertToChinaTime(source.path("createdDate").asText());
//                        String formattedexpiryDate = convertToChinaTime(source.path("expiryDate").asText());

                        // 处理holderIdentityNum数组
                        JsonNode idNumNode = source.path("holderIdentityNum");
                        String holderIdentityNum = idNumNode.isArray() && idNumNode.size() > 0
                                ? idNumNode.get(0).asText()
                                : "";
//                        JsonNode holderName1 = source.path("holderName");
//                        String holderName = holderName1.isArray() && holderName1.size() > 0
//                                ? holderName1.get(0).asText()
//                                : "";

                        String createdDate = source.path("createdDate").asText();
//
//                        String issueDate = source.path("issueDate").asText();
                        String idCode = source.path("idCode").asText();

                        // 转换日期格式
                        String formattedCreatedDate = convertToChinaTime(createdDate);
//                        String formattedIssueDate = convertToChinaTime(issueDate);

                        // 写入CSV行
/*                        String csvLine = String.format("%s,%s,%s,%s,%s,%s\n",
                                escapeCsv(licenseCode),
                                escapeCsv(holderIdentityNum),
                                escapeCsv(holderName),
                                escapeCsv(formattedCreatedDate),
                                escapeCsv(formattedIssueDate),
                                escapeCsv(idCode)
                                );*/

                        String csvLine = String.format("%s,%s,%s,%s\n",

                                escapeCsv(licenseCode),
                                escapeCsv(formattedCreatedDate),
                                escapeCsv(idCode),
                                escapeCsv(holderIdentityNum)
                                );
/*                        String csvLine = String.format("%s,%s,%s,%s\n",
                                escapeCsv(licenseCode),
                                escapeCsv(formattedCreatedDate),
                                escapeCsv(idCode),
                                escapeCsv(formattedexpiryDate)
                                );*/
/*                        String csvLine = String.format("%s\n",
                                escapeCsv(licenseCode)
//                                escapeCsv(formattedCreatedDate),
//                                escapeCsv(holderIdentityNum)
                        );*/
                        writer.write(csvLine);

                        // 更新计数器
                        int count = totalProcessed.incrementAndGet();
                        if (count % 10000 == 0) {
                            System.out.printf("已处理 %d 条记录...%n", count);
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("处理行时出错: %s. 错误详情: %s%n", line, e.getMessage());
                    // 可以选择继续处理下一行，或者根据需要终止程序
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.printf("TXT文件生成成功！共处理 %d 条数据，耗时 %d 毫秒%n",
                    totalProcessed.get(), endTime - startTime);

        } catch (IOException e) {
            System.err.printf("文件操作错误: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    // 处理CSV特殊字符
    private static String escapeCsv(String value) {
        if (value == null) return "";
        // 检查是否需要引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // 转义双引号并添加引号
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}