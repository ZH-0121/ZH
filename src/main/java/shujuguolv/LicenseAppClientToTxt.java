package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LicenseAppClientToTxt {

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

            for (String identityNumber : identityNumbers) {
                System.out.println("正在处理身份证号: " + identityNumber);

                List<String> authCodes = callFirstApi(identityNumber);
                if (authCodes != null && !authCodes.isEmpty()) {
                    saveAuthCodesToFile("C:\\Users\\潘强\\Desktop\\auth_codes.txt", authCodes, identityNumber);

                    for (String authCode : authCodes) {
                        String secondApiResponse = callSecondApi(authCode);
                        if (secondApiResponse != null) {
                            extractAndSaveFields(secondApiResponse, identityNumber, authCode);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 Excel 文件中读取身份证号（第一列）
     */
    private static List<String> readIdentityNumbersFromExcel(String filePath) throws IOException {
        List<String> identityNumbers = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            for (Row row : sheet) {
                Cell cell = row.getCell(0); // 读取第一列
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    identityNumbers.add(cell.getStringCellValue());
                }
            }
        }
        return identityNumbers;
    }

    /**
     * 调用第一个接口，获取所有 auth_codes
     */
    private static List<String> callFirstApi(String identityNumber) throws IOException {
        String url = BASE_URL + "/auth?access_token=" + ACCESS_TOKEN;
        String requestBody = String.format("{\n" +
                "    \"identity_number\": %s,\n" +
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
            }
        }
        return null;
    }

    /**
     * 调用第二个接口，获取响应数据
     */
    private static String callSecondApi(String authCode) throws IOException {
        String url = BASE_URL + "/get_license?auth_code=" + authCode + "&access_token=" + ACCESS_TOKEN;
        return sendGetRequest(url);
    }


    /**
     * 发送 POST 请求
     */
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

    /**
     * 发送 GET 请求
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
     * 将 auth_codes 保存到文件
     */
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

    /**
     * 从第二个接口的响应中提取指定字段并保存到文件（仅当 ack_code 为 SUCCESS 时保存）
     */
    private static void extractAndSaveFields(String response, String identityNumber, String authCode) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);

        // 检查 ack_code 是否为 SUCCESS
        String ackCode = rootNode.path("ack_code").asText();
        if (!"SUCCESS".equals(ackCode)) {
            System.out.println("提取证照数据接口返回的 ack_code 为 FAILURE，跳过保存。AuthCode: " + authCode);
            return; // 不保存
        }

        JsonNode dataNode = rootNode.path("data");
        if (!dataNode.isMissingNode()) {
            String jg_ssxqdm = dataNode.path("JG_SSXQDM").asText();
            String hz_xm = dataNode.path("HZ_XM").asText();
            String hdz_qhnxxdz = dataNode.path("HDZ_QHNXXDZ").asText();

            String result = String.format("身份证号: %s, AuthCode: %s, 'JG_SSXQDM':'%s','HZ_XM':'%s','HDZ_QHNXXDZ':'%s'",
                    identityNumber, authCode, jg_ssxqdm, hz_xm, hdz_qhnxxdz);
            saveToFile("C:\\Users\\潘强\\Desktop\\extracted_fields.txt", result);
            System.out.println("提取的字段已保存到 extracted_fields.txt");
        }
    }

    /**
     * 将内容保存到文件（仅在 ack_code 为 SUCCESS 时调用）
     */
    private static void saveToFile(String fileName, String content) throws IOException {
        try (FileWriter writer = new FileWriter(fileName, true)) { // 追加模式
            writer.write(content + "\n");
        }
        System.out.println("内容已保存到 " + fileName);
    }
}