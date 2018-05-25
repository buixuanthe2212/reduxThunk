package com.jinjerkeihi.scancard.api.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jinjerkeihi.BuildConfig;
import com.jinjerkeihi.scancard.api.ApiService;
import com.jinjerkeihi.scancard.preference.JinjerKeihiPreference;

import java.io.IOException;
import java.lang.reflect.Modifier;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Use to create RestAdapter with options in order to request API.
 * <p/>
 * Needs to call "init" in Application before using it.
 *
 * @author LongHV3
 */
public final class ApiClient {
    private static final String TAG = ApiClient.class.getSimpleName();
//    private static final String HEADER_UA = "User-Agent";
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final int TIMEOUT_CONNECTION = 3000;

    @SuppressLint("StaticFieldLeak")
    private static ApiClient sInstance;
    private Context context;
    private ApiService service;


    public static synchronized ApiClient getInstance() {
        if (sInstance == null) {
            sInstance = new ApiClient();
        }
        return sInstance;
    }

    public static ApiService getService() {
        return getInstance().service;
    }

    private ApiClient() {
    }

    public void init(ApiConfig apiConfig) {
        context = apiConfig.context;
        OkHttpClient okHttpClient = provideOkHttpClientDefault(provideHttpLoggingInterceptor());
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
        final Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(apiConfig.baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        service = builder.build().create(ApiService.class);
    }

    protected HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }

    protected OkHttpClient provideOkHttpClientDefault(HttpLoggingInterceptor interceptor) {
        final JinjerKeihiPreference jinjerKeihiPreference = new JinjerKeihiPreference(context.getApplicationContext());
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
//        okBuilder.addInterceptor(interceptor);
        okBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request newRequest;
                newRequest = request.newBuilder()
//                        .addHeader(HEADER_UA, createUserAgent())
                        .addHeader(ACCESS_TOKEN,  jinjerKeihiPreference.token)
                        .build();
                return chain.proceed(newRequest);
            }
        });

        okBuilder.connectTimeout(TIMEOUT_CONNECTION, SECONDS);
        okBuilder.readTimeout(TIMEOUT_CONNECTION, SECONDS);
        okBuilder.writeTimeout(TIMEOUT_CONNECTION, SECONDS);

        return okBuilder.build();
    }

    private String createUserAgent() {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "occurs error when creating user agent!!!");
        }
        return System.getProperty("http.agent") + " " + context.getPackageName() + "/" + versionName;
    }
}
