package shujuguolv;

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

public class ExcelToCsv {

    public static void main(String[] args) {
        String folderPath = "C:\\Users\\潘强\\Desktop\\中华人民共和国不动产权证书数据";
        String csvFilePath = "C:\\Users\\潘强\\Desktop\\中华人民共和国不动产权证书数据.csv";
        try {
            convertExcelFilesToCsv(folderPath, csvFilePath);
            System.out.println("数据已成功写入 CSV 文件。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertExcelFilesToCsv(String folderPath, String csvFilePath) throws IOException {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder) ||!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("指定的文件夹不存在或不是有效的文件夹。");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFilePath), StandardCharsets.UTF_8))) {
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
                    writeRowToCsv(writer, rowData);
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

    private static void writeRowToCsv(BufferedWriter writer, List<String> rowData) throws IOException {
        for (int i = 0; i < rowData.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }
            String value = rowData.get(i);
            if (value.contains(",")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            writer.write(value);
        }
        writer.newLine();
    }
}