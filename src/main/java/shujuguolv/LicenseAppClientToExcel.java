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
 * 户口簿信息提取客户端
 * 核心功能：
 * 1. 从Excel读取身份证号列表
 * 2. 调用认证接口获取用证码（AuthCode）
 * 3. 使用用证码调用证照提取接口
 * 4. 解析返回的JSON数据并保存到Excel
 */
public class LicenseAppClientToExcel {

    // 配置常量
    private static final String ACCESS_TOKEN = "db86e0a7-355b-4b19-9d85-51572e39f942";
    private static final String BASE_URL = "http://172.26.50.55:9090/license-app/v1/license";
    private static final ObjectMapper objectMapper = new ObjectMapper();  // JSON解析器

    public static void main(String[] args) {
        try {
            // 从Excel文件读取身份证号（第一列数据）
            List<String> identityNumbers = readIdentityNumbersFromExcel("C:\\Users\\潘强\\Desktop\\feijing_jianhuren.xlsx");
            if (identityNumbers.isEmpty()) {
                System.err.println("Excel 文件中未找到身份证号！");
                return;
            }

            // 创建新的Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Extracted Data");  // 创建数据表

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("身份证号");
            headerRow.createCell(1).setCellValue("AuthCode");
            headerRow.createCell(2).setCellValue("YHZGXDM");  // 与户主关系代码
            headerRow.createCell(3).setCellValue("HZ_XM");     // 户主姓名

            int rowNum = 1;  // 数据行起始位置

            // 遍历处理每个身份证号
            for (String identityNumber : identityNumbers) {
                System.out.println("正在处理身份证号: " + identityNumber);

                // 关键步骤1：获取用证码列表
                List<String> authCodes = callFirstApi(identityNumber);
                if (authCodes != null && !authCodes.isEmpty()) {
                    // 保存用证码到文本文件
                    saveAuthCodesToFile("C:\\Users\\潘强\\Desktop\\auth_codes.txt", authCodes, identityNumber);

                    // 遍历每个用证码获取详细信息
                    for (String authCode : authCodes) {
                        // 关键步骤2：调用数据提取接口
                        String secondApiResponse = callSecondApi(authCode);
                        if (secondApiResponse != null) {
                            // 解析并保存数据到Excel
                            extractAndSaveFields(secondApiResponse, identityNumber, authCode, sheet, rowNum);
                            rowNum++;  // 行号递增
                        }
                    }
                }
            }

            // 写入Excel文件
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\户口簿信息_feijing_jianhuren.xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println("数据已保存到 户口簿信息.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从Excel文件读取身份证号列表
     * @param filePath Excel文件路径
     * @return 身份证号列表
     */
    private static List<String> readIdentityNumbersFromExcel(String filePath) throws IOException {
        List<String> identityNumbers = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);  // 读取第一个工作表
            for (Row row : sheet) {
                Cell cell = row.getCell(0);      // 读取第一列数据
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    identityNumbers.add(cell.getStringCellValue());
                }
            }
        }
        return identityNumbers;
    }

    /**
     * 调用认证接口获取用证码
     * @param identityNumber 身份证号
     * @return 用证码列表
     */
    private static List<String> callFirstApi(String identityNumber) throws IOException {
        String url = BASE_URL + "/auth?access_token=" + ACCESS_TOKEN;
        // 构建JSON请求体
        String requestBody = String.format("{\n" +
                "    \"identity_number\": \"%s\",\n" +
                "    \"service_item_code\": \"0345674110X45111111111000002140X100\",\n" +  // 服务事项唯一编码
                "    \"service_item_name\": \"户口簿查询事项\",\n" +                        // 服务事项名称
                "    \"page_size\": 150,\n" +                                              // 每页数量
                "    \"page_index\": 1,\n" +                                               // 页码
                "    \"request_id\": \"999556f1f488269e016a382d8dd8111e\",\n" +            // 请求ID
                "    \"operator\": {\n" +                                                  // 操作员信息
                "        \"account\": \"1\",\n" +
                "        \"identity_num\": \"11111\",\n" +
                "        \"role\": \"csjs\",\n" +
                "        \"division\": \"北京市\",\n" +
                "        \"division_code\": \"110000\",\n" +
                "        \"service_org\": \"北京市公安局平谷分局\",\n" +
                "        \"name\": \"张三\",\n" +
                "        \"service_org_code\": \"11110000000026884K\"\n" +
                "    }\n" +
                "}", identityNumber);

        String response = sendPostRequest(url, requestBody);
        if (response != null) {
            JsonNode rootNode = objectMapper.readTree(response);
            // 解析返回的auth_codes数组
            JsonNode authCodesNode = rootNode.path("auth_codes");
            if (authCodesNode.isArray() && !authCodesNode.isEmpty()) {
                List<String> authCodes = new ArrayList<>();
                for (JsonNode codeNode : authCodesNode) {
                    authCodes.add(codeNode.asText());
                }
                return authCodes;
            } else {
                System.err.println("接口返回无可用用证码: " + response);
            }
        }
        return null;
    }

    /**
     * 调用数据提取接口
     * @param authCode 用证码
     * @return 接口响应数据
     */
    private static String callSecondApi(String authCode) throws IOException {
        String url = BASE_URL + "/get_license?auth_code=" + authCode + "&access_token=" + ACCESS_TOKEN;
        return sendGetRequest(url);
    }

    /**
     * 发送POST请求
     */
    private static String sendPostRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        // 发送请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 处理响应
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("POST请求失败，状态码: " + responseCode);
            return null;
        }
    }

    /**
     * 保存用证码到文本文件（追加模式）
     */
    private static void saveAuthCodesToFile(String fileName, List<String> authCodes, String identityNumber) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, true)) {  // true表示追加模式
            writer.write("身份证号: " + identityNumber + "\n");
            for (String authCode : authCodes) {
                writer.write("AuthCode: " + authCode + "\n");
            }
            writer.write("\n");  // 添加空行分隔不同身份证号
        }
        System.out.println("用证码已保存至: " + fileName);
    }

    /**
     * 发送GET请求
     */
    private static String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("GET请求失败，状态码: " + responseCode);
            return null;
        }
    }

    /**
     * 解析并保存字段数据（核心数据解析逻辑）
     */
    private static void extractAndSaveFields(String response, String identityNumber, String authCode, Sheet sheet, int rowNum) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);

        // 检查接口返回状态
        String ackCode = rootNode.path("ack_code").asText();
        if (!"SUCCESS".equals(ackCode)) {
            System.out.println("接口返回失败状态，AuthCode: " + authCode);
            return;
        }

        // 获取数据节点
        JsonNode dataNode = rootNode.path("data");
        if (dataNode.isMissingNode()) return;

        /** 关键解析步骤：处理嵌套的JSON字符串 */
        String dataFieldsStr = dataNode.path("data_fields").asText()
                .replace('\'', '\"');  // 统一引号格式

        try {
            JsonNode dataFieldsNode = objectMapper.readTree(dataFieldsStr);

            // 提取目标字段（带默认值处理）
            String yhzgxdm = dataFieldsNode.path("YHZGXDM").asText("");  // 与户主关系代码
            String hzXm = dataFieldsNode.path("HZ_XM").asText("");       // 户主姓名

            // 创建Excel数据行
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(identityNumber);  // 身份证号
            row.createCell(1).setCellValue(authCode);        // 用证码
            row.createCell(2).setCellValue(yhzgxdm);         // 关系代码
            row.createCell(3).setCellValue(hzXm);           // 户主姓名
        } catch (Exception e) {
            System.err.println("解析data_fields异常，AuthCode: " + authCode);
            e.printStackTrace();
        }
    }
}