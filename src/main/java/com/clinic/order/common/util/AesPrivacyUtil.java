package com.clinic.order.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesPrivacyUtil {
    private AesPrivacyUtil() {}

    public static String encrypt(String plainText, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key(secret));
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("隐私数据加密失败", e); }
    }

    public static String decrypt(String cipherText, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key(secret));
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("隐私数据解密失败", e); }
    }

    private static SecretKeySpec key(String secret) {
        byte[] bytes = new byte[16];
        byte[] src = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, bytes, 0, Math.min(src.length, bytes.length));
        return new SecretKeySpec(bytes, "AES");
    }
}
