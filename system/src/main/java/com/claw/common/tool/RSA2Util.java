package com.claw.common.tool;

import com.claw.common.exception.BusinessException;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author Sakura
 * @date 2024/12/6 19:10
 */
public class RSA2Util {

    // 定义密钥长度
    private static final int KEY_SIZE = 1024; // 更安全的密钥长度
    private static final String ALGORITHM = "RSA";

    /**
     * 生成RSA密钥对
     *
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
     *
     * @param data         待加密的数据
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 加密后的数据（Base64编码）
     * @throws Exception 如果加密失败
     */
    public static String encrypt(String data, String publicKeyStr) throws Exception {
        PublicKey publicKey = getPublicKeyFromBase64(publicKeyStr);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 使用私钥解密
     *
     * @param encryptedData Base64编码的加密数据
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 解密后的数据
     * @throws Exception 如果解密失败
     */
    public static String decrypt(String encryptedData, String privateKeyStr) throws Exception {
        try {
            PrivateKey privateKey = getPrivateKeyFromBase64(privateKeyStr);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
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
     * 使用私钥对数据进行签名（SHA256withRSA）
     *
     * @param data          待签名的数据
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 签名后的数据（Base64编码）
     * @throws Exception 如果签名失败
     */
    public static String sign(String data, String privateKeyStr) throws Exception {
        PrivateKey privateKey = getPrivateKeyFromBase64(privateKeyStr);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    /**
     * 使用公钥验证签名（SHA256withRSA）
     *
     * @param data         原始数据
     * @param signedData   Base64编码的签名数据
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 验签结果，true为签名有效
     * @throws Exception 如果验证失败
     */
    public static boolean verify(String data, String signedData, String publicKeyStr) throws Exception {
        PublicKey publicKey = getPublicKeyFromBase64(publicKeyStr);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] decodedSignedData = Base64.getDecoder().decode(signedData);
        return signature.verify(decodedSignedData);
    }

    public static PublicKey getPublicKeyFromBase64(String publicKeyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKeyFromBase64(String privateKeyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    public static String encodePublicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String encodePrivateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static void main(String[] args) throws Exception {
        // 生成私钥和公钥
        KeyPair keyPair = generateKeyPair();
        String publicKeyStr = encodePublicKeyToBase64(keyPair.getPublic());
        String privateKeyStr = encodePrivateKeyToBase64(keyPair.getPrivate());

        System.out.println("公钥: " + publicKeyStr);
        System.out.println("私钥: " + privateKeyStr);

        // 测试加密解密
        String data = "qaz123456.";
        String encryptedData = encrypt(data, publicKeyStr);
        System.out.println("加密后: " + encryptedData);
        String decryptedData = decrypt(encryptedData, privateKeyStr);
        System.out.println("解密后: " + decryptedData);

        // 测试签名和验签
        String signature = sign(data, privateKeyStr);
        System.out.println("签名: " + signature);
        boolean isVerified = verify(data, signature, publicKeyStr);
        System.out.println("验签结果: " + isVerified);
    }
}
