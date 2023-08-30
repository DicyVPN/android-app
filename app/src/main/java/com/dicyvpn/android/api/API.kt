package com.dicyvpn.android.api

import com.dicyvpn.android.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.lang.ref.WeakReference

const val BASE_URL = "https://api.dicyvpn.com"

interface PublicAPI {
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<Unit>

    class LoginRequest (val email: String, val password: String, val isDevice: Boolean = true)

    companion object {
        private var apiService: WeakReference<PublicAPI> = WeakReference(null)
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
                        .baseUrl("$BASE_URL/v1/public/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build().create(PublicAPI::class.java)
                )
            }
            return apiService.get()!!
        }
    }
}
