package com.dicyvpn.android.vpn

import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicyvpn.android.DicyVPN
import com.dicyvpn.android.api.API
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import retrofit2.Callback

class VPN {
    companion object {
        private const val TAG = "DicyVPN/VPN"

        @Throws(Exception::class)
        fun connect(server: API.ServerList.Server, currentServer: API.ServerList.Server?) {
            DicyVPN.getStatus().value = Status.CONNECTING
            val response = API.get().connect(id = server.id, API.ConnectionRequest(type = server.type.name.lowercase(), protocol = "wireguard")).execute()

            if (!response.isSuccessful) {
                DicyVPN.getStatus().value = Status.DISCONNECTED

                val error = response.errorBody()?.string()
                val code: String
                val message: String

                try {
                    val json = JSONObject(error!!)
                    val reply = json.getJSONObject("reply")
                    code = reply.getString("code")
                    message = reply.getString("message")
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw Exception("Unknown error, please try again later\n\n$error")
                }

                when (code) {
                    "NO_SUBSCRIPTION" -> throw NoSubscriptionException()
                    else -> throw Exception(message)
                }
            }

            val info = response.body()!!
            stop(true, currentServer, newServer = server)
            Log.i(TAG, "Connecting to a WireGuard ${server.name} (${server.id})")

            val config = getWireGuardConfig(info)
            Log.d(TAG, "WireGuard config: $config")
            DicyVPN.getTunnel().waitForStopped {
                DicyVPN.setTunnelUp(config)
            }
        }

        fun stop(isSwitching: Boolean = false, currentServer: API.ServerList.Server?, newServer: API.ServerList.Server?) {
            if (DicyVPN.getStatus().value == Status.DISCONNECTED) {
                return
            }

            Log.i(TAG, "Stopping VPN")
            if (!isSwitching) {
                DicyVPN.getStatus().value = Status.DISCONNECTING
            }

            if (currentServer != null && currentServer.type == API.ServerList.Type.PRIMARY && currentServer.id != newServer?.id) {
                Log.i(TAG, "Disconnecting from the primary server")
                try {
                    API.get().disconnect(id = currentServer.id, API.ConnectionRequest(type = currentServer.type.name.lowercase(), protocol = "wireguard"))
                        .enqueue(object : Callback<Unit> {
                            override fun onResponse(call: retrofit2.Call<Unit>, response: retrofit2.Response<Unit>) {
                                Log.i(TAG, "Sent disconnection request for ${currentServer.name} (${currentServer.id})")
                            }

                            override fun onFailure(call: retrofit2.Call<Unit>, t: Throwable) {
                                Log.e(TAG, "Failed to send disconnection request for ${currentServer.name} (${currentServer.id})", t)
                            }
                        })
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to disconnect from the primary server", e)
                }
            }

            DicyVPN.setTunnelDown()
        }

        private fun getWireGuardConfig(info: API.ConnectionInfo): String {
            val dns = arrayOf("1.1.1.1", "1.1.0.0")
            val privateKey = runBlocking {
                DicyVPN.getPreferencesDataStore().data.map { it[stringPreferencesKey("auth.privateKey")] }.first()
            }

            return """
                [Interface]
                PrivateKey = $privateKey
                Address = ${info.internalIp}/32
                DNS = ${dns.joinToString(", ")}
                ExcludedApplications = ${DicyVPN.get().applicationContext.packageName}
    
                [Peer]
                PublicKey = ${info.publicKey}
                Endpoint = ${info.serverIp}:${info.ports.wireguard.udp[0]}
                PersistentKeepalive = 15
                AllowedIPs = 0.0.0.0/0, ::/0
            """.trimIndent()
        }
    }

    class NoSubscriptionException : Exception()
}
