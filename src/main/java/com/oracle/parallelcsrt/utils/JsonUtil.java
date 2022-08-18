package com.oracle.parallelcsrt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtil {
    /**
     * Check Json is Valid or not
     * 
     * @param testString
     * @return
     */
    public static boolean isJsonValid(String testString) {
        try {
            JsonParser.parseString(testString);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    public static <T> T deserialize(String jsonString, Class<T> genericClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("MM/dd/yy HH:mm:ss");
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonString, genericClass);
    }

}
