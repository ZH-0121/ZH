package weibanzhizheng.SWZM;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SWZM {

    // 文件路径配置
    private static final String CSV_FILE_PATH = "C:\\Users\\潘强\\Desktop\\matched_lines_unique.csv";
    private static final String OUTPUT_CSV = "C:\\Users\\潘强\\Desktop\\yizuoshuju.csv";
    private static final String LOG_FILE = "C:\\Users\\潘强\\Desktop\\rizhi.txt";
    private static final String PDF_DIRECTORY = "C:\\Users\\潘强\\Desktop\\PDF\\";
    private static final String API_URL = "http://172.26.50.55:9090/license-app/v1/license/10009161500002888X110000/issue?access_token=";
    private static final String LOGIN_URL = "http://172.26.50.55:9090/license-app/v1/security/login";

    // 单位信息映射表
    private static final Map<String, String[]> UNIT_INFO_MAP = createUnitInfoMap();

    private static Map<String, String[]> createUnitInfoMap() {
        Map<String, String[]> map = new HashMap<>();
        map.put("0803", new String[]{"北京市公安局公安交通管理局事故处", "191100000000269136"});
        map.put("4201", new String[]{"北京市公安局刑事侦查总队八支队", "1111000000002888XF"});
        map.put("1601", new String[]{"北京市公安局海淀分局刑事侦查支队", "11110000000028935M"});
        map.put("2501", new String[]{"北京市公安局大兴分局刑事侦查支队", "11110000000029006R"});
        map.put("1101", new String[]{"北京市公安局东城分局刑事侦查支队", "11110000000028898F"});
        map.put("2801", new String[]{"北京市公安局平谷分局刑事侦查支队", "11110000000026884K"});
        map.put("1701", new String[]{"北京市公安局丰台分局刑事侦查支队", "11110000000028951B"});
        map.put("2601", new String[]{"北京市公安局顺义分局刑事侦查支队", "11110000000029014L"});
        map.put("3201", new String[]{"北京市公安局密云分局刑事侦查支队", "111100000000294011"});
        map.put("1501", new String[]{"北京市公安局朝阳分局刑事侦查支队", "11110000000028943G"});
        map.put("3101", new String[]{"北京市公安局怀柔分局刑事侦查支队", "111100000000290497"});
        map.put("1201", new String[]{"北京市公安局西城分局刑事侦查支队", "111100000000289003"});
        map.put("1801", new String[]{"北京市公安局石景山分局刑事侦查支队", "1111000000002896X3"});
        map.put("3001", new String[]{"北京市公安局昌平分局刑事侦查支队", "11110000000029030A"});
        map.put("2901", new String[]{"北京市公安局房山分局刑事侦查支队", "11110000000028986X"});
        map.put("2201", new String[]{"北京市公安局门头沟分局刑事侦查支队", "11110000000073592E"});
        map.put("2701", new String[]{"北京市公安局通州分局刑事侦查支队", "11110000000029022F"});
        map.put("3301", new String[]{"北京市公安局延庆分局刑事侦查支队", "111100000000290572"});
        map.put("5801", new String[]{"北京市公安局清河分局刑事侦查大队", "11110000000026892E"});
        map.put("2301", new String[]{"首都机场公安局北京首都国际机场分局刑事侦查支队", "1111000000002888XF"});
        map.put("2401", new String[]{"首都机场公安局北京大兴国际机场分局刑事侦查支队", "1111000000002888XF"});
        map.put("2101", new String[]{"北京市公安局公共交通安全保卫总队刑侦支队", "1111000000002888XF"});
        map.put("7002", new String[]{"北京市公安局天安门地区分局", "11110000MB17403771"});
        map.put("5802", new String[]{"北京市公安局清河分局", "11110000000026892E"});
        map.put("2102", new String[]{"首都机场公安局北京首都国际机场分局", "1111000000002888XF"});
        map.put("2402", new String[]{"首都机场公安局北京大兴国际机场分局", "1111000000002888XF"});
        map.put("1102", new String[]{"北京市公安局东城分局", "11110000000028898F"});
        map.put("1202", new String[]{"北京市公安局西城分局", "111100000000289003"});
        map.put("1502", new String[]{"北京市公安局朝阳分局", "11110000000028943G"});
        map.put("1602", new String[]{"北京市公安局海淀分局", "11110000000028935M"});
        map.put("1702", new String[]{"北京市公安局丰台分局", "11110000000028951B"});
        map.put("1802", new String[]{"北京市公安局石景山分局", "1111000000002896X3"});
        map.put("2202", new String[]{"北京市公安局门头沟分局", "11110000000073592E"});
        map.put("2902", new String[]{"北京市公安局房山分局", "11110000000028986X"});
        map.put("2702", new String[]{"北京市公安局通州分局", "11110000000029022F"});
        map.put("2602", new String[]{"北京市公安局顺义分局", "11110000000029014L"});
        map.put("3002", new String[]{"北京市公安局昌平分局", "11110000000029030A"});
        map.put("2502", new String[]{"北京市公安局大兴分局", "11110000000029006R"});
        map.put("3102", new String[]{"北京市公安局怀柔分局", "111100000000290497"});
        map.put("2802", new String[]{"北京市公安局平谷分局", "11110000000026884K"});
        map.put("3202", new String[]{"北京市公安局密云分局", "111100000000294011"});
        map.put("3302", new String[]{"北京市公安局延庆分局", "111100000000290572"});
        return map;
    }

    private static PrintWriter logWriter;
    private static BufferedWriter csvWriter;
    private static String access_token;

    public static void main(String[] args) {
        initialize();
        try {
            access_token = login();
            processData();
            log("✅ 所有数据处理完成！");
        } catch (Exception e) {
            logError("程序执行异常", e);
        } finally {
            closeResources();
        }
    }

    private static void initialize() {
        try {
            logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(LOG_FILE), StandardCharsets.UTF_8));
            csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_CSV), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("初始化文件输出流失败: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String login() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

        JSONObject credentials = new JSONObject();
        credentials.put("password", "Zhanghao0121");
        credentials.put("app_key", "zVBPicdfPBAc4kTRPYaRANRryUpTI9II");
        credentials.put("app_secret", "97dcbe65763315d0bd17869a3f5e7396");
        credentials.put("org_code", "org_code");
        credentials.put("account", "zhanghao");


        try (OutputStream os = connection.getOutputStream()) {
            os.write(credentials.toJSONString().getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("登录失败，HTTP状态码: " + connection.getResponseCode());
        }

        try (InputStream is = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 这里应该使用JSON解析获取token，简化示例中使用子字符串
            return extractToken(response.toString());
        }
    }

    private static String extractToken(String response) {
        // 实际应用中应该使用JSON解析获取token
        // 这里简化处理，仅作示例
        int start = response.indexOf("\"access_token\":\"") + 16;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    private static void processData() throws Exception {
        List<String> lines = readFileWithAutoEncoding(CSV_FILE_PATH);
        List<Map<String, String>> records = parseCSV(lines);
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < records.size(); i++) {
            Map<String, String> record = records.get(i);
            String originalLine = lines.get(i);

            try {
                String requestBody = buildRequestBody(record);
                log("处理记录 " + (i + 1) + "/" + records.size() + ": " + record.get("CYRMC"));

                String response = callApi(API_URL + access_token, requestBody);
                boolean success = isSuccessResponse(mapper, response);

                writeResult(originalLine, success);
                log("处理结果: " + (success ? "成功" : "失败"));
            } catch (Exception e) {
                logError("处理记录时出错", e);
                writeResult(originalLine, false);
            }
        }
    }

    private static List<String> readFileWithAutoEncoding(String filePath) throws IOException {
        Charset[] encodings = {StandardCharsets.UTF_8, Charset.forName("GBK")};
        Exception lastException = null;

        for (Charset encoding : encodings) {
            try {
                return Files.readAllLines(Paths.get(filePath), encoding);
            } catch (Exception e) {
                lastException = e;
            }
        }

        throw new IOException("无法确定文件编码: " + filePath, lastException);
    }

    private static List<Map<String, String>> parseCSV(List<String> lines) {
        List<Map<String, String>> records = new ArrayList<>();
        for (String line : lines) {
            String[] values = line.split(",", -1);
            if (values.length >= 9) {
                Map<String, String> record = new HashMap<>();
                record.put("CYRMC", values[1].trim());
                record.put("CYRSFZJLX", parseIdType(values[2].trim()));
                record.put("CYRSFZJHM", values[3].trim());
                record.put("SZXB", parseGender(values[4].trim()));
                record.put("SWHFXRQ", formatDate(values[5].trim()));
                record.put("SWYY", values[6].trim());
                record.put("ZZHM", values[8].trim());
                records.add(record);
            } else {
                log("跳过格式错误的行: " + line);
            }
        }
        return records;
    }

    private static String parseIdType(String type) {
        return "身份证".equals(type) ? "10" : type;
    }

    private static String parseGender(String code) {
        switch (code) {
            case "1": return "男";
            case "2": return "女";
            default: return code;
        }
    }

    private static String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private static String buildRequestBody(Map<String, String> record) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> dataFields = new HashMap<>();

        String[] unitInfo = getUnitInfoByZZHM(record.get("ZZHM"));

        // 基本信息
        dataFields.put("ZZMC", "死亡证明");
        dataFields.put("ZZHM", record.get("ZZHM"));
        dataFields.put("CYRMC", record.get("CYRMC"));
        dataFields.put("CYRSFZJLX", record.get("CYRSFZJLX"));
        dataFields.put("CYRSFZJHM", record.get("CYRSFZJHM"));
        dataFields.put("FZJGMC", unitInfo[0]);
        dataFields.put("FZJGZZJGDM", unitInfo[1]);
        dataFields.put("FZJGSSXZQHDM", "110000");
        dataFields.put("FZRQ", parseDateFromZZHM(record.get("ZZHM")));
        dataFields.put("YXQJSRQ", "");
        dataFields.put("SZXB", record.get("SZXB"));
        dataFields.put("SWHFXRQ", record.get("SWHFXRQ"));
        dataFields.put("SWYY", record.get("SWYY"));

        // 附件信息
        data.put("attachments", Collections.singletonList(createAttachment(record.get("CYRSFZJHM"))));
        data.put("data_fields", dataFields);
        data.put("operator", createOperator());
        data.put("service_item_name", "CA测试事项");
        data.put("seal_code", "DZYZ00002888XjRCheT");
        data.put("license_group", "组别1");
        data.put("sign_attach", true);
        data.put("service_item_code", "12345676543211234566");

        requestBody.put("data", data);
        return new ObjectMapper().writeValueAsString(requestBody);
    }

    private static String parseDateFromZZHM(String zzhm) {
        if (zzhm != null && zzhm.length() >= 12) {
            try {
                String dateStr = zzhm.substring(4, 12);
                SimpleDateFormat input = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
                return output.format(input.parse(dateStr));
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    private static Map<String, Object> createAttachment(String idCard) throws IOException {
        String pdfPath = PDF_DIRECTORY + idCard + ".pdf";
        byte[] fileContent = Files.readAllBytes(Paths.get(pdfPath));
        String base64Pdf = Base64.getEncoder().encodeToString(fileContent);

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("is_show_template", true);
        attachment.put("is_license_image", true);
        attachment.put("file_type", "pdf");
        attachment.put("name", "测试");
        attachment.put("description", "已盖章证照");
        attachment.put("file_data", base64Pdf);
        return attachment;
    }

    private static Map<String, String> createOperator() {
        Map<String, String> operator = new HashMap<>();
        operator.put("division", "北京市");
        operator.put("role", "管理员");
        operator.put("identity_num", "372925200001211933");
        operator.put("name", "张浩");
        operator.put("account", "bjswhlvj");
        operator.put("service_org", "11110000MB16786141");
        operator.put("division_code", "110000");
        return operator;
    }

    private static String[] getUnitInfoByZZHM(String zzhm) {
        String prefix = (zzhm != null && zzhm.length() >= 4) ? zzhm.substring(0, 4) : "0803";
        return UNIT_INFO_MAP.getOrDefault(prefix,
                new String[]{"北京市公安局公安交通管理局事故处", "191100000000269136"});
    }

    private static String callApi(String apiUrl, String requestBody) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            connection.getResponseCode() >= 400 ?
                                    connection.getErrorStream() :
                                    connection.getInputStream(),
                            StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static boolean isSuccessResponse(ObjectMapper mapper, String response) throws IOException {
        JsonNode root = mapper.readTree(response);
        JsonNode ackCode = root.path("ack_code");
        return ackCode.asText("").equals("SUCCESS");
    }

    private static void writeResult(String originalLine, boolean success) throws IOException {
        csvWriter.write(originalLine + "," + (success ? "成功" : "失败"));
        csvWriter.newLine();
        csvWriter.flush();
    }

    private static void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        System.out.println(logMessage);
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }

    private static void logError(String message, Exception e) {
        log(message + ": " + e.getMessage());
        e.printStackTrace();
        if (logWriter != null) {
            e.printStackTrace(logWriter);
            logWriter.flush();
        }
    }

    private static void closeResources() {
        try {
            if (logWriter != null) {
                logWriter.close();
            }
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            System.err.println("关闭资源时出错: " + e.getMessage());
        }
    }
}