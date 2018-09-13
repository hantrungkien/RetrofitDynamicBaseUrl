# RetrofitDynamicBaseUrl

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![](https://jitpack.io/v/hantrungkien/RetrofitDynamicBaseUrl.svg)](https://jitpack.io/#hantrungkien/RetrofitDynamicBaseUrl)

## Let Retrofit support multiple dynamic baseUrl and can be change the baseUrl at runtime.

## Download

````gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
````

``` gradle
implementation 'com.github.hantrungkien:RetrofitDynamicBaseUrl:v1.0.0'

```

## Usage
### Initialize
``` java
 // When building OkHttpClient, the OkHttpClient.Builder() is passed to the with() method to initialize the configuration
 OkHttpClient = RetrofitDynamicBaseUrlManager.getInstance().with(new OkHttpClient.Builder())
                .build();
```

### Step 1
``` java
 public interface ApiService {
     @Headers({"Domain-Name: oicsoft"}) // Add the Domain-Name header
     @GET("/v2/book/{id}")
     Call<ResponseBody> getBook(@Path("id") int id);
}

```

### Step 2
``` java
 // You can change BaseUrl at any time while App is running (The interface that declared the Domain-Name header)
 RetrofitDynamicBaseUrlManager.getInstance().putDomain("douban", "https://api.oicsoft.com");
```

### If you want to change the global BaseUrl:
```java
 // BaseUrl configured in the Domain-Name header will override BaseUrl in the global setting
 RetrofitDynamicBaseUrlManager.getInstance().setGlobalDomain("your BaseUrl");

```

### Thanks

A special thanks go to [JessYan](https://github.com/JessYanCoding/RetrofitUrlManager).

I re-up this project to use inside Java Module.

### LICENCE

    Copyright 2018 Kien Han Trung

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
