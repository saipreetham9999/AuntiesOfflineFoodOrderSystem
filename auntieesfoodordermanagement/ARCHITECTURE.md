# Auntiees Food Order Management System - Architecture Overview

This document provides a detailed overview of the architecture of the Auntiees Food Order Management System.

## 1. Core Technologies

- **Java 17:** The primary programming language.
- **Spring Boot 3.3.4:** The core framework for building the application.
- **Maven:** The build automation tool.

## 2. Development Environment

The application is designed to run in a containerized environment managed by **Docker Compose**. This ensures consistency between development, testing, and production environments. The `docker-compose.yml` file defines the following services:

- **MariaDB (`mariadb:10.6`):** The primary relational database.
- **Redis (`redis:6.2-alpine`):** Used for high-performance caching.
- **RabbitMQ (`rabbitmq:3.9-management`):** The message broker for asynchronous task processing.

## 3. Project Structure

The project follows a standard layered architecture:

- `config`: Contains configuration classes for security, caching, and other infrastructure components.
- `controller`: Contains the REST APIs that handle incoming HTTP requests.
- `service`: Contains the core business logic.
- `repository`: The data access layer, containing Spring Data JPA repositories.
- `entity`: Contains the JPA entities that map to database tables.
- `payload`: Contains Data Transfer Objects (DTOs) for API requests and responses.
- `sai`: Contains the experimental implementation of the **SAI (Secure Application Interchange)** protocol.

## 4. Services and Third-Party Integrations

### 4.1. Database and Persistence

- **Spring Data JPA:** Used for data persistence and interaction with the MariaDB database.
- **MariaDB:** The relational database used to store all application data, including users, orders, and menu items.

### 4.2. Caching

- **Spring Data Redis:** Used for high-performance caching.
- **User Authentication Cache:** The results of `UserDetailsService` lookups are cached in Redis. This significantly speeds up subsequent requests from authenticated users by avoiding repeated database queries to fetch user details.

### 4.3. Asynchronous Messaging

- **Spring AMQP and RabbitMQ:** The application is configured to use RabbitMQ for asynchronous communication. This is intended for background tasks like sending email notifications, processing long-running jobs, or feeding data into the security event stream without blocking the main request thread. *(Note: The infrastructure is in place, but specific event producers and consumers are pending implementation).*

### 4.4. Payment Processing

- **Stripe:** The application uses the Stripe SDK for payment processing.
- **Flow:**
    1. The client sends order details to the backend.
    2. The backend calculates the final amount and creates a `PaymentIntent` with Stripe.
    3. The backend returns the `client_secret` from the `PaymentIntent` to the client.
    4. The client uses the `client_secret` to confirm the payment directly with Stripe using Stripe's frontend libraries.
    5. The backend listens for a webhook event from Stripe to confirm the payment success and finalize the order in the database.

### 4.5. Resilience and Fault Tolerance

- **Resilience4j:** Used to implement resilience patterns to protect the application from failures in external services like Stripe.
    - **Circuit Breaker:** Prevents the application from repeatedly calling a failing service.
    - **Retry:** Automatically retries a failed operation a configured number of times.

## 5. Security Architecture

### 5.1. Current Model: JWT for User Authentication

- **Spring Security:** Provides the foundation for authentication and authorization.
- **JSON Web Tokens (JWT):** Used for securing the public-facing REST APIs.
    - Upon successful login (`/api/auth/login`), the server generates a JWT containing the user's identity and roles.
    - The JWT secret key and expiration are externalized in `application.properties` for better security and configurability.
- **Role-Based Access Control (RBAC):** Access to endpoints is restricted based on user roles (e.g., `CUSTOMER`, `ADMIN`, `KITCHEN`).

### 5.2. Future Vision: SAI (Secure Application Interchange) Protocol

For internal service-to-service communication, the project is pioneering the **SAI Protocol**. This is a quantum-inspired security model designed to create a communication channel so intrinsically linked to the two communicating services that the message itself becomes secondary.

- **Core Concept:** Two services establish an "entangled" state via a shared, synchronized, and constantly evolving cryptographic generator provided by a central **SAI Nexus**.
- **Communication:** Messages are encrypted with a one-time key derived from the current state, and the request is authenticated with a **Transient Authentication Hash (TAH)**.
- **Benefit:** This provides extreme security, preventing replay attacks and eliminating the need for long-lived static secrets for internal communication. The full details and UML diagram are documented in the main `README.md` file.
