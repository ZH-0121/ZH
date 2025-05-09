package shujuguolv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFieldExtractor {
        public static void main(String[] args) {
                String filePath = "C:\\Users\\潘强\\Desktop\\output_6.txt"; // 替换为实际文件路径

                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                        String line;
                        // 正则表达式匹配 ZZMC、CYRSFZJHM、ZZHM 字段
                        // 示例匹配逻辑：
                        // 1. 匹配 license_zz 表的插入或更新语句中的字段
                        // 2. 提取 NAME（ZZMC）、HOLDER_IDENTITY_NUM（CYRSFZJHM）、LICENSE_CODE（ZZHM）
                        Pattern pattern = Pattern.compile(
                                "(?i)(?<=`ZZMC` = \\?).*?(?=,)|" + // ZZMC（名称）
                                        "(?i)(?<=`CYRSFZJHM` = \\?).*?(?=,)|" + // CYRSFZJHM（身份证号）
                                        "(?i)(?<=`ZZHM` = \\?).*?(?=,|')" // ZZHM（证照号码）
                        );

                        while ((line = reader.readLine()) != null) {
                                Matcher matcher = pattern.matcher(line);
                                while (matcher.find()) {
                                        String value = matcher.group().trim();
                                        // 去除前后的引号或特殊字符
                                        value = value.replace("'", "").replace("?", "").trim();
                                        if (value.contains(",")) {
                                                // 按逗号分割多个值（如有）
                                                String[] parts = value.split(",");
                                                for (String part : parts) {
                                                        printField(part);
                                                }
                                        } else {
                                                printField(value);
                                        }
                                }
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private static void printField(String value) {
                // 简单判断字段类型（根据实际日志调整）
                if (value.matches("^[\\u4e00-\\u9fa5]+")) { // 中文名称（假设 ZZMC 为中文）
                        System.out.println("ZZMC: " + value);
                } else if (value.matches("\\d{17}[\\dXx]")) { // 身份证号码格式校验
                        System.out.println("CYRSFZJHM: " + value);
                } else { // 其他可能为 ZZHM
                        System.out.println("ZZHM: " + value);
                }
        }
}