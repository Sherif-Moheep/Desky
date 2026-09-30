package org.example.desky.network

object LaptopRoutes {
    const val PING = "/ping"
    const val TELEMETRY = "/telemetry"

    object Actions {
        const val LOCK = "/actions/lock"
        const val SLEEP = "/actions/sleep"
    }

    object Power {
        const val RESTART = "/power/restart"
        const val SHUTDOWN = "/power/shutdown"
    }
}
