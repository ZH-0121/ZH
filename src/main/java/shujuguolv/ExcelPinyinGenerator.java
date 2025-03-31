package shujuguolv;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ExcelPinyinGenerator {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\潘强\\Desktop\\input.xlsx";
        String outputFilePath = "C:\\Users\\潘强\\Desktop\\output.xlsx";

        try (FileInputStream fis = new FileInputStream(new File(inputFilePath));
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(new File(outputFilePath))) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    row = sheet.createRow(i);
                }
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String name = cell.getStringCellValue();
                    String pinyinInitials = getPinyinInitials(name);
                    String randomString = generateRandomString(8);
                    String result = pinyinInitials + randomString;

                    Cell resultCell = row.createCell(1);
                    resultCell.setCellValue(result);
                }
            }

            workbook.write(fos);
            System.out.println("处理完成，结果已保存到 " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPinyinInitials(String name) {
        StringBuilder initials = new StringBuilder();
        for (char c : name.toCharArray()) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyinArray != null) {
                initials.append(pinyinArray[0].charAt(0));
            } else {
                initials.append(c);
            }
        }
        return initials.toString().toUpperCase();
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
}
