package weibanzhizheng.YXBYSZS;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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

public class YXBYSRYZS {
    static BASE64Encoder encoder = new BASE64Encoder();
    static BASE64Decoder decoder = new BASE64Decoder();
    String base = null;

    public YXBYSRYZS() {
    }

    public List readExcel(File file) {
        try {
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int sheet_size = wb.getNumberOfSheets();
            int index = 0;
            if (index < sheet_size) {
                List<List> outerList = new ArrayList();
                Sheet sheet = wb.getSheet(index);

                for(int i = 0; i < sheet.getRows(); ++i) {
                    List innerList = new ArrayList();

                    for(int j = 0; j < sheet.getColumns(); ++j) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if (!cellinfo.isEmpty()) {
                            innerList.add(cellinfo);
                            System.out.print(cellinfo);
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

    public static String getPDFBinary(File file) {
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;

        try {
            fin = new FileInputStream(file);
            bin = new BufferedInputStream(fin);
            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];

            for(int len = bin.read(buffer); len != -1; len = bin.read(buffer)) {
                bout.write(buffer, 0, len);
            }

            bout.flush();
            byte[] bytes = baos.toByteArray();
            String var8 = encoder.encodeBuffer(bytes).trim();
            return var8;
        } catch (FileNotFoundException var20) {
            var20.printStackTrace();
        } catch (IOException var21) {
            var21.printStackTrace();
        } finally {
            try {
                fin.close();
                bin.close();
                bout.close();
            } catch (IOException var19) {
                var19.printStackTrace();
            }

        }

        return null;
    }

    public static String Login() throws Exception {
        URL urlLogin = new URL("http://192.158.148.28:9090/license-app/v1/security/login");
        HttpURLConnection connectionLoginLogin = (HttpURLConnection)urlLogin.openConnection();
        connectionLoginLogin.setRequestMethod("POST");
        connectionLoginLogin.setDoOutput(true);
        connectionLoginLogin.setDoInput(true);
        connectionLoginLogin.setUseCaches(false);
        connectionLoginLogin.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connectionLoginLogin.connect();
        JSONObject jsonLogin = new JSONObject();
        jsonLogin.put("password", "ATIICiVfbRJWHii");
        jsonLogin.put("app_key", "KvmonFdrBLLRIuJ");
        jsonLogin.put("app_secret", "hGgaKEQolqGXaiU");
        jsonLogin.put("org_code", "org_code");
        jsonLogin.put("account", "gonganjudaizhizheng");
        BufferedWriter writerLogin = new BufferedWriter(new OutputStreamWriter(connectionLoginLogin.getOutputStream(), "UTF-8"));
        writerLogin.write(jsonLogin.toJSONString());
        writerLogin.close();
        int responseCodeLogin = connectionLoginLogin.getResponseCode();
        String result1Login = null;
        String access_token = null;
        if (responseCodeLogin == 200) {
            InputStream inputStreamLogin = connectionLoginLogin.getInputStream();
            result1Login = inputStream2String(inputStreamLogin);
            access_token = result1Login.substring(219, 255);
        }

        return access_token;
    }

    public static void main(String[] args) throws Exception {
        String access_token = Login();
        new Date();
        new SimpleDateFormat("yyyy-MM-dd");
        new ArrayList();

        try {
            Workbook book = Workbook.getWorkbook(new File("/share/jzz/YXBYSZS/YXBYSZS.xls"));
            Sheet sheet = book.getSheet(0);
            Cell cell1 = sheet.getCell(1, 0);
            int i = 1;

            while(true) {
                cell1 = sheet.getCell(0, i);
                Cell cell2 = sheet.getCell(1, i);
                Cell cell3 = sheet.getCell(2, i);
                Cell cell4 = sheet.getCell(3, i);
                sheet.getCell(4, i);
                sheet.getCell(5, i);
                sheet.getCell(6, i);
                Cell cell8 = sheet.getCell(7, i);
                sheet.getCell(8, i);
                Cell cell10 = sheet.getCell(9, i);
                Cell cell11 = sheet.getCell(10, i);
                Cell cell12 = sheet.getCell(11, i);
                Cell cell13 = sheet.getCell(12, i);
                Cell cell14 = sheet.getCell(13, i);
                Cell cell15 = sheet.getCell(14, i);
                Cell cell16 = sheet.getCell(15, i);
                if ("".equals(cell2.getContents())) {
                    book.close();
                    break;
                }

                String ZZMC = "北京市普通高等学校优秀毕业生荣誉证书";
                String ZZHM = cell1.getContents();
                String CYRMC = cell2.getContents();
                String CYRSFZJLX = cell3.getContents();
                String CYRSFZJHM = cell4.getContents();
                String FZJGMC = "北京市教育委员会";
                String FZJGZZJGDM = "11110000000026833B";
                String FZJGSSXZQHDM = "110000";
                String FZRQ = cell8.getContents();
                String YXQJSRQ = "";
                String YXMC = cell10.getContents();
                String YXDM = cell11.getContents();
                String BYNF = cell12.getContents();
                String XB = cell13.getContents();
                String XL = cell14.getContents();
                String ZY = cell15.getContents();
                String ZH = cell16.getContents();

                try {
                    URL url = new URL("http://192.158.148.58:9090/license-app/v1/license/200089501000026833110000/issue?access_token=" + access_token);
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
                    new JSONObject();
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
                    data_fields.put("YXMC", YXMC);
                    data_fields.put("YXDM", YXDM);
                    data_fields.put("BYNF", BYNF);
                    data_fields.put("XB", XB);
                    data_fields.put("XL", XL);
                    data_fields.put("ZY", ZY);
                    data_fields.put("ZH", ZH);
                    json1.put("data_fields", data_fields);
                    json1.put("service_item_code", "11110000000026833B200080501200001");
                    json1.put("service_item_name", "省普通高校优秀毕业生表彰");
                    json1.put("license_group", "组别1");
                    json1.put("seal_code", "DZYZ000026833vlDIhd");
                    json1.put("operator", json2);
                    json2.put("account", "gonganjudaizhizheng");
                    json2.put("name", "潘强");
                    json2.put("identity_num", "130481199706242117");
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
                            String SUCCESS = YXMC + "_" + CYRMC + "_" + CYRSFZJHM + "," + license_code + "," + auth_code;
                            FileOutputStream fos = new FileOutputStream("/share/jzz/YXBYSZS/SUCCESS.txt", true);
                            str = SUCCESS + "\r\n";
                            byte[] bytes = str.getBytes();
                            fos.write(bytes);
                            fos.close();
                        } else if (ack_code1.equals("FAILURE")) {
                            FileOutputStream fos = new FileOutputStream("/share/jzz/YXBYSZS/FAILURE.txt", true);
                            str = ZZHM + ":" + result + "\r\n";
                            byte[] bytes = str.getBytes();
                            fos.write(bytes);
                            fos.close();
                        }

                        System.out.println("持证人身份证号码：" + CYRSFZJHM + "制证" + ack_code1 + "-----" + result);
                    }
                } catch (Exception var64) {
                    var64.printStackTrace();
                }

                ++i;
            }
        } catch (Exception var65) {
            System.out.println(var65);
        }

    }

    public static String archive(String access_token, String auth_code, String pdfName) {
        try {
            String realUrl = "http://192.158.148.58:9090/license-app/v1/license/archive?auth_code=" + auth_code + "&access_token=" + access_token + "&show_sensitive_data=false";
            URL url = new URL(realUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            String result;
            for(result = ""; (line = reader.readLine()) != null; result = result + line) {
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            Object jsonData = jsonObject.get("data");
            JSONObject data = (JSONObject)jsonData;
            String file_name = data.getString("file_name");
            String file_data = data.getString("file_data");
            if (result.contains("SUCCESS")) {
                base64StringToPDF(file_data, pdfName);
                System.out.println("证照号码：" + pdfName + "归档成功");
            } else {
                System.out.println("证照号码：" + pdfName + "归档失败");
            }

            reader.close();
            conn.disconnect();
            return result;
        } catch (MalformedURLException var14) {
            var14.printStackTrace();
        } catch (SocketTimeoutException var15) {
            var15.printStackTrace();
        } catch (IOException var16) {
            var16.printStackTrace();
        }

        return "";
    }

    static void base64StringToPDF(String base64sString, String pdfName) {
        BufferedInputStream bin = null;
        FileOutputStream fout = null;
        BufferedOutputStream bout = null;

        try {
            byte[] bytes = decoder.decodeBuffer(base64sString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            bin = new BufferedInputStream(bais);
            File file = new File("/share/jzz/YXBYSZS/PDF/" + pdfName + ".pdf");
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

    static String getImageBinary(String img) {
        File f = new File("/data/JSZGZ/20231030/FZZP/" + img);

        try {
            BufferedImage bi = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return encoder.encodeBuffer(bytes).trim();
        } catch (Exception var5) {
            var5.printStackTrace();
            return "无此照片";
        }
    }
}

