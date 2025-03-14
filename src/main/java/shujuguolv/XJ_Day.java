package shujuguolv;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellAddress;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
public class XJ_Day {
    /**
     * jxl读取文件
     *workbook为只读文件 可以读取内容
     * @throws BiffException
     * @throws IOException
     */
    /*public static void ExcelReader(){
        File file=new File("D:\\A项目整理\\移动公服材料\\电子证照\\验收材料\\移动公服_电子证照系统验收材料\\日巡检记录最终版\\新建文件夹\\");
        Workbook workbook;
        try {
            workbook = Workbook.getWorkbook(file);
            Sheet sheet=workbook.getSheet(0);
            Cell cell=sheet.getCell(2, 1);
            Cell cell1=sheet.getCell(2, 1);
            Cell cell2=sheet.getCell(2, 1);
            Cell cell3=sheet.getCell(2, 1);
            System.out.println(cell.getContents());
        } catch (BiffException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    *//**‘
     * jxl实现excel读取并输出
     * 读取excel修改之后重新输出
     *//*
    public static void ModifyExcelAndOutput() {
        try {
            File file=new File("D:\\A项目整理\\移动公服材料\\电子证照\\验收材料\\移动公服_电子证照系统验收材料\\日巡检记录最终版\\");
            //这里读出来的workbook作为模版
            Workbook workbook=Workbook.getWorkbook(file);
            //这里是将要输出的workbook
            for(int i=0;i<2;i++){//这里模拟输出两个文件
                //输出文件名
                String outFileName="D:\\电子证照日报\\电子证照日报\\2024\\电子证照日报\\电子证照巡检记录单"+i+".xls";
                // jxl.Workbook 对象是只读的，所以如果要修改Excel，需要创建一个可读的副本，副本指向原Excel文件（即下面的new File(excelpath)）
                //WritableWorkbook如果直接createWorkbook模版文件会覆盖原有的文件
                WritableWorkbook writeBook=Workbook.createWorkbook(new File(outFileName),workbook);
                //读取第一个sheet
                WritableSheet sheet=writeBook.getSheet(0);
                //读取将要修改的cell
                WritableCell cell=sheet.getWritableCell(2, 1);
                //获取上一部cell的格式
                jxl.format.CellFormat cf=cell.getCellFormat();
                Label lable=new Label(2, 1, "商户名称：修改后的商户名"+i);
                //将修改后的单元格格式设置成和原来一样的
                lable.setCellFormat(cf);
                //将修改后的cell放回sheet中
                sheet.addCell(lable);
                writeBook.write();
                writeBook.close();
            }
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    /**
     * poi实现excel修改
     * 读取excel并且修改部分内容并输出
     */

    public static void ModifyAndExport(String time) {
        InputStream io;
        try {
            io = new FileInputStream(new File("D:\\电子证照日报\\电子证照日报\\2024\\电子证照巡检记录单.xls"));
            HSSFWorkbook workbook = new HSSFWorkbook(io);
            HSSFSheet sheet = workbook.getSheetAt(0);

            // 确保行存在，如果不存在则创建
            HSSFRow row = sheet.getRow(2);
            if (row == null) {
                row = sheet.createRow(2);
            }

            // 确保单元格存在，如果不存在则创建
            HSSFCell cell = row.getCell(1);
            if (cell == null) {
                cell = row.createCell(1);
            }

            // 设置日期
            cell.setCellValue(time);

            // 巡检人
            HSSFRow row_xunjianren = sheet.getRow(2);
            if (row_xunjianren == null) {
                row_xunjianren = sheet.createRow(2);
            }
            HSSFCell cell_xunjianren = row_xunjianren.getCell(2);
            if (cell_xunjianren == null) {
                cell_xunjianren = row_xunjianren.createCell(2);
            }
            cell_xunjianren.setCellValue("");

            // 定义需要处理的行号范围
            int[] rows = {21,22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
            for (int rowIndex : rows) {
                // CPU 设置
                HSSFRow cpuRow = sheet.getRow(rowIndex);
                if (cpuRow == null) {
                    cpuRow = sheet.createRow(rowIndex);
                }
                HSSFCell cpuCell = cpuRow.getCell(5);
                if (cpuCell == null) {
                    cpuCell = cpuRow.createCell(5);
                }
                cpuCell.setCellValue(RandomByCPU() + "%");

                // 内存设置
                HSSFCell memCell = cpuRow.getCell(6);
                if (memCell == null) {
                    memCell = cpuRow.createCell(6);
                }
                memCell.setCellValue(RandomByFree() + "%");

                // 磁盘设置
                HSSFCell diskCell = cpuRow.getCell(7);
                if (diskCell == null) {
                    diskCell = cpuRow.createCell(7);
                }
                int diskUsage;
                if (rowIndex == 27 || rowIndex == 28) {
                    diskUsage = RandomByCP_50_55();
                } else if (rowIndex == 29 || rowIndex == 32) {
                    diskUsage = RandomByCP_60_65();
                } else if (rowIndex == 40 || rowIndex == 41 || rowIndex == 44 || rowIndex == 45 || rowIndex == 58) {
                    diskUsage = RandomByCP_55_60();
                } else {
                    diskUsage = RandomByCP();
                }
                diskCell.setCellValue(diskUsage + "%");
            }

            String outputPath = "D:\\电子证照日报\\电子证照日报\\2024\\电子证照日报\\"+"电子证照系统日常巡检记录单" + time + ".xls";
            FileOutputStream fo = new FileOutputStream(new File(outputPath));
            workbook.write(fo);
            workbook.close();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws ParseException {
        //RandomByCP();//
        Scanner scan=new Scanner(System.in);
        System.out.println("请输入年份");
        int year=scan.nextInt();
        SimpleDateFormat sdfBefore =   new SimpleDateFormat( "yyyy-MM-dd" );
        ArrayList<String>list0=new ArrayList<>();
        Date date0 = new SimpleDateFormat("yyyy-MM-dd").parse(""+year+"-01-00");
        Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(""+year+"-12-30");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date0);
        while(cal.getTime().compareTo(date1)<=0){
            cal.add(Calendar.DAY_OF_MONTH,1);
            list0.add(sdfBefore.format(cal.getTime()));
        }
        for (String string : list0) {
            ModifyAndExport(string);
        }

    }
    /**
     * 获取CPU的随机数
     */
    public static int RandomByCPU(){
        Random rand=new Random();
        int n3=rand.nextInt(25)+30;//[10,89]内的随机整数
        return n3;
    }
    /**
     * 获取内存的随机数
     */
    public static int RandomByFree(){
        Random rand=new Random();
        int n3=rand.nextInt(30)+30;//[10,89]内的随机整数
        return n3;
    }
    /**
     * 获取磁盘的随机数(45-50之间)
     */
    public static int RandomByCP(){
        Random rand=new Random();
        int random= (int) (45+Math.random()*(50-45+1));
        //System.out.println(random);
        return random;
    }
    /**
     * 获取磁盘的随机数(50-55之间)
     */
    public static int RandomByCP_50_55(){
        Random rand=new Random();
        int random= (int) (50+Math.random()*(55-50+1));
        //System.out.println(random);
        return random;
    }
    /**
     * 获取磁盘的随机数(55-60之间)
     */
    public static int RandomByCP_55_60(){
        Random rand=new Random();
        int random= (int) (55+Math.random()*(60-55+1));
        //System.out.println(random);
        return random;
    }
    /**
     * 获取磁盘的随机数(60-65之间)
     */
    public static int RandomByCP_60_65(){
        Random rand=new Random();
        int random= (int) (60+Math.random()*(65-60+1));
        //System.out.println(random);
        return random;
    }


}
