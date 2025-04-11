package shujuguolv;
/*
* 将文件夹中所有excel中的数据读取到txt中
* */
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExcelToTxt {

    public static void main(String[] args) {
        String folderPath = "C:\\Users\\潘强\\Desktop\\sfz";
        String txtFilePath = "C:\\Users\\潘强\\Desktop\\sfz.txt";
        try {
            convertExcelFilesToTxt(folderPath, txtFilePath);
            System.out.println("数据已成功追加写入 TXT 文件。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertExcelFilesToTxt(String folderPath, String txtFilePath) throws IOException {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder) ||!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("指定的文件夹不存在或不是有效的文件夹。");
        }

        // 以追加模式打开文件
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFilePath, true), StandardCharsets.UTF_8))) {
            File[] files = folder.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx"))) {
                        processExcelFile(file, writer);
                    }
                }
            }
        }
    }

    private static void processExcelFile(File file, BufferedWriter writer) throws IOException {
        try (InputStream inputStream = new FileInputStream(file);
             Workbook workbook = getWorkbook(inputStream, file.getName())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        rowData.add(getCellValueAsString(cell));
                    }
                    writeRowToTxt(writer, rowData);
                }
            }
        }
    }

    private static Workbook getWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        }
        throw new IllegalArgumentException("不支持的文件格式: " + fileName);
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

    private static void writeRowToTxt(BufferedWriter writer, List<String> rowData) throws IOException {
        for (int i = 0; i < rowData.size(); i++) {
            if (i > 0) {
                // 这里使用制表符作为分隔符，你也可以根据需要修改为其他分隔符，如逗号
                writer.write(",");
            }
            String value = rowData.get(i);
            if (value.contains(",")) {
                // 如果数据中包含分隔符，将其用引号括起来
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            writer.write(value);
        }
        writer.newLine();
    }
}