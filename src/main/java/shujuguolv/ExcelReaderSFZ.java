package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;

/**
 * 读取txt文件，提取 "holderIdentityNum" 并保存在新的 txt 文件中
 */
public class ExcelReaderSFZ {

    // 读取文件内容并返回字符串
    public static String readFileAndOutput(String filePath) {
        StringBuilder content = new StringBuilder();
        // 创建 BufferedReader 来读取文件内容
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 循环读取每一行，直到文件结束
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n"); // 读取每行并追加到内容字符串
            }
        } catch (IOException e) {
            // 捕获异常并打印错误信息
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        return content.toString(); // 返回文件的内容
    }

    // 将提取的holderIdentityNum写入新文件
    public static void writeToFile(String outputFilePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 设置文件路径
        String filePath = "C:\\Users\\潘强\\Desktop\\data.txt"; // 请根据实际文件路径修改
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\holderIdentityNum_output.txt"; // 输出文件路径

        // 读取文件并获取文件内容
        String jsonString = readFileAndOutput(filePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;  // 如果读取失败，终止程序
        }

        // 解析 JSON 数据
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode hitsNode = rootNode.path("hits").path("hits");

            // 创建一个 StringBuilder 用于保存 "holderIdentityNum" 值
            StringBuilder outputContent = new StringBuilder();

            // 遍历 JSON 数据并提取 "holderIdentityNum"
            Iterator<JsonNode> elements = hitsNode.elements();
            while (elements.hasNext()) {
                JsonNode hit = elements.next();
                JsonNode sourceNode = hit.path("_source");

                // "holderIdentityNum" 是一个数组，这里取第一个元素
                JsonNode holderIdentityNumNode = sourceNode.path("holderIdentityNum");
                if (holderIdentityNumNode.isArray() && holderIdentityNumNode.size() > 0) {
                    outputContent.append(holderIdentityNumNode.get(0).asText()).append("\n");
                }
            }

            // 将提取的内容写入新的 TXT 文件
            writeToFile(outputFilePath, outputContent.toString());

            System.out.println("TXT 文件生成成功！");
        } catch (IOException e) {
            System.err.println("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }
}
