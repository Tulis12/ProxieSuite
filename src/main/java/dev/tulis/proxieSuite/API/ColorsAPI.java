package dev.tulis.proxieSuite.API;

/**
 * ColorsAPI
 */
public class ColorsAPI {

    public static String stripColors(String str) {
        return str.replaceAll("(?<!\\\\)&.", "").replaceAll("(?<!\\\\)§.", "");
    }
}
