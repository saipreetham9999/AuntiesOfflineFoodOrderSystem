# Implementation Notes

This document tracks the improvements and changes made to the Auntiees Food Order Management System.

## 1. Performance: User Authentication Caching

- **Date:** 2024-07-26
- **Goal:** Improve performance of authenticated requests by caching user details.
- **Changes:**
    1.  Created `UserDetailsServiceImpl.java` to encapsulate user loading logic.
    2.  Annotated the `loadUserByUsername` method with `@Cacheable("users")` to cache `UserDetails` objects in Redis.
    3.  Updated `SecurityConfig.java` to use the new `UserDetailsServiceImpl`.
    4.  Added `docker-compose.yml` to easily manage and run dependent services (MariaDB, Redis, RabbitMQ).
    5.  Updated `application.properties` to connect to the services managed by Docker Compose.
- **Outcome:** Subsequent requests for the same user will fetch user details from the Redis cache instead of the database, significantly reducing latency.

## 2. Security: Externalize JWT Configuration

- **Date:** 2024-07-26
- **Goal:** Improve security by removing hardcoded JWT secret key from the source code.
- **Changes:**
    1.  Moved the JWT secret key and expiration time to `application.properties`.
    2.  Removed the hardcoded default values from `JwtService.java`.
- **Outcome:** The JWT secret is no longer exposed in the codebase, making the application more secure and easier to manage in different environments.
