package shujuguolv;
/*
* 证照删除（物理删除）接口
* 物理删除 海草：zz，fj，pdf，jpg。ES：zz
* */

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LicenseCodeDelete {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\license_codes.xlsx";
        String accessToken = "c13d62a2-0a77-42d5-8b08-b0214cc38ea1";
        String baseUrl = "http://172.26.50.55:9090/license-app/v1/license/deleteZzInfo";

        try {
            // 读取 Excel 文件
            FileInputStream file = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            // 创建线程池，线程数量可根据实际情况调整
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            // 遍历每一行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell licenseCodeCell = row.getCell(0);
                    if (licenseCodeCell != null) {
                        String licenseCode = licenseCodeCell.getStringCellValue();
                        // 构建请求 URL
                        String requestUrl = baseUrl + "?access_token=" + accessToken + "&licenseCode=" + licenseCode;
                        // 提交任务到线程池
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

            // 关闭线程池
            executorService.shutdown();

            // 关闭工作簿和文件输入流
            workbook.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("发送请求到: " + urlStr);
        System.out.println("响应码: " + responseCode);

        connection.disconnect();
    }
}