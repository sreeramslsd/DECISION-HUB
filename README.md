# DecisionHub Backend

DecisionHub is an enterprise-grade collaborative decision-making and community polling platform.

---

## Technical Stack

*   **Language**: Java 21
*   **Framework**: Spring Boot 3.3.x, Spring Security 6.x
*   **Database**: MySQL 8.4 LTS
*   **Migration**: Flyway
*   **Libraries**: MapStruct, Lombok, JJWT (0.12.5)
*   **Containerization**: Docker, Docker Compose

---

## Project Structure

```text
src/main/java/com/decisionhub/
├── config/        # OpenAPI, Jackson serialization properties
├── controller/    # REST Endpoints (Auth, etc.)
├── dto/           # JSR-380 validated immutable Request/Response records
├── entity/        # JPA Entities matching MySQL 3NF tables
├── exception/     # Custom ExceptionHierarchy and GlobalExceptionHandler
├── mapper/        # MapStruct interfaces mapping Entities to DTOs
├── repository/    # JpaRepositories leveraging optimized EntityGraphs
├── security/      # JWT Filters, Custom UserDetails, SecurityConfig
└── service/       # Service interfaces and transactional implementations
```

---

## Database Configuration

The application is synchronized with the approved ER Diagram:
*   Uses a MySQL 8.4 database.
*   Schema changes are fully managed via Flyway migration scripts under `src/main/resources/db/migration/`.
*   Supports soft deletes (`deleted_at`), optimistic locking (`version`), and auditable metadata.

---

## Build and Run Instructions

### Prerequisites
*   JDK 21 (Temurin or OpenJDK)
*   Maven 3.9+
*   MySQL 8.4 running locally or via Docker

### Running Locally with Docker (Recommended)
Compile the application and spin up the database and application containers:
```bash
# Start MySQL and the App service
docker compose up --build -d

# Check health and logs
docker compose ps
docker compose logs -f app
```

### Running Manually
1.  Initialize database schema by running [mysql_schema.sql](file:///c:/Users/sreeram/OneDrive/Desktop/sweets/mysql_schema.sql) and [init_db.sql](file:///c:/Users/sreeram/OneDrive/Desktop/sweets/init_db.sql).
2.  Run the application using Maven:
    ```bash
    mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
    ```

---

## REST Endpoints Summary

### Authentication API
*   `POST /api/v1/auth/register` - Registers a user and hashes the password via BCrypt (strength 12).
*   `POST /api/v1/auth/login` - Authenticates user credentials and issues access & refresh tokens.
*   `POST /api/v1/auth/refresh` - Rotates expired access tokens using a secure refresh token database lookup.
*   `POST /api/v1/auth/logout` - Revokes refresh tokens and logs out.
*   `POST /api/v1/auth/forgot-password` - Requests a secure password reset token (logged to terminal in dev).
*   `POST /api/v1/auth/reset-password` - Validates reset token and sets a new password.

### Swagger/OpenAPI Documentation
Once the application starts, API documentation can be accessed at:
*   Swagger UI: [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
*   API Docs (JSON): [http://localhost:8080/api/v1/api-docs](http://localhost:8080/api/v1/api-docs)
