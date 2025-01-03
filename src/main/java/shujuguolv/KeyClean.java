package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;

public class KeyClean {

    public static void main(String[] args) {
        // 假设你的 JSON 数据存在于一个文件中
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\key.txt"; // 替换为你的 JSON 文件路径
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\key.txt"; // 输出文件的路径
        String jsonString = readFileAndOutput(inputFilePath);  // 读取文件的内容
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;
        }

        // 解析 JSON 数据并将结果写入到文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // 处理 aggregations 数据
            JsonNode aggregationsNode = rootNode.path("aggregations").path("group_by_idcard").path("buckets");

            // 如果 buckets 存在并且不为空
            if (aggregationsNode.isArray() && aggregationsNode.size() > 0) {
                for (JsonNode bucket : aggregationsNode) {
                    String key = bucket.path("key").asText();  // 获取身份证号码
                    //   int docCount = bucket.path("doc_count").asInt();  // 获取文档计数
                    //   double idCodeCount = bucket.path("idCode_count").path("value").asDouble();  // 获取 ID 代码计数

                    // 写入结果到输出文件
                    writer.write("\"" + key + "\",");  // 将 key 以双引号包裹
                    writer.newLine();  // 换行
                }
            } else {
                writer.write("没有找到有效的 'aggregations' 数据！");
                writer.newLine();
            }

            System.out.println("结果已成功写入到 " + outputFilePath);
        } catch (IOException e) {
            System.err.println("解析 JSON 数据或写入文件时发生错误: " + e.getMessage());
        }
    }

    // 读取文件内容的辅助方法
    public static String readFileAndOutput(String filePath) {
        // 读取文件并返回文件内容的字符串，具体实现依赖于你实际的文件读取方式
        StringBuilder content = new StringBuilder();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return content.toString();
    }
}
