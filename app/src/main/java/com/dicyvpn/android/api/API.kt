package com.dicyvpn.android.api

import com.dicyvpn.android.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference

const val BASE_URL = "https://api.dicyvpn.com"

interface PublicAPI {

    companion object {
        private lateinit var apiService: WeakReference<PublicAPI>
        fun get(): PublicAPI {
            if (apiService.get() == null) {
                val client = OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", "DicyVPN Android v." + BuildConfig.VERSION_NAME)
                                .build()
                        )
                    }
                    .build()

                apiService = WeakReference(
                    Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build().create(PublicAPI::class.java)
                )
            }
            return apiService.get()!!
        }
    }
}
