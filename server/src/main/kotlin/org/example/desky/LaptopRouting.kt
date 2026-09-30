package org.example.desky

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.example.desky.services.ActionsService
import org.example.desky.services.PowerService
import org.example.desky.services.WindowsTelemetryService

fun Route.laptopRouting() {

    get("/ping") {
            call.respond(HttpStatusCode.OK)
    }

    get("/telemetry") {
        val telemetry = WindowsTelemetryService.getTelemetry()
        call.respond(telemetry)
    }

    route("/actions") {
        post("/lock") {
            val success = ActionsService.lock()
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/sleep") {
            val success = ActionsService.sleep()
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    route("/power") {
        post("/restart") {
            val success = PowerService.restart()
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/shutdown") {
            val success = PowerService.shutdown()
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}