import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelToSQL {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\潘强\\Desktop\\license_data.xlsx";
        String tableName = "`bjca_license_base`.`license_content`";

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row
                    continue;
                }

                // Read each cell
                String id = getCellValue(row.getCell(0));
                String licenseName = getCellValue(row.getCell(1));
                String licenseIdentificationCode = getCellValue(row.getCell(2));
                String stateOrgansName = getCellValue(row.getCell(3));
                String uniqueIdentificationCode = getCellValue(row.getCell(4));
                String createBy = getCellValue(row.getCell(5));
                String updateBy = getCellValue(row.getCell(6));

                // Optional: handle timestamps manually or let DB auto-generate them
                 String createTime = formatTimestamp(getCellValue(row.getCell(7)));
                 String updateTime = formatTimestamp(getCellValue(row.getCell(8)));

                // Build SQL
                String sql = String.format(
                        "INSERT INTO %s (`id`, `license_name`, `license_identification_code`, `state_organs_name`, `unique_identification_code`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                        tableName,
                        id,
                        licenseName.replace("'", "''"), // Escape single quotes
                        licenseIdentificationCode,
                        stateOrgansName.replace("'", "''"),
                        uniqueIdentificationCode,
                        createBy,
                        updateBy,
                        createTime,
                        updateTime
                );

                System.out.println(sql);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get cell value as String
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // Optional: format timestamp string
    private static String formatTimestamp(String timestampStr) {
        return  timestampStr  ;
    }
}