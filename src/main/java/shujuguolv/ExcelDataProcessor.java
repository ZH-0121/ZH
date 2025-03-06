package shujuguolv;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelDataProcessor {

    public static void main(String[] args) {
        try {
            // 读取 Excel 第一列数据
            List<String> identityNumbers = readExcelColumn("C:\\Users\\潘强\\Desktop\\sfz.xlsx", 0);

            // 处理每个身份证号
            for (String identityNumber : identityNumbers) {
                Instant start = Instant.now();

                // 调用接口1
                String api1Response = callApi1(identityNumber);
                JsonObject api1Json = JsonParser.parseString(api1Response).getAsJsonObject();

                // 提取 auth_codes 和 license_code
                String authCode = api1Json.getAsJsonArray("auth_codes").get(0).getAsString();
                String licenseCode = api1Json.getAsJsonArray("data").get(0).getAsJsonObject().get("license_code").getAsString();

                // 保存到 txt 文件
                saveToTxt(authCode + "," + licenseCode, "C:\\Users\\潘强\\Desktop\\output1.txt");

                // 调用接口2
                String api2Response = callApi2(authCode);
                JsonObject api2Json = JsonParser.parseString(api2Response).getAsJsonObject();

                // 处理 data_fields
                String dataFields = api2Json.getAsJsonObject("data").get("data_fields").getAsString();
                JsonObject dataFieldsJson = JsonParser.parseString(dataFields).getAsJsonObject();

                // 检查并替换ZZ字段
                if (dataFieldsJson.has("ZZ")) {
                    String zzValue = dataFieldsJson.get("ZZ").getAsString();
                    zzValue = zzValue.replace("110228", "北京市密云县");
                    dataFieldsJson.addProperty("ZZ", zzValue);
                }

                // 转换回字符串
                dataFields = dataFieldsJson.toString();

                // 保存到 txt 文件
                saveToTxt(dataFields, "C:\\Users\\潘强\\Desktop\\output2.txt");

                // 调用新接口
                String api3Response = callApi3(licenseCode, dataFields);
                System.out.println("新接口响应: " + api3Response);

                Instant end = Instant.now();
                System.out.println(licenseCode + " 处理 dataFields，耗时： " + Duration.between(start, end).toMillis() + " 毫秒");
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
        Instant start = Instant.now();
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

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // 处理接口调用失败的情况
            System.err.println("接口 1 调用失败，状态码: " + connection.getResponseCode());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
            throw e;
        }

        Instant end = Instant.now();
        System.out.println("接口 1 调用耗时： " + Duration.between(start, end).toMillis() + " 毫秒");
        return response.toString();
    }

    // 调用接口 2
    public static String callApi2(String authCode) throws IOException {
        String apiUrl = "172.26.50.55:9090/license-app/v1/license/get_license?auth_code=" + authCode + "&access_token=38dbda8f-ba05-4bfa-99ef-70b5ed361cde";
        Instant start = Instant.now();
        URL url = new URL("http://" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // 处理接口调用失败的情况
            System.err.println("接口 2 调用失败，状态码: " + connection.getResponseCode());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
            throw e;
        }

        Instant end = Instant.now();
        System.out.println("接口 2 调用耗时： " + Duration.between(start, end).toMillis() + " 毫秒");
        return response.toString();
    }

    // 调用新接口
    public static String callApi3(String licenseCode, String dataFields) throws IOException {
        String apiUrl = "172.26.50.55:9090/license-app/v1/license/10000760100002888X110000/change?access_token=38dbda8f-ba05-4bfa-99ef-70b5ed361cde";
        Instant start = Instant.now();
        URL url = new URL("http://" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 构建请求体
        Map<String, Object> data = new HashMap<>();
        Map<String, JsonElement> dataFieldsMap = JsonParser.parseString(dataFields).getAsJsonObject().asMap();
        data.put("data_fields", dataFieldsMap);
        data.put("license_code", licenseCode);
        data.put("license_group", "组别1");

        Map<String, Object> operator = new HashMap<>();
        operator.put("account", "bjstyjgwxxtyxm");
        operator.put("division", "六里桥");
        operator.put("division_code", "110000");
        operator.put("identity_num", "130481199706242117");
        operator.put("name", "潘强");
        operator.put("role", "bjca潘强");
        operator.put("service_org", "bjca");
        data.put("operator", operator);

        data.put("seal_code", "DZYZ00002888XWJAgjY");
        data.put("service_item_code", "0700023003110114000000000029030100");
        data.put("service_item_name", "对公民申请领取居民身份证进行办理-昌平");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestBody);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // 处理接口调用失败的情况
            System.err.println("接口 3 调用失败，状态码: " + connection.getResponseCode());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
            throw e;
        }

        Instant end = Instant.now();
        System.out.println("接口 3 调用耗时： " + Duration.between(start, end).toMillis() + " 毫秒");
        return response.toString();
    }

    // 保存数据到 txt 文件
    public static void saveToTxt(String data, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(data);
            writer.newLine();
        }
    }
}