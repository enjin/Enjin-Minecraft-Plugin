package com.enjin.bukkit.util;

import java.util.Base64;

public class EncodeUtil {
    public static String base64Encode(String string) {
        return Base64.getUrlEncoder().encodeToString(string.getBytes());
    }
}
