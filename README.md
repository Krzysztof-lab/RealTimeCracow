# RealTimeCracow

Real-time public transport tracking and route planning API for Kraków, Poland.  
Built with **Java + Spring Boot**, parsing live **GTFS-RT** feeds to serve 
accurate departure and delay data for trams and buses.

> Integrates as an **MCP Server** — plug directly into Cursor, Claude Desktop, 
> Windsurf or any AI agent to query Kraków transit in natural language.

## Tech Stack

- **Java + Spring Boot** — REST API backend
- **GTFS / GTFS-Realtime** — static schedules + live delay feeds
- **Protocol Buffers** — GTFS-RT binary data parsing
- **SSE (Server-Sent Events)** — MCP transport layer
- **Gradle** — build & dependency management

## Features

### REST API

**GET /departures** — fastest direct connection between two stops

| Parameter  | Required | Description |
|------------|----------|-------------|
| `from`     | ✅       | Departure stop name |
| `to`       | ✅       | Destination stop name |
| `feedType` | ✅       | GTFS feed type (tram/bus) |
| `at`       | ❌       | Departure time ISO 8601 (default: now) |

- Live delay tracking from GTFS-RT stream
- Smart calendar handling with service exceptions
- Automatic feed refresh mechanism

### MCP Server (AI Agent Integration)

Connect any MCP-compatible AI agent to Kraków public transport data:

- `list_stops` — full list of available transit stops  
- `search_connections` — 3 fastest connections between stops  
- `departure_board` — next 5 departures for a given stop & line with delays

## Quick Start

### Prerequisites
- Java 21+
- Gradle 8+ (wrapper included)

### Run locally
```bash
git clone https://github.com/Krzysztof-lab/RealTimeCracow
cd RealTimeCracow
./gradlew bootRun
# API available at http://localhost:8080
```

### Connect to AI Agent (MCP)
Add to your agent's MCP config (Cursor, Claude Desktop, Windsurf):
```json
{
  "mcpServers": {
    "realtime-cracow": {
      "url": "http://localhost:8080/sse"
    }
  }
}
```

## Architecture
```
┌─────────────────────────────────────┐
│           REST Controller           │
├─────────────────────────────────────┤
│         Business Services           │
│   (GTFS parsing, route search,      │
│    delay calculation, calendar)     │
├─────────────────────────────────────┤
│      Data Repositories              │
│   (auto-refresh, GTFS-RT stream)    │
└─────────────────────────────────────┘
```

SOLID principles throughout — clean separation between 
controller, service and data layers.

## Data Sources

- [GTFS Specification](https://gtfs.org/documentation/overview/)
- [Kraków GTFS Feed — ZTP Kraków](https://gtfs.ztp.krakow.pl/)

## Authors

- Krzysztof Pieczka
- Jakub Biłko  
- Szymon Semeńczuk
