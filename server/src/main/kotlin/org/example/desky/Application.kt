package org.example.desky

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.desky.services.MdnsServerService

fun main() {
    MdnsServerService.start(port = 8080)
    Runtime.getRuntime().addShutdownHook(Thread {
        MdnsServerService.stop()
    })

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    monitor.subscribe(ApplicationStopping) {
        MdnsServerService.stop()
    }

    install(ContentNegotiation) {
        json()
    }

    routing {
        laptopRouting()
    }
}