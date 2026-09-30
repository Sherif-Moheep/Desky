package org.example.desky.devices.laptop.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.model.LaptopTelemetryDto
import org.example.desky.network.LaptopRoutes

class LaptopRemoteDataSource(
    private val settingsRepository: SettingsRepository
) {
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 2000
            requestTimeoutMillis = 3000
            socketTimeoutMillis = 3000
        }
    }
    private suspend fun baseUrl(): String {
        val endpoint = settingsRepository.laptopEndpointFlow.first()
        return "http://${endpoint.host}:${endpoint.port}"
    }

    suspend fun ping(): HttpResponse = client.get("${baseUrl()}${LaptopRoutes.PING}")

    suspend fun getTelemetry(): LaptopTelemetryDto = client.get("${baseUrl()}${LaptopRoutes.TELEMETRY}").body()

    suspend fun lock(): HttpResponse = client.post("${baseUrl()}${LaptopRoutes.Actions.LOCK}")

    suspend fun sleep(): HttpResponse = client.post("${baseUrl()}${LaptopRoutes.Actions.SLEEP}")

    suspend fun restart(): HttpResponse = client.post("${baseUrl()}${LaptopRoutes.Power.RESTART}")

    suspend fun shutdown(): HttpResponse = client.post("${baseUrl()}${LaptopRoutes.Power.SHUTDOWN}")
}
