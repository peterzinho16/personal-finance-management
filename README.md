# Personal Finance Management

A Spring Boot application to manage personal finances: track expenditures, categorize spending, import expenses from emails, and view summaries via a web UI. It also exposes REST endpoints and generates OpenAPI documentation.

## Overview

This project provides:
- A web UI (Thymeleaf) to view dashboards like Resume, Expenditures, and Others.
- REST APIs for managing expenditures, categories, subcategories, and recurring expenses.
- Email integration (Gmail/Microsoft) to ingest/extract transactions from mail messages.
- PostgreSQL persistence via Spring Data JPA.
- OpenAPI/Swagger UI for API exploration.

## Tech stack

- Language: Java 21
- Frameworks/Libraries:
  - Spring Boot 3.4.x (Web, Validation, Data JPA, DevTools)
  - Spring Cloud OpenFeign
  - Springdoc OpenAPI
  - Thymeleaf (server-side rendering)
  - Jackson + JavaTime + Hibernate6 modules
  - Lombok
  - Google API Client, Gmail API, Google OAuth Jetty helper
  - Jsoup, Apache Commons Text
- Database: PostgreSQL
- Build & Package Manager: Maven (wrapper included: `./mvnw`)

## Entry points

- Application main class: `com.bindord.financemanagement.PersonalFinanceManagementApplication` (standard `public static void main`)
- Default server port: 8080 (see `application.properties`)
- Web UI: Thymeleaf templates are under `src/main/resources/templates`.
- API documentation: Swagger UI at `/swagger-ui.html` (OpenAPI docs at `/v3/api-docs`).

Example notable controllers (non-exhaustive):
- Auth: Google/Microsoft auth controllers
- Expenditures: CRUD, sync, and mass ingest endpoints
- Master data: Categories, SubCategories, Payee categorization
- Resume: Expenditure summary endpoints

To explore all endpoints, run the app and open the Swagger UI.

## Requirements

- Java 21 (e.g., Temurin/Adoptium or Oracle JDK)
- Maven 3.9+ (or use the Maven Wrapper `./mvnw`)
- PostgreSQL 14+ (a DB named `finance` by default)

## Configuration

Configuration is provided via `application.yml` and `application.properties`.

Active profile by default: `local` (see `spring.profiles.active` in `application.yml`).

Database (defaults suitable for local dev):
- `spring.datasource.url=jdbc:postgresql://${DEFAULT_HOST:localhost}:5432/finance`
- `spring.datasource.username=postgres`
- `spring.datasource.password=sql`

JPA:
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.jpa.show-sql=true`
- `spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy`

Thymeleaf (dev):
- `spring.thymeleaf.prefix=file:src/main/resources/templates/`
- `spring.thymeleaf.cache=false`

OpenAPI/Swagger:
- `springdoc.api-docs.path=/v3/api-docs`
- `springdoc.swagger-ui.path=/swagger-ui.html`

Google API integration:
- `google.credentials.file.path=classpath:secrets/client_karsam.apps.googleusercontent.com.json`
- `gmail.oauth.redirect.uri=http://localhost:8080/eureka/finance-app/api-google/exchange-code`

Tomcat limits:
- `server.tomcat.max-swallow-size=5MB`
- `server.tomcat.max-http-form-post-size=5MB`

### Environment variables

These environment variables are referenced in code or devops manifests:
- `DEFAULT_HOST` (used to compose the JDBC URL; defaults to `localhost` if not set)
- `APP_CLIENT_ID` (used by Microsoft auth; seen in `src/devops/local/deployment.yaml`)
- `APP_CC_CLIENT_SECRET` (used by Microsoft auth; seen in `src/devops/local/deployment.yaml`)

Notes:
- For Google integration, a credentials file is expected at the path set by `google.credentials.file.path` (a file exists under `src/main/resources/secrets/`).
- You may also need to configure Gmail/Microsoft application credentials and consent screens in their respective consoles.

## Setup

1. Ensure PostgreSQL is running and create the `finance` database.
2. Optionally set `DEFAULT_HOST` if your DB is not on localhost (e.g., when connecting from Docker/containers).
3. Review or adjust credentials in `src/main/resources/application.properties` and secrets under `src/main/resources/secrets/` as appropriate for local development.

## Running the application

Using Maven wrapper (recommended):
- Dev run with hot reload: `./mvnw spring-boot:run`
- Package a jar: `./mvnw clean package`
- Run jar: `java -jar target/finance-management-0.0.1-SNAPSHOT.jar`

The app will be available at `http://localhost:8080`.

Open Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Scripts and devops

- Kubernetes deployment manifest for local/dev: `src/devops/local/deployment.yaml`
  - References Docker image: `peterzinho16/finance-management:v2`
  - Exposes container port 8080
  - Sets env vars: `APP_CC_CLIENT_SECRET`, `APP_CLIENT_ID`, `DEFAULT_HOST`

TODO:
- Provide a `docker-compose.yml` or Kubernetes service/ingress manifests for a full local stack (app + Postgres).
- Add build/push scripts for the Docker image or a GitHub Actions workflow.

## Project structure

- `src/main/java/com/bindord/financemanagement` — Java sources
  - `PersonalFinanceManagementApplication.java` — Main entrypoint
  - `controller/` — MVC and REST controllers (auth, expend, master, etc.)
  - `config/`, `advice/`, `service/`, `repository/`, `model/` — typical Spring layers (inspect code for details)
- `src/main/resources` — resources
  - `application.yml`, `application.properties` — configuration
  - `templates/` — Thymeleaf templates (pages and common fragments)
  - `static/` — static assets (JS/CSS)
  - `migration/` — SQL/migration helpers
  - `secrets/` — local secrets (e.g., Google client file)
  - `mail-responses/` — sample mail response payloads for development/testing
- `src/test/java` — tests
- `src/devops/local` — deployment manifests
- `documentation/` — additional docs (Kafka integration notes, etc.)
- `backlog.md` — backlog/notes
- `HELP.md` — generated help file from Spring Initializr

## Tests

- Run unit/integration tests: `./mvnw test`
- Test reports: Surefire reports under `target/surefire-reports`

TODO:
- Document test categories and any required external services/mocks.

## Database

- Uses PostgreSQL, configured via Spring Data JPA.
- Default schema updates are managed via Hibernate (`ddl-auto=update`).

TODO:
- If you plan to use schema migrations consistently, consider adding and documenting a tool like Flyway or Liquibase.

## Security & Auth

- Google and Microsoft auth integrations are present for mail ingestion through OpenID.

TODO:
- Document the full OAuth setup steps and scopes required.
- Clarify whether authentication is required for the UI/API in production.

## API Docs

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## License

The `pom.xml` does not currently specify a concrete license.

TODO:
- Choose and add a LICENSE file (e.g., MIT, Apache-2.0) and update `pom.xml` and this README accordingly.

## Notes

- This README intentionally avoids inventing unknowns. Where details were unclear or not present in the repository, TODOs have been added for the project maintainer to fill in.
