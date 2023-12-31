package com.dicyvpn.android.api

import android.os.Parcelable
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicyvpn.android.DicyVPN
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import okhttp3.Headers
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.lang.ref.WeakReference

const val TAG = "DicyVPN/API"
const val BASE_URL = "https://api.dicyvpn.com"

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
                val userAgent = DicyVPN.get().getUserAgent()
                val client = OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", userAgent)
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

    @POST("servers/connect/{id}")
    fun connect(@Path("id") id: String, @Body connectionRequest: ConnectionRequest): Call<ConnectionInfo>

    @POST("servers/disconnect/{id}")
    fun disconnect(@Path("id") id: String, @Body connectionRequest: ConnectionRequest): Call<Unit>

    @GET("logout")
    fun logout(): Call<Unit>

    class ServerList(val primary: Map<String, List<Server>>, val secondary: Map<String, List<Server>>) {
        enum class Type {
            @SerializedName("primary")
            PRIMARY,

            @SerializedName("secondary")
            SECONDARY
        }

        @Parcelize
        class Server(val id: String, val name: String, val type: Type, val country: String, val city: String, val load: Double) : Parcelable
    }

    class ConnectionRequest(val type: String, val protocol: String)

    class ConnectionInfo(val serverIp: String, val publicKey: String, val privateKey: String?, val internalIp: String, val ports: Ports) {
        class Ports(val wireguard: ProtocolPorts, val openvpn: ProtocolPorts) {
            class ProtocolPorts(val udp: List<Int>, val tcp: List<Int>)
        }
    }

    companion object {
        private var apiService: WeakReference<API> = WeakReference(null)
        fun get(): API {
            if (apiService.get() == null) {
                Log.i(TAG, "Creating new API service")
                val userAgent = DicyVPN.get().getUserAgent()
                val flowToken = DicyVPN.getPreferencesDataStore().data.map { it[stringPreferencesKey("auth.token")] ?: "" }

                val client = OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        val token = runBlocking {
                            flowToken.first()
                        }
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .header("User-Agent", userAgent)
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
