package shujuguolv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Iterator;

/**
 * 读取txt文件，提取 "licenseCode" 并保存到新的 txt 文件中
 */
public class TxtReader {

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

    // 将结果写入到 txt 文件
    public static void writeLicenseCodeToFile(String outputFilePath, String licenseCode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(licenseCode);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 设置输入文件路径和输出文件路径
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\data.txt"; // 请根据实际文件路径修改
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\license_codes.txt"; // 输出文件路径

        // 读取文件并获取文件内容
        String jsonString = readFileAndOutput(inputFilePath);
        if (jsonString.isEmpty()) {
            System.err.println("文件内容为空或读取失败，程序终止");
            return;  // 如果读取失败，终止程序
        }

        // 解析 JSON 数据
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode hitsNode = rootNode.path("hits").path("hits");

            // 遍历 JSON 数据并提取 licenseCode
            Iterator<JsonNode> elements = hitsNode.elements();
            StringBuilder licenseCodes = new StringBuilder(); // 用于保存所有 licenseCode

            while (elements.hasNext()) {
                JsonNode hit = elements.next();
                JsonNode sourceNode = hit.path("_source");

                // 提取 licenseCode 并追加到 StringBuilder
                String licenseCode = sourceNode.path("licenseCode").asText();
                if (!licenseCode.isEmpty()) {
                    licenseCodes.append(licenseCode).append("\n");
                }
            }

            // 将提取的 licenseCode 写入到文件
            writeLicenseCodeToFile(outputFilePath, licenseCodes.toString());

            System.out.println("License Codes 已成功保存到 " + outputFilePath);
        } catch (IOException e) {
            System.err.println("解析 JSON 数据时发生错误: " + e.getMessage());
        }
    }
}
