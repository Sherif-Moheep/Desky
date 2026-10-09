package org.example.desky.devices.esp.data.datasource

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
import org.example.desky.devices.esp.data.model.EspRelayDto

class EspRemoteDataSource(
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
        val endpoint = settingsRepository.espEndpointFlow.first()
        return "http://${endpoint.host}:${endpoint.port}"
    }

    suspend fun ping(): HttpResponse = client.get("${baseUrl()}${EspRoutes.PING}")

    suspend fun getStatus(): EspRelayDto = client.get("${baseUrl()}${EspRoutes.STATUS}").body()

    suspend fun powerOn(): HttpResponse = client.post("${baseUrl()}${EspRoutes.Power.ON}")

    suspend fun powerOff(): HttpResponse = client.post("${baseUrl()}${EspRoutes.Power.OFF}")
}
