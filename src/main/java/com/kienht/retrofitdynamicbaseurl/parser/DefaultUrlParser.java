package com.kienht.retrofitdynamicbaseurl.parser;

import com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager;
import okhttp3.HttpUrl;

import static com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager.IDENTIFICATION_PATH_SIZE;

public class DefaultUrlParser implements UrlParser {

    private UrlParser mDomainUrlParser;
    private volatile UrlParser mAdvancedUrlParser;
    private volatile UrlParser mSuperUrlParser;
    private RetrofitDynamicBaseUrlManager mRetrofitDynamicBaseUrlManager;

    @Override
    public void init(RetrofitDynamicBaseUrlManager retrofitDynamicBaseUrlManager) {
        this.mRetrofitDynamicBaseUrlManager = retrofitDynamicBaseUrlManager;
        this.mDomainUrlParser = new DomainUrlParser();
        this.mDomainUrlParser.init(retrofitDynamicBaseUrlManager);
    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url) {
        if (null == domainUrl) return url;

        if (url.toString().contains(IDENTIFICATION_PATH_SIZE)) {
            if (mSuperUrlParser == null) {
                synchronized (this) {
                    if (mSuperUrlParser == null) {
                        mSuperUrlParser = new SuperUrlParser();
                        mSuperUrlParser.init(mRetrofitDynamicBaseUrlManager);
                    }
                }
            }
            return mSuperUrlParser.parseUrl(domainUrl, url);
        }

        if (mRetrofitDynamicBaseUrlManager.isAdvancedModel()) {
            if (mAdvancedUrlParser == null) {
                synchronized (this) {
                    if (mAdvancedUrlParser == null) {
                        mAdvancedUrlParser = new AdvancedUrlParser();
                        mAdvancedUrlParser.init(mRetrofitDynamicBaseUrlManager);
                    }
                }
            }
            return mAdvancedUrlParser.parseUrl(domainUrl, url);
        }
        return mDomainUrlParser.parseUrl(domainUrl, url);
    }
}
