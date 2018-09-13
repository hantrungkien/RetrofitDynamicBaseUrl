package com.kienht.retrofitdynamicbaseurl.parser;

import com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager;
import com.kienht.retrofitdynamicbaseurl.Utils;
import com.kienht.retrofitdynamicbaseurl.cache.Cache;
import com.kienht.retrofitdynamicbaseurl.cache.LruCache;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

public class AdvancedUrlParser implements UrlParser {
    private RetrofitDynamicBaseUrlManager mRetrofitDynamicBaseUrlManager;
    private Cache<String, String> mCache;

    @Override
    public void init(RetrofitDynamicBaseUrlManager retrofitDynamicBaseUrlManager) {
        this.mRetrofitDynamicBaseUrlManager = retrofitDynamicBaseUrlManager;
        this.mCache = new LruCache<>(100);
    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url) {
        if (null == domainUrl) return url;

        HttpUrl.Builder builder = url.newBuilder();

        if (Utils.isEmpty(mCache.get(getKey(domainUrl, url)))) {
            for (int i = 0; i < url.pathSize(); i++) {
                builder.removePathSegment(0);
            }

            List<String> newPathSegments = new ArrayList<>();
            newPathSegments.addAll(domainUrl.encodedPathSegments());

            if (url.pathSize() > mRetrofitDynamicBaseUrlManager.getPathSize()) {
                List<String> encodedPathSegments = url.encodedPathSegments();
                for (int i = mRetrofitDynamicBaseUrlManager.getPathSize(); i < encodedPathSegments.size(); i++) {
                    newPathSegments.add(encodedPathSegments.get(i));
                }
            } else if (url.pathSize() < mRetrofitDynamicBaseUrlManager.getPathSize()) {
                throw new IllegalArgumentException(String.format("Your final path is %s, but the baseUrl of your RetrofitUrlManager#startAdvancedModel is %s",
                        url.scheme() + "://" + url.host() + url.encodedPath(),
                        mRetrofitDynamicBaseUrlManager.getBaseUrl().scheme() + "://"
                                + mRetrofitDynamicBaseUrlManager.getBaseUrl().host()
                                + mRetrofitDynamicBaseUrlManager.getBaseUrl().encodedPath()));
            }

            for (String PathSegment : newPathSegments) {
                builder.addEncodedPathSegment(PathSegment);
            }
        } else {
            builder.encodedPath(mCache.get(getKey(domainUrl, url)));
        }

        HttpUrl httpUrl = builder
                .scheme(domainUrl.scheme())
                .host(domainUrl.host())
                .port(domainUrl.port())
                .build();

        if (Utils.isEmpty(mCache.get(getKey(domainUrl, url)))) {
            mCache.put(getKey(domainUrl, url), httpUrl.encodedPath());
        }
        return httpUrl;
    }

    private String getKey(HttpUrl domainUrl, HttpUrl url) {
        return domainUrl.encodedPath() + url.encodedPath()
                + mRetrofitDynamicBaseUrlManager.getPathSize();
    }
}
