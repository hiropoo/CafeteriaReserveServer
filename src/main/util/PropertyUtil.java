package main.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyUtil {

    private static final String INIT_FILE_PATH = "src/properties/common.properties";
    private static final Properties properties;

    private PropertyUtil() throws Exception {
    }

    static {
        properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(Paths.get(INIT_FILE_PATH), StandardCharsets.UTF_8));
        } catch (IOException e) {
            // ファイル読み込みに失敗
            System.out.println(String.format("ファイルの読み込みに失敗しました。ファイル名:%s", INIT_FILE_PATH));
        }
    }

    // プロパティ値を取得する
    public static String getProperty(final String key) {
        return getProperty(key, "");
    }

    // プロパティ値を取得する
    // プロパティ値が存在しない場合は引数に渡したデフォルト値を返す
    public static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}