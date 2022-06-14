package com.example.avatararvr.retrofit

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    const val mainServer = "https://api.avatarsdk.com/"


    val retrofitClient: Retrofit.Builder by lazy {

        val levelType: Level = Level.BODY


        val logging = HttpLoggingInterceptor()
        logging.level = levelType

        val okhttpClient = OkHttpClient.Builder()
        okhttpClient.addInterceptor(logging)
        addHeaders(okhttpClient)

        Retrofit.Builder()
            .baseUrl(mainServer)
            .client(okhttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiInterface: APIService by lazy {
        retrofitClient
            .build()
            .create(APIService::class.java)
    }

    private fun addHeaders(okHttpClient: OkHttpClient.Builder) {
        okHttpClient.addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }
    }
}