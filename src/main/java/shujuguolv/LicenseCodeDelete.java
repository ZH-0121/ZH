package shujuguolv;
/*
* 证照删除（物理删除），删除海草中zz,pdf,jpg,ES,数据库数据
* */
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LicenseCodeDelete {
    private static PrintWriter writer;

    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\license_codes0417.xlsx";
        String accessToken = "da0bb9db-a575-4bdb-bba1-87a857ffee87";
        String baseUrl = "http://172.26.50.55:9090/license-app/v1/license/deleteZzInfo";
        String outputPath = "C:\\Users\\潘强\\Desktop\\100010601000021135110000.txt";

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            // 修改此处实现追加模式（注意第二个参数true）
            writer = new PrintWriter(new FileWriter(outputPath, true)); // <-- 核心修改点

            FileInputStream file = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell licenseCodeCell = row.getCell(0);
                    if (licenseCodeCell != null) {
                        String licenseCode = getCellValueAsString(licenseCodeCell);
                        String requestUrl = baseUrl + "?access_token=" + accessToken + "&licenseCode=" + licenseCode;
                        executorService.submit(() -> {
                            try {
                                sendGetRequest(requestUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            workbook.close();
            file.close();

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private static void sendGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        StringBuilder responseBody = new StringBuilder();

        try {
            InputStream inputStream;
            if (responseCode < 400) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                }
            }
        } catch (IOException e) {
            responseBody.append("Error reading response: ").append(e.getMessage());
        }

        synchronized (LicenseCodeDelete.class) {
            writer.println("请求URL: " + urlStr);
            writer.println("响应状态码: " + responseCode);
            writer.println("响应内容: " + responseBody.toString());
            writer.println("------------------------------");
            writer.flush();
            System.out.println("请求URL: " + urlStr);
            System.out.println("响应状态码: " + responseCode);
        }

        connection.disconnect();
    }
}