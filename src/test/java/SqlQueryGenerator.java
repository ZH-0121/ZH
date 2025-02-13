import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // 用于处理 .xls 文件
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // 用于处理 .xlsx 文件

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlQueryGenerator {

    public static void main(String[] args) throws IOException {
        // 读取 Excel 文件并提取表名
        List<String> tableNames = readTableNamesFromExcel("C:\\Users\\潘强\\Desktop\\76.xls"); // 注意路径和文件类型

        // 调用方法生成 SQL 查询
        String sqlQuery = generateSqlQuery(tableNames);

        // 输出最终生成的 SQL 查询
        System.out.println(sqlQuery);
    }

    // 从 Excel 文件中读取表名（假设表名在第一列）
    public static List<String> readTableNamesFromExcel(String filePath) throws IOException {
        List<String> tableNames = new ArrayList<>();

        // 打开 Excel 文件
        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook;

        // 判断文件类型，选择对应的 POI 类
        if (filePath.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(fis); // 处理 .xlsx 文件
        } else if (filePath.endsWith(".xls")) {
            workbook = new HSSFWorkbook(fis); // 处理 .xls 文件
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }

        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);

        // 遍历每一行，提取第一列（表名）
        for (Row row : sheet) {
            // 获取第一列的值
            Cell cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                tableNames.add(cell.getStringCellValue());
            }
        }

        // 关闭文件输入流
        fis.close();

        return tableNames;
    }

    // 生成 SQL 查询的方法
    public static String generateSqlQuery(List<String> tableNames) {
        StringBuilder sqlBuilder = new StringBuilder();

        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            // 生成每个表的查询语句
            String query = String.format("SELECT '%s' AS `Table`, COUNT(1) AS `Row Count` FROM %s", tableName, tableName);

            // 如果不是最后一条查询，添加 UNION ALL
            if (i < tableNames.size() - 1) {
                query += " UNION ALL";
            }

            // 将查询语句添加到 StringBuilder
            sqlBuilder.append(query).append("\n");
        }

        return sqlBuilder.toString();
    }
}
