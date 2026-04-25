# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ensitech is a school information system built as a Spring Boot / Spring Cloud microservices monorepo. All services share a Maven parent POM at the root.

## Build & Run Commands

```bash
# Build all services from root
./mvnw clean package

# Build a single service
cd <service-name> && ../mvnw clean package

# Run tests for all services
./mvnw test

# Run tests for a single service
cd <service-name> && ../mvnw test

# Run a specific test class
cd <service-name> && ../mvnw test -Dtest=MyTestClass

# Run a service locally (after build)
cd <service-name> && java -jar target/<service-name>-*.jar

# SonarQube analysis (user-service example)
cd user-service && mvn sonar:sonar
```

## Service Startup Order

**Order is critical** — each service must be fully started before the next:

1. `config-service` (port 9999)
2. `discovery-service` (port 8761 — Eureka)
3. `user-service`, `course-service`, `authentication-service`, `training-service`, `academic-service`, `registration-service` (in any order)
4. `gateway-service` (port 8888) — last, always

All external requests go through the gateway on port **8888**.

## Architecture

### Infrastructure Services
- **config-service**: Centralized config server. Reads config from `config-service/src/main/resources/config-repo/` (native/classpath mode). Each business service fetches its config on startup via `spring.config.import=optional:configserver:http://localhost:9999`.
- **discovery-service**: Eureka server. All services register here; the gateway uses `lb://` URIs to route requests.
- **gateway-service**: Spring Cloud Gateway. Routes and JWT validation. Routes defined in `config-repo/gateway-service.yml`.

### Business Services
| Service | Port | Responsibility |
|---|---|---|
| `user-service` | 8081 | Students, teachers, directors, study managers |
| `course-service` | 8082 | Courses and subject/user associations |
| `authentication-service` | 8083 | Login, JWT issuance, user accounts |
| `training-service` | 8084 | Specialities, evaluations, student-evaluation links |
| `academic-service` | 8085 | Academic years (state machine: OPEN/CLOSED), periods |
| `registration-service` | — | Student registrations; calls academic-service and user-service via Feign |

### Gateway Routes
| Path prefix | Target service |
|---|---|
| `/api/students/**`, `/api/teachers/**`, `/api/directors/**`, `/api/administrators/**`, `/api/studymanagers/**` | `user-service` |
| `/api/courses/**` | `course-service` |
| `/api/auth/**` | `authentication-service` |
| `/api/training/**` | `training-service` |
| `/api/registrations/**` | `registration-service` |
| `/api/academic-year/**` | `academic-service` |

## Configuration System

Configurations live in `config-service/src/main/resources/config-repo/`. Files follow the pattern:
- `application.properties` — shared by all services (Eureka URL, actuator endpoints)
- `<service-name>.properties` — service defaults
- `<service-name>-dev.properties` — dev profile overrides (MySQL datasource via env vars)
- `<service-name>-docker.properties` / `<service-name>-prod.properties` — other profiles

Each service's `application.properties` sets `spring.profiles.active=dev` by default. To switch profiles, change this or set the env var.

### Environment Variables (dev/prod)
Services expect these env vars when running with MySQL (set in `.env` file or OS environment):
- `MYSQL_ACY_DB`, `MYSQL_REG_DB`, `MYSQL_DB_USER` — per-service JDBC URLs
- `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET_KEY` — used by `authentication-service` and `gateway-service`

## Adding a New Microservice

1. Create folder `<new-service>/` with a Spring Boot project.
2. Set the parent in `pom.xml`:
   ```xml
   <parent>
     <groupId>com.project.ensitech</groupId>
     <artifactId>ensitech-parent</artifactId>
     <version>1.0-SNAPSHOT</version>
     <relativePath>../pom.xml</relativePath>
   </parent>
   ```
3. Add `<module>new-service</module>` to the root `pom.xml`.
4. Add config files in `config-service/src/main/resources/config-repo/`.
5. Add a route in `config-repo/gateway-service.yml`.

## Code Patterns

- **Layer structure**: `controller` → `service` (interface + impl) → `dao/repository` → `domain`/`model`
- **DTOs**: Separate DTOs for create, update, REST responses, and Feign client payloads. Mappers in a `mapper` package handle conversions.
- **Inter-service calls**: Use OpenFeign clients (see `registration-service/feign/`) with Resilience4j circuit breakers and fallback classes.
- **`academic-service`**: Uses Spring State Machine for `AcademicYear` lifecycle (`AcademicYearStatus`, `AcademicYearEvent`).
- **`registration-service`**: Uses Thymeleaf + OpenHTMLtoPDF + ZXing to generate PDF registration certificates with QR codes.
- **Tests**: H2 in-memory DB used for tests; `src/test/resources/application.properties` overrides datasource config.
- **Coverage**: JaCoCo configured in `academic-service` and `registration-service`; reports published via Jenkins `recordCoverage`.

## CI/CD

Jenkins pipeline (`Jenkinsfile`) builds all JARs with `./mvnw clean package`, builds Docker images in parallel, pushes to AWS ECR, then deploys to AWS ECS by patching the task definition. Credentials (`DB_*`, `JWT_SECRET_KEY`) are injected at deploy time via Jenkins secrets.
