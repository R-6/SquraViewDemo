package com.example.squraviewdemo;

import android.util.Base64;

import androidx.annotation.Nullable;

/**
 * Create by lvwenrui on 2021/7/20 14:42
 */
public class Utils {

    @Nullable
    public static byte[] decryByBase64(String encrypted) {
        try {
            return Base64.decode(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int convertTwoBytesToShort(byte low, byte high) {
        return (short) ((high << 8) | (low & 0xFF));
    }
}
