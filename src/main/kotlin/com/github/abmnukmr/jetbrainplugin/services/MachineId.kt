package com.github.abmnukmr.jetbrainplugin.services

import java.net.NetworkInterface
import java.util.*

class MachineId {
    fun getMachineId(): String {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
        for (iface in networkInterfaces) {
            if (!iface.isLoopback && iface.hardwareAddress != null) {
                return UUID.nameUUIDFromBytes(iface.hardwareAddress).toString()
            }
        }
        throw RuntimeException("No valid MAC address found")
    }
}