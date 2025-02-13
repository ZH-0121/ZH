package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 读取Excel文件中身份证和结离婚证，输出既有结婚证又有离婚证且离婚证最大签发日期大于结婚证最大签发日期的身份证号，
 */
public class ExcelReaderJLHZ_L {

    public static void main(String[] args) {
        // Excel 文件路径
        String filePath = "C:\\Users\\潘强\\Desktop\\license_data.xlsx";

        // 存储身份证号和对应的证照类型及其签发时间
        Map<String, Map<String, List<Date>>> idCardMap = new HashMap<>();

        // 创建日期格式对象，用于解析签发时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

                // 读取第三列（签发时间）
                Cell issueDateCell = row.getCell(2);
                Date issueDate = null;
                try {
                    issueDate = issueDateCell.getDateCellValue();
                } catch (Exception e) {
                    // 如果日期格式不正确，尝试使用字符串解析
                    try {
                        issueDate = dateFormat.parse(issueDateCell.getStringCellValue());
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                }

                // 将身份证号和证照类型及签发时间存储到 Map 中
                idCardMap.computeIfAbsent(idCard, k -> new HashMap<>())
                        .computeIfAbsent(type, k -> new ArrayList<>())
                        .add(issueDate);
            }

            // 筛选既有结婚证又有离婚证且离婚证最大签发日期大于结婚证最大签发日期的身份证号
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<Date>>> entry : idCardMap.entrySet()) {
                Map<String, List<Date>> certificateMap = entry.getValue();

                // 确保包含结婚证和离婚证
                if (certificateMap.containsKey("中华人民共和国结婚证") && certificateMap.containsKey("中华人民共和国离婚证")) {
                    List<Date> marriageDates = certificateMap.get("中华人民共和国结婚证");
                    List<Date> divorceDates = certificateMap.get("中华人民共和国离婚证");

                    // 找到结婚证的最大签发日期
                    Date maxMarriageDate = Collections.max(marriageDates);

                    // 找到离婚证的最大签发日期
                    Date maxDivorceDate = Collections.max(divorceDates);

                    // 检查离婚证的最大签发日期是否大于结婚证的最大签发日期
                    if (maxDivorceDate.after(maxMarriageDate)) {
                        result.add(entry.getKey());
                    }
                }
            }

            // 输出结果到控制台
            System.out.println("既有结婚证又有离婚证且离婚证最大签发日期大于结婚证最大签发日期的身份证号：");
            for (String idCard : result) {
                System.out.println(idCard);
            }

            // 将结果保存到 txt 文件
            saveResultToFile(result, "C:\\Users\\潘强\\Desktop\\JLHZL.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将结果保存到 txt 文件
     *
     * @param result   结果列表
     * @param filePath 保存文件的路径
     */
    private static void saveResultToFile(List<String> result, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("既有结婚证又有离婚证且离婚证最大签发日期大于结婚证最大签发日期的身份证号：\n");
            for (String idCard : result) {
                writer.write(idCard + "\n");
            }
            System.out.println("结果已保存到文件：" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}