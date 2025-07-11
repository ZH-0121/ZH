package weibanzhizheng.YXBYSZS;

import com.alibaba.fastjson.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class YXBYSGD {
    static BASE64Encoder encoder = new BASE64Encoder();
    static BASE64Decoder decoder = new BASE64Decoder();

    public YXBYSGD() {
    }

    public static void main(String[] args) {
        try {
            read();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public static String read() throws Exception {
        String access_token = Login();
        String result = null;
        File file1 = new File("/share/jzz/YXBYSZS/SUCCESS.txt");
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            fis = new FileInputStream(file1);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String datas = null;

            while((datas = br.readLine()) != null) {
                String[] data = datas.split(",");
                String auth_code = data[2];
                String pdfName = data[0];
                archive(access_token, auth_code, pdfName);
            }
        } catch (FileNotFoundException var30) {
            var30.printStackTrace();
        } catch (IOException var31) {
            var31.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException var29) {
                    var29.printStackTrace();
                }
            }

            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException var28) {
                    var28.printStackTrace();
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException var27) {
                    var27.printStackTrace();
                }
            }

        }

        return (String)result;
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
        } catch (MalformedURLException var13) {
            var13.printStackTrace();
        } catch (SocketTimeoutException var14) {
            var14.printStackTrace();
        } catch (IOException var15) {
            var15.printStackTrace();
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

    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";

        while((line = in.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }
}
