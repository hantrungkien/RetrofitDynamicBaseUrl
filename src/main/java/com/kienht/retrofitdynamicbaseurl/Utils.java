package com.kienht.retrofitdynamicbaseurl;

import okhttp3.HttpUrl;

public class Utils {

    private Utils() {
        throw new IllegalStateException("do not instantiation me");
    }

    static HttpUrl checkUrl(String url) {
        HttpUrl parseUrl = HttpUrl.parse(url);
        if (null == parseUrl) {
            throw new InvalidUrlException(url);
        } else {
            return parseUrl;
        }
    }

    static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.equals("");
    }
}
