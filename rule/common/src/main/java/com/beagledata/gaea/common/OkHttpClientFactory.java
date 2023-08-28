package com.beagledata.gaea.common;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by liulu on 2020/5/14.
 */
public class OkHttpClientFactory {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build();

    public static OkHttpClient get() {
        return INSTANCE;
    }
}
