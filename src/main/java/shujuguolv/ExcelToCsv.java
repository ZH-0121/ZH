package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ExcelToCsv {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        // 设置更大的字节数组限制
        IOUtils.setByteArrayMaxOverride(200000000); // 设置更大的限制

        String excelFilePath = "C:\\Users\\潘强\\Desktop\\juzhudengjika.xlsx";  // 修改为你表格文件的路径
        String outputCsvFilePath = "C:\\Users\\潘强\\Desktop\\license_codes.csv";  // 输出 CSV 的文件路径

        try {
            convertExcelToCsv(excelFilePath, outputCsvFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 逐行读取 Excel 文件并写入 CSV 文件
    private static void convertExcelToCsv(String excelFilePath, String outputCsvFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new SXSSFWorkbook(new XSSFWorkbook(fis))) {
            Sheet sheet = workbook.getSheetAt(0);  // 获取第一个工作表

            try (FileWriter fileWriter = new FileWriter(new File(outputCsvFilePath))) {
                // 写入 CSV 文件的标题行
                fileWriter.write("license_code,holderIdentityNum,idCode,issueDate,expiryDate\n");

                // 逐行读取数据并写入 CSV 文件
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        String licenseCode = getCellValueAsString(row.getCell(0));  // 第一列
                        String holderIdentityNum = getCellValueAsString(row.getCell(1)); // 第二列
                        String idCode = getCellValueAsString(row.getCell(2));  // 第三列
                        String issueDate = getCellValueAsString(row.getCell(3));  // 第四列
                        String expiryDate = getCellValueAsString(row.getCell(4));  // 第五列

                        // 打印调试信息
                        System.out.println("Row " + rowIndex + ": " + licenseCode + ", " + holderIdentityNum + ", " + idCode + ", " + issueDate + ", " + expiryDate);

                        // 写入一行数据到 CSV 文件
                        fileWriter.write(licenseCode + "," + holderIdentityNum + "," + idCode + "," + issueDate + "," + expiryDate + "\n");
                    }
                }
            }
        }
        System.out.println("数据已保存到文件: " + outputCsvFilePath);
    }

    // 获取单元格的值并转换为字符串
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";  // 如果单元格为空，返回空字符串
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 使用静态的 SimpleDateFormat 对象
                    return SDF.format(cell.getDateCellValue());
                } else {
                    // 如果是数字类型，转换为字符串
                    return String.valueOf(cell.getNumericCellValue());
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