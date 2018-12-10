package com.bridou_n.beaconscanner.dagger

import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


/**
 * Created by bridou_n on 24/08/2017.
 */

@Module
object NetworkModule {

    val EXAMPLE_BASE_URL = "http://www.example.com/"
    val DEVICE_NAME = "device-name"

    @JvmStatic @Provides @Singleton @Named("injectDeviceName")
    fun provideInjectTokenInterceptor(prefs: PreferencesHelper) : Interceptor {
        return Interceptor { chain ->
            val req = chain.request()

            val newReq = req.newBuilder()
                    .method(req.method(), req.body())

            if (prefs.loggingDeviceName != null) {
                newReq.header(DEVICE_NAME, "${prefs.loggingDeviceName}")
            }

            chain.proceed(newReq.build())
        }
    }

    @JvmStatic @Provides @Singleton @Named("logging")
    fun provideLoggingInterceptor() : Interceptor {
        val interceptor = HttpLoggingInterceptor()

        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @JvmStatic @Provides @Singleton
    fun provideOkHttpClient(@Named("injectDeviceName") injectTokenInterceptor: Interceptor,
                            @Named("logging") loggingInterceptor: Interceptor) : OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(injectTokenInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
    }

    @JvmStatic @Provides @Singleton
    fun provideGson() = GsonBuilder().create()

    @JvmStatic @Provides @Singleton
    fun provideRetrofit(httpClient: OkHttpClient, gson: Gson) : Retrofit {
        return Retrofit.Builder()
                .baseUrl(EXAMPLE_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()
    }

    @JvmStatic @Provides @Singleton
    fun provideLoggingService(retrofit: Retrofit) = retrofit.create(LoggingService::class.java)
}