package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;

public class ESJsonToExcel_1 {

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
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\export_10000760100002888X110000_0718.txt";
        // 修改输出为CSV格式（Excel可打开）
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\身份证过期71.csv";

        AtomicInteger totalProcessed = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // 使用UTF-8编码并添加BOM解决Excel中文乱码
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {

            // 写入UTF-8 BOM标识和表头
            writer.write('\uFEFF'); // BOM for UTF-8
//            writer.write("idCode,licenseCode,createdDate,expiryDate,holderIdentityNum\n"); // Excel列标题
//            writer.write("idCode,licenseCode,expiryDate,holderIdentityNum\n"); // Excel列标题

            writer.write("idCode,licenseCode,createdDate,expiryDate,issueDate,holderIdentityNum,implementCode,issueOrgName\n"); // Excel列标题
            ObjectMapper mapper = new ObjectMapper();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    JsonNode rootNode = mapper.readTree(line);
                    JsonNode hitsNode = rootNode.path("hits").path("hits");
                    if (!hitsNode.isArray()) continue;

                    for (JsonNode hit : (ArrayNode) hitsNode) {
                        JsonNode source = hit.path("_source");
                        String licenseCode = source.path("licenseCode").asText();
                        String idCode = source.path("idCode").asText();
                        String createdDate = source.path("createdDate").asText();
                        String expiryDate = source.path("expiryDate").asText();
                        String issueDate = source.path("issueDate").asText();
                        // 处理holderIdentityNum数组
                        JsonNode identityNumNode = source.path("holderIdentityNum");
                        StringBuilder holderIdentityNumBuilder = new StringBuilder();
                        if (identityNumNode.isArray()) {
                            for (JsonNode element : identityNumNode) {
                                if (holderIdentityNumBuilder.length() > 0) {
                                    holderIdentityNumBuilder.append(";");
                                }
                                holderIdentityNumBuilder.append(element.asText());
                            }
                        } else {
                            holderIdentityNumBuilder.append(identityNumNode.asText());
                        }
                        String holderIdentityNum = holderIdentityNumBuilder.toString();
                        String implementCode = source.path("implementCode").asText();
                        String issueOrgName = source.path("issueOrgName").asText();

                        // 直接写入CSV行（已处理特殊字符）
                        writer.write(escapeCsv(idCode)+","
                                +escapeCsv(licenseCode) +","
                                + escapeCsv(convertToChinaTime(createdDate))+","
                               +escapeCsv(convertToChinaTime(expiryDate))+","
                                +escapeCsv(convertToChinaTime(issueDate))+","
                                +escapeCsv(holderIdentityNum) +","
                                +escapeCsv(implementCode) +","
                                +escapeCsv(issueOrgName) +"\n");

                        // 进度跟踪
                        int count = totalProcessed.incrementAndGet();
                        if (count % 10000 == 0) {
                            System.out.printf("已处理 %d 条记录...%n", count);
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("处理行时出错: %s%n错误详情: %s%n", line, e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.printf("CSV文件生成成功！共处理 %d 条数据，耗时 %d 毫秒%n",
                    totalProcessed.get(), endTime - startTime);

        } catch (IOException e) {
            System.err.printf("文件操作错误: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    // CSV特殊字符处理
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}