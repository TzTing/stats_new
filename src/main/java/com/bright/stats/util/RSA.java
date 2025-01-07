package com.bright.stats.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * <p> Project: external-api - RSA </p>
 *
 * RSA加解密工具
 * @author Tz
 * @version 1.0.0
 * @date 2024/01/15 10:11
 * @since 1.0.0
 */
public class RSA {
 
    /**
     * 定义加密方式
     */
    public static final String KEY_RSA = "RSA";

    /**
     * 定义签名算法
     */
    private static final String KEY_RSA_SIGNATURE = "MD5withRSA";
 
    /**
     * 公钥
     */
    public static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 私钥
     */
    public static final String PRIVATE_KEY = "RSAPrivateKey";
 
    /**
     * 生成公私密钥对
     */
    public static Map<String, String> init() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_RSA);
        //设置密钥对的bit数，越大越安全，但速度减慢，一般使用512或1024
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        // 获取公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        // 获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
 
        String publicKeyStr = getPublicKey(publicKey);
        String privateKeyStr = getPrivateKey(privateKey);

        Map<String, String> map = new HashMap<>();
        map.put(PUBLIC_KEY, publicKeyStr);
        map.put(PRIVATE_KEY, privateKeyStr);
        return map;
    }
 
    /**
     * 获取Base64编码的公钥字符串
     */
    public static String getPublicKey(RSAPublicKey publicKey) {
        String str = "";
        Key key = (Key) publicKey;
        str = encryptBase64(key.getEncoded());
        return str;
    }
 
    /**
     * 获取Base64编码的私钥字符串
     */
    public static String getPrivateKey(RSAPrivateKey privateKey) {
        String str = "";
        Key key = (Key) privateKey;
        str = encryptBase64(key.getEncoded());
        return str;
    }
 
    /**
     * BASE64 解码
     *
     * @param key 需要Base64解码的字符串
     * @return 字节数组
     */
    public static byte[] decryptBase64(String key) {
        return Base64.getDecoder().decode(key);
    }
 
    /**
     * BASE64 编码
     *
     * @param key 需要Base64编码的字节数组
     * @return 字符串
     */
    public static String encryptBase64(byte[] key) {
        return new String(Base64.getEncoder().encode(key));
    }
 
    /**
     * 公钥加密
     *
     * @param encryptingStr
     * @return
     */
    public static String encryptByPublic(String encryptingStr, String publicKeyStr) {
        StringBuilder encryptedDataBuilder = new StringBuilder();
        try {
            // Base64解码公钥字符串
            byte[] publicKeyBytes = decryptBase64(publicKeyStr);
            // 构造X509EncodedKeySpec对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            // 初始化密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
            // 生成公钥对象
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            // 确保公钥是RSA公钥
            if (publicKey instanceof RSAPublicKey) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
                // 获取密钥长度（以字节为单位）
                int keyLength = rsaPublicKey.getModulus().bitLength() / 8;
                // 计算最大加密数据长度（适用于PKCS#1 v1.5）
                // 11字节用于填充
                int maxDataLength = keyLength - 11;

                // 待加密数据
                byte[] data = encryptingStr.getBytes(StandardCharsets.UTF_8);
                // 分割数据并逐块加密
                for (int i = 0; i < data.length; i += maxDataLength) {
                    int chunkSize = Math.min(maxDataLength, data.length - i);
                    byte[] dataChunk = Arrays.copyOfRange(data, i, i + chunkSize);
                    // 初始化Cipher对象，使用PKCS#1填充
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    // 设置加密模式
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                    // 加密数据块
                    byte[] encryptedDataChunk = cipher.doFinal(dataChunk);
                    // 将加密块的Base64编码添加到构建器中
                    if (encryptedDataBuilder.length() > 0) {
                        // 添加分隔符
                        encryptedDataBuilder.append(",");
                    }
                    encryptedDataBuilder.append(encryptBase64(encryptedDataChunk));
                }
                return encryptedDataBuilder.toString();
            } else {
                throw new InvalidKeyException("Provided key is not a RSA Public Key.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果发生异常，返回null或错误信息
            return null;
        }
    }


    /**
     * 私钥解密
     * @param encryptedDataList
     * @param privateKeyStr
     * @return
     */
    public static String decryptByPrivate(String encryptedDataList, String privateKeyStr) {
        StringBuilder decryptedDataBuilder = new StringBuilder();
        try {
            // 根据分隔符分割加密数据块
            String[] encryptedDataChunks = encryptedDataList.split(",");
            for (String encryptedDataChunk : encryptedDataChunks) {
                // Base64解码加密的数据块
                byte[] encryptedDataBytes = decryptBase64(encryptedDataChunk);
                // 其余解密步骤与前面的decryptByPrivate方法相同
                // Base64解码私钥字符串
                byte[] privateKeyBytes = decryptBase64(privateKeyStr);
                // 构造PKCS8EncodedKeySpec对象
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                // 初始化密钥工厂
                KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
                // 生成私钥对象
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
                // 初始化Cipher对象，使用PKCS#1填充
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                // 设置解密模式
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                // 解密数据块
                byte[] decryptedDataChunk = cipher.doFinal(encryptedDataBytes);
                // 将解密数据块转换为字符串，并添加到构建器中
                String decryptedDataChunkStr = new String(decryptedDataChunk, StandardCharsets.UTF_8);
                if (decryptedDataBuilder.length() > 0) {
                    // 如果不是第一个块，则不添加分隔符
                    decryptedDataBuilder.append("");
                }
                decryptedDataBuilder.append(decryptedDataChunkStr);
            }
            return decryptedDataBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果发生异常，返回null或错误信息
            return null;
        }
    }
 
    /**
     * 私钥加密
     *
     * @param encryptingStr
     * @return
     */
    public static String encryptByPrivate(String encryptingStr, String privateKeyStr) {
        try {
            byte[] privateKeyBytes = decryptBase64(privateKeyStr);
            // 获得私钥
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            // 取得待加密数据
            byte[] data = encryptingStr.getBytes(StandardCharsets.UTF_8);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            // 对数据加密
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            // 返回加密后由Base64编码的加密信息
            return encryptBase64(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return "";
    }
 
    /**
     * 公钥解密
     *
     * @param encryptedStr
     * @return
     */
    public static String decryptByPublic(String encryptedStr, String publicKeyStr) {
        try {
            // 对公钥解密
            byte[] publicKeyBytes = decryptBase64(publicKeyStr);
            // 取得公钥
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            // 取得待加密数据
            byte[] data = decryptBase64(encryptedStr);
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            PublicKey publicKey = factory.generatePublic(keySpec);
            // 对数据解密
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            // 返回UTF-8编码的解密信息
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return "";
    }
 
    /**
     * 用私钥对加密数据进行签名
     *
     * @param encryptedStr
     * @return
     */
    public static String sign(String encryptedStr, String privateKeyStr) {
        String str = "";
        try {
            //将私钥加密数据字符串转换为字节数组
            byte[] data = encryptedStr.getBytes();
            // 解密由base64编码的私钥
            byte[] bytes = decryptBase64(privateKeyStr);
            // 构造PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(bytes);
            // 指定的加密算法
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            // 取私钥对象
            PrivateKey key = factory.generatePrivate(pkcs);
            // 用私钥对信息生成数字签名
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initSign(key);
            signature.update(data);
            str = encryptBase64(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
 
    /**
     * 校验数字签名
     *
     * @param encryptedStr
     * @param sign
     * @return 校验成功返回true，失败返回false
     */
    public static boolean verify(String encryptedStr, String sign, String publicKeyStr) {
        boolean flag = false;
        try {
            //将私钥加密数据字符串转换为字节数组
            byte[] data = encryptedStr.getBytes();
            // 解密由base64编码的公钥
            byte[] bytes = decryptBase64(publicKeyStr);
            // 构造X509EncodedKeySpec对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            // 指定的加密算法
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
            // 取公钥对象
            PublicKey key = factory.generatePublic(keySpec);
            // 用公钥验证数字签名
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initVerify(key);
            signature.update(data);
            flag = signature.verify(decryptBase64(sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

}