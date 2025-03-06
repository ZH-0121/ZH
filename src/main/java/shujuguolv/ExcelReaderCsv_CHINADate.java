package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

public class ExcelReaderCsv_CHINADate {

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
        String filePath = "C:\\Users\\潘强\\Desktop\\中华人民共和国不动产权证书数据\\data9.txt";
        String jsonString = readFileAndOutput(filePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode hitsNode = rootNode.path("hits").path("hits");

            String csvFilePath = "C:\\Users\\潘强\\Desktop\\中华人民共和国不动产权证书数据\\license_data00.csv";
            boolean isFileExists = new File(csvFilePath).exists();

            try (FileWriter writer = new FileWriter(csvFilePath, true)) {
                // 如果文件不存在，先写入表头
                if (!isFileExists) {
                    writer.append("createdDate,licenseCode,issueDate,holderIdentityNum,idCode,holderName\n");
                }

                Iterator<JsonNode> elements = hitsNode.elements();
                while (elements.hasNext()) {
                    JsonNode hit = elements.next();
                    JsonNode sourceNode = hit.path("_source");

                    // createdDate
                    String createdDate = sourceNode.path("createdDate").asText("");
                    String formattedCreatedDate = convertToChinaTime(createdDate);

                    // licenseCode
                    String licenseCode = sourceNode.path("licenseCode").asText("");

                    // issueDate
                    String issueDate = sourceNode.path("issueDate").asText("");
                    String formattedIssueDate = convertToChinaTime(issueDate);

                    // holderIdentityNum
                    JsonNode holderIdentityNum = sourceNode.path("holderIdentityNum");
                    String identityNum = "";
                    if (holderIdentityNum.isArray() && !holderIdentityNum.isEmpty()) {
                        identityNum = holderIdentityNum.get(0).asText("");
                    }

                    // idCode
                    String idCode = sourceNode.path("idCode").asText("");

                    // holderName
                    JsonNode holderName = sourceNode.path("holderName");
                    String name = "";
                    if (holderName.isArray() && !holderName.isEmpty()) {
                        name = holderName.get(0).asText("");
                    }

                    // 写入一行数据
                    writer.append(formatCsvValue(formattedCreatedDate))
                            .append(",")
                            .append(formatCsvValue(licenseCode))
                            .append(",")
                            .append(formatCsvValue(formattedIssueDate))
                            .append(",")
                            .append(formatCsvValue(identityNum))
                            .append(",")
                            .append(formatCsvValue(idCode))
                            .append(",")
                            .append(formatCsvValue(name))
                            .append("\n");
                }

                System.out.println("CSV 文件追加成功！");
            } catch (IOException e) {
                System.err.println("写入 CSV 文件时发生错误: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }

    // 格式化 CSV 值，处理包含逗号和引号的情况
    private static String formatCsvValue(String value) {
        if (value.contains(",") || value.contains("\"")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}