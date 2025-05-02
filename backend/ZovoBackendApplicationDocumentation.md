# Zovo Backend Application

## Introduction

Zovo is a robust backend application built with Java and the Spring Boot framework. It provides essential services for user management, authentication, authorization, and other related functionalities, designed with security and scalability in mind. The application follows modern development practices to ensure maintainability and reliability.

## Features

*   **Authentication & Authorization:**
    *   Secure user registration and login processes.
    *   Password hashing using industry-standard algorithms (via Spring Security's `PasswordEncoder`).
    *   Role-Based Access Control (RBAC) using Spring Security, allowing different permission levels for users (e.g., regular users, administrators).
    *   Session management (potentially using JWT or session cookies, facilitated by Spring Security).
    *   Two-Factor Authentication (2FA) support for enhanced security.
    *   Password reset functionality via secure tokens.
    *   Custom handling for access denied and authentication entry points.
*   **User Management:**
    *   API endpoints for managing user accounts (details likely in `UserController` and `AdminController`).
    *   Retrieval of user information, mapping database entities to Data Transfer Objects (DTOs) for API responses.
*   **Role Management:**
    *   Functionality to manage application roles (details likely in `RoleService` and `AdminController`).
*   **Security:**
    *   **Rate Limiting:** Protects against brute-force attacks using Redis (`RedisRateLimitingService`).
    *   **Secret Encryption:** Provides mechanisms for encrypting sensitive data (`SecretEncryptionService`).
    *   **Input Validation:** Custom validators for inputs like passwords (`@PasswordMatches`, `@ValidPassword`).
    *   **Spring Security:** Leverages the comprehensive security features of the Spring Security framework (CSRF protection, security headers are typically included).
*   **API:**
    *   RESTful API design for interactions with clients.
    *   Standardized API response format using a common wrapper (`ApiResponse`).
    *   Segregated endpoints based on roles (e.g., `/api/users`, `/api/admin`, `/api/auth`).
*   **Persistence:**
    *   Uses Jakarta Persistence API (JPA) and Hibernate for Object-Relational Mapping (ORM) with a PostgreSQL database.
    *   Spring Data JPA repositories for simplified database interaction (`UserRepository`, `RoleRepository`, `VerificationTokenRepository`).
    *   Database schema versioning and management using Flyway migrations (located in `src/main/resources/db/migrations`).
*   **Caching:**
    *   Implements caching strategies (using Spring Cache and Redis) to improve performance, especially for frequently accessed data like user lists (`@Cacheable` in `UserService`).
*   **Asynchronous Operations:**
    *   Supports asynchronous task execution (`@Async`, `AsyncConfig`), suitable for operations like sending emails without blocking the main request thread.
*   **Email Notifications:**
    *   Integrated email service (`EmailService`) for sending notifications (e.g., registration confirmation, password resets) using customizable templates.
*   **Verification Tokens:**
    *   Manages temporary tokens for email verification and password resets (`VerificationTokenService`).
    *   Includes a scheduled task (`TokenCleanupScheduler`) to automatically remove expired tokens.
*   **Configuration:**
    *   Flexible configuration using `application.yml` files for different environments (e.g., `dev`).
    *   Support for externalized configuration via environment variables (e.g., `.env` file).

## Technology Stack

*   **Language:** Java 21
*   **Framework:** Spring Boot
*   **Security:** Spring Security
*   **Data Access:** Spring Data JPA, Hibernate
*   **Database:** PostgreSQL
*   **Database Migrations:** Flyway
*   **Caching/Rate Limiting:** Redis
*   **Build Tool:** Maven
*   **API:** REST
*   **Libraries:** Lombok, Jakarta EE

## Architecture & Best Practices

The Zovo backend application adheres to several software engineering best practices:

*   **Layered Architecture:** Follows a standard Controller-Service-Repository pattern, promoting separation of concerns and maintainability.
*   **Dependency Injection:** Heavily utilizes Spring's DI container to manage component lifecycles and dependencies.
*   **DTO Pattern:** Uses Data Transfer Objects (DTOs) to decouple the API layer from the internal data models and prevent exposing sensitive information. Mappers (`UserMapper`, `RoleMapper`) handle the conversion.
*   **RESTful Principles:** Aims to follow REST principles for its API design.
*   **Centralized Exception Handling:** Implements a global exception handler (`GlobalExceptionHandler`) and custom exceptions for consistent error responses.
*   **Security by Design:** Integrates security features throughout the application (password hashing, RBAC, rate limiting, 2FA).
*   **Configuration Management:** Uses Spring profiles and external configuration files/environment variables for managing settings across different deployment environments.
*   **Database Migrations:** Employs Flyway for reliable database schema evolution.
*   **Caching:** Uses caching effectively to reduce database load and improve response times.
*   **Asynchronous Processing:** Leverages async execution for non-critical, potentially long-running tasks.
*   **Code Quality:** Uses tools like Lombok to reduce boilerplate code and `slf4j` for structured logging.

## Setup & Running

1.  **Prerequisites:**
    *   Java SDK 21
    *   Maven
    *   Redis instance
    *   PostgreSQL database instance
2.  **Configuration:**
    *   Copy `example_env` to `.env`.
    *   Update `.env` with your specific PostgreSQL database credentials, Redis connection details, email server settings, session secrets, etc.
    *   Ensure the PostgreSQL connection details, Redis settings, and other configurations are correctly set in `src/main/resources/application.yml` or environment-specific profiles like `application-dev.yml`. Spring Boot will automatically apply Flyway migrations on startup by default if the Flyway dependency is present. Spring Boot can also load properties from `.env` if the appropriate dependency (`dotenv-java`) is included, or you can set them as system environment variables.
3.  **Build:**
    ```bash
    ./mvnw clean install
    ```
4.  **Run:**
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

*   `/api/auth`: For authentication-related operations (login, register, password reset, 2FA).
*   `/api/users`: For user-specific operations accessible by logged-in users.
*   `/api/admin`: For administrative tasks (managing users, roles, etc.).

Refer to the specific controller classes (`AuthController`, `UserController`, `AdminController`) for detailed endpoint definitions.