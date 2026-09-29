package org.example.desky

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.laptopRouting() {

    get("/ping") {
            call.respond(HttpStatusCode.OK)
    }

    get("/telemetry") {
        val telemetry = WindowsTelemetryService.getTelemetry()
        call.respond(telemetry)
    }

    post("/actions/lock") {
        val success = ActionsService.lock()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    post("/actions/sleep") {
        val success = ActionsService.sleep()
        if (success) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}