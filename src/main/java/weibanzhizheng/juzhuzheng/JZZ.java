package weibanzhizheng.juzhuzheng;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class JZZ {
    static BASE64Encoder encoder = new BASE64Encoder();
    static BASE64Decoder decoder = new BASE64Decoder();
    static String access_token = null;
    String base = null;

    public JZZ() {
    }

    /**
     * 读取Excel文件内容，返回二维列表
     * @param file Excel文件对象
     * @return List<List> 外层List对应行，内层List对应列数据
     */
    public List readExcel(File file) {
        try {
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is); // 获取工作簿
            int sheet_size = wb.getNumberOfSheets();
            int index = 0; // 默认处理第一个Sheet
            if (index < sheet_size) {
                List<List> outerList = new ArrayList();
                Sheet sheet = wb.getSheet(index);

                for(int i = 0; i < sheet.getRows(); ++i) {
                    List innerList = new ArrayList();

                    for(int j = 0; j < sheet.getColumns(); ++j) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if (!cellinfo.isEmpty()) { // 跳过空单元格（可能导致数据错位）
                            innerList.add(cellinfo);
                            System.out.print(cellinfo); // 打印单元格内容（调试用）
                        }
                    }

                    outerList.add(i, innerList);
                }

                return outerList;
            }
        } catch (FileNotFoundException var12) {
            var12.printStackTrace();
        } catch (BiffException var13) {
            var13.printStackTrace();
        } catch (IOException var14) {
            var14.printStackTrace();
        }
        return null;
    }

    /**
     * 登录系统获取access_token
     * @return access_token 用于后续API调用
     */
    public static String Login() throws Exception {
        URL urlLogin = new URL("http://172.26.50.55:9090/license-app/v1/security/login");
        HttpURLConnection connectionLoginLogin = (HttpURLConnection)urlLogin.openConnection();
        connectionLoginLogin.setRequestMethod("POST");
        connectionLoginLogin.setDoOutput(true);
        connectionLoginLogin.setDoInput(true);
        connectionLoginLogin.setUseCaches(false);
        connectionLoginLogin.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connectionLoginLogin.connect();

        // 构造登录请求JSON体
        JSONObject jsonLogin = new JSONObject();
        jsonLogin.put("password", "ATIICiVfbRJWHii");
        jsonLogin.put("app_key", "KvmonFdrBLLRIuJ");
        jsonLogin.put("app_secret", "hGgaKEQolqGXaiU");
        jsonLogin.put("org_code", "org_code"); // 注意：org_code值可能无效
        jsonLogin.put("account", "gonganjudaizhizheng");

        // 发送请求
        BufferedWriter writerLogin = new BufferedWriter(new OutputStreamWriter(connectionLoginLogin.getOutputStream(), "UTF-8"));
        writerLogin.write(jsonLogin.toJSONString());
        writerLogin.close();

        // 处理响应
        int responseCodeLogin = connectionLoginLogin.getResponseCode();
        String result1Login = null;
        String access_token = null;
        if (responseCodeLogin == 200) {
            InputStream inputStreamLogin = connectionLoginLogin.getInputStream();
            result1Login = inputStream2String(inputStreamLogin); // 将响应流转换为字符串
            // 注意：通过字符串截取access_token存在风险，建议使用JSON解析
            access_token = result1Login.substring(219, 255);
        }
        return access_token;
    }

    public static void main(String[] args) throws Exception {
        access_token = Login();

        new Date();
        new SimpleDateFormat("yyyy-MM-dd");
        new ArrayList();

        try {
            Workbook book = Workbook.getWorkbook(new File("C:\\Users\\潘强\\Desktop\\juzhuzheng\\JZZ.xls"));
            Sheet sheet = book.getSheet(0);
            Cell cell1 = sheet.getCell(1, 0);
            int i = 1;

            while(true) {
                sheet.getCell(0, i);
                Cell cell2 = sheet.getCell(1, i);   //证照号码
                Cell cell3 = sheet.getCell(2, i);   //持证人名称
                Cell cell4 = sheet.getCell(3, i);   //持证人身份类型
                Cell cell5 = sheet.getCell(4, i);   //持有人身份证件号码
                sheet.getCell(5, i);
                sheet.getCell(6, i);
                sheet.getCell(7, i);
                Cell cell9 = sheet.getCell(8, i);   //发证日期
                Cell cell10 = sheet.getCell(9, i);  //有效结束日期
                Cell cell11 = sheet.getCell(10, i);
                Cell cell12 = sheet.getCell(11, i);
                Cell cell13 = sheet.getCell(12, i);
                Cell cell14 = sheet.getCell(13, i);
                Cell cell15 = sheet.getCell(14, i);
                Cell cell16 = sheet.getCell(15, i);//照片
                if ("".equals(cell2.getContents())) {
                    book.close();
                    break;
                }
                Cell cell17 = sheet.getCell(16, i);
                Cell cell18 = sheet.getCell(17, i);
                Cell cell19 = sheet.getCell(18, i);
                Cell cell20 = sheet.getCell(19, i);

                String ZZMC = "北京市居住证";
                String ZZHM = cell2.getContents();
                String CYRMC = cell3.getContents();
                String CYRSFZJLX = cell4.getContents();
                String CYRSFZJHM = cell5.getContents();
                String FZJGMC = "北京市公安局";
                String FZJGZZJGDM = "1111000000002888XF";
                String FZJGSSXZQHDM = "110000";
                String FZRQ = cell9.getContents();
                String YXQJSRQ = cell10.getContents();
                String MZ = cell11.getContents();
                String HJSZD = cell12.getContents();
                String XJZDZ = cell13.getContents();
                String YXQKSRQ = cell14.getContents();
                String XB = cell15.getContents();
                String ZP = cell16.getContents();
                String format = "jpg";
                File f = new File("C:\\Users\\潘强\\Desktop\\juzhuzheng\\zhaopian\\" + ZP + "." + format);
                if (!f.exists()) {
                    format = "png";
                    f = new File("C:\\Users\\潘强\\Desktop\\juzhuzheng\\zhaopian\\" + ZP + "." + format);
                }

                if (!f.exists()) {
                    format = "jpeg";
                }

                String GRZP = getImageBinary(ZP, format).replaceAll("\r|\n", "");
                String CSRQ1;
                if (GRZP.equals("无此照片") || GRZP == "无此照片") {
                    FileOutputStream fos = new FileOutputStream("C:\\Users\\潘强\\Desktop\\juzhuzheng\\notImg.txt", true);
                    CSRQ1 = CYRSFZJHM + "无此数据照片\r\n";
                    byte[] bytes = CSRQ1.getBytes();
                    fos.write(bytes);
                    fos.close();
                }
                String CSRQ = cell17.getContents();
                String GXSJ = cell18.getContents();
                String BJTH = cell19.getContents();
                String CCLZSJ = cell20.getContents();

               /* String XB = cell12.getContents();
                CSRQ = cell13.getContents();
                String MZ = cell14.getContents();
                String ZDXM = cell15.getContents();

                String ZDZD = cell16.getContents();
                String DBDW = cell17.getContents();*/

                try {
                    URL url = new URL("http://172.26.50.55:9090/license-app/v1/license/10005000100002888X110000/issue?access_token=" + access_token);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    connection.connect();
                    JSONObject json = new JSONObject();
                    JSONObject json1 = new JSONObject();
                    JSONObject json2 = new JSONObject();
                    JSONObject data_fields = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    JSONObject json3 = new JSONObject();
                    JSONObject jsonImg = new JSONObject();
                    data_fields.put("ZZHM", ZZHM);
                    data_fields.put("ZZMC", ZZMC);
                    data_fields.put("CYRMC", CYRMC);
                    data_fields.put("CYRSFZJLX", CYRSFZJLX);
                    data_fields.put("CYRSFZJHM", CYRSFZJHM);
                    data_fields.put("FZJGMC", FZJGMC);
                    data_fields.put("FZJGZZJGDM", FZJGZZJGDM);
                    data_fields.put("FZJGSSXZQHDM", FZJGSSXZQHDM);
                    data_fields.put("FZRQ", FZRQ);
                    data_fields.put("YXQJSRQ", YXQJSRQ);
                    data_fields.put("MZ",MZ);
                    data_fields.put("HJSZD",HJSZD);
                    data_fields.put("XJZDZ",XJZDZ);
                    if (GRZP.equals("无此照片")) {
                        data_fields.put("ZP", "");
                    } else {
                        jsonImg.put("file_suffix", "." + format);
                        jsonImg.put("file_data", GRZP);
                        data_fields.put("ZP", jsonImg);
                    }


                    data_fields.put("YXQKSRQ", YXQKSRQ);
                    data_fields.put("CSRQ", CSRQ);
                    data_fields.put("GXSJ", GXSJ);
                    data_fields.put("XB", XB);
                    data_fields.put("BJTH", BJTH);
                    data_fields.put("CCLZSJ", CCLZSJ);


                    json1.put("data_fields", data_fields);
                    json1.put("service_item_code", "0100909004567683330000002140X100");
                    json1.put("service_item_name", "北京市居住证");
                    json1.put("license_group", "组别1");
                    json1.put("seal_code", "DZYZ00002888XjRCheT");
                    json1.put("operator", json2);
                    json2.put("account", "gonganjudaizhizheng");
                    json2.put("name", "张浩");
                    json2.put("identity_num", "372925200001211933");
                    json2.put("role", "证照系统管理员");
                    json2.put("service_org", "bjca");
                    json2.put("division", "BJCA");
                    json2.put("division_code", "110000");
                    json.put("data", json1);
                    json3.put("file_data", "");
                    json3.put("name", "潘强");
                    json3.put("file_type", "pdf");
                    json3.put("is_license_image", true);
                    json3.put("is_show_template", false);
                    json3.put("description", "已盖章证照");
                    jsonArray.add(json3);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                    writer.write(json.toJSONString());
                    writer.close();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputStream = connection.getInputStream();
                        String result = inputStream2String(inputStream);
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        String ack_code1 = jsonObject.getString("ack_code");
                        String str;
                        if (ack_code1.equals("SUCCESS")) {
                            String data = jsonObject.getString("data");
                            JSONObject JsonData_Fields = JSONObject.parseObject(data);
                            String license_code = JsonData_Fields.get("license_code").toString();
                            String auth_code = JsonData_Fields.get("auth_code").toString();
                            String SUCCESS = "证照号码：" + ZZHM + "," + license_code + "," + auth_code;
                            FileOutputStream fos = new FileOutputStream("C:\\Users\\潘强\\Desktop\\juzhuzheng\\SUCCESS.txt", true);
                            str = SUCCESS + "\r\n";
                            byte[] bytes = str.getBytes();
                            fos.write(bytes);
                            fos.close();
                        } else if (ack_code1.equals("FAILURE")) {
                            FileOutputStream fos = new FileOutputStream("C:\\Users\\潘强\\Desktop\\juzhuzheng\\FAILURE.txt", true);
                            str = ZZHM + ":" + result + "\r\n";
                            byte[] bytes = str.getBytes();
                            fos.write(bytes);
                            fos.close();
                        }

                        System.out.println("持证人身份证号码：" + CYRSFZJHM + "制证" + ack_code1 + "-----" + result);
                    }
                } catch (Exception var68) {
                    var68.printStackTrace();
                }

                ++i;
            }
        } catch (Exception var69) {
            System.out.println(var69);
        }

    }

    static void base64StringToPDF(String base64sString, String pdfName) {
        BufferedInputStream bin = null;
        FileOutputStream fout = null;
        BufferedOutputStream bout = null;

        try {
            byte[] bytes = decoder.decodeBuffer(base64sString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            bin = new BufferedInputStream(bais);
            File file = new File("C:\\Users\\潘强\\Desktop\\juzhuzheng\\PDF\\" + pdfName + ".pdf");
            fout = new FileOutputStream(file);
            bout = new BufferedOutputStream(fout);
            byte[] buffers = new byte[1024];

            for(int len = bin.read(buffers); len != -1; len = bin.read(buffers)) {
                bout.write(buffers, 0, len);
            }

            bout.flush();
        } catch (IOException var18) {
            var18.printStackTrace();
        } finally {
            try {
                bin.close();
                fout.close();
                bout.close();
            } catch (IOException var17) {
                var17.printStackTrace();
            }

        }

    }

    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";

        while((line = in.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }

    static String getImageBinary(String img, String format) {
        System.out.println("img = " + img + ",format = " + format);
        File f = new File("C:\\Users\\潘强\\Desktop\\juzhuzheng\\zhaopian\\" + img + "." + format);

        try {
            BufferedImage bi = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, format, baos);
            byte[] bytes = baos.toByteArray();
            return encoder.encodeBuffer(bytes).trim();
        } catch (Exception var6) {
            var6.printStackTrace();
            return "无此照片";
        }
    }
}
