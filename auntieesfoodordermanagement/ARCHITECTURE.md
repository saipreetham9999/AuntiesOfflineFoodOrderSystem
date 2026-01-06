# Auntiees Food Order Management System - Architecture Overview

This document provides a detailed overview of the architecture of the Auntiees Food Order Management System.

## 1. Core Technologies

The application is built using the following core technologies:

- **Java 17:** The primary programming language.
- **Spring Boot 3.3.4:** The core framework for building the application.
- **Maven:** The build automation tool.

## 2. Project Structure

The project follows a standard layered architecture, with the following packages:

- `config`: Contains configuration files for security, database, and other services.
- `controller`: Contains the REST APIs that handle incoming HTTP requests.
- `service`: Contains the business logic of the application.
- `repository`: Contains the data access layer, which interacts with the database.
- `entity`: Contains the JPA entities that represent the database tables.
- `payload`: Contains the data transfer objects (DTOs) used for request and response bodies.

## 3. Services and Third-Party Integrations

The application uses several services and third-party integrations to provide its functionality:

### 3.1. Database and Persistence

- **Spring Data JPA:** Used for data persistence and interaction with the database.
- **MariaDB:** The relational database used to store the application's data.

### 3.2. Security

- **Spring Security:** Used for authentication and authorization.
- **JSON Web Tokens (JWT):** Used for securing the REST APIs. The system uses JWTs to represent claims securely between two parties.
- **Role-Based Access Control (RBAC):** The application implements role-based access control to restrict access to certain resources based on user roles (e.g., `USER`, `ADMIN`).

### 3.3. Payment Processing

- **Stripe:** The application uses the Stripe SDK to handle payments. This allows for secure and reliable payment processing.

### 3.4. Caching

- **Spring Data Redis:** Used for caching frequently accessed data to improve performance.

### 3.5. Asynchronous Messaging

- **Spring AMQP and RabbitMQ:** Used for asynchronous communication between different parts of the application. This is useful for tasks such as sending notifications or processing long-running jobs without blocking the main thread.

### 3.6. Resilience and Fault Tolerance

- **Resilience4j:** Used to implement resilience patterns such as:
    - **Circuit Breaker:** To prevent a network or service failure from cascading to other services.
    - **Retry:** To automatically retry a failed operation.
    - **Rate Limiter:** To limit the number of requests to a service.

### 3.7. Email Notifications

- **Spring Boot Starter Mail:** Used for sending emails to users for events such as registration, order confirmation, and password reset.

### 3.8. Frontend

- **Thymeleaf:** A server-side Java template engine for both web and standalone environments. It is used to render the HTML pages of the application.

## 4. High-Level Flow

1. **User Authentication:**
   - A user registers or logs in to the application.
   - Upon successful authentication, the server generates a JWT and returns it to the client.
   - The client includes the JWT in the `Authorization` header of subsequent requests to access protected resources.

2. **Ordering Food:**
   - The user browses the menu and adds items to their cart.
   - The user proceeds to checkout and provides payment information.
   - The application uses the Stripe SDK to process the payment.
   - Upon successful payment, the order is created and stored in the database.

3. **Admin Functionality:**
   - Admins can manage menu items, view orders, and perform other administrative tasks.
   - Access to admin functionality is restricted to users with the `ADMIN` role.

4. **Notifications:**
   - The application sends email notifications to users for events such as order confirmation.
   - Asynchronous messaging with RabbitMQ can be used to handle the sending of these notifications.

## 5. Future Improvements

- **Implement a more robust caching strategy:** To further improve performance.
- **Add more comprehensive monitoring and logging:** To better understand the application's behavior and diagnose issues.
- **Expand the test suite:** To ensure the quality and reliability of the code.
- **Containerize the application:** Using Docker and Kubernetes for easier deployment and scaling.
