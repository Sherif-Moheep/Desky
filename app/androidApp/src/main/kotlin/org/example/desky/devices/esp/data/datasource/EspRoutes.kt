package org.example.desky.devices.esp.data.datasource

object EspRoutes {
    const val PING = "/ping"
    const val STATUS = "/status"

    object Power {
        const val ON = "/power/on"
        const val OFF = "/power/off"
    }
}
