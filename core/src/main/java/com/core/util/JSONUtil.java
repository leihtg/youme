package com.core.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Thinkpad on 2018/1/15 20:23.
 */
public class JSONUtil {
    private static Gson gson = new Gson();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json) {
        return gson.fromJson(json, new TypeToken<T>() {
        }.getType());
    }
}
