# customer-profile

Akka SDK kata: event-sourced customer entities (with preferences and education/work history) and a query view, built on the Akka Java SDK.

## Requirements

- Java 21
- Maven

## Running locally

```
mvn compile exec:java
```

The service starts in dev mode on `http://localhost:9000` (see [application.conf](src/main/resources/application.conf)).

## API

All endpoints are served under `/api`.

### Customers

| Method | Path | Description |
| --- | --- | --- |
| POST | `/customers` | Create a customer (`customerId`, `FirstName`, `LastName`) |
| GET | `/customers` | List all customers |
| PUT | `/customers/{customerId}/email` | Update a customer's email |
| DELETE | `/customers/{customerId}` | Delete a customer |

### Customer profile (education & work history)

| Method | Path | Description |
| --- | --- | --- |
| POST | `/customers/{customerId}/profile/education` | Add an education entry |
| POST | `/customers/{customerId}/profile/work` | Add a work experience entry |
| GET | `/customers/{customerId}/profile` | Get the customer's profile |

### Preferences

| Method | Path | Description |
| --- | --- | --- |
| PUT | `/customers/{customerId}/preferences` | Set key-value preferences |
| GET | `/customers/{customerId}/preferences` | Get preferences |

A Postman collection covering these endpoints is available at [postman_collection.json](postman_collection.json).

## Project structure

```
src/main/java/com/pradeepl/akkakata/
  api/         HTTP endpoints
  domain/
    commands/  Command records handled by each entity
    entities/  Event-sourced / key-value entities
    events/    Domain events
    model/     State and value types
  views/       Query views over entity state
```

## Notes

Development notes and open issues are tracked in [daily-update.md](daily-update.md).
