package shujuguolv;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {
    public static final long MAX_FILE_SIZE = 90 * 1024 * 1024; // 100MB

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\all.2025-04-22.79.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            int fileIndex = 1;
            long currentFileSize = 0;
            BufferedWriter writer = null;

            while ((line = reader.readLine()) != null) {
                if (writer == null || currentFileSize + line.getBytes().length + 1 > MAX_FILE_SIZE) {
                    if (writer != null) {
                        writer.close();
                    }
                    if (fileIndex > 6) {
                        System.err.println("已经生成了 6 个文件，但是源文件内容还未处理完。");
                        break;
                    }
                    String outputFilePath = "C:\\Users\\潘强\\Desktop\\output_" + fileIndex + ".txt";
                    writer = new BufferedWriter(new FileWriter(outputFilePath));
                    currentFileSize = 0;
                    fileIndex++;
                }
                writer.write(line);
                writer.newLine();
                currentFileSize += line.getBytes().length + 1; // 加 1 是因为换行符
            }

            if (writer != null) {
                writer.close();
            }
            System.out.println("文件分割完成。");
        } catch (IOException e) {
            System.err.println("文件操作时出现错误: " + e.getMessage());
        }
    }
}