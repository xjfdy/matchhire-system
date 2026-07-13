# MatchHire System

A microservices-based job matching and recruitment platform built with Spring Boot.

## Service Status

| Service | Status |
|---------|--------|
| auth-service | ✅ Complete |
| job-service | ✅ Complete |
| application-service | ✅ Complete |
| api-gateway | ✅ Complete |
| matching-service | ✅ Complete |
| notification-service | ✅ Complete |

## Architecture

```
Client
  ↓
API Gateway (port 8083)     ← JWT validation, route forwarding, X-User-Id injection
  ↓              ↓                    ↓                    ↓
Auth Service   Job Service   Application Service   Matching Service
(port 8081)   (port 8080)      (port 8082)           (port 8084)
  ↓              ↓                    ↓                    │
PostgreSQL    PostgreSQL          PostgreSQL          (no DB — calls
(port 5001)   (port 5000)         (port 5002)          job/auth via gRPC)

                                    ↓
                          Notification Service (port 8085)
                          (no DB — Kafka consumer only)

gRPC:  application-service → job-service (employer lookup)
       matching-service    → job-service (all jobs + required skills)
       matching-service    → auth-service (candidate skills)

Kafka: application-service publishes application-events
       (SUBMITTED / STATUS_CHANGED) — consumed by notification-service
```

## Services

### Auth Service
Handles user registration, login, and profile management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT token |
| GET | `/auth/me` | Get current user profile |
| PUT | `/auth/modify-profile` | Update profile (including candidate `skills`) |
| PUT | `/auth/change-password` | Change password |

Also exposes a gRPC service (`UserGrpcService`) so other services can fetch candidate info (id, name, skills) without a REST round-trip.

### Job Service
Handles job posting management by employers.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/jobs` | Create a new job posting (including `requiredSkills`) |
| GET | `/jobs` | Get all jobs (paginated) |
| GET | `/jobs/{id}` | Get job by ID |
| PUT | `/jobs/{id}` | Update a job |
| DELETE | `/jobs/{id}` | Delete a job |

Also exposes a gRPC service (`JobGrpcService`) with `GetJobById` and `GetAllJobs`, used by application-service and matching-service.

### Application Service
Handles job applications submitted by candidates.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/applications` | Submit a job application |
| GET | `/applications/my` | Get my applications |
| GET | `/applications/job/{jobId}` | Get all applicants for a job |
| GET | `/applications/{id}` | Get application by ID |
| PUT | `/applications/{id}/status` | Update application status |
| DELETE | `/applications/{id}` | Withdraw an application |

Publishes Kafka events (`SUBMITTED`, `STATUS_CHANGED`) to the `application-events` topic on submission and status change.

### API Gateway
Single entry point for all services. Validates JWT and injects `X-User-Id` header before forwarding requests to downstream services.

### Matching Service
Computes skill-based match scores between a candidate and all open jobs, and returns a ranked recommendation list. Stateless — fetches live data via gRPC from auth-service (candidate skills) and job-service (job requirements) on every request, no database of its own.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/matching/candidates/{candidateId}/recommended-jobs` | Get jobs ranked by match score for a candidate |

Match score = (number of overlapping skills) / (number of skills required by the job).

### Notification Service
Consumes `application-events` from Kafka and simulates sending notifications (logged) when an application is submitted or its status changes. Stateless — no database, no REST API, purely a Kafka consumer.

## Inter-Service Communication

- **REST** — external clients (via API Gateway) talk to each service over HTTP
- **gRPC** — synchronous service-to-service calls where an immediate response is required (e.g. application-service fetching job/employer details from job-service before saving an application; matching-service fetching candidate skills and job data on demand)
- **Kafka** — asynchronous, event-driven communication for things other services may care about without blocking the request (e.g. application-service publishing `SUBMITTED` / `STATUS_CHANGED` events on the `application-events` topic, consumed independently by notification-service)

## Testing

Unit and integration tests using JUnit 5 and Mockito, covering:

- **Pure logic** — e.g. matching-service's skill-overlap scoring algorithm
- **Service layer (Mockito)** — business rules such as duplicate application detection, application withdrawal rules, and status-change event publishing, with gRPC clients and repositories mocked
- **Security filters (Mockito)** — JWT validation and request wrapping in api-gateway's `JwtAuthFilter`
- **Controller layer (MockMvc)** — HTTP status codes and error handling, e.g. job-service returning 404 for a missing job

## Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud Gateway** (MVC)
- **Spring Security + JWT** (jjwt 0.12.3)
- **Spring Data JPA + Hibernate**
- **PostgreSQL** (via Docker)
- **Apache Kafka** (KRaft mode, single-node, via Docker)
- **gRPC** (net.devh grpc-spring-boot-starter)
- **JUnit 5 + Mockito** (unit/integration testing)

## Prerequisites

- Java 17
- Maven
- Docker

## Getting Started

### 1. Start databases and Kafka

Each service has its own `docker-compose.yml`. Start them all:

```bash
cd auth-service && docker compose up -d
cd job-service && docker compose up -d
cd application-service && docker compose up -d
```

The `application-service` compose file also starts a local Kafka broker on port 9092.

### 2. Start services

Start each Spring Boot application:

1. `auth-service` — port 8081, gRPC 9093
2. `job-service` — port 8080, gRPC 9091
3. `application-service` — port 8082, gRPC 9090
4. `api-gateway` — port 8083
5. `matching-service` — port 8084 (gRPC client only, no server)
6. `notification-service` — port 8085 (Kafka consumer only, no gRPC)

### 3. Test the API

HTTP request files are available in the `api-request/` directory and can be run directly in IntelliJ IDEA.

## Application Status Flow

```
PENDING → REVIEWED → ACCEPTED
                   → REJECTED
```

| Status | Description |
|--------|-------------|
| PENDING | Application submitted, awaiting review |
| REVIEWED | Employer has viewed the application |
| ACCEPTED | Candidate accepted |
| REJECTED | Candidate rejected |

## User Types

| Type | Description |
|------|-------------|
| CANDIDATE | Job seeker |
| EMPLOYER | Company / recruiter posting jobs |

## Planned Features

- [ ] Resume upload
- [ ] Job search/filter endpoint (separate from matching — explicit user-driven query vs. system-computed recommendations)
- [ ] Admin dashboard
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Kubernetes deployment
