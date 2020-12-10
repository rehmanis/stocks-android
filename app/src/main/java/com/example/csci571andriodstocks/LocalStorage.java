package com.example.csci571andriodstocks;


import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalStorage {

    public static final String SHARED_PREFS_FILE = "mypref";
    public static final String FAVOURITES = "favourites";
    public static final String PORTFOLIO = "portfolio";
    public static final String NET_WORTH = "networth";
    public static final String CASH_IN_HAND = "cash";


    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;


    public LocalStorage(SharedPreferences sharedPreferences, SharedPreferences.Editor editor){
        this.sharedPreferences = sharedPreferences;
        this.editor = editor;

    }


    public static <K, V> void setMap(String key, Map<K,V> dict) {
        Gson gson = new Gson();
        String json = gson.toJson(dict);

        set(key, json);
    }

    private static void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }


    public static <K, V> Map<K,V> getFromStorage(String key){
        Map<K, V> map = new LinkedHashMap<>();
        String serializedObject = sharedPreferences.getString(key, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<K, V>>(){}.getType();
            map = gson.fromJson(serializedObject, type);
        }
        return map;
    }


}
