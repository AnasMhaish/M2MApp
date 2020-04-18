package com.m2mapp.utilities;

public class ParseMessage {
    public static byte[] parseBytes(String str) {
        str = str.trim();
        String[] strArr = str.split(" ");
        if (strArr.length < 1) {
            return null;
        }
        byte[] result = new byte[strArr.length];
        int i = 0;
        for (String token : strArr) {
            try {
                result[i++] = (byte) Integer.parseInt(token);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return result;
    }

    public static byte[] parseBytesHex(String str) {
        str = str.trim();
        String[] strArr = str.split(" ");
        if (strArr.length < 1) {
            return null;
        }
        byte[] result = new byte[strArr.length];
        int i = 0;
        for (String token : strArr) {
            if (token.length() > 2) {
                return null;
            }
            try {
                result[i++] = (byte) Integer.parseInt(token, 16);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return result;
    }
}
