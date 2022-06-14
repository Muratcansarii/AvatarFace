package com.example.avatararvr.retrofit

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response = chain.proceed(request)

        var tryCount = 0
        while (!response.isSuccessful && tryCount < 3) {
            tryCount++
            Log.d("Service", "retry")
            response = chain.proceed(request)
        }
        return response

    }
}