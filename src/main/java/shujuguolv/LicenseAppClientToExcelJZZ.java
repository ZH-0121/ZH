package shujuguolv;

// 导入所需的库
import com.alibaba.fastjson.JSONObject;
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
 * 居住证信息提取客户端程序
 * 主要功能：
 * 1. 从Excel读取身份证号列表
 * 2. 调用认证接口获取用证码（AuthCode）
 * 3. 使用用证码调用证照提取接口
 * 4. 解析返回的JSON数据并保存到Excel
 */
public class LicenseAppClientToExcelJZZ {

    // 配置常量
    private static final String ACCESS_TOKEN = "b3db1942-8807-46e0-996f-74542991cf79";
    private static final String BASE_URL = "http://172.26.50.55:9090/license-app/v1/license";
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) {
        try {
            // 从Excel读取身份证号列表（第一列数据）
            List<String> identityNumbers = readIdentityNumbersFromExcel("C:\\Users\\潘强\\Desktop\\datajuzhu.xlsx");
            if (identityNumbers.isEmpty()) {
                System.err.println("Excel 文件中未找到身份证号！");
                return;
            }

            // 创建新的Excel工作簿和Sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Extracted Data");

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("身份证号");
            headerRow.createCell(1).setCellValue("AuthCode");
            headerRow.createCell(2).setCellValue("ZZHM");
            headerRow.createCell(3).setCellValue("XJZDZ");
            headerRow.createCell(4).setCellValue("YXQKSRQ");
            headerRow.createCell(5).setCellValue("YXQJSRQ");
            headerRow.createCell(6).setCellValue("ZZMC");
            headerRow.createCell(7).setCellValue("FZRQ");

            int rowNum = 1;  // 数据行从第二行开始

            // 遍历每个身份证号进行处理
            for (String identityNumber : identityNumbers) {
                System.out.println("正在处理身份证号: " + identityNumber);

                // **关键步骤1：调用第一个接口获取用证码列表**
                List<String> authCodes = callFirstApi(identityNumber);
                if (authCodes != null && !authCodes.isEmpty()) {
                    // 将用证码保存到文本文件
                    saveAuthCodesToFile("C:\\Users\\潘强\\Desktop\\auth_codes.txt", authCodes, identityNumber);

                    // 遍历每个用证码进行详细查询
                    for (String authCode : authCodes) {
                        // **关键步骤2：调用第二个接口获取证照详细信息**
                        String secondApiResponse = callSecondApi(authCode);
                        if (secondApiResponse != null) {
                            // 解析并保存返回数据到Excel
                            extractAndSaveFields(secondApiResponse, identityNumber, authCode, sheet, rowNum);
                            rowNum++;  // 行号递增
                        }
                    }
                }
            }

            // 将Excel写入文件
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\居住证信息.xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println("数据已保存到 居住证信息.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从Excel文件读取身份证号列表
     */
    private static List<String> readIdentityNumbersFromExcel(String filePath) throws IOException {
        List<String> identityNumbers = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);  // 读取第一个工作表
            for (Row row : sheet) {
                Cell cell = row.getCell(0);  // 读取第一列
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    identityNumbers.add(cell.getStringCellValue());
                }
            }
        }
        return identityNumbers;
    }

    /**
     * 调用第一个接口获取用证码列表
     */
    private static List<String> callFirstApi(String identityNumber) throws IOException {
        String url = BASE_URL + "/auth?access_token=" + ACCESS_TOKEN;
        // 构建JSON请求体
        String requestBody = String.format("{\n" +
                "    \"identity_number\": \"%s\",\n" +
                "    \"service_item_code\": \"SJWJSZGZSJZZCX\",\n" +
                "    \"service_item_name\": \"居住证查询事项\",\n" +
                "    \"page_size\": 150,\n" +
                "    \"page_index\": 1,\n" +
                "    \"request_id\": \"999556f1f488269e016a382d8dd8111e\",\n" +
                "    \"operator\": {\n" +
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
                System.err.println("返回结果中未找到用证码: " + response);
            }
        } else {
            System.err.println("接口返回空响应，身份证号: " + identityNumber);
        }
        return null;
    }

    /**
     * 调用第二个接口获取证照详细信息
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
        connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(connection.getInputStream(), String.valueOf(StandardCharsets.UTF_8))) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("POST 请求失败，响应码: " + responseCode);
            return null;
        }
    }

    /**
     * 保存用证码到文本文件（追加模式）
     */
    private static void saveAuthCodesToFile(String fileName, List<String> authCodes, String identityNumber) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("身份证号: " + identityNumber + "\n");
            for (String authCode : authCodes) {
                writer.write("AuthCode: " + authCode + "\n");
            }
            writer.write("\n");
        }
        System.out.println("AuthCodes 已保存到 " + fileName);
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
            try (Scanner scanner = new Scanner(connection.getInputStream(), String.valueOf(StandardCharsets.UTF_8))) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            return response.toString();
        } else {
            System.err.println("GET 请求失败，响应码: " + responseCode);
            return null;
        }
    }

    /**
     * 解析返回数据并保存到Excel
     */
    private static void extractAndSaveFields(String response, String identityNumber, String authCode, Sheet sheet, int rowNum) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);

        // 检查返回状态码
        String ackCode = rootNode.path("ack_code").asText();
        if (!"SUCCESS".equals(ackCode)) {
            System.out.println("接口返回失败状态，跳过保存。AuthCode: " + authCode+response);
            return;
        }

        // 获取数据节点
        JsonNode dataNode = rootNode.path("data");
        if (dataNode.isMissingNode()) return;

        // **关键步骤3：处理嵌套的JSON字符串**
        String dataFieldsStr = dataNode.path("data_fields").asText()
                .replace('\'', '\"'); // 替换单引号为双引号确保JSON解析成功

        try {
            // 解析二级JSON数据
            JsonNode dataFieldsNode = objectMapper.readTree(dataFieldsStr);

            // 提取目标字段
            String zzhm = dataFieldsNode.path("ZZHM").asText(""); // 居住证号码
            String xjzdz = dataFieldsNode.path("XJZDZ").asText("");
            String yxqksrq = dataFieldsNode.path("YXQKSRQ").asText("");
            String yxqjsrq = dataFieldsNode.path("YXQJSRQ").asText("");
            String zzmc = dataFieldsNode.path("ZZMC").asText("");
            String fzrq = dataFieldsNode.path("FZRQ").asText("");




            // 创建Excel行并写入数据
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(identityNumber);
            row.createCell(1).setCellValue(authCode);
            row.createCell(2).setCellValue(zzhm);
            row.createCell(3).setCellValue(xjzdz);
            row.createCell(4).setCellValue(yxqksrq);
            row.createCell(5).setCellValue(yxqjsrq);
            row.createCell(6).setCellValue(zzmc);
            row.createCell(7).setCellValue(fzrq);



        } catch (Exception e) {
            System.err.println("解析data_fields失败，AuthCode: " + authCode);
            e.printStackTrace();
        }
    }
}

