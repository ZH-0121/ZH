package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelToSQL1 {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\sql.xlsx"; // 请替换为实际的 Excel 文件路径
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            int implementCodeIndex = -1;
            int sealsIndex = -1;
            int convergeTypeIndex = -1;
            int appIndex = -1;

            // 查找各列的索引
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String cellValue = cell.getStringCellValue();
                switch (cellValue) {
                    case "implement_code":
                        implementCodeIndex = i;
                        break;
                    case "seals":
                        sealsIndex = i;
                        break;
                    case "converge_type":
                        convergeTypeIndex = i;
                        break;
                    case "app":
                        appIndex = i;
                        break;
                }
            }

            // 遍历数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String implementCode = getCellValueAsString(row.getCell(implementCodeIndex));
                String seals = getCellValueAsString(row.getCell(sealsIndex));
                String convergeType = getCellValueAsString(row.getCell(convergeTypeIndex));
                String app = getCellValueAsString(row.getCell(appIndex));

                // 存储非空的修改参数
                List<String> setParams = new ArrayList<>();
                if (!seals.isEmpty()) {
                    setParams.add("`seals` = '" + seals + "'");
                }
                if (!convergeType.isEmpty()) {
                    setParams.add("`converge_type` = '" + convergeType + "'");
                }
                if (!app.isEmpty()) {
                    setParams.add("`app` = '" + app + "'");
                }

                // 生成 SQL 语句
                if (!setParams.isEmpty()) {
                    String setClause = String.join(", ", setParams);
                    String sql = String.format("UPDATE `license_directory_statistic` SET %s WHERE implement_code = '%s';",
                            setClause, implementCode);
                    System.out.println(sql);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}