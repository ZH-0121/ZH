/*import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticsearchConcurrentSearch {

    // 配置参数
    private static final String ELASTICSEARCH_URL = "http://172.25.173.91:9200/test_license/_search";
    private static final String AUTHORIZATION = "Basic ZWxhc3RpYzoxcWF6WkFRIQ==";
    private static final int CONCURRENT_REQUESTS = 1000;
    private static final int TIMEOUT_MS = 50000; // 50秒超时

    // 统计计数器
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("开始并发搜索请求 (并发数: " + CONCURRENT_REQUESTS + ")");
        long startTime = System.currentTimeMillis();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        // 提交任务
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    sendSearchRequest();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("请求失败: " + e);
                    System.err.println("请求失败: " + e.getMessage());
                    failureCount.incrementAndGet();
                }
            });
        }

        // 关闭线程池并等待完成
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 输出统计结果
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n===== 执行完成 =====");
        System.out.println("总请求数: " + CONCURRENT_REQUESTS);
        System.out.println("成功请求: " + successCount.get());
        System.out.println("失败请求: " + failureCount.get());
        System.out.printf("总耗时: %.2f 秒%n", duration / 1000.0);
        System.out.printf("平均每秒请求: %.2f%n",
                CONCURRENT_REQUESTS / (duration / 1000.0));
    }

    private static void sendSearchRequest() throws IOException {
        // 创建HTTP连接
        HttpURLConnection connection = (HttpURLConnection) new URL(ELASTICSEARCH_URL).openConnection();

        try {
            // 配置请求
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", AUTHORIZATION);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 设置请求体
            String jsonBody = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[]}}}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误: " + responseCode);
            }

            // 读取响应内容（可选，这里只读取状态）
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // 如果需要处理响应数据，可以在这里添加代码
            }
        } finally {
            connection.disconnect();
        }
    }
}*/

/*


import java.io.*;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;
        import java.util.concurrent.TimeUnit;
        import java.util.concurrent.atomic.AtomicInteger;

public class ElasticsearchConcurrentSearch {

    // 配置参数
    private static final String ELASTICSEARCH_URL = "http://172.25.173.90:9200/test_license/_search";
    private static final String AUTHORIZATION = "Basic ZWxhc3RpYzoxcWF6WkFRIQ==";
    private static final int CONCURRENT_REQUESTS = 1000;
    private static final int TIMEOUT_MS = 5000; // 5秒超时

    // 统计计数器
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("开始并发搜索请求 (并发数: " + CONCURRENT_REQUESTS + ")");
        long startTime = System.currentTimeMillis();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        // 提交任务
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    String response = sendSearchRequest();  // 接收响应
                    System.out.println("响应示例（前200字符）：" + response.substring(0, Math.min(200, response.length())));  // 打印部分响应
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("请求失败: " + e.getMessage());
                    failureCount.incrementAndGet();
                }
            });
        }

        // 关闭线程池并等待完成
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 输出统计结果
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n===== 执行完成 =====");
        System.out.println("总请求数: " + CONCURRENT_REQUESTS);
        System.out.println("成功请求: " + successCount.get());
        System.out.println("失败请求: " + failureCount.get());
        System.out.printf("总耗时: %.2f 秒%n", duration / 1000.0);
        System.out.printf("平均每秒请求: %.2f%n",
                CONCURRENT_REQUESTS / (duration / 1000.0));
    }

// 发送 Elasticsearch 请求，并返回响应字符串


    private static String sendSearchRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(ELASTICSEARCH_URL).openConnection();

        try {
            // 配置请求
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", AUTHORIZATION);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 设置请求体
            String jsonBody = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[]}}}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误: " + responseCode);
            }

            // 读取响应内容
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            return response.toString();  // 返回完整响应

        } finally {
            connection.disconnect();
        }
    }
}



*/





/*

// 持续请求
import java.io.*;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticsearchConcurrentSearch {

    // 配置参数
    private static final String ELASTICSEARCH_URL = "http://172.25.173.95:9200/test_license/_search";
    private static final String AUTHORIZATION = "Basic ZWxhc3RpYzoxcWF6WkFRIQ==";
    private static final int CONCURRENT_REQUESTS = 800;
    private static final int TIMEOUT_MS = 50000; // 5秒超时
    private static final int DURATION_SECONDS = 60; // 持续时间：60秒

    public static void main(String[] args) {
        System.out.println("开始持续并发搜索请求 (并发数: " + CONCURRENT_REQUESTS + ", 持续时间: " + DURATION_SECONDS + " 秒)");

        long endTime = System.currentTimeMillis() + DURATION_SECONDS * 1000;

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFailures = new AtomicInteger(0);

        while (System.currentTimeMillis() < endTime) {
            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                executor.submit(() -> {
                    try {
                        sendSearchRequest();
                        totalSuccess.incrementAndGet();
                    } catch (Exception e) {
                        totalFailures.incrementAndGet();
                    }
                    totalRequests.incrementAndGet();
                });
            }

            try {
                Thread.sleep(1000); // 每隔 1 秒发起新一轮并发请求
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n===== 压测结束 =====");
        System.out.println("总请求数: " + totalRequests.get());
        System.out.println("成功请求数: " + totalSuccess.get());
        System.out.println("失败请求数: " + totalFailures.get());
        System.out.printf("成功率: %.2f%%\n", (totalSuccess.get() * 100.0) / totalRequests.get());
    }

 //发送 HTTP POST 请求到 Elasticsearch，并读取响应
     

    private static void sendSearchRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(ELASTICSEARCH_URL).openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", AUTHORIZATION);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 设置请求体
            String jsonBody = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[]}}}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误: " + responseCode);
            }

            // 读取响应内容（可选）
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // 可以在这里解析 JSON 或记录日志
            }
        } finally {
            connection.disconnect();
        }
    }
}
*/


//持续10分钟，每10s进行一次
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticsearchConcurrentSearch {

    // 配置参数
    private static final String ELASTICSEARCH_URL = "http://172.25.173.95:9200/test_license/_search";
    private static final String AUTHORIZATION = "Basic ZWxhc3RpYzoxcWF6WkFRIQ==";
    private static final int CONCURRENT_REQUESTS = 400; // 单次并发数
    private static final int TIMEOUT_MS = 10000; // 10秒超时
    private static final int TOTAL_DURATION_MINUTES = 10; // 总运行时间（分钟）
    private static final int INTERVAL_SECONDS = 10; // 请求间隔时间（秒）

    public static void main(String[] args) {
        System.out.println("开始定时并发搜索请求 (并发数: " + CONCURRENT_REQUESTS + ", 每隔 " + INTERVAL_SECONDS + " 秒执行一次)");

        long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TOTAL_DURATION_MINUTES);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFailures = new AtomicInteger(0);

        // 定时任务：每隔 INTERVAL_SECONDS 秒执行一次并发请求
        scheduler.scheduleAtFixedRate(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                executor.submit(() -> {
                    try {
                        sendSearchRequest();
                        totalSuccess.incrementAndGet();
                    } catch (Exception e) {
                        totalFailures.incrementAndGet();
                    }
                    totalRequests.incrementAndGet();
                });
            }

            executor.shutdown();
        }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 等待总时间结束后关闭调度器
        try {
            Thread.sleep(endTime - System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduler.shutdownNow();

        // 输出统计结果
        System.out.println("\n===== 压测结束 =====");
        System.out.println("总请求数: " + totalRequests.get());
        System.out.println("成功请求数: " + totalSuccess.get());
        System.out.println("失败请求数: " + totalFailures.get());
        System.out.printf("成功率: %.2f%%\n", (totalSuccess.get() * 100.0) / Math.max(1, totalRequests.get()));
    }

// 发送 HTTP POST 请求到 Elasticsearch，并读取响应


    private static void sendSearchRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(ELASTICSEARCH_URL).openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", AUTHORIZATION);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 设置请求体
            String jsonBody = "{\"size\":6000,\"query\":{\"bool\":{\"must\":[]}}}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误: " + responseCode);
            }

            // 读取响应内容（可选）
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // 可以在这里解析 JSON 或记录日志
            }
        } finally {
            connection.disconnect();
        }
    }
}





/*






import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticsearchConcurrentSearch {

    // 配置参数
    private static final List<String> ES_URLS = Arrays.asList(
            "http://172.25.173.90:9200/test_license/_search",
            "http://172.25.173.91:9200/test_license/_search",
            "http://172.25.173.95:9200/test_license/_search"
    );
    private static final String AUTHORIZATION = "Basic ZWxhc3RpYzoxcWF6WkFRIQ==";
    private static final int CONCURRENT_REQUESTS = 800; // 单次并发数
    private static final int TIMEOUT_MS = 30000; // 30秒超时
    private static final int TOTAL_DURATION_MINUTES = 10; // 总运行时间（分钟）
    private static final int INTERVAL_SECONDS = 10; // 请求间隔时间（秒）

    public static void main(String[] args) {
        System.out.println("开始定时并发搜索请求 (并发数: " + CONCURRENT_REQUESTS + ", 每隔 " + INTERVAL_SECONDS + " 秒执行一次)");

        long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TOTAL_DURATION_MINUTES);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFailures = new AtomicInteger(0);

        // 定时任务：每隔 INTERVAL_SECONDS 秒执行一次并发请求
        scheduler.scheduleAtFixedRate(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                executor.submit(() -> {
                    try {
                        sendSearchRequest();
                        totalSuccess.incrementAndGet();
                    } catch (Exception e) {
                        totalFailures.incrementAndGet();
                    }
                    totalRequests.incrementAndGet();
                });
            }

            executor.shutdown();
        }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 等待总时间结束后关闭调度器
        try {
            Thread.sleep(endTime - System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduler.shutdownNow();

        // 输出统计结果
        System.out.println("\n===== 压测结束 =====");
        System.out.println("总请求数: " + totalRequests.get());
        System.out.println("成功请求数: " + totalSuccess.get());
        System.out.println("失败请求数: " + totalFailures.get());
        System.out.printf("成功率: %.2f%%\n", (totalSuccess.get() * 100.0) / Math.max(1, totalRequests.get()));
    }

    */
/**
     * 发送 HTTP POST 请求到 Elasticsearch，并读取响应
     * 支持多个 IP，失败时自动尝试下一个
     *//*

    private static void sendSearchRequest() throws Exception {
        IOException lastException = null;

        for (String url : ES_URLS) {
            try {
                sendRequestToUrl(url);
                return; // 成功则退出
            } catch (IOException e) {
                lastException = e;
                System.err.println("访问失败 " + url + ": " + e.getMessage());
            }
        }

        // 所有 IP 都失败
        if (lastException != null) {
            throw lastException;
        } else {
            throw new IOException("未知错误：所有 IP 均不可用");
        }
    }

    */
/**
     * 向指定 URL 发送请求
     *//*

    private static void sendRequestToUrl(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", AUTHORIZATION);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 设置请求体
            String jsonBody = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[]}}}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误: " + responseCode);
            }

            // 读取响应内容（可选）
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // 可以在这里解析 JSON 或记录日志
            }
        } finally {
            connection.disconnect();
        }
    }
}



*/

