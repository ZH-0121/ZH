package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 读取Excel文件中身份证和结离婚证，输出既有结婚证又有离婚证的身份证号
 */
public class ExcelReaderJLHZ {

    public static void main(String[] args) {
        // Excel 文件路径
        String filePath = "C:\\Users\\潘强\\Desktop\\JLHZ.xlsx";

        // 存储身份证号和证照类型的 Map
        Map<String, Set<String>> idCardMap = new HashMap<>();

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            // 创建工作簿对象
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);

            // 遍历每一行
            for (Row row : sheet) {
                // 跳过表头
                if (row.getRowNum() == 0) {
                    continue;
                }

                // 读取第一列（身份证号）
                Cell idCardCell = row.getCell(0);
                String idCard = idCardCell.getStringCellValue();

                // 读取第二列（证照类型）
                Cell typeCell = row.getCell(1);
                String type = typeCell.getStringCellValue();

                // 将身份证号和证照类型存储到 Map 中
                idCardMap.computeIfAbsent(idCard, k -> new HashSet<>()).add(type);
            }

            // 筛选既有结婚证又有离婚证的身份证号
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : idCardMap.entrySet()) {
                if (entry.getValue().contains("中华人民共和国结婚证") && entry.getValue().contains("中华人民共和国离婚证")) {
                    result.add(entry.getKey());
                }
            }

            // 输出结果到控制台
            System.out.println("既有结婚证又有离婚证的身份证号：");
            for (String idCard : result) {
                System.out.println(idCard);
            }

            // 将结果保存到 txt 文件
            saveResultToFile(result, "C:\\Users\\潘强\\Desktop\\result.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将结果保存到 txt 文件
     *
     * @param result 结果列表
     * @param filePath 保存文件的路径
     */
    private static void saveResultToFile(List<String> result, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("既有结婚证又有离婚证的身份证号：\n");
            for (String idCard : result) {
                writer.write(idCard + "\n");
            }
            System.out.println("结果已保存到文件：" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}