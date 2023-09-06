package com.dicyvpn.android.vpn

import androidx.compose.runtime.MutableState
import com.wireguard.android.backend.Tunnel

class VPNTunnel(private val status: MutableState<Status>) : Tunnel {
    private var lastState: Tunnel.State = Tunnel.State.DOWN
    private val waitForStoppedCallbacks: MutableList<() -> Unit> = mutableListOf()

    override fun getName(): String {
        return "DicyVPN"
    }

    override fun onStateChange(newState: Tunnel.State) {
        lastState = newState

        if (newState == Tunnel.State.UP) {
            status.value = Status.CONNECTED
        } else if (newState == Tunnel.State.DOWN) {
            status.value = Status.DISCONNECTED

            waitForStoppedCallbacks.removeAll {
                it()
                true
            }
        }
    }

    fun waitForStopped(callback: () -> Unit) {
        if (lastState == Tunnel.State.DOWN) {
            callback()
            return
        }

        waitForStoppedCallbacks += callback
    }
}