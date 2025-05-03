
# Zovo Backend Application

## Introduction

Zovo is a robust backend application built with Java and the Spring Boot framework. It provides essential services for user management, authentication, authorization, and other related functionalities, designed with security and scalability in mind. The application follows modern development practices to ensure maintainability and reliability.

## Features

* **Authentication & Authorization:**
    * Secure user registration and login processes.
    * Password hashing using industry-standard algorithms (via Spring Security's `PasswordEncoder`).
    * Role-Based Access Control (RBAC) using Spring Security, allowing different permission levels for users (e.g., regular users, administrators).
    * Session management (potentially using JWT or session cookies, facilitated by Spring Security).
    * Two-Factor Authentication (2FA) support for enhanced security.
    * Password reset functionality via secure tokens.
    * Custom handling for access denied and authentication entry points.
    * OAuth2 authentication support with multiple providers (Google, GitHub).

* **User Management:**
    * API endpoints for managing user accounts (details in `UserController` and `AdminController`).
    * Retrieval of user information, mapping database entities to Data Transfer Objects (DTOs) for API responses.
    * Profile management capabilities allowing users to update their details.

* **Role Management:**
    * Functionality to manage application roles (details in `RoleService` and `AdminController`).
    * Hierarchical role structure with inheritance capabilities.

* **Security:**
    * **Rate Limiting:** Protects against brute-force attacks using Redis (`RedisRateLimitingService`).
    * **Secret Encryption:** Provides mechanisms for encrypting sensitive data (`SecretEncryptionService`).
    * **Input Validation:** Custom validators for inputs like passwords (`@PasswordMatches`, `@ValidPassword`).
    * **Spring Security:** Leverages the comprehensive security features of the Spring Security framework (BCrypt, security headers).
    * **Session Refresh:** Automatic session cookie refreshing via `RefreshSessionCookieFilter`.
    * **Custom Security Handlers:** Tailored access denied and authentication entry point handlers.

* **API:**
    * RESTful API designed for interactions with clients.
    * Standardized API response format using a common wrapper (`ApiResponse`).
    * Segregated endpoints based on roles (e.g., `/api/users`, `/api/admin`, `/api/auth`).
    * Versioned API endpoints (e.g., `/api/v1/auth`).

* **OAuth2 Integration:**
    * Support for multiple OAuth2 providers (Google, GitHub).
    * Extensible OAuth2 architecture via the provider pattern.
    * Account linking capabilities for connecting social logins with existing accounts.

* **Persistence:**
    * Uses Jakarta Persistence API (JPA) and Hibernate for Object-Relational Mapping (ORM) with a PostgreSQL database.
    * Spring Data JPA repositories for simplified database interaction (`UserRepository`, `RoleRepository`, `VerificationTokenRepository`, `OAuthConnectionRepository`).
    * Database schema versioning and management using Flyway migrations (located in `src/main/resources/db/migrations`).

* **Caching:**
    * Implements caching strategies (using Spring Cache and Redis) to improve performance, especially for frequently accessed data like user lists (`@Cacheable` in `UserService`).
    * Redis-based storage service for efficient temporary data management.

* **Asynchronous Operations:**
    * Supports asynchronous task execution (`@Async`, `AsyncConfig`), suitable for operations like sending emails without blocking the main request thread.
    * Event-based architecture for certain operations using Spring's event publishing system.

* **Email Notifications:**
    * Integrated email service (`EmailService`) for sending notifications (e.g., registration confirmation, password resets) using customizable templates.
    * HTML email templates available in the `templates.email` directory.

* **Verification Tokens:**
    * Manages temporary tokens for email verification and password resets (`VerificationTokenService`).
    * Includes a scheduled task (`TokenCleanupScheduler`) to automatically remove expired tokens.

* **Two-Factor Authentication:**
    * Complete 2FA implementation with a challenge-response flow.
    * Support for different 2FA providers.
    * User-friendly onboarding process for enabling 2FA.

* **Configuration:**
    * Flexible configuration using `application.yml` files for different environments (e.g., `dev`).
    * Support for externalized configuration via environment variables (e.g., `.env` file).
    * Session management configurations via custom properties classes.

## Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot
* **Security:** Spring Security
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** PostgreSQL
* **Database Migrations:** Flyway
* **Caching/Rate Limiting:** Redis
* **Build Tool:** Maven
* **API:** REST
* **OAuth2:** Support for Google and GitHub
* **Libraries:** Lombok, Jakarta EE, Spring Validation

## Architecture & Best Practices

The Zovo backend application adheres to several software engineering best practices:

* **Layered Architecture:** Follows a standard Controller-Service-Repository pattern, promoting separation of concerns and maintainability.
* **Dependency Injection:** Heavily utilizes Spring's DI container to manage component lifecycles and dependencies.
* **DTO Pattern:** Uses Data Transfer Objects (DTOs) to decouple the API layer from the internal data models and prevent exposing sensitive information. Mappers (`UserMapper`) handle the conversion.
* **RESTful Principles:** Aims to follow REST principles for its API design.
* **Centralized Exception Handling:** Implements a global exception handler (`GlobalExceptionHandler`) and custom exceptions for consistent error responses.
* **Security by Design:** Integrates security features throughout the application (password hashing, RBAC, rate limiting, 2FA).
* **Configuration Management:** Uses Spring profiles and external configuration files/environment variables for managing settings across different deployment environments.
* **Database Migrations:** Employs Flyway for reliable database schema evolution.
* **Caching:** Uses caching effectively to reduce the database load and improve response times.
* **Asynchronous Processing:** Leverages async execution for non-critical, potentially long-running tasks.
* **Code Quality:** Uses tools like Lombok to reduce boilerplate code and `slf4j` for structured logging.
* **Provider Pattern:** Implements extensible OAuth2 integration through the provider pattern, making it easy to add new authentication providers.

## Setup & Running

1. **Prerequisites:**
    * Java SDK 21
    * Maven
    * Redis instance
    * PostgreSQL database instance

2. **Configuration:**
    * Copy `example_env` to `.env`.
    * Update `.env` with your specific PostgreSQL database credentials, Redis connection details, email server settings, session secrets, OAuth2 provider credentials, etc.
    * Ensure the PostgreSQL connection details, Redis settings, and other configurations are correctly set in `src/main/resources/application.yml` or environment-specific profiles like `application-dev.yml`. Spring Boot will automatically apply Flyway migrations on startup by default if the Flyway dependency is present. Spring Boot can also load properties from `.env` if the appropriate dependency (`dotenv-java`) is included, or you can set them as system environment variables.

3. **Build:**
   ```bash
   ./mvnw clean install
   ```

4. **Run:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Alternatively, run the packaged JAR file:
   ```bash
   java -jar target/zovo-*.jar
   ```
   The application will connect to the database and apply any pending Flyway migrations automatically upon startup.

## API Endpoints (Overview)

The application exposes RESTful endpoints, likely under base paths such as:

* `/api/v1/auth`: For authentication-related operations (login, register, password reset, 2FA, OAuth2).
* `/api/v1/users`: For user-specific operations accessible by logged-in users.
* `/api/v1/admin`: For administrative tasks (managing users, roles, etc.).

Refer to the specific controller classes (`AuthController`, `UserController`, `AdminController`) for detailed
documentation of all available endpoints and their request/response formats.