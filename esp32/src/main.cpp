#include <Arduino.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>

#define DEBUG 1
#define RELAY_PIN 4

#if DEBUG
  #define DEBUG_BEGIN(baud) Serial.begin(baud)
  #define DEBUG_PRINT(x)    Serial.print(x)
  #define DEBUG_PRINTLN(x)  Serial.println(x)
#else
  #define DEBUG_BEGIN(baud)
  #define DEBUG_PRINT(x)
  #define DEBUG_PRINTLN(x)
#endif

// =========================================================================
// Global State & Function Prototypes
// =========================================================================
WebServer server(80);
bool isRelayOn = false;

void setRelay(bool on);
void handlePing();
void handleStatus();
void handlePowerOn();
void handlePowerOff();

// =========================================================================
// Main Setup & Loop
// =========================================================================
void setup() {
  DEBUG_BEGIN(115200);

  digitalWrite(RELAY_PIN, HIGH);
  pinMode(RELAY_PIN, OUTPUT);

  WiFiManager wm;
  wm.setConfigPortalTimeout(180); // 3 minutes

  bool isConnected = wm.autoConnect("Desky-Setup");
  if (!isConnected) {
    DEBUG_PRINTLN("Failed to connect or timed out. Restarting...");
    delay(2000);
    ESP.restart();
  }

  DEBUG_PRINTLN("\nConnected to Wi-Fi!");
  DEBUG_PRINT("IP Address: ");
  DEBUG_PRINTLN(WiFi.localIP());

  if (MDNS.begin("desky-relay")) {
    MDNS.addService("desky-relay", "tcp", 80);
    MDNS.addServiceTxt("desky-relay", "tcp", "device", "relay");
    DEBUG_PRINTLN("Auto-discovery beacon active: _desky-relay._tcp");
  } else {
    DEBUG_PRINTLN("Failed to start discovery beacon");
  }

  server.on("/ping", HTTP_GET, handlePing);
  server.on("/status", HTTP_GET, handleStatus);
  server.on("/power/on", HTTP_POST, handlePowerOn);
  server.on("/power/off", HTTP_POST, handlePowerOff);

  server.begin();
  DEBUG_PRINTLN("HTTP REST server started");
}

void loop() {
  server.handleClient();
}

// =========================================================================
// Helper & Route Handler Implementations
// =========================================================================
void setRelay(bool on) {
  isRelayOn = on;
  digitalWrite(RELAY_PIN, on ? LOW : HIGH); // Active-LOW logic
  DEBUG_PRINTLN(on ? "Relay turned ON" : "Relay turned OFF");
}

void handlePing() {
  server.send(200, "text/plain", "OK");
}

void handleStatus() {
  JsonDocument doc;
  doc["status"] = "online";
  doc["is_on"] = isRelayOn;
  doc["uptime_seconds"] = millis() / 1000;
  doc["wifi_rssi"] = WiFi.RSSI();
  doc["ip"] = WiFi.localIP().toString();

  String response;
  serializeJson(doc, response);
  server.send(200, "application/json", response);
}

void handlePowerOn() {
  setRelay(true);
  handleStatus();
}

void handlePowerOff() {
  setRelay(false);
  handleStatus();
}