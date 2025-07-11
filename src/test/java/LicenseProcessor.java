import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.gson.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
* 根据表格中身份证号和idCode，按照身份证号去走更新接口替换idcode
*
* */


public class LicenseProcessor {
    // 配置常量
    private static final String ACCESS_TOKEN = "f7cbe636-0311-49e0-8474-015f4c22651a";
    private static final String BASE_URL = "http://192.168.136.15:9090/license-app/v1/license/";
    private static final String EXCEL_PATH = "C:\\Users\\潘强\\Desktop\\input.xlsx";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 用于存储身份证号和对应的新ZZHM值
    static class IdCardInfo {
        String idCard;
        String newZzhm;

        IdCardInfo(String idCard, String newZzhm) {
            this.idCard = idCard;
            this.newZzhm = newZzhm;
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. 读取Excel中的身份证号和新ZZHM值
        List<IdCardInfo> idCardInfos = readIdCardsFromExcel(EXCEL_PATH);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            for (IdCardInfo info : idCardInfos) {
                String idCard = info.idCard;
                String newZzhm = info.newZzhm;

                System.out.println("\n============ 开始处理身份证: " + idCard + " ============");
                System.out.println("从Excel读取的新ZZHM值: " + newZzhm);

                // 2. 调用"依职能"接口
                JsonObject authResult = callAuthApi(httpClient, idCard);
                if (authResult == null) continue;

                // 3. 获取auth_code和license_code
                String authCode = authResult.getAsJsonArray("auth_codes").get(0).getAsString();
                JsonArray dataArray = authResult.getAsJsonArray("data");
                if (dataArray == null || dataArray.size() == 0) {
                    System.err.println("⚠️ Auth响应中未找到data数组，身份证: " + idCard);
                    continue;
                }
                String licenseCode = dataArray.get(0).getAsJsonObject().get("license_code").getAsString();
                System.out.println("✅ 获取到授权码: " + authCode + ", 许可证码: " + licenseCode);

                // 4. 调用get_license接口
                JsonObject licenseInfo = callGetLicenseApi(httpClient, authCode);
                if (licenseInfo == null) continue;

                // 5. 处理data_fields，传入新的ZZHM值
                JsonObject dataFields = processDataFields(licenseInfo, newZzhm);
                if (dataFields == null) continue;

                // 6. 调用变更接口
                callChangeApi(httpClient, dataFields, licenseCode);

                // 添加处理间隔避免服务器压力
                Thread.sleep(500);
            }
        }
    }

    // 读取Excel中的身份证号和第二列的新ZZHM值
    private static List<IdCardInfo> readIdCardsFromExcel(String filePath) throws Exception {
        List<IdCardInfo> idCardInfos = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell idCardCell = row.getCell(0);
                Cell newZzhmCell = row.getCell(1); // 第二列

                if (idCardCell == null) continue;

                String idCard;
                if (idCardCell.getCellType() == CellType.STRING) {
                    idCard = idCardCell.getStringCellValue();
                } else if (idCardCell.getCellType() == CellType.NUMERIC) {
                    idCard = String.valueOf((long) idCardCell.getNumericCellValue());
                } else {
                    continue;
                }

                if (idCard.isEmpty()) continue;

                // 处理第二列的新ZZHM值
                String newZzhm = "";
                if (newZzhmCell != null) {
                    if (newZzhmCell.getCellType() == CellType.STRING) {
                        newZzhm = newZzhmCell.getStringCellValue();
                    } else if (newZzhmCell.getCellType() == CellType.NUMERIC) {
                        newZzhm = String.valueOf((long) newZzhmCell.getNumericCellValue());
                    }
                }

                idCardInfos.add(new IdCardInfo(idCard, newZzhm));
            }
        }
        System.out.println("📊 从Excel读取到 " + idCardInfos.size() + " 条记录");
        return idCardInfos;
    }

    // 调用"依职能"接口 - 添加UTF-8编码支持
    private static JsonObject callAuthApi(CloseableHttpClient httpClient, String idCard) throws Exception {
        String endpoint = "auth";
        HttpPost post = new HttpPost(BASE_URL + endpoint + "?access_token=" + ACCESS_TOKEN);
        post.setHeader("Content-Type", "application/json; charset=utf-8");

        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("identity_number", idCard);
        requestBody.addProperty("service_item_code", "111130345013002717420243");
        requestBody.addProperty("service_item_name", "收养子女在京入户");
        requestBody.addProperty("page_size", 150);
        requestBody.addProperty("page_index", 1);
        requestBody.addProperty("request_id", UUID.randomUUID().toString());

        JsonObject operator = new JsonObject();
        operator.addProperty("account", "1");
        operator.addProperty("identity_num", idCard);
        operator.addProperty("role", "csjs");
        operator.addProperty("name", "测试");
        operator.addProperty("division", "北京市");
        operator.addProperty("division_code", "110000");
        operator.addProperty("service_org", "北京市公安局平谷分局");
        operator.addProperty("service_org_code", "11110000000026884K");
        requestBody.add("operator", operator);

        String requestJson = requestBody.toString();
        // 明确指定UTF-8编码
        post.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
        System.out.println("\n🔍 调用依职能接口 (" + endpoint + ")");
        System.out.println("请求体: " + requestJson);

        HttpResponse response = httpClient.execute(post);
        return parseResponse(response, "依职能接口");
    }

    // 调用get_license接口
    private static JsonObject callGetLicenseApi(CloseableHttpClient httpClient, String authCode) throws Exception {
        String endpoint = "get_license";
        HttpGet get = new HttpGet(BASE_URL + endpoint + "?auth_code=" + authCode + "&access_token=" + ACCESS_TOKEN);
        System.out.println("\n🔍 调用获取许可证接口 (" + endpoint + ")");
        System.out.println("请求URL: " + get.getURI());

        HttpResponse response = httpClient.execute(get);
        return parseResponse(response, "获取许可证接口");
    }

    // 处理data_fields（增强单引号处理） - 添加newZzhm参数
    private static JsonObject processDataFields(JsonObject licenseInfo, String newZzhm) {
        System.out.println("\n🛠️ 开始处理data_fields");

        // 打印完整的许可证信息
        System.out.println("完整的许可证响应:");
        System.out.println(gson.toJson(licenseInfo));

        JsonElement dataFieldsElem = licenseInfo.getAsJsonObject("data").get("data_fields");
        if (dataFieldsElem == null || dataFieldsElem.isJsonNull()) {
            System.err.println("❌ data_fields缺失或为空");
            return null;
        }

        String dataFieldsStr = dataFieldsElem.getAsString();
        System.out.println("原始data_fields字符串: " + dataFieldsStr);

        // 处理单引号情况
        if (dataFieldsStr.startsWith("'") && dataFieldsStr.endsWith("'")) {
            // 替换外部单引号为双引号
            dataFieldsStr = "\"" + dataFieldsStr.substring(1, dataFieldsStr.length() - 1) + "\"";
        }

        // 处理转义字符和引号
        dataFieldsStr = dataFieldsStr
                .replace("\\", "")  // 移除转义字符
                .replace("\"{", "{")  // 移除开头的双引号
                .replace("}\"", "}")  // 移除结尾的双引号
                .replace("'", "\"");  // 替换内部单引号为双引号

        System.out.println("处理后的data_fields字符串: " + dataFieldsStr);

        try {
            // 转换为JsonObject
            JsonObject dataFields = gson.fromJson(dataFieldsStr, JsonObject.class);

            // 打印解析后的JSON结构
            System.out.println("解析后的data_fields JSON:");
            System.out.println(gson.toJson(dataFields));

            // 修改ZZHM字段 - 使用Excel中的新值
            if (dataFields.has("ZZHM")) {
                String originalZzhm = dataFields.get("ZZHM").getAsString();
                dataFields.addProperty("ZZHM", newZzhm);
                System.out.println("✅ 修改ZZHM字段: " + originalZzhm + " -> " + newZzhm);
            } else {
                System.err.println("⚠️ data_fields中未找到ZZHM字段");
                // 如果未找到ZZHM字段，但Excel提供了新值，则添加新字段
                if (!newZzhm.isEmpty()) {
                    dataFields.addProperty("ZZHM", newZzhm);
                    System.out.println("➕ 添加ZZHM字段: " + newZzhm);
                }
            }

            return dataFields;
        } catch (JsonSyntaxException e) {
            System.err.println("❌ 解析data_fields失败: " + e.getMessage());
            System.err.println("❌ 有问题的JSON: " + dataFieldsStr);
            return null;
        }
    }

    // 调用变更接口 - 添加UTF-8编码支持
    private static void callChangeApi(CloseableHttpClient httpClient, JsonObject dataFields, String licenseCode) throws Exception {
        String endpoint = "10000760100002888X110000/change";
        HttpPost post = new HttpPost(BASE_URL + endpoint + "?access_token=" + ACCESS_TOKEN);
        post.setHeader("Content-Type", "application/json; charset=utf-8");

        // 构建请求体
        JsonObject data = new JsonObject();
        data.add("data_fields", dataFields);
        data.addProperty("license_code", licenseCode);
        data.addProperty("license_group", "组别1");

        JsonObject operator = new JsonObject();
        operator.addProperty("account", "bjstyjgwxxtyxm");
        operator.addProperty("division", "六里桥");
        operator.addProperty("division_code", "110000");
        operator.addProperty("identity_num", "372925200001211933");
        operator.addProperty("name", "张浩");
        operator.addProperty("role", "bjca");
        operator.addProperty("service_org", "bjca");
        data.add("operator", operator);

        data.addProperty("seal_code", "DZYZ00002888XWJAgjY");
        data.addProperty("service_item_code", "12345676543211234566");
        data.addProperty("service_item_name", "CA测试事项");

        JsonObject requestBody = new JsonObject();
        requestBody.add("data", data);

        String requestJson = gson.toJson(requestBody);
        // 明确指定UTF-8编码
        post.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));

        System.out.println("\n🔧 调用变更接口 (" + endpoint + ")");
        System.out.println("请求体: " + requestJson);

        HttpResponse response = httpClient.execute(post);
        JsonObject result = parseResponse(response, "变更接口");
        if (result != null) {
            System.out.println("✅ 变更结果状态码: " + result.get("ack_code").getAsString());
        }
    }

    // 解析HTTP响应（增强版） - 添加UTF-8编码支持
    private static JsonObject parseResponse(HttpResponse response, String apiName) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        // 明确指定UTF-8编码
        String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

        System.out.println("\n==================== " + apiName + " 响应 ====================");
        System.out.println("HTTP状态码: " + statusCode);
        System.out.println("响应体:");

        try {
            // 尝试美化打印JSON
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            System.out.println(gson.toJson(jsonElement));

            if (statusCode != 200) {
                System.err.println("❌ " + apiName + " 调用失败，状态码: " + statusCode);
                return null;
            }

            return jsonElement.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            // 如果不是JSON格式，直接打印原始响应
            System.out.println(responseBody);

            if (statusCode != 200) {
                System.err.println("❌ " + apiName + " 调用失败，状态码: " + statusCode);
            } else {
                System.err.println("⚠️ " + apiName + " 响应不是有效的JSON格式");
            }
            return null;
        }
    }
}