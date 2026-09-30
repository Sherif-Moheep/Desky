package org.example.desky

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testPing() = testApplication {
        application {
            module()
        }
        val response = client.get("/ping")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}