package com.claw.common.tool;

import com.claw.common.exception.BusinessException;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author Sakura
 * @date 2024/11/11 11:11
 */
public class RSAUtil {

    // 定义密钥长度
    private static final int KEY_SIZE = 1024;
    private static final String ALGORITHM = "RSA";

    /**
     * 生成RSA密钥对
     * @return KeyPair 包含公钥和私钥
     * @throws Exception 如果生成失败
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 使用公钥加密
     * @param data 待加密的数据
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 加密后的数据（Base64编码）
     * @throws Exception 如果加密失败
     */
    public static String encrypt(String data, String publicKeyStr) throws Exception {
        PublicKey publicKey = getPublicKeyFromBase64(publicKeyStr);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 使用私钥解密
     * @param encryptedData Base64编码的加密数据
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 解密后的数据
     * @throws Exception 如果解密失败
     */
    public static String decrypt(String encryptedData, String privateKeyStr) throws Exception {
        try {
            PrivateKey privateKey = getPrivateKeyFromBase64(privateKeyStr);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("数据异常");
        }
    }

    /**
     * 将Base64编码的公钥字符串转换为PublicKey对象
     * @param publicKeyStr Base64编码的公钥字符串
     * @return PublicKey对象
     * @throws Exception 如果转换失败
     */
    public static PublicKey getPublicKeyFromBase64(String publicKeyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 将Base64编码的私钥字符串转换为PrivateKey对象
     * @param privateKeyStr Base64编码的私钥字符串
     * @return PrivateKey对象
     * @throws Exception 如果转换失败
     */
    public static PrivateKey getPrivateKeyFromBase64(String privateKeyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 将PublicKey对象转换为Base64编码的字符串
     * @param publicKey PublicKey对象
     * @return Base64编码的公钥字符串
     */
    public static String encodePublicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 将PrivateKey对象转换为Base64编码的字符串
     * @param privateKey PrivateKey对象
     * @return Base64编码的私钥字符串
     */
    public static String encodePrivateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 从 RSA 私钥计算出对应的公钥
     * @param privateKey RSA 私钥对象
     * @return PublicKey 对象
     * @throws Exception 如果转换失败
     */
    public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) throws Exception {
        // 检查私钥类型是否为 RSA 私钥
        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException("The provided key is not an RSA private key.");
        }

        // 从私钥中提取模数和私钥指数
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        java.math.BigInteger modulus = rsaPrivateKey.getModulus();
        java.math.BigInteger publicExponent = java.math.BigInteger.valueOf(65537); // 常用的公钥指数

        // 使用模数和公钥指数构造公钥规范
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

        // 根据规范生成公钥
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static void main(String[] args) throws Exception {
//        // 生成私钥和公钥
//        KeyPair keyPair = generateKeyPair();
//        String publicKeyStr = encodePublicKeyToBase64(keyPair.getPublic());
//        String privateKeyStr = encodePrivateKeyToBase64(keyPair.getPrivate());
//        System.out.println(publicKeyStr);
//        System.out.println(privateKeyStr);

        // 公钥对外暴露加密用
        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuggI4MbASTLHkTbqn6kKIDSKqFKMkiqVymD6DnY5Rcx9U8X97pjK/0pgRvu1xKs4S8Zwo6dgRxjX9Tzao/2OsxjJBIecj6iX47nx7vz6W30G7D+fOu5pNd9iqtu99tcNx+jen5vgqda5NmDHO4sg0oQsNqUy4+jTx+QeU5x0NesvGHO8A+dEKAbDGpyak6MH7X7N9CWTyRyLlmh0jqqc2zs0pkUJsHqtBO7EkPsmI82pSbCIPWmviBN1Z4OXz0bUJdgs2785O+91E00EP0Ktnsvgqg6rKJ2LA7EZczMpcipkCcxjEYMnU+TIIrkXiJX/QrLoOnwJNqUPxfy6haNDcwIDAQAB";
        // 私钥服务器保留解密用
        String privateKeyStr = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6CAjgxsBJMseRNuqfqQogNIqoUoySKpXKYPoOdjlFzH1Txf3umMr/SmBG+7XEqzhLxnCjp2BHGNf1PNqj/Y6zGMkEh5yPqJfjufHu/PpbfQbsP5867mk132Kq27321w3H6N6fm+Cp1rk2YMc7iyDShCw2pTLj6NPH5B5TnHQ16y8Yc7wD50QoBsManJqTowftfs30JZPJHIuWaHSOqpzbOzSmRQmweq0E7sSQ+yYjzalJsIg9aa+IE3Vng5fPRtQl2Czbvzk773UTTQQ/Qq2ey+CqDqsonYsDsRlzMylyKmQJzGMRgydT5MgiuReIlf9Csug6fAk2pQ/F/LqFo0NzAgMBAAECggEAFVt+yW+H+99ckgtf/FyH0RK3KIPxp4ZEFCv2CVsBUEYdzVRZxt67z3tWek/DmoSdvfVWUTOSFheAE6Oc5l7h1yXtThvTmZqddYOPhwR8Kay88rqLKVTdt1WiGkOIF6Kw7laJ2IEo1dbVBK0vsmJSrliGSu/EW+4LAlPqN2cq4FWhtIA5yV3VUuScV9/rI9jvYiwP2ltVUs92Mbkj8AHOpMR+ZrijJupXoWldLSFA91BitKSAZga2xkLRMWeT1TNwhMEU4ihZTJBfFICuGDxzdUXWGXqm8lvMR0bSuerIiXRYspQ/DyobfEfggI+lYeLscp7DtGu0DwMlvrTdNkS2AQKBgQDkwgZ6EGWKVu3Qu3kYLt6gGtqxcUBfbdZyvJFRTxDda5RIFUn3NUDwXNMzB+bZpR4G42P6oVQON8BtMu3tNCUr/97oiTfEMrkkiIRzCVDOrmmrXtmus6rmcDKChlZrfb5TzoHa1borEEFRV7QCoA1ChEkxEMJoL7P39tQ8BqCnMwKBgQDQL2/m2PsIl7wEl5xC+uFrqfd4bl9Ljmt9Ywy0J1DYY/obrtGifPGnkePRAh+o4s7eP2brtBFiRG3/fNOMgHsmk501g3t/wbQ3m0JsKTN6/AtDVcwKTe4ysZpJDVaaJygTzoRRZ/opo77LBc8e7WxZedixM9EFgtFbQCqsqa3ywQKBgEO/Vo2152yF/B5SNzW7Q8Fk0pm0cZ0ReW43fE35PYRlxN9oNqSYx+enhgDZ+TtB1Fez5jsmpi8jwcBVUfNq+wtB1vFFGhfn8b7pE1jCTIU5UCApkgxUN2vRlJPlVxi0f7ZwNTLrExyHzuBZOf2BCwxFAywdy4VxgzoPhqFUUSXTAoGAQelS52Z5C/5mM5oAiiA98EVOqV1gZF6B35/VbNApw2jbzZnqmyQtqh0BYLHobiLo1eqC3ksTWeZKYSIXJBulYdBIbKHlHsyoDxbjz7S3rM9RjY8DW5vSt1ANheVoLsQI50RyFgfyZu6FwGdlbOhEbouqNGgwXCtPtzE4kZPjE8ECgYEA0LY49wpA3UQc8d2PEhTleRhNkpBGbV+wBDrJOlVoxIrTzFsk00vnPGf4OCwIEagxxmdI51dgXgq0BHU/ry3t1fAI1y3YL0WLJxuTQpS0iETcwL/gYkgGLRASm9cswRy+NEKx0l5shMT/0Ow8ntFrdRYOkfGtLIDlXx4LezhsUaY=";
        // 带加密密码
        String password = "qyzj2024";

        // 加密后字符串
        String str = encrypt(password, publicKeyStr);
        System.out.println(str);
        // 解密字符串
        String str1 = decrypt(str, privateKeyStr);
        System.out.println(str1);
//
//        // 如果只知道私钥计算公钥
//        PublicKey publicKeyFromPrivate = RSAUtil.getPublicKeyFromPrivateKey(getPrivateKeyFromBase64(privateKeyStr));
//
//        // 打印公钥
//        System.out.println(Base64.getEncoder().encodeToString(publicKeyFromPrivate.getEncoded()));

    }
}
