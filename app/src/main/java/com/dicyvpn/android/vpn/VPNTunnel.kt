package com.dicyvpn.android.vpn

import androidx.compose.runtime.MutableState
import com.wireguard.android.backend.Tunnel

class VPNTunnel(private val status: MutableState<Status>) : Tunnel {
    override fun getName(): String {
        return "DicyVPN"
    }

    override fun onStateChange(newState: Tunnel.State) {
        if (newState == Tunnel.State.UP) {
            status.value = Status.CONNECTED
        } else if (newState == Tunnel.State.DOWN) {
            status.value = Status.NOT_RUNNING
        }
    }
}