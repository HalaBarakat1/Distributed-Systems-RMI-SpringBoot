# Homework 6 - Java RMI & Spring Boot (REST + gRPC)

The full report (Arabic) is in [Report.pdf](Report.pdf).

```
rmi-lab/                      course RMI lab (EX1-EX3) + the Skype-like chat case study
  src/main/java/org/ds/skype/   shared/ server/ client/  - callbacks and peer-to-peer messages
  SKYPE.md                      how to run it, the Callback explanation, RMI use cases
grpc-orders/                  Spring Boot 4.2 microservices (Java 21, Maven)
  user-service/                 gRPC server (port 9090): users by id
  order-service/                REST API (port 8080) + JPA/H2, checks users via gRPC
  ANSWERS-Q2.md                 Spring Boot theory: features, default thread pool
  requests.http, test-orders.ps1  the required test cases
Report.pdf                    report with screenshots of every run
```

> Keep the projects in a folder whose path has no Arabic letters: Maven and the IntelliJ
> builder fail on such paths (`InvalidPathException: Illegal char <?>`).

## Question 1 - RMI chat (Skype-like)

Open `rmi-lab` in IntelliJ, run `org.ds.skype.server.Server`, then run `org.ds.skype.client.Client`
two or more times (Run configuration -> Modify options -> Allow multiple instances).

```
/signup Ahmad 123 Ahmad Ali      /login Ahmad 123 General
/msg Sara Hi                     message through the server (server calls the client back)
/p2p Sara Hi                     message sent directly to Sara's client object (peer-to-peer)
Hello everyone                   broadcast to the room
```

The server log shows every call it receives; peer-to-peer messages never appear there.

## Question 2 - Order system with REST + gRPC

```
Client --REST (HTTP :8080)--> order-service --gRPC (:9090)--> user-service
                                   |
                                   +-- H2 in-memory DB (orders)
```

Start **user-service first**, then order-service (or press Run on each `...Application` class):

```powershell
cd grpc-orders\user-service;  .\mvnw.cmd spring-boot:run
cd grpc-orders\order-service; .\mvnw.cmd spring-boot:run
```

Run the tests from `grpc-orders` (stop user-service before case 3):

```powershell
powershell -ExecutionPolicy Bypass -File .\test-orders.ps1
powershell -ExecutionPolicy Bypass -File .\test-orders.ps1 -Case 3
```

## Results

| Case | Expected | Result |
|---|---|---|
| User exists (101) | 201 | 201 - order 5001 for Ahmed |
| User not found (999) | 404, no order saved | 404 `{"message":"User not found"}` |
| user-service stopped | 503 | 503 `{"message":"User service is unavailable"}` |
| Invalid data | 400 | 400 with the failing fields |

After all cases the `ORDERS` table (H2 console, `/h2-console`) holds only order 5001.
