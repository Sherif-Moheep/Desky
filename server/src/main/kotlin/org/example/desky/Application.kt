package org.example.desky

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.desky.services.LedService
import org.example.desky.services.MdnsServerService
import org.example.desky.services.SystemTrayService
import kotlin.system.exitProcess

fun main() {
    val port = 8080
    MdnsServerService.start(port = port)

    val server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)

    SystemTrayService.init(port = port) {
        SystemTrayService.remove()
        MdnsServerService.stop()
        server.stop(500, 1000)
        exitProcess(0)
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        SystemTrayService.remove()
        MdnsServerService.stop()
    })

    LedService.turnOnOnStartup()

    server.start(wait = true)
}

fun Application.module() {
    monitor.subscribe(ApplicationStopping) {
        SystemTrayService.remove()
        MdnsServerService.stop()
    }

    install(ContentNegotiation) {
        json()
    }

    routing {
        laptopRouting()
    }
}