package shujuguolv;
/*
* 废止接口，读取txt中的idCode进行批量废止
*
* */


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LicenseAbolishClientTxt {

    private static final String API_URL = "http://172.26.50.55:9090/license-app/v1/license/10004970100002888X110000/abolish?access_token=cdf4a184-1747-449d-97c1-e9fc6b443252";
    private static final int THREAD_POOL_SIZE = 10;
    private static final String OUTPUT_FILE = "C:\\Users\\潘强\\Desktop\\result.txt";

    public static void main(String[] args) {
        String txtPath = "C:\\Users\\潘强\\Desktop\\djk0427.txt"; // 改为TXT文件路径

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            List<String> idCodes = readidCodesFromTxt(txtPath);
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (String idCode : idCodes) {
                executor.submit(new LicenseTask(idCode, writer));
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class LicenseTask implements Runnable {
        private final String idCode;
        private final BufferedWriter writer;

        public LicenseTask(String idCode, BufferedWriter writer) {
            this.idCode = idCode;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                boolean isSuccess = sendPostRequest(idCode);
                synchronized (writer) {
                    System.out.println("idCode: " + idCode + " -> Success: " + isSuccess);
                    writer.write("idCode: " + idCode + " -> Success: " + isSuccess);
                    writer.newLine();
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean sendPostRequest(String idCode) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);

            String jsonBody = buildRequestBody(idCode);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);
            return response.getStatusLine().getStatusCode() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String buildRequestBody(String idCode) {
        return String.format(
                "{ \"data\": { " +
                        "\"id_code\": \"%s\", " +
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
                idCode
        );
    }

    /**
     * 从TXT文件读取idCode（每行一个）
     */
    private static List<String> readidCodesFromTxt(String filePath) throws IOException {
        List<String> codes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    codes.add(trimmed);
                }
            }
        }
        return codes;
    }
}