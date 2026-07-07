# MatchHire System

A microservices-based job matching and recruitment platform built with Spring Boot.

## Service Status

| Service | Status |
|---------|--------|
| auth-service | ✅ Complete |
| job-service | ✅ Complete |
| application-service | ✅ Complete |
| api-gateway | ✅ Complete |
| matching-service | 📋 Planned |
| notification-service | 📋 Planned |

## Architecture

```
Client
  ↓
API Gateway (port 8083)     ← JWT validation, route forwarding, X-User-Id injection
  ↓              ↓                    ↓
Auth Service   Job Service   Application Service
(port 8081)   (port 8080)      (port 8082)
  ↓              ↓                    ↓
PostgreSQL    PostgreSQL          PostgreSQL
(port 5001)   (port 5000)         (port 5002)
```

## Services

### Auth Service
Handles user registration, login, and profile management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT token |
| GET | `/auth/me` | Get current user profile |
| PUT | `/auth/modify-profile` | Update profile |
| PUT | `/auth/change-password` | Change password |

### Job Service
Handles job posting management by employers.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/jobs` | Create a new job posting |
| GET | `/jobs` | Get all jobs (paginated) |
| GET | `/jobs/{id}` | Get job by ID |
| PUT | `/jobs/{id}` | Update a job |
| DELETE | `/jobs/{id}` | Delete a job |

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

### API Gateway
Single entry point for all services. Validates JWT and injects `X-User-Id` header before forwarding requests to downstream services.

### Matching Service 📋
Planned service to calculate match scores between candidates and job postings based on skills, experience, and location. Will consume candidate/job data via gRPC from auth-service and job-service.

### Notification Service 📋
Planned service to consume Kafka events and send email notifications on application submission and status changes.

## Inter-Service Communication

- **REST** — external clients (via API Gateway) talk to each service over HTTP
- **gRPC** — synchronous service-to-service calls where an immediate response is required (e.g. application-service fetching job/employer details from job-service before saving an application)
- **Kafka** — asynchronous, event-driven communication for things other services may care about without blocking the request (e.g. application-service publishing `SUBMITTED` / `STATUS_CHANGED` events on the `application-events` topic)
## Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud Gateway** (MVC)
- **Spring Security + JWT** (jjwt 0.12.3)
- **Spring Data JPA + Hibernate**
- **PostgreSQL** (via Docker)
- **Apache Kafka** (planned)
- **gRPC** (planned)

## Prerequisites

- Java 17
- Maven
- Docker

## Getting Started

### 1. Start databases

Each service has its own `docker-compose.yml`. Start them all:

```bash
cd auth-service && docker-compose up -d
cd job-service && docker-compose up -d
cd application-service && docker-compose up -d
```

### 2. Start services

Start each Spring Boot application in order:

1. `auth-service` — port 8081
2. `job-service` — port 8080
3. `application-service` — port 8082
4. `api-gateway` — port 8083 (in progress)

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
- [ ] Matching service (skill-based recommendations with match score, gRPC calls to auth-service and job-service)
- [ ] Notification service (email alerts via Kafka consumer)
- [ ] Resume upload
- [ ] Candidate profile — skills field (prerequisite for matching-service)
- [ ] Job posting — required skills field (prerequisite for matching-service)
- [ ] Admin dashboard
- [ ] Job search/filter endpoint (separate from matching — explicit user-driven query vs. system-computed recommendations)

