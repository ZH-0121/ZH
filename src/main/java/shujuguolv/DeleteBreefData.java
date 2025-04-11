package shujuguolv;

import okhttp3.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 删除海草中jpg,zz,pdf数据
 *
 */


public class DeleteBreefData {
    public static void main(String[] args) {
        String excelFilePath = "path/to/your/excel/file.xlsx";
        List<String> licenseCodes = readLicenseCodesFromExcel(excelFilePath);

        // 创建线程池，线程数量可以根据实际情况调整
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (String licenseCode : licenseCodes) {
            executorService.submit(() -> sendDeleteRequests(licenseCode));
        }

        // 关闭线程池
        executorService.shutdown();
    }

    private static List<String> readLicenseCodesFromExcel(String filePath) {
        List<String> licenseCodes = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    if (cell != null) {
                        licenseCodes.add(cell.getStringCellValue());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return licenseCodes;
    }

    private static void sendDeleteRequests(String licenseCode) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        String[] endpoints = {
                "http://192.158.148.16:9335/jpg_" + licenseCode,
                "http://192.158.148.16:9335/pdf_" + licenseCode,
                "http://192.158.148.16:9335/zz_" + licenseCode
        };
        for (String endpoint : endpoints) {
            try {
                Request request = new Request.Builder()
                        .url(endpoint)
                        .method("DELETE", body)
                        .addHeader("Authorization", "Basic c2NvdHQ6dGlnZXI=")
                        .build();
                Response response = client.newCall(request).execute();
                System.out.println("Response from " + endpoint + ": " + response.code());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}