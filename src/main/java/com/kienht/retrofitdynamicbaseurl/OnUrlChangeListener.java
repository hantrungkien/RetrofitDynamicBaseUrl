package com.kienht.retrofitdynamicbaseurl;

import okhttp3.HttpUrl;

public interface OnUrlChangeListener {

    void onUrlChangeBefore(HttpUrl oldUrl, String domainName);

    void onUrlChanged(HttpUrl newUrl, HttpUrl oldUrl);
}
