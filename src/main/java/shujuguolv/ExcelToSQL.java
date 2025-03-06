package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelToSQL {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\潘强\\Desktop\\sql.xlsx";
        try {
            FileInputStream file = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            int basicCodeIndex = -1;
            int convergeDataIndex = -1;
            int isConvergeCompleteIndex = -1;
            int isOriginalIndex = -1;
            int isHaveNationStandardIndex = -1;
            int isNationalStandardIndex = -1;
            int isNationalConvergeIndex = -1;
            int sealtypeIndex = -1;
            int convergeConditionIndex = -1;
            int isAppViewIndex = -1;
            int noUseReasonIndex = -1;

            // 查找各列的索引
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String cellValue = cell.getStringCellValue();
                switch (cellValue) {
                    case "basic_code":
                        basicCodeIndex = i;
                        break;
                    case "converge_data":
                        convergeDataIndex = i;
                        break;
                    case "is_converge_complete":
                        isConvergeCompleteIndex = i;
                        break;
                    case "is_original":
                        isOriginalIndex = i;
                        break;
                    case "is_have_nation_standard":
                        isHaveNationStandardIndex = i;
                        break;
                    case "is_national_standard":
                        isNationalStandardIndex = i;
                        break;
                    case "is_national_converge":
                        isNationalConvergeIndex = i;
                        break;
                    case "seal_type":
                        sealtypeIndex = i;
                        break;
                    case "converge_condition":
                        convergeConditionIndex = i;
                        break;
                    case "is_app_view":
                        isAppViewIndex = i;
                        break;
                    case "no_use_reason":
                        noUseReasonIndex = i;
                        break;
                }
            }

            // 遍历数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                String basicCode = getCellValueAsString(row.getCell(basicCodeIndex));
                String convergeData = getCellValueAsString(row.getCell(convergeDataIndex));
                String isConvergeComplete = getCellValueAsString(row.getCell(isConvergeCompleteIndex));
                String isOriginal = getCellValueAsString(row.getCell(isOriginalIndex));
                String isHaveNationStandard = getCellValueAsString(row.getCell(isHaveNationStandardIndex));
                String isNationalStandard = getCellValueAsString(row.getCell(isNationalStandardIndex));
                String isNationalConverge = getCellValueAsString(row.getCell(isNationalConvergeIndex));
                String sealtype = getCellValueAsString(row.getCell(sealtypeIndex));
                String convergeCondition = getCellValueAsString(row.getCell(convergeConditionIndex));
                String isAppView = getCellValueAsString(row.getCell(isAppViewIndex));
                String noUseReason = getCellValueAsString(row.getCell(noUseReasonIndex));

                // 存储非空的修改参数
                List<String> setParams = new ArrayList<>();
                if (!convergeData.isEmpty()) {
                    setParams.add("`converge_data` = '" + convergeData + "'");
                }
                if (!isConvergeComplete.isEmpty()) {
                    setParams.add("`is_converge_complete` = '" + isConvergeComplete + "'");
                }
                if (!isOriginal.isEmpty()) {
                    setParams.add(" `is_original` = '" + isOriginal + "'");
                }
                if (!isHaveNationStandard.isEmpty()) {
                    setParams.add(" is_have_nation_standard='" + isHaveNationStandard + "'");
                }
                if (!isNationalStandard.isEmpty()) {
                    setParams.add(" is_national_standard = '" + isNationalStandard + "'");
                }
                if (!isNationalConverge.isEmpty()) {
                    setParams.add(" `is_national_converge` = '" + isNationalConverge + "'");
                }
                if (!sealtype.isEmpty()) {
                    setParams.add(" `seal_type` = '" + sealtype + "'");
                }
                if (!convergeCondition.isEmpty()) {
                    setParams.add(" `converge_condition` = '" + convergeCondition + "'");
                }
                if (!isAppView.isEmpty()) {
                    setParams.add(" `is_app_view`='" + isAppView + "'");
                }
                if (!noUseReason.isEmpty()) {
                    setParams.add(" `no_use_reason` = '" + noUseReason + "'");
                }

                // 生成 SQL 语句
                if (!setParams.isEmpty()) {
                    String setClause = String.join(",\n", setParams);
                    String sql = String.format("UPDATE `license_directory_statistic`\n" +
                                    "SET %s\n" +
                                    "WHERE\n" +
                                    "    basic_code = '%s';",
                            setClause, basicCode);

                    // 打印 SQL 语句
                    System.out.println(sql);
                }
            }

            workbook.close();
            file.close();
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