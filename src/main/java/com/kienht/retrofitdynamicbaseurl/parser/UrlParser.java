package com.kienht.retrofitdynamicbaseurl.parser;

import com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager;
import okhttp3.HttpUrl;

public interface UrlParser {

    void init(RetrofitDynamicBaseUrlManager retrofitDynamicBaseUrlManager);

    HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url);
}
