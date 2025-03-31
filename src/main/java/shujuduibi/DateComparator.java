package shujuduibi;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class DateComparator {
    public static void main(String[] args) {
        // 文件路径配置
        String file1Path = "txt1.txt";
        String file2Path = "txt2.txt";
        String outputPath = "result.txt";

        // 时间格式配置（根据实际情况修改）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 字段分隔符配置
        String delimiter = ",";

        try {
            // 读取文件数据
            Map<String, LocalDate> data1 = readDataFile(file1Path, formatter, delimiter);
            Map<String, LocalDate> data2 = readDataFile(file2Path, formatter, delimiter);

            // 比较数据并生成结果
            List<String> results = new ArrayList<>();
            for (Map.Entry<String, LocalDate> entry : data1.entrySet()) {
                String id = entry.getKey();
                LocalDate date1 = entry.getValue();

                if (data2.containsKey(id)) {
                    LocalDate date2 = data2.get(id);
                    if (date1.isBefore(date2)) {
                        results.add(id + delimiter + date1.format(formatter));
                    }
                }
            }

            // 输出结果到文件
            writeResults(outputPath, results);
            System.out.println("处理完成，共找到 " + results.size() + " 条结果，已保存至：" + outputPath);

        } catch (IOException e) {
            System.err.println("文件处理错误：" + e.getMessage());
        }
    }

    private static Map<String, LocalDate> readDataFile(String filePath,
                                                       DateTimeFormatter formatter,
                                                       String delimiter) throws IOException {
        Map<String, LocalDate> dataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(delimiter);
                if (parts.length < 2) {
                    System.err.println("文件 " + filePath + " 第 " + lineNumber + " 行格式错误，已跳过");
                    continue;
                }

                String id = parts[0].trim();
                String dateStr = parts[1].trim();

                try {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    // 处理重复ID：后出现的记录覆盖先前的
                    dataMap.put(id, date);
                } catch (DateTimeParseException e) {
                    System.err.println("文件 " + filePath + " 第 " + lineNumber + " 行日期格式错误：" + dateStr);
                }
            }
        }
        return dataMap;
    }

    private static void writeResults(String outputPath, List<String> results) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            for (String line : results) {
                bw.write(line);
                bw.newLine();
            }
        }
    }
}