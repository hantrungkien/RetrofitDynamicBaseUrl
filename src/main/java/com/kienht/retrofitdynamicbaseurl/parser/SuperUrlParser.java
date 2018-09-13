package com.kienht.retrofitdynamicbaseurl.parser;

import com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager;
import com.kienht.retrofitdynamicbaseurl.Utils;
import com.kienht.retrofitdynamicbaseurl.cache.Cache;
import com.kienht.retrofitdynamicbaseurl.cache.LruCache;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

import static com.kienht.retrofitdynamicbaseurl.RetrofitDynamicBaseUrlManager.IDENTIFICATION_PATH_SIZE;

public class SuperUrlParser implements UrlParser {
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

        int pathSize = resolvePathSize(url, builder);

        if (Utils.isEmpty(mCache.get(getKey(domainUrl, url, pathSize)))) {
            for (int i = 0; i < url.pathSize(); i++) {
                builder.removePathSegment(0);
            }

            List<String> newPathSegments = new ArrayList<>();
            newPathSegments.addAll(domainUrl.encodedPathSegments());


            if (url.pathSize() > pathSize) {
                List<String> encodedPathSegments = url.encodedPathSegments();
                for (int i = pathSize; i < encodedPathSegments.size(); i++) {
                    newPathSegments.add(encodedPathSegments.get(i));
                }
            } else if (url.pathSize() < pathSize) {
                throw new IllegalArgumentException(String.format(
                        "Your final path is %s, the pathSize = %d, but the #baseurl_path_size = %d, #baseurl_path_size must be less than or equal to pathSize of the final path",
                        url.scheme() + "://" + url.host() + url.encodedPath(), url.pathSize(), pathSize));
            }

            for (String PathSegment : newPathSegments) {
                builder.addEncodedPathSegment(PathSegment);
            }
        } else {
            builder.encodedPath(mCache.get(getKey(domainUrl, url, pathSize)));
        }

        HttpUrl httpUrl = builder
                .scheme(domainUrl.scheme())
                .host(domainUrl.host())
                .port(domainUrl.port())
                .build();

        if (Utils.isEmpty(mCache.get(getKey(domainUrl, url, pathSize)))) {
            mCache.put(getKey(domainUrl, url, pathSize), httpUrl.encodedPath());
        }
        return httpUrl;
    }

    private String getKey(HttpUrl domainUrl, HttpUrl url, int PathSize) {
        return domainUrl.encodedPath() + url.encodedPath()
                + PathSize;
    }

    private int resolvePathSize(HttpUrl httpUrl, HttpUrl.Builder builder) {
        String fragment = httpUrl.fragment();

        int pathSize = 0;
        StringBuffer newFragment = new StringBuffer();

        if (fragment.indexOf("#") == -1) {
            String[] split = fragment.split("=");
            if (split.length > 1) {
                pathSize = Integer.parseInt(split[1]);
            }
        } else {
            if (fragment.indexOf(IDENTIFICATION_PATH_SIZE) == -1) {
                int index = fragment.indexOf("#");
                newFragment.append(fragment.substring(index + 1, fragment.length()));
                String[] split = fragment.substring(0, index).split("=");
                if (split.length > 1) {
                    pathSize = Integer.parseInt(split[1]);
                }
            } else {
                String[] split = fragment.split(IDENTIFICATION_PATH_SIZE);
                newFragment.append(split[0]);
                if (split.length > 1) {
                    int index = split[1].indexOf("#");
                    if (index != -1) {
                        newFragment.append(split[1].substring(index, split[1].length()));
                        String substring = split[1].substring(0, index);
                        if (!Utils.isEmpty(substring)) {
                            pathSize = Integer.parseInt(substring);
                        }
                    } else {
                        pathSize = Integer.parseInt(split[1]);
                    }
                }
            }
        }
        if (Utils.isEmpty(newFragment.toString())) {
            builder.fragment(null);
        } else {
            builder.fragment(newFragment.toString());
        }
        return pathSize;
    }
}
