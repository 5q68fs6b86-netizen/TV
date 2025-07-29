package com.fongmi.android.tv.api;

import java.security.MessageDigest;

public class Util {

    public static byte[] sign(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(name.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString().getBytes();
        } catch (Exception e) {
            return name.getBytes();
        }
    }
}