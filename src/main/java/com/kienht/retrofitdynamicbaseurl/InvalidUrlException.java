package com.kienht.retrofitdynamicbaseurl;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String url) {
        super("You've configured an invalid url : " + (Utils.isEmpty(url) ? "EMPTY_OR_NULL_URL" : url));
    }
}
