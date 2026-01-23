# RealTimeCracow
Real-time public transport tracking and route planning server for Kraków, Poland. Built with Java and Spring Boot, utilizing GTFS (General Transit Feed Specification) data to provide accurate information about trams and buses.

## Features

### 🚌 REST API Endpoint
**GET /departures** - Find the fastest direct connection between two stops
- **Parameters:**
  - `from` (required) - departure stop name
  - `to` (required) - destination stop name
  - `at` (optional) - departure time in ISO format (defaults to current time)
  - `feedType` (required) - type of GTFS feed to use
- **Response:** Next available direct departure with real-time delay information
- **Features:**
  - Real-time delay tracking from GTFS Realtime data
  - Smart calendar handling with exceptions support
  - Automatic data refresh mechanism

### 🔌 MCP Server Integration
- **List all stops** - retrieve complete list of available transit stops
- **Connection search** - find 3 fastest connections between stops with optional date/time
- **Departure board** - next 5 departures for specific stop and line with delay information

## MCP Client Configuration

### Prerequisites
Run the application on localhost (default port: 8080)

### Setup
Add the following configuration to your AI agent's MCP settings (works with Cursor, Windsurf, Claude Desktop, etc.):
```jsonc
{
  "mcpServers": {
    "realtime-cracow": {
      "url": "http://localhost:8080/sse"
    }
  }
}
```

### Usage
Once configured, your AI agent can:
- Query Kraków public transport schedules
- Search for optimal connections
- Check real-time delays and departures
- Access complete stop information
  
## Technology Stack
- **Java** - core programming language
- **Spring Boot** - application framework
- **Protocol Buffers** - GTFS Realtime data parsing

## Architecture
Application follows SOLID principles and leverages proven design patterns:
- REST controller layer
- Business services for GTFS data processing
- Data repositories with automatic refresh mechanism
- Calendar and exceptions handling
- Integrated real-time delay tracking
  
## Data Sources
- [GTFS Documentation](https://gtfs.org/documentation/overview/)
- [Kraków GTFS Feed](https://gtfs.ztp.krakow.pl/)

## Authors
- Krzysztof Pieczka
- Jakub Biłko
- Szymon Semeńczuk
