import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LicenseAppAbolish {

    public static void main(String[] args) {
        String inputFile = "input.xlsx";
        String outputFile = "result.txt";
        String apiUrl = "http://192.158.148.28:9090/license-app/v1/license/20009156300008286X110112/abolish?access_token=de10d5a5-1c63-4590-a833-d8d5f6b8eb51";

        try {
            List<String> licenseCodes = readLicenseCodes(inputFile);
            processLicenses(licenseCodes, apiUrl, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readLicenseCodes(String filePath) throws IOException {
        List<String> codes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                Cell cell = row.getCell(0);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    codes.add(cell.getStringCellValue());
                }
            }
        }
        return codes;
    }

    private static void processLicenses(List<String> licenseCodes, String apiUrl, String outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String licenseCode : licenseCodes) {
                String status = "N/A";
                String responseContent = "";

                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(apiUrl);
                    httpPost.setHeader("Content-Type", "application/json");

                    // Build request body
                    LicenseRequest request = buildRequest(licenseCode);
                    String json = mapper.writeValueAsString(request);
                    httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

                    try (CloseableHttpResponse response = client.execute(httpPost)) {
                        status = String.valueOf(response.getStatusLine().getStatusCode());
                        HttpEntity entity = response.getEntity();
                        responseContent = EntityUtils.toString(entity);
                    }
                } catch (Exception e) {
                    responseContent = "Error: " + e.getMessage();
                }

                // Write to file
                writer.write(licenseCode + "," + status + "," + responseContent);
                writer.newLine();
            }
        }
    }

    private static LicenseRequest buildRequest(String licenseCode) {
        LicenseRequest request = new LicenseRequest();
        LicenseRequest.Data data = new LicenseRequest.Data();
        LicenseRequest.Operator operator = new LicenseRequest.Operator();

        data.license_code = licenseCode;
        data.service_item_name = "CA测试事项";
        data.service_item_code = "12345676543211234566";
        data.biz_num = "0000003";

        operator.account = "gz_rjj";
        operator.name = "张三";
        operator.identity_num = "441702199104224123";
        operator.role = "市计生局 xx 科室科员";
        operator.service_org = "市计生局";
        operator.division = "朝阳区";
        operator.division_code = "110000";

        data.operator = operator;
        request.data = data;
        return request;
    }

    // Request model classes
    static class LicenseRequest {
        public Data data;

        static class Data {
            public String license_code;
            public String service_item_name;
            public String service_item_code;
            public String biz_num;
            public Operator operator;
        }

        static class Operator {
            public String account;
            public String name;
            public String identity_num;
            public String role;
            public String service_org;
            public String division;
            public String division_code;
        }
    }
}