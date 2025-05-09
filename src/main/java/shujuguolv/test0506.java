package shujuguolv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test0506 {
    public static void main(String[] args) {
        String filePath = "your_file.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("废止证件号码：")) {
                    String idNumber = extractIdNumber(line);
                    String ackCode = extractAckCode(line);
                    if ("FAILURE".equals(ackCode)) {
                        String errorCode = extractErrorCode(line);
                        System.out.println("证件号码: " + idNumber + ", 错误代码: " + errorCode);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
        }
    }

    private static String extractIdNumber(String line) {
        Pattern pattern = Pattern.compile("废止证件号码：(\\d+[Xx]?)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String extractAckCode(String line) {
        Pattern pattern = Pattern.compile("\"ack_code\":\"(\\w+)\"");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String extractErrorCode(String line) {
        Pattern pattern = Pattern.compile("\"code\":\"(\\d+)\"");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
