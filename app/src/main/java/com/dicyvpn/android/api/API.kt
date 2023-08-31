package com.dicyvpn.android.api

import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicyvpn.android.BuildConfig
import com.dicyvpn.android.DicyVPN
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.ref.WeakReference

const val TAG = "DicyVPN/API"
const val BASE_URL = "https://api.dicyvpn.com"
const val USER_AGENT = "DicyVPN Android v." + BuildConfig.VERSION_NAME

interface PublicAPI {
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<Unit>

    @POST("refresh-token")
    fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Call<Unit>

    class LoginRequest(val email: String, val password: String, val isDevice: Boolean = true)
    class RefreshTokenRequest(val refreshToken: String, val refreshTokenId: String, val accountId: String)

    companion object {
        private var apiService: WeakReference<PublicAPI> = WeakReference(null)
        fun get(): PublicAPI {
            if (apiService.get() == null) {
                Log.i(TAG, "Creating new PublicAPI service")
                val client = OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", USER_AGENT)
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

interface API {
    @GET("servers/list")
    fun getServersList(): Call<ServerList>

    class ServerList(val primary: Map<String, List<Server>>, val secondary: Map<String, List<Server>>) {
        class Server(val id: String, val name: String, val type: String, val country: String, val city: String, val load: Double)
    }

    companion object {
        private var apiService: WeakReference<API> = WeakReference(null)
        fun get(): API {
            if (apiService.get() == null) {
                Log.i(TAG, "Creating new API service")
                val flowToken = DicyVPN.getPreferencesDataStore().data.map { it[stringPreferencesKey("auth.token")] ?: "" }

                val client = OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        val token = runBlocking {
                            flowToken.first()
                        }
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", USER_AGENT)
                                .header("Authorization", "Bearer $token")
                                .build()
                        )
                    }
                    .addInterceptor { chain ->
                        val response = chain.proceed(chain.request())
                        if (response.code() == 401) { // Unauthorized, refresh the token
                            Log.i(TAG, "Token has expired, refreshing")
                            val (refreshToken, refreshTokenId, accountId) = runBlocking {
                                DicyVPN.getPreferencesDataStore().data.map {
                                    Triple(
                                        it[stringPreferencesKey("auth.refreshToken")] ?: "",
                                        it[stringPreferencesKey("auth.refreshTokenId")] ?: "",
                                        it[stringPreferencesKey("auth.accountId")] ?: ""
                                    )
                                }.first()
                            }
                            val refreshResponse = PublicAPI.get().refreshToken(PublicAPI.RefreshTokenRequest(refreshToken, refreshTokenId, accountId)).execute()

                            if (!refreshResponse.isSuccessful) {
                                Log.e(TAG, "Failed to refresh token, logging out")
                                removeAuthInfo()
                                return@addInterceptor response
                            }

                            setNewToken(refreshResponse.headers())
                            Log.i(TAG, "Token has been refreshed, retrying request")
                            response.close()
                            chain.proceed(chain.request())
                        } else {
                            response
                        }
                    }
                    .build()

                apiService = WeakReference(
                    Retrofit.Builder()
                        .baseUrl("$BASE_URL/v1/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build().create(API::class.java)
                )
            }
            return apiService.get()!!
        }

        @Throws(Exception::class)
        fun setAuthInfo(headers: Headers) {
            val token = headers.get("X-Auth-Token")!!
            val refreshToken = headers.get("X-Auth-Refresh-Token")!!
            val privateKey = headers.get("X-Auth-Private-Key")!!
            val payload = token.split(".")[1]
            val json = JSONObject(String(Base64.decode(payload, Base64.DEFAULT)))
            val refreshTokenId = json.getString("refreshTokenId")
            val accountId = json.getString("_id")

            runBlocking {
                DicyVPN.getPreferencesDataStore().edit {
                    it[stringPreferencesKey("auth.token")] = token
                    it[stringPreferencesKey("auth.refreshToken")] = refreshToken
                    it[stringPreferencesKey("auth.refreshTokenId")] = refreshTokenId
                    it[stringPreferencesKey("auth.accountId")] = accountId
                    it[stringPreferencesKey("auth.privateKey")] = privateKey
                }
            }
            Log.i(TAG, "Token has been set, accountId: $accountId")
        }

        @Throws(Exception::class)
        fun setNewToken(headers: Headers) {
            val token = headers.get("X-Auth-Token")!!

            runBlocking {
                DicyVPN.getPreferencesDataStore().edit {
                    it[stringPreferencesKey("auth.token")] = token
                }
            }
            Log.i(TAG, "Token has been refreshed")
        }

        @Throws(Exception::class)
        fun removeAuthInfo() {
            runBlocking {
                DicyVPN.getPreferencesDataStore().edit {
                    it.remove(stringPreferencesKey("auth.token"))
                    it.remove(stringPreferencesKey("auth.refreshToken"))
                    it.remove(stringPreferencesKey("auth.refreshTokenId"))
                    it.remove(stringPreferencesKey("auth.accountId"))
                    it.remove(stringPreferencesKey("auth.privateKey"))
                }
            }
        }
    }
}
