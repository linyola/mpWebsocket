package org.orial.websocket.utils;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;
import java.security.MessageDigest;

/**
 * 加解密安全类
 */
@Slf4j
public class SecurityUtil {
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private static final String ALGORITHM = "DES";
    private static String akey = "7c5dbf14aedd98a2b3b6d901d479f086";


    /**
     * 对字符串md5加密(大写+数字)
     *
     * @param s 传入要加密的字符串
     * @return  MD5加密后的字符串
     */

    public static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(s.getBytes("UTF-8"));
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
    /**
     * 转换密钥<br>
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(dks);
        return secretKey;
    }


    public static String decrypt(String src) {
        return decrypt(akey, src);
    }
    public static String decrypt(String key, String src) {
        try{
            return new String(decrypt(key, decoder.decodeBuffer(src)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String key, String src, String encoding) {
        try{
            return new String(decrypt(key, decoder.decodeBuffer(src)), encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(String key, byte[] src) {
        try{
            Key k = toKey(key.getBytes());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, k);
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String src) {
        try{
            return encoder.encode(encryptDes(akey, src.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String encrypt(String key, String src) {
        try{
            return encoder.encode(encryptDes(key, src.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String key, String src, String encoding) {
        try{
            return encoder.encode(encryptDes(key, src.getBytes(encoding)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static byte[] encryptDes(String key, byte[] src) {
        try{
            Key k = toKey(key.getBytes());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, k);
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String src = "nba历史上单赛季最高胜场是哪支球队";
        String key = "7c5dbf14aedd98a2b3b6d901d479f086";
        String encrypt = SecurityUtil.encrypt(key, src);
        log.info("加密后:{}", encrypt);
        String decrypt = SecurityUtil.decrypt(key,encrypt);
        log.info("解密后:{}", decrypt);
        int sum = 0;
        for(int i=11; i>0; i--) {
            sum += 11/i;
        }
        System.out.println(sum);
    }

}
