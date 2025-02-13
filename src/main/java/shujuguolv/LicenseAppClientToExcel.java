package shujuguolv;

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

public class LicenseAppClientToExcel {

    private static final String ACCESS_TOKEN = "a6ec167c-8382-4ca2-b92b-8a4de54fb588";
    private static final String BASE_URL = "http://172.26.50.55:9090/license-app/v1/license";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            List<String> identityNumbers = readIdentityNumbersFromExcel("C:\\Users\\潘强\\Desktop\\identity_numbers.xlsx");
            if (identityNumbers.isEmpty()) {
                System.err.println("Excel 文件中未找到身份证号！");
                return;
            }

            // Create or open the Excel file to save the results
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Extracted Data");

            // Create the header row in Excel
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("身份证号");
            headerRow.createCell(1).setCellValue("AuthCode");
            headerRow.createCell(2).setCellValue("YHZGXDM");
            headerRow.createCell(3).setCellValue("HZ_XM");

            int rowNum = 1;  // Start from the second row (row 1)

            for (String identityNumber : identityNumbers) {
                System.out.println("正在处理身份证号: " + identityNumber);

                List<String> authCodes = callFirstApi(identityNumber);
                if (authCodes != null && !authCodes.isEmpty()) {
                    saveAuthCodesToFile("C:\\Users\\潘强\\Desktop\\auth_codes.txt", authCodes, identityNumber);

                    for (String authCode : authCodes) {
                        String secondApiResponse = callSecondApi(authCode);
                        if (secondApiResponse != null) {
                            extractAndSaveFields(secondApiResponse, identityNumber, authCode, sheet, rowNum);
                            rowNum++;  // Move to the next row after saving
                        }
                    }
                }
            }

            // Write the Excel file to disk
            try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\潘强\\Desktop\\extracted_fields.xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println("数据已保存到 extracted_fields.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readIdentityNumbersFromExcel(String filePath) throws IOException {
        List<String> identityNumbers = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);  // Read the first sheet
            for (Row row : sheet) {
                Cell cell = row.getCell(0);  // Read the first column (身份证号)
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    identityNumbers.add(cell.getStringCellValue());
                }
            }
        }
        return identityNumbers;
    }

    private static List<String> callFirstApi(String identityNumber) throws IOException {
        String url = BASE_URL + "/auth?access_token=" + ACCESS_TOKEN;
        String requestBody = String.format("{\n" +
                "    \"identity_number\": \"%s\",\n" +
                "    \"service_item_code\": \"0345674110X45111111111000002140X100\",\n" +
                "    \"service_item_name\": \"北京市政务服务电子证照查询事项\",\n" +
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
            JsonNode authCodesNode = rootNode.path("auth_codes");
            if (authCodesNode.isArray() && !authCodesNode.isEmpty()) {
                List<String> authCodes = new ArrayList<>();
                for (JsonNode codeNode : authCodesNode) {
                    authCodes.add(codeNode.asText());
                }
                return authCodes;
            } else {
                System.err.println("No auth codes found in the response: " + response);
            }
        } else {
            System.err.println("Response is null for identity number: " + identityNumber);
        }
        return null;
    }


    private static String callSecondApi(String authCode) throws IOException {
        String url = BASE_URL + "/get_license?auth_code=" + authCode + "&access_token=" + ACCESS_TOKEN;
        return sendGetRequest(url);
    }

    private static String sendPostRequest(String urlString, String requestBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

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
    private static void saveAuthCodesToFile(String fileName, List<String> authCodes, String identityNumber) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, true)) { // 追加模式
            writer.write("身份证号: " + identityNumber + "\n");
            for (String authCode : authCodes) {
                writer.write("AuthCode: " + authCode + "\n");
            }
            writer.write("\n");
        }
        System.out.println("AuthCodes 已保存到 " + fileName);
    }

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

    private static void extractAndSaveFields(String response, String identityNumber, String authCode, Sheet sheet, int rowNum) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);

        String ackCode = rootNode.path("ack_code").asText();
        if (!"SUCCESS".equals(ackCode)) {
            System.out.println("提取证照数据接口返回的 ack_code 为 FAILURE，跳过保存。AuthCode: " + authCode);
            return;
        }

        JsonNode dataNode = rootNode.path("data");
        if (dataNode.isMissingNode()) return;

        // 1. 处理 data_fields 的 JSON 字符串
        String dataFieldsStr = dataNode.path("data_fields").asText()
                .replace('\'', '\"'); // 将单引号替换为双引号，确保 JSON 解析成功

        try {
            // 2. 解析 data_fields
            JsonNode dataFieldsNode = objectMapper.readTree(dataFieldsStr);

            // 3. 提取目标字段（如果字段不存在则返回空字符串）
            String yhzgxdm = dataFieldsNode.path("YHZGXDM").asText("");
            String hzXm = dataFieldsNode.path("HZ_XM").asText("");

            // 4. 写入 Excel
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(identityNumber);
            row.createCell(1).setCellValue(authCode);
            row.createCell(2).setCellValue(yhzgxdm);
            row.createCell(3).setCellValue(hzXm);

            rowNum++; // 只有成功写入时才递增行号
        } catch (Exception e) {
            System.err.println("解析 data_fields 失败，AuthCode: " + authCode);
            e.printStackTrace();
        }
    }
}
