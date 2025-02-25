package shujuguolv;

// 导入依赖库
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * 房产证信息提取客户端
 * 核心功能：
 * 1. 从Excel读取身份证号（支持单元格多值）
 * 2. 调用认证接口获取用证码（AuthCode）
 * 3. 使用用证码调用证照提取接口
 * 4. 解析JSON数据并保存到Excel
 */
public class LicenseAppClientToExcelFCZ {

    // 配置常量
    private static final String ACCESS_TOKEN = "db86e0a7-355b-4b19-9d85-51572e39f942"; // API访问令牌
    private static final String BASE_URL = "http://172.26.50.55:9090/license-app/v1/license"; // 服务基础地址
    private static final ObjectMapper objectMapper = new ObjectMapper(); // JSON解析器

    public static void main(String[] args) {
        try {
            // 从Excel读取原始身份证号（保留单元格原始内容）
            List<String> identityNumbers = readIdentityNumbersFromExcel("C:\\Users\\潘强\\Desktop\\feijing_fangchan.xlsx");
            if (identityNumbers.isEmpty()) {
                System.err.println("Excel 文件中未找到身份证号！");
                return;
            }

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Extracted Data"); // 创建数据表

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("身份证号");   // 原始身份证号列
            headerRow.createCell(1).setCellValue("AuthCode"); // 认证码列
            headerRow.createCell(2).setCellValue("ZZHM");     // 房产证号码列

            int rowNum = 1;  // 数据行起始索引

            // 遍历处理每个身份证号条目
            for (String identityEntry : identityNumbers) {
                System.out.println("正在处理条目: " + identityEntry);

                /** 关键处理流程开始 */
                // 1. 调用认证接口获取用证码
                List<String> authCodes = callFirstApi(identityEntry);
                if (authCodes != null && !authCodes.isEmpty()) {
                    // 2. 保存用证码到文本文件
                    saveAuthCodesToFile("C:\\Users\\潘强\\Desktop\\auth_codes.txt", authCodes, identityEntry);

                    // 3. 遍历用证码获取详细信息
                    for (String authCode : authCodes) {
                        String apiResponse = callSecondApi(authCode);
                        if (apiResponse != null) {
                            // 4. 解析并保存数据到Excel
                            extractAndSaveFields(apiResponse, identityEntry, authCode, sheet, rowNum);
                            rowNum++; // 行号递增
                        }
                    }
                }
                /** 关键处理流程结束 */
            }

            // 保存Excel文件
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\房产证信息_feijing_fangchan.xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println("数据已保存到 房产证信息.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从Excel读取身份证号（保留单元格原始内容）
     * @param filePath Excel文件路径
     * @return 包含原始条目（可能含多个身份证号）的列表
     */
    private static List<String> readIdentityNumbersFromExcel(String filePath) throws IOException {
        List<String> entries = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            for (Row row : sheet) {
                Cell cell = row.getCell(0);      // 读取第一列数据
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        entries.add(value); // 保留原始内容（可能含逗号分隔的多个身份证号）
                    }
                }
            }
        }
        return entries;
    }

    /**
     * 调用认证接口获取用证码
     * @param identityNumber 身份证号（可能是多个号码的组合）
     * @return 用证码列表
     */
    private static List<String> callFirstApi(String identityNumber) throws IOException {
        String url = BASE_URL + "/auth?access_token=" + ACCESS_TOKEN;
        // 构建JSON请求体
        String requestBody = String.format("{\n"
                + "    \"identity_number\": \"%s\",\n" // 身份证号参数
                + "    \"service_item_code\": \"12000340021100000002113\",\n" // 服务事项唯一编码
                + "    \"service_item_name\": \"房产证查询事项\",\n"           // 服务事项名称
                + "    \"page_size\": 150,\n"                              // 分页大小
                + "    \"page_index\": 1,\n"                               // 页码
                + "    \"request_id\": \"999556f1f488269e016a382d8dd8111e\",\n" // 请求ID
                + "    \"operator\": {\n"                                   // 操作员信息
                + "        \"account\": \"1\",\n"
                + "        \"identity_num\": \"11111\",\n"
                + "        \"role\": \"csjs\",\n"
                + "        \"division\": \"北京市\",\n"
                + "        \"division_code\": \"110000\",\n"
                + "        \"service_org\": \"北京市公安局平谷分局\",\n"
                + "        \"name\": \"张三\",\n"
                + "        \"service_org_code\": \"11110000000026884K\"\n"
                + "    }\n"
                + "}", identityNumber);

        String response = sendPostRequest(url, requestBody);
        if (response != null) {
            JsonNode root = objectMapper.readTree(response);
            JsonNode codesNode = root.path("auth_codes");
            if (codesNode.isArray() && codesNode.size() > 0) {
                List<String> codes = new ArrayList<>();
                for (JsonNode node : codesNode) {
                    codes.add(node.asText());
                }
                return codes;
            } else {
                System.err.println("未找到有效用证码: " + response);
            }
        }
        return null;
    }

    /**
     * 调用证照数据提取接口
     */
    private static String callSecondApi(String authCode) throws IOException {
        String url = BASE_URL + "/get_license?auth_code=" + authCode + "&access_token=" + ACCESS_TOKEN;
        return sendGetRequest(url);
    }

    /**
     * 发送POST请求（带JSON请求体）
     */
    private static String sendPostRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // 发送请求数据
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 处理响应
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("POST请求失败，状态码: " + conn.getResponseCode());
            return null;
        }
    }

    /**
     * 保存用证码到文本文件（追加模式）
     */
    private static void saveAuthCodesToFile(String fileName, List<String> authCodes, String identityEntry) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("原始条目: " + identityEntry + "\n");
            for (String code : authCodes) {
                writer.write("AuthCode: " + code + "\n");
            }
            writer.write("\n"); // 条目间空行分隔
        }
        System.out.println("用证码已保存至: " + fileName);
    }

    /**
     * 发送GET请求
     */
    private static String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("GET请求失败，状态码: " + conn.getResponseCode());
            return null;
        }
    }

    /**
     * 解析并保存数据到Excel（核心解析逻辑）
     */
    private static void extractAndSaveFields(String response, String identityEntry, String authCode, Sheet sheet, int rowNum) throws IOException {
        JsonNode root = objectMapper.readTree(response);

        // 校验接口返回状态
        if (!"SUCCESS".equals(root.path("ack_code").asText())) {
            System.out.println("接口返回失败状态，AuthCode: " + authCode);
            return;
        }

        JsonNode dataNode = root.path("data");
        if (dataNode.isMissingNode()) return;

        /** 关键数据处理步骤 */
        try {
            // 处理嵌套JSON字符串
            String jsonStr = dataNode.path("data_fields").asText()
                    .replace('\'', '\"'); // 统一引号格式

            JsonNode fields = objectMapper.readTree(jsonStr);
            String zzhm = fields.path("ZZHM").asText(""); // 房产证号码

            // 写入Excel行
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(identityEntry); // 原始身份证号条目
            row.createCell(1).setCellValue(authCode);      // 用证码
            row.createCell(2).setCellValue(zzhm);          // 房产证号码
        } catch (Exception e) {
            System.err.println("解析数据字段异常: " + e.getMessage());
        }
    }
}