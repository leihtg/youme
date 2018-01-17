package com.core.util;

import com.anser.model.FileModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thinkpad on 2018/1/15 20:23.
 */
public class JSONUtil {
    private static Gson gson = new Gson();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json,Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static List<FileModel> getFileModelList(String json) {
        return gson.fromJson(json, new TypeToken<List<FileModel>>() {
        }.getType());
    }

    public static void main(String[] args) {
        FileModel fm = new FileModel();
        fm.setDir(true);
        fm.setName("hehe");
        List<FileModel> list = new ArrayList<>();
        list.add(fm);
        String json = gson.toJson(fm);
        System.out.println(json);
        fm = fromJson(json,FileModel.class);
        System.out.println(fm);
    }
}
