package com.kienht.retrofitdynamicbaseurl;

import com.kienht.retrofitdynamicbaseurl.parser.DefaultUrlParser;
import com.kienht.retrofitdynamicbaseurl.parser.UrlParser;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kienht.retrofitdynamicbaseurl.Utils.checkNotNull;
import static com.kienht.retrofitdynamicbaseurl.Utils.checkUrl;

public class RetrofitDynamicBaseUrlManager {
    private static final String TAG = "RetrofitUrlManager";
    private static final boolean DEPENDENCY_OKHTTP;
    private static final String DOMAIN_NAME = "Domain-Name";
    private static final String GLOBAL_DOMAIN_NAME = "com.kienht.retrofitdynamicbaseurl.parser.globalDomainName";
    public static final String DOMAIN_NAME_HEADER = DOMAIN_NAME + ": ";

    public static final String IDENTIFICATION_IGNORE = "#url_ignore";
    public static final String IDENTIFICATION_PATH_SIZE = "#baseurl_path_size=";

    private HttpUrl baseUrl;
    private int pathSize;
    private boolean isRun = true;
    private boolean debug = false;
    private final Map<String, HttpUrl> mDomainNameHub = new HashMap<>();
    private final Interceptor mInterceptor;
    private final List<OnUrlChangeListener> mListeners = new ArrayList<>();
    private UrlParser mUrlParser;

    static {
        boolean hasDependency;
        try {
            Class.forName("okhttp3.OkHttpClient");
            hasDependency = true;
        } catch (ClassNotFoundException e) {
            hasDependency = false;
        }
        DEPENDENCY_OKHTTP = hasDependency;
    }


    private RetrofitDynamicBaseUrlManager() {
        if (!DEPENDENCY_OKHTTP) {
            throw new IllegalStateException("Must be dependency Okhttp");
        }
        UrlParser urlParser = new DefaultUrlParser();
        urlParser.init(this);
        setUrlParser(urlParser);
        this.mInterceptor = chain -> {
            if (!isRun())
                return chain.proceed(chain.request());
            return chain.proceed(processRequest(chain.request()));
        };
    }

    private static class RetrofitUrlManagerHolder {
        private static final RetrofitDynamicBaseUrlManager INSTANCE = new RetrofitDynamicBaseUrlManager();
    }

    public static final RetrofitDynamicBaseUrlManager getInstance() {
        return RetrofitUrlManagerHolder.INSTANCE;
    }

    public OkHttpClient.Builder with(OkHttpClient.Builder builder) {
        checkNotNull(builder, "builder cannot be null");
        return builder
                .addInterceptor(mInterceptor);
    }

    public Request processRequest(Request request) {
        if (request == null) return request;

        Request.Builder newBuilder = request.newBuilder();

        String url = request.url().toString();
        if (url.contains(IDENTIFICATION_IGNORE)) {
            return pruneIdentification(newBuilder, url);
        }

        String domainName = obtainDomainNameFromHeaders(request);

        HttpUrl httpUrl;

        Object[] listeners = listenersToArray();

        if (!Utils.isEmpty(domainName)) {
            notifyListener(request, domainName, listeners);
            httpUrl = fetchDomain(domainName);
            newBuilder.removeHeader(DOMAIN_NAME);
        } else {
            notifyListener(request, GLOBAL_DOMAIN_NAME, listeners);
            httpUrl = getGlobalDomain();
        }

        if (null != httpUrl) {
            HttpUrl newUrl = mUrlParser.parseUrl(httpUrl, request.url());
            if (debug)

                if (listeners != null) {
                    for (int i = 0; i < listeners.length; i++) {
                        ((OnUrlChangeListener) listeners[i]).onUrlChanged(newUrl, request.url()); // 通知监听器此 Url 的 BaseUrl 已被切换
                    }
                }

            return newBuilder
                    .url(newUrl)
                    .build();
        }

        return newBuilder.build();

    }

    private Request pruneIdentification(Request.Builder newBuilder, String url) {
        String[] split = url.split(IDENTIFICATION_IGNORE);
        StringBuffer buffer = new StringBuffer();
        for (String s : split) {
            buffer.append(s);
        }
        return newBuilder
                .url(buffer.toString())
                .build();
    }

    private void notifyListener(Request request, String domainName, Object[] listeners) {
        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                ((OnUrlChangeListener) listeners[i]).onUrlChangeBefore(request.url(), domainName);
            }
        }
    }

    public boolean isRun() {
        return this.isRun;
    }

    public void setRun(boolean run) {
        this.isRun = run;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void startAdvancedModel(String baseUrl) {
        checkNotNull(baseUrl, "baseUrl cannot be null");
        startAdvancedModel(checkUrl(baseUrl));
    }

    public synchronized void startAdvancedModel(HttpUrl baseUrl) {
        checkNotNull(baseUrl, "baseUrl cannot be null");
        this.baseUrl = baseUrl;
        this.pathSize = baseUrl.pathSize();
        List<String> baseUrlpathSegments = baseUrl.pathSegments();
        if ("".equals(baseUrlpathSegments.get(baseUrlpathSegments.size() - 1))) {
            this.pathSize -= 1;
        }
    }

    public int getPathSize() {
        return pathSize;
    }

    public boolean isAdvancedModel() {
        return baseUrl != null;
    }

    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    public String setUrlNotChange(String url) {
        checkNotNull(url, "url cannot be null");
        return url + IDENTIFICATION_IGNORE;
    }

    public String setPathSizeOfUrl(String url, int pathSize) {
        checkNotNull(url, "url cannot be null");
        if (pathSize < 0) throw new IllegalArgumentException("pathSize must be >= 0");
        return url + IDENTIFICATION_PATH_SIZE + pathSize;
    }

    public void setGlobalDomain(String globalDomain) {
        checkNotNull(globalDomain, "globalDomain cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.put(GLOBAL_DOMAIN_NAME, Utils.checkUrl(globalDomain));
        }
    }

    public synchronized HttpUrl getGlobalDomain() {
        return mDomainNameHub.get(GLOBAL_DOMAIN_NAME);
    }

    public void removeGlobalDomain() {
        synchronized (mDomainNameHub) {
            mDomainNameHub.remove(GLOBAL_DOMAIN_NAME);
        }
    }

    public void putDomain(String domainName, String domainUrl) {
        checkNotNull(domainName, "domainName cannot be null");
        checkNotNull(domainUrl, "domainUrl cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.put(domainName, Utils.checkUrl(domainUrl));
        }
    }

    public synchronized HttpUrl fetchDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        return mDomainNameHub.get(domainName);
    }

    public void removeDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.remove(domainName);
        }
    }

    public void clearAllDomain() {
        mDomainNameHub.clear();
    }

    public synchronized boolean haveDomain(String domainName) {
        return mDomainNameHub.containsKey(domainName);
    }

    public synchronized int domainSize() {
        return mDomainNameHub.size();
    }

    public void setUrlParser(UrlParser parser) {
        checkNotNull(parser, "parser cannot be null");
        this.mUrlParser = parser;
    }

    public void registerUrlChangeListener(OnUrlChangeListener listener) {
        checkNotNull(listener, "listener cannot be null");
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void unregisterUrlChangeListener(OnUrlChangeListener listener) {
        checkNotNull(listener, "listener cannot be null");
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    private Object[] listenersToArray() {
        Object[] listeners = null;
        synchronized (mListeners) {
            if (mListeners.size() > 0) {
                listeners = mListeners.toArray();
            }
        }
        return listeners;
    }

    private String obtainDomainNameFromHeaders(Request request) {
        List<String> headers = request.headers(DOMAIN_NAME);
        if (headers == null || headers.size() == 0)
            return null;
        if (headers.size() > 1)
            throw new IllegalArgumentException("Only one Domain-Name in the headers");
        return request.header(DOMAIN_NAME);
    }
}
