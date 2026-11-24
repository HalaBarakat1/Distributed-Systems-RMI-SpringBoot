# Homework 6 - Order system with REST + gRPC (Spring Boot)

Theory answers for question 2 (Spring Boot features, default thread pool): [ANSWERS-Q2.md](ANSWERS-Q2.md)

```
Client --REST (HTTP :8080)--> order-service --gRPC (:9090)--> user-service
                                   |
                                   +-- H2 in-memory DB (orders)
```

| Service | Role | Port |
|---|---|---|
| `user-service` | gRPC server: returns a user's data by `userId` (sample users 101 Ahmed, 102 Sara, 103 Omar) | 9090 (gRPC) |
| `order-service` | REST API `POST /api/orders`, checks the user through gRPC, saves the order with Spring Data JPA + H2 | 8080 (HTTP) |

Stack: Spring Boot 4.2.0, Java 21, Maven, built-in Spring gRPC starters, H2.

## Project layout

```
user.proto                         same file in both services: src/main/proto/user.proto
user-service/
  grpc/UserGrpcService             gRPC server (NOT_FOUND if the user does not exist)
  repository/UserRepository        in-memory users
order-service/
  controller/OrderController       REST layer (HTTP only)
  service/OrderService             business rules: verify user first, then save
  client/UserGrpcClient            gRPC layer: StatusRuntimeException -> app exceptions
  client/GrpcClientConfig          creates the gRPC stub (channel "user-service")
  entity/Order, repository/        JPA entity + repository
  dto/                             CreateOrderRequest, OrderResponse, ErrorResponse
  exception/GlobalExceptionHandler exceptions -> 400 / 404 / 503
```

Java classes for gRPC (`UserServiceGrpc`, `GetUserRequest`, `UserResponse`) are generated from `user.proto`
automatically during the Maven build (into `target/generated-sources`).

## Run

Start **user-service first**, then order-service, each in its own terminal (or press Run on the
`...Application` class in IntelliJ):

```powershell
cd user-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd order-service
.\mvnw.cmd spring-boot:run
```

> Keep the project in a folder whose path has no Arabic letters: Maven fails to start from such paths.

H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ordersdb`, user `sa`, empty password).

## Test

Either open `requests.http` in IntelliJ and press the green arrow next to each request, or run:

```powershell
powershell -ExecutionPolicy Bypass -File .\test-orders.ps1
```

For case 3, stop user-service (Ctrl+C in its terminal) and run:

```powershell
powershell -ExecutionPolicy Bypass -File .\test-orders.ps1 -Case 3
```

## Test results

| Case | Request | Expected | Result |
|---|---|---|---|
| User exists | `{"userId":101,"productName":"Laptop","quantity":2}` | 201 | 201 `{"orderId":5001,"userId":101,"userName":"Ahmed","productName":"Laptop","quantity":2,"status":"CREATED"}` |
| User not found | `{"userId":999,...}` | 404, no order saved | 404 `{"message":"User not found"}` |
| user-service stopped | `{"userId":102,...}` | 503 | 503 `{"message":"User service is unavailable"}` |
| Invalid data | `{"userId":101,"quantity":0}` | 400 | 400 `{"message":"productName is required, quantity must be at least 1"}` |
| Malformed JSON | `{"userId":101,` | 400 | 400 `{"message":"Malformed request body"}` |

After all cases, `GET /api/orders` returns only order 5001: the failed requests saved nothing.

## How errors are mapped

| What happens | gRPC status | Exception in order-service | HTTP |
|---|---|---|---|
| User missing | `NOT_FOUND` | `UserNotFoundException` | 404 |
| user-service stopped / no answer within 3 s | `UNAVAILABLE` / `DEADLINE_EXCEEDED` | `UserServiceUnavailableException` | 503 |
| Bad request body | - | `MethodArgumentNotValidException`, `HttpMessageNotReadableException` | 400 |

The order is saved only after `UserGrpcClient.getUser` returns successfully, so any of the failures above
stops the request before anything reaches the database.
