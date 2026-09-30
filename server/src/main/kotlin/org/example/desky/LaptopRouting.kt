package org.example.desky

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.example.desky.network.LaptopRoutes
import org.example.desky.services.ActionsService
import org.example.desky.services.PowerService
import org.example.desky.services.WindowsTelemetryService

fun Route.laptopRouting() {

    get(LaptopRoutes.PING) {
        call.respond(HttpStatusCode.OK)
    }

    get(LaptopRoutes.TELEMETRY) {
        val telemetry = WindowsTelemetryService.getTelemetry()
        call.respond(telemetry)
    }

    post(LaptopRoutes.Actions.LOCK) {
        val success = ActionsService.lock()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    post(LaptopRoutes.Actions.SLEEP) {
        val success = ActionsService.sleep()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    post(LaptopRoutes.Power.RESTART) {
        val success = PowerService.restart()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    post(LaptopRoutes.Power.SHUTDOWN) {
        val success = PowerService.shutdown()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}