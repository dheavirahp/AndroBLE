package com.example.androble;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES128 {
    private static final byte[] keyHex =
            new byte[]{'t', 'a', 'n', 'd', 'e', 'b', 'i', 'k', 'e', 's', 'k', 'r', 'i', 'p', 's', 'i'};

    public static String encrypt(String plaintext) throws Exception {

        byte[] plaintextHex = plaintext.getBytes("UTF-8");

        SecretKey key = new SecretKeySpec(keyHex, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plaintextHex);
        return toHex(encrypted);
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}