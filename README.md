# E-commerce Application

A Spring Boot 4.0.5 e-commerce API using Clean Architecture / Hexagonal Architecture with Java 21.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Web MVC
- PostgreSQL
- JUnit 5 + Mockito

## Prerequisites

- Java 21+
- Docker & Docker Compose

## Quick Start

```bash
# Start PostgreSQL
docker-compose up -d

# Run the application
mvn spring-boot:run
```

## Swagger UI

Available at: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /products | Create a product |

### Create Product

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "description": "Gaming Laptop", "price": 1500.0}'
```

## Commands

```bash
mvn compile          # Compile
mvn package          # Build JAR
mvn spring-boot:run  # Run
mvn test             # Run tests
mvn test -Dtest=ProductTest#shouldCreateProductWithValidData  # Single test
```

## Database Migrations (Flyway)

- Flyway is enabled and runs at startup before Hibernate validation.
- Runtime schema mode is `spring.jpa.hibernate.ddl-auto=validate`.
- Transitional setting `spring.flyway.baseline-on-migrate=true` is enabled to adopt existing non-empty schemas safely during cutover.
- Migration location: `src/main/resources/db/migration`.
- Naming conventions:
  - Versioned migrations: `V{n}__{snake_case}.sql`
  - Optional repeatable deterministic migrations: `R__{name}.sql`
- Migration-first rule: every schema change must be delivered as a Flyway migration. Entity-only schema mutation without migration is disallowed.

## Testing Lanes

- Unit lane (container-free):

```bash
./mvnw test -Dgroups='!integration'
```

- Integration lane (Testcontainers PostgreSQL):

```bash
./mvnw test -Dgroups=integration
```

- Integration tests self-provision PostgreSQL via Testcontainers; `docker-compose` is optional for manual app runs only.

## CI Policy

- CI is split into unit and integration jobs in `.github/workflows/backend-ci.yml`.
- Integration lane validates Flyway startup/migrations and persistence behavior.
- Schema-affecting changes require passing integration lane before merge.

## Rollback Safety Checklist

- Take DB snapshot/backup before deploying irreversible DDL migrations.
- Do not rollback by ad-hoc manual schema mutation.
- If cutover issues appear, temporarily restore previous runtime toggle strategy, fix migrations, and re-run integration lane.

## Project Structure

```
src/main/java/com/example/ecommerce/product/
├── domain/           # Business entities & exceptions
├── application/      # Use cases & ports
└── infrastructure/   # Adapters (REST, persistence)
```

## Stopping

```bash
docker-compose down
```
