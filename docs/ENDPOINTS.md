# 📡 Desky API & Network Endpoints Reference

A complete technical specification of all REST endpoints, binary TCP protocols, and UDP discovery beacons used across the Desky ecosystem.

---

## Table of Contents
1. [Node Addressing & Discovery](#1-node-addressing--discovery)
2. [ESP32 Relay Controller (`Port 80`)](#2-esp32-relay-controller-port-80)
3. [Laptop Workstation Server (`Port 8080`)](#3-laptop-workstation-server-port-8080)
4. [Magic Home LED Strip Controller (`Port 5577`)](#4-magic-home-led-strip-controller-port-5577)

---

## 1. Node Addressing & Discovery

All nodes operate exclusively over the local Wi-Fi subnet (LAN) without internet routing:

| Node | Default Hostname / Service | Transport | Default Port | Description |
|:---|:---|:---:|:---:|:---|
| **ESP32 Relay** | `_desky-relay._tcp` | HTTP REST | `80` | AC charger relay control & status |
| **Laptop Server** | `_desky-laptop._tcp` | HTTP REST | `8080` | Windows OS commands & telemetry |
| **LED Strip** | UDP Broadcast | Raw TCP | `5577` | Magic Home 5050 RGB lighting |

> [!NOTE]
> The Android mobile app resolves device IP addresses using Android's native `NsdManager` (Network Service Discovery) with an active `WifiManager.MulticastLock`, caching the resolved LAN IPs (`192.168.x.x`) in DataStore.

---

## 2. ESP32 Relay Controller (`Port 80`)

**Base URL**: `http://<esp32-ip>:80`

### 2.1 Ping / Health Check
Quick round-trip latency probe used by the mobile app's ping diagnostic.

- **Method**: `GET`
- **Path**: `/ping`
- **Request Body**: *None*
- **Response**: `200 OK`
  ```text
  OK
  ```
- **Example cURL**:
  ```bash
  curl -i http://192.168.1.22/ping
  ```

---

### 2.2 Get Relay Status & Telemetry
Returns the physical relay state, Wi-Fi signal strength, and board uptime.

- **Method**: `GET`
- **Path**: `/status`
- **Request Body**: *None*
- **Response**: `200 OK` (`application/json`)
  ```json
  {
    "status": "online",
    "is_on": true,
    "uptime_seconds": 25480,
    "wifi_rssi": -58,
    "ip": "192.168.1.22"
  }
  ```
- **Example cURL**:
  ```bash
  curl http://192.168.1.22/status
  ```

---

### 2.3 Power ON (Energize Relay)
Sets `GPIO 4` to active-LOW, energizing the relay coil to restore AC wall power to the laptop charger and trigger BIOS AC-boot.

- **Method**: `POST`
- **Path**: `/power/on`
- **Request Body**: *None*
- **Response**: `200 OK` (`application/json`)
  ```json
  {
    "status": "online",
    "is_on": true,
    "uptime_seconds": 25485,
    "wifi_rssi": -58,
    "ip": "192.168.1.22"
  }
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.22/power/on
  ```

---

### 2.4 Power OFF (De-energize Relay)
Sets `GPIO 4` to HIGH, turning off the relay and cutting AC wall power to the laptop charger.

- **Method**: `POST`
- **Path**: `/power/off`
- **Request Body**: *None*
- **Response**: `200 OK` (`application/json`)
  ```json
  {
    "status": "online",
    "is_on": false,
    "uptime_seconds": 25492,
    "wifi_rssi": -58,
    "ip": "192.168.1.22"
  }
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.22/power/off
  ```

---

## 3. Laptop Workstation Server (`Port 8080`)

**Base URL**: `http://<laptop-ip>:8080`  
**Engine**: Ktor 3.0 (CIO) / Windows Background Service

### 3.1 Ping / Health Check
- **Method**: `GET`
- **Path**: `/ping`
- **Request Body**: *None*
- **Response**: `200 OK`
- **Example cURL**:
  ```bash
  curl -i http://192.168.1.2:8080/ping
  ```

---

### 3.2 Get Workstation Telemetry
Queries the Windows OS for battery metrics, AC connection state, lock state, and uptime.

- **Method**: `GET`
- **Path**: `/telemetry`
- **Request Body**: *None*
- **Response**: `200 OK` (`application/json`)
  ```json
  {
    "isLocked": false,
    "isAcConnected": true,
    "batteryPercent": 59,
    "uptimeSeconds": 26940,
    "uptimeFormatted": "7h 29m"
  }
  ```
- **Underlying Windows Commands**:
  - Battery & AC: `Get-CimInstance Win32_Battery | Select-Object EstimatedChargeRemaining, BatteryStatus`
  - System Uptime: `(Get-Date) - (Get-CimInstance Win32_OperatingSystem).LastBootUpTime`
- **Example cURL**:
  ```bash
  curl http://192.168.1.2:8080/telemetry
  ```

---

### 3.3 Lock Workstation
Instantly locks the active Windows user session.

- **Method**: `POST`
- **Path**: `/actions/lock`
- **Request Body**: *None*
- **Response**: `200 OK`
- **Underlying Windows Command**:
  ```powershell
  rundll32.exe user32.dll,LockWorkStation
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.2:8080/actions/lock
  ```

---

### 3.4 Sleep / Standby
Puts Windows into a low-power sleep / standby state.

- **Method**: `POST`
- **Path**: `/actions/sleep`
- **Request Body**: *None*
- **Response**: `200 OK`
- **Underlying Windows Command**:
  ```powershell
  rundll32.exe powrprof.dll,SetSuspendState 0,1,0
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.2:8080/actions/sleep
  ```

---

### 3.5 Restart Workstation
Initiates a graceful Windows restart.

- **Method**: `POST`
- **Path**: `/power/restart`
- **Request Body**: *None*
- **Response**: `200 OK`
- **Underlying Windows Command**:
  ```powershell
  shutdown.exe /r /t 5 /c "Restart initiated from Desky app"
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.2:8080/power/restart
  ```

---

### 3.6 Shutdown Workstation
Dispatches a clean Windows OS shutdown. This is called concurrently with the ESP32 `/power/off` command during the master desk power-off sequence.

- **Method**: `POST`
- **Path**: `/power/shutdown`
- **Request Body**: *None*
- **Response**: `200 OK`
- **Underlying Windows Command**:
  ```powershell
  shutdown.exe /s /t 5 /c "Shutdown initiated from Desky app"
  ```
- **Example cURL**:
  ```bash
  curl -X POST http://192.168.1.2:8080/power/shutdown
  ```

---

## 4. Magic Home LED Strip Controller (`Port 5577`)

The desk backlight uses an inexpensive Wi-Fi smart controller running the **Magic Home (Zengge/Flux)** protocol.

### 4.1 UDP Broadcast Discovery
The controller announces its presence or responds to broadcast queries on the local subnet.

- **Protocol**: UDP
- **Port**: `48899`
- **Payload**: `HF-A11ASSISTHREAD`
- **Response**: String format `IP,MAC,Model` (e.g. `192.168.1.19,ACCF23D8A1B2,HF-LPB100-ZJ200`)

---

### 4.2 Raw TCP Binary Commands (`Port 5577`)
The mobile app opens a direct TCP socket to `192.168.1.19:5577` and transmits byte frames with an 8-bit checksum:

$$\text{Checksum} = \left( \sum \text{Bytes} \right) \pmod{256}$$

#### Power ON
- **Bytes**: `0x71, 0x23, 0x0F, 0xA3`

#### Power OFF
- **Bytes**: `0x71, 0x24, 0x0F, 0xA4`

#### Set RGB Color
To set an RGB color (e.g. Red: $R=255, G=0, B=0$):
- **Byte 1**: `0x31` (Command header)
- **Byte 2**: $R$ (0–255)
- **Byte 3**: $G$ (0–255)
- **Byte 4**: $B$ (0–255)
- **Byte 5**: `0x00` (White channel / unused)
- **Byte 6**: `0xF0` (Color mask)
- **Byte 7**: `0x0F` (Trailer)
- **Byte 8**: Checksum $\left( 0x31 + R + G + B + 0x00 + 0xF0 + 0x0F \right) \pmod{256}$
