import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
/*
* 生成ES测试堵塞数据，
* */
public class GenerateBulkData {
    private static final int TARGET_SIZE = 100 * 1024 * 1024; // 100 MB in bytes
    private static final int RECORD_SIZE = 129; // 每条记录的平均大小（字节）
    private static final int RECORDS_NEEDED = TARGET_SIZE / RECORD_SIZE;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final Random random = new Random();

    public static void main(String[] args) {
        String fileName = "C:\\Users\\潘强\\Desktop\\bulk_data1.json";
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 1; i <= RECORDS_NEEDED; i++) {
                // 写入元数据行
                writer.write(String.format(
                        "{ \"index\": { \"_index\": \"test_license\", \"_type\": \"_doc\", \"_id\": \"%d\" } }\n",
                        i
                ));

                // 写入数据行
                writer.write(String.format(
                        "{\"name\":\"%s\",\"age\":%d,\"city\":\"%s\"}\n",
                        generateRandomString(8),
                        random.nextInt(100),
                        generateRandomString(10)
                ));

                // 进度显示
                if (i % 100000 == 0) {
                    System.out.printf("已生成 %d 条记录 (%.1f MB)%n",
                            i, (i * RECORD_SIZE) / (1024.0 * 1024));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("生成完成！共 %d 条记录，文件大小约 %.2f MB，耗时 %d 秒%n",
                RECORDS_NEEDED,
                (RECORDS_NEEDED * RECORD_SIZE) / (1024.0 * 1024),
                duration);
    }

    // 生成随机小写字母字符串
    private static String generateRandomString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}