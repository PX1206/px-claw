package com.claw.common.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Sakura
 * @date 2025/8/27 17:02
 */
public class YamlConfigLoader {

    private static Map<String, Object> config;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // 处理服务器上读取外部文件，默认与 jar 同目录
        File externalFile = new File("application.yml");
        try (InputStream in = externalFile.exists() ?
                new FileInputStream(externalFile) :
                YamlConfigLoader.class.getClassLoader().getResourceAsStream("application.yml")) {

            if (in != null) {
                Yaml yaml = new Yaml();
                config = yaml.load(in);
            } else {
                throw new RuntimeException("找不到 application.yml 文件");
            }

        } catch (Exception e) {
            throw new RuntimeException("加载 YAML 配置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String keyPath) {
        String[] keys = keyPath.split("\\.");
        Map<String, Object> current = config;
        Object value = null;
        for (int i = 0; i < keys.length; i++) {
            value = current.get(keys[i]);
            if (i < keys.length - 1) {
                if (value instanceof Map) {
                    current = (Map<String, Object>) value;
                } else {
                    return null;
                }
            }
        }
        return (T) value;
    }

    public static String getString(String keyPath) {
        Object value = get(keyPath);
        return value != null ? value.toString() : null;
    }
}
