# Auntiees Food Order Management System - Detailed Documentation

This document provides a comprehensive overview of the Auntiees Food Order Management System, a robust Spring Boot application designed to manage food orders, users, and menu items for Auntiees Cafe. It details the architecture, technologies, features, and future security enhancements.

## 1. Project Overview

The Auntiees Food Order Management System is a backend application that exposes a RESTful API. It is built to be scalable, secure, and maintainable, leveraging modern Java development practices and the Spring ecosystem.

### Key Features
*   **User Management:** Registration, authentication (JWT), role-based access control (Customer, Cashier, Kitchen, Admin).
*   **Menu Management:** Create, read, update, and delete menu items with support for categories and images.
*   **Order Management:** Place orders (registered users and guests), track order status, and view order history.
*   **Payment Processing:** Secure integration with Stripe for handling payments.
*   **Promotions:** Manage and display promotional banners.
*   **Notifications:** Email notifications for order updates and administrative actions.

## 2. Technology Stack

The project utilizes a modern stack centered around the Java ecosystem:

*   **Language:** Java 17
*   **Framework:** Spring Boot 3.3.4
*   **Build Tool:** Maven
*   **Database:** MariaDB (Relational Data), Redis (Caching)
*   **Messaging:** RabbitMQ (Asynchronous Tasks)
*   **Security:** Spring Security, JWT (JSON Web Tokens)
*   **Payment:** Stripe SDK
*   **Resilience:** Resilience4j (Circuit Breaker, Retry)
*   **Monitoring:** Spring Boot Actuator
*   **Containerization:** Docker & Docker Compose

## 3. Architecture

The application follows a layered architecture to ensure separation of concerns:

1.  **Controller Layer:** Handles incoming HTTP requests and maps them to service methods.
    *   `AuthController`: Handles user registration, login, and OTP verification.
    *   `OrderController`: Manages order creation, retrieval, and status updates.
    *   `MenuController`: Handles menu item operations.
    *   `PaymentController`: Manages payment intents and processing.
    *   `AdminController`: Provides administrative functions for user management.
    *   `PromotionController`: Manages promotional content.
    *   `StripeWebhookController`: Listens for Stripe events (e.g., payment success).
2.  **Service Layer:** Contains the business logic of the application.
    *   `UserService`: Manages user data, roles, and admin promotions.
    *   `OrderService`: Handles order processing, status changes, and email notifications.
    *   `PaymentService`: Integrates with Stripe and manages transaction records.
    *   `EmailService`: Sends emails for OTPs, notifications, and order updates.
    *   `SaiNexusService`: (Experimental) Implements the SAI protocol for secure channel establishment.
3.  **Repository Layer:** Manages data access using Spring Data JPA.
4.  **Entity Layer:** Defines the data models mapped to the database.
    *   `User`: Represents system users with roles.
    *   `Order`: Represents customer orders.
    *   `MenuItem`: Represents food items.
    *   `Transaction`: Tracks payment details.
    *   `Promotion`: Represents marketing banners.
    *   `Token`: Manages OTPs and verification tokens.

### Infrastructure Components
*   **MariaDB:** Stores persistent data such as user profiles, orders, and menu items.
*   **Redis:** Acts as a high-performance cache for frequently accessed data like menu items and user details, reducing database load.
*   **RabbitMQ:** Facilitates asynchronous communication for tasks like sending emails, decoupling them from the main request flow.

## 4. Security

Security is a paramount concern, addressed through multiple layers:

### 4.1. Authentication & Authorization
*   **JWT:** Stateless authentication mechanism. Upon login, users receive a token that must be included in the header of subsequent requests.
*   **RBAC:** Fine-grained access control based on roles (`CUSTOMER`, `ADMIN`, `KITCHEN`, `CASHIER`).
    *   `ADMIN`: Full access to users, menu, and promotions.
    *   `KITCHEN`: Access to active orders and status updates.
    *   `CASHIER`: Ability to place orders for customers/guests.
    *   `CUSTOMER`: Access to own order history.

### 4.2. Future Enhancement: SAI Protocol
The project plans to implement the **Secure Application Interchange (SAI)** protocol for internal service-to-service communication.
*   **Concept:** Uses a synchronized, evolving cryptographic state ("entanglement") between services.
*   **Mechanism:** Messages are encrypted with a one-time key derived from this state, and authenticated with a transient hash.
*   **Benefit:** Eliminates static secrets and prevents replay attacks.

## 5. Payment System

The payment module is designed for security and reliability:

*   **Stripe Integration:** Uses `PaymentIntent` for secure transaction processing. Sensitive card data is handled directly by Stripe's client-side libraries.
*   **Resilience:** Implements Circuit Breaker and Retry patterns via Resilience4j to handle external service failures gracefully.
*   **Audit Trail:** All transactions are logged with unique codes (e.g., `user@email.com-PAY-1`) and linked to orders.
*   **Webhooks:** Listens for `payment_intent.succeeded` events to finalize transactions asynchronously.

## 6. API Endpoints

### Authentication
*   `POST /api/auth/register`: Register a new user.
*   `POST /api/auth/login`: Authenticate and receive a JWT.
*   `POST /api/auth/verify-otp`: Verify email via OTP.

### Menu
*   `GET /api/menu`: Retrieve all active menu items.
*   `GET /api/menu/search?code={code}`: Search menu item by code.
*   `POST /api/menu`: Add a new item (Admin only).
*   `PUT /api/menu/{id}`: Update a menu item (Admin only).
*   `DELETE /api/menu/{id}`: Delete a menu item (Admin only).

### Orders
*   `POST /api/orders`: Place a new order.
*   `GET /api/orders/customer`: View personal order history.
*   `GET /api/orders/kitchen`: View active kitchen orders.
*   `PUT /api/orders/{orderId}/status`: Update order status (Kitchen/Admin).

### Admin
*   `GET /api/admin/users`: List all users.
*   `POST /api/admin/users`: Create a new user (Staff).
*   `PUT /api/admin/users/{userId}/role`: Update user role.
*   `POST /api/admin/promote/initiate`: Start admin promotion process.

### Payments
*   `POST /api/payments/create-intent`: Create a payment intent for an order.

### Promotions
*   `GET /api/promotions`: Get active promotions.
*   `POST /api/promotions`: Create a promotion (Admin only).

*(Refer to the main README for a complete list of endpoints)*

## 7. Setup and Running

The application is containerized for easy setup.

### Prerequisites
*   Docker and Docker Compose
*   Java 17 (for local development)
*   Maven

### Steps
1.  **Clone the repository.**
2.  **Start Infrastructure:** Run `docker-compose up -d` to start MariaDB, Redis, and RabbitMQ.
3.  **Build:** Run `mvn clean install`.
4.  **Run:** Start the Spring Boot application via your IDE or `mvn spring-boot:run`.

## 8. Development Notes

*   **Caching:** User details are cached in Redis to improve authentication performance.
*   **Configuration:** Sensitive keys (JWT secret, Stripe keys) should be managed via environment variables or `application.properties`.
*   **Logging:** Structured logging is implemented with SLF4J.

---
*This documentation is intended to provide a clear understanding of the system's design and capabilities for developers and stakeholders.*
