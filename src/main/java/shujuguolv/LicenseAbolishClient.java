package shujuguolv;
/*
* 废止接口，读取表格中licensecode批量废止
* */
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LicenseAbolishClient {

    private static final String API_URL = "http://172.26.50.55:9090/license-app/v1/license/200091571000026825110000/abolish?access_token=ccb19454-b547-4b9b-aa9c-9e688f178544";
    private static final int THREAD_POOL_SIZE = 10;
    private static final String OUTPUT_FILE = "C:\\Users\\潘强\\Desktop\\result.txt"; // 结果输出文件路径

    public static void main(String[] args) {
        String excelPath = "C:\\Users\\潘强\\Desktop\\（自行提交）学历证书.xlsx";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            List<String> licenseCodes = readLicenseCodesFromExcel(excelPath);
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            // 提交任务到线程池
            for (String licenseCode : licenseCodes) {
                executor.submit(new LicenseTask(licenseCode, writer));
            }

            // 关闭线程池并等待任务完成
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class LicenseTask implements Runnable {
        private final String licenseCode;
        private final BufferedWriter writer;

        public LicenseTask(String licenseCode, BufferedWriter writer) {
            this.licenseCode = licenseCode;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                boolean isSuccess = sendPostRequest(licenseCode);
                synchronized (writer) {  // 同步写入操作
                    System.out.println("License Code: " + licenseCode + " -> Success: " + isSuccess);
                    writer.write("License Code: " + licenseCode + " -> Success: " + isSuccess);
                    writer.newLine();
                    writer.flush();  // 确保实时写入
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 发送POST请求
     */
    private static boolean sendPostRequest(String licenseCode) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);

            // 构建JSON请求体
            String jsonBody = buildRequestBody(licenseCode);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            // 执行请求
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            // 根据HTTP状态码判断是否成功（假设200为成功）
            return statusCode == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 构建JSON请求体
     */
    private static String buildRequestBody(String licenseCode) {
        return String.format(
                "{ \"data\": { " +
                        "\"license_code\": \"%s\", " +
                        "\"service_item_name\": \"CA测试事项\", " +
                        "\"service_item_code\": \"12345676543211234566\", " +
                        "\"biz_num\": \"0000003\", " +
                        "\"operator\": { " +
                        "\"account\": \"gz_rjj\", " +
                        "\"name\": \"张三\", " +
                        "\"identity_num\": \"441702199104224123\", " +
                        "\"role\": \"市计生局 xx 科室科员\", " +
                        "\"service_org\": \"市计生局\", " +
                        "\"division\": \"朝阳区\", " +
                        "\"division_code\": \"110000\" " +
                        "} } }",
                licenseCode
        );
    }

    /**
     * 从Excel读取license_code（第一列）
     */
    private static List<String> readLicenseCodesFromExcel(String filePath) throws IOException {
        List<String> codes = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);  // 读取第一个Sheet
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳过标题行

                Cell cell = row.getCell(0);  // 第一列
                if (cell != null) {
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        codes.add(value);
                    }
                }
            }
        }
        return codes;
    }
}