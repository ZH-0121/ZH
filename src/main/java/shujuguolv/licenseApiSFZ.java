package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class licenseApiSFZ {

    public static void main(String[] args) {
        try {
            // 读取 Excel 第一列数据
            List<String> identityNumbers = readExcelColumn("C:\\Users\\潘强\\Desktop\\sfz.xlsx", 0);

            // 调用接口 1
            for (String identityNumber : identityNumbers) {
                String api1Response = callApi1(identityNumber);
                JsonObject api1Json = JsonParser.parseString(api1Response).getAsJsonObject();

                // 提取 auth_codes 和 license_code
                String authCode = api1Json.getAsJsonArray("auth_codes").get(0).getAsString();
                String licenseCode = api1Json.getAsJsonArray("data").get(0).getAsJsonObject().get("license_code").getAsString();

                // 保存到 txt 文件
                saveToTxt(authCode + "," + licenseCode, "C:\\Users\\潘强\\Desktop\\output1.txt");

                // 调用接口 2
                String api2Response = callApi2(authCode);
                JsonObject api2Json = JsonParser.parseString(api2Response).getAsJsonObject();

                // 处理 data_fields
                String dataFields = api2Json.getAsJsonObject("data").get("data_fields").getAsString();
                dataFields = dataFields.replace("110128", "北京市密云县");

                // 保存到 txt 文件
                saveToTxt(dataFields, "C:\\Users\\潘强\\Desktop\\output2.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取 Excel 某一列的数据
    public static List<String> readExcelColumn(String filePath, int columnIndex) throws IOException {
        List<String> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null) {
                    data.add(cell.toString());
                }
            }
        }
        return data;
    }

    // 调用接口 1
    public static String callApi1(String identityNumber) throws IOException {
        String apiUrl = "172.26.50.55:9090/license-app/v1/license/auth?access_token=38dbda8f-ba05-4bfa-99ef-70b5ed361cde";
        URL url = new URL("http://" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("identity_number", identityNumber);
        requestBody.put("service_item_code", "0100909004110X45111666611000002140X100");
        requestBody.put("service_item_name", "结婚-补发结婚证");
        requestBody.put("page_size", 150);
        requestBody.put("page_index", 1);
        requestBody.put("request_id", "999556f1f488269e016a382d8dd8111e");

        Map<String, Object> operator = new HashMap<>();
        operator.put("account", "1");
        operator.put("identity_num", "11111");
        operator.put("role", "csjs");
        operator.put("division", "北京市");
        operator.put("division_code", "110000");
        operator.put("service_org", "北京市公安局密云分局");
        operator.put("name", "张三");
        operator.put("service_org_code", "111100000000294011");
        requestBody.put("operator", operator);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestBody);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    // 调用接口 2
    public static String callApi2(String authCode) throws IOException {
        String apiUrl = "172.26.50.55:9090/license-app/v1/license/get_license?auth_code=" + authCode + "&access_token=38dbda8f-ba05-4bfa-99ef-70b5ed361cde";
        URL url = new URL("http://" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    // 保存数据到 txt 文件
    public static void saveToTxt(String data, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(data);
            writer.newLine();
        }
    }
}