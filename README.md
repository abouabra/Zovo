![Logo](frontend/public/images/logo.png)

# Zovo Full-Stack Chatting Application

## Overview

**Zovo** is a modern, secure, and scalable full-stack application, providing a robust backend (Java/Spring Boot) with a type-safe, high-performance frontend (React/Next.js + TypeScript).  
It delivers advanced features for user management, authentication (inc. 2FA & OAuth2), profile and role management, and an extensible API for client applications. The application emphasizes security, code quality, and developer productivity through best practices on both ends.

---

## Features

### Authentication & Security

- **Secure Registration & Login:** Industry-standard password hashing (BCrypt), RBAC, and custom handlers for security endpoints.
- **2FA (Two-Factor Authentication):** User-friendly onboarding, challenge-response flows, and provider extensibility.
- **OAuth2:** Social login (Google, GitHub), easy future provider integration.
- **Password Reset:** Secure, token-based system for recovery.
- **Rate Limiting:** Redis-backed for brute-force protection.
- **Session Management:** Secure, persistent user sessions for seamless authentication continuity.

### Real-time Communication

- **STOMP over WebSockets:** Enables real-time features such as chat by leveraging the STOMP protocol over WebSocket connections.
- **Secure and Scalable:** Connection management and message delivery are securely handled via Spring’s WebSocket support and integrated with the existing authentication system.
- **Frontend Integration:** The frontend connects using modern STOMP/WebSocket clients for instant updates and a dynamic user experience.

### Modern Frontend (React)

- **Responsive UI:** Professionally designed with Tailwind CSS, Radix UI, and modern interaction patterns.
- **Form Validation:** Powered by `zod` and `react-hook-form` for a robust user input experience.
- **Theming:** Light/dark mode toggle with persistent storage.
- **Notifications:** Beautiful feedback with animated toasts (Sonner).
- **State Management:** Reliable and scalable application state handled efficiently with Zustand for consistent user experiences.
- **Async Data Fetching:** Typed API calls, loading indicators, error handling.

### API

- **RESTful API:** Versioned, consistent endpoints for authentication, user management, and chat.
- **DTO Pattern:** Secure, decoupled API response structure.

### Infrastructure

- **Persistence:** JPA/Hibernate w/ PostgreSQL, Flyway db migrations.
- **Caching & Asynchronicity:** Redis cache, event-driven emails (Spring async/events).
- **Email Service:** Rich HTML notifications for key events, generated using Thymeleaf-based templates.
- **Environment Config:** `.env` for and backend (secrets, OAuth app IDs, etc.).

---

## Technology Stack

| Layer    | Technology                                                                                                                                                                         |
|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Frontend | React 19, Next.js 15, TypeScript, Tailwind CSS, Radix UI, Zustand, react-hook-form, zod, Sonner, date-fns, lucide-react                                                            |
| Backend  | Java 21, Spring Boot, Spring Security, Spring Data JPA, Hibernate, PostgreSQL, Redis, Flyway, Lombok, slf4j, Jakarta Validation, Spring WebSocket, Spring Caching, Spring Schedule |
---

## Architecture

- **Layered Design:** Controller, Service, Repository pattern (backend); component-driven (frontend).
- **Dependency Injection:** Spring on backend, custom hooks, and providers on frontend.
- **DTO & Mapper Patterns:** For secure, clean data transfer.
- **Centralized Error Handling:** Backend: global exception handler; frontend: error boundaries and toast alerts.
- **Security:** Multi-layered (RBAC, 2FA, rate limiting), security headers (backend and frontend), XSS/CSRF protection.

---

## Setup & Running

### Prerequisites

- **make**
- **docker compose**

---
## Configuration

1. Copy and edit `example_env` → `.env` (PostgreSQL, Redis, email, JWT secrets, OAuth2 credentials).
2. Confirm `src/main/resources/application.yml` and any profile-specific `application-<env>.yml`.

### Build & Run

```bash
make
```

The application stack is exposed through an NGINX endpoint at [https://localhost](https://localhost), which serves as a unified entry point encapsulating the frontend, backend API, and related services.

---