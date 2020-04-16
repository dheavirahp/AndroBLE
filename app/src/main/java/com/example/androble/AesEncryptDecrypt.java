package com.example.androble;

import android.util.Base64;

import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryptDecrypt
{

    private static final byte[] keyValue =
            new byte[]{'t', 'a', 'n', 'd', 'e', 'b', 'i', 'k', 'e', 's', 'k', 'r', 'i', 'p', 's', 'i'};

    public static String encrypt(String plaintext) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        //serialize
        String serializedPlaintext = plaintext;
        byte[] plaintextBytes = serializedPlaintext.getBytes("UTF-8");

        Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = c.getIV();
        byte[] encVal = c.doFinal(plaintextBytes);
        String encryptedData = Base64.encodeToString(encVal, Base64.NO_WRAP);

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(Base64.encode(iv, Base64.NO_WRAP));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        String mac = new String(Hex.encodeHex(calcMac));
        //Log.d("MAC",mac);

        AesEncryptionData aesData = new AesEncryptionData(
                Base64.encodeToString(iv, Base64.NO_WRAP),
                encryptedData,
                mac);

        String aesDataJson = new Gson().toJson(aesData);

        return Base64.encodeToString(aesDataJson.getBytes("UTF-8"), Base64.DEFAULT);
    }

    public static String decrypt(String ivValue, String encryptedData, String macValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = Base64.decode(ivValue.getBytes("UTF-8"), Base64.DEFAULT);
        byte[] decodedValue = Base64.decode(encryptedData.getBytes("UTF-8"), Base64.DEFAULT);

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(ivValue.getBytes("UTF-8"));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        byte[] mac = Hex.decodeHex(macValue.toCharArray());
        if (!Arrays.equals(calcMac, mac))
            return "MAC mismatch";

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // or PKCS5Padding
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decValue = c.doFinal(decodedValue);

        int firstQuoteIndex = 0;
        while (decValue[firstQuoteIndex] != (byte) '"') firstQuoteIndex++;
        return new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length - 2));
    }

}