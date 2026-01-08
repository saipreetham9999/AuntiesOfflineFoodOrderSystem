# Payment & Transaction Management

This document outlines the payment architecture and transaction tracking system implemented in the Auntiees Food Order Management System.

## 💳 Payment Gateway: Stripe Integration

The system uses **Stripe** as the primary payment gateway. The integration is designed to be secure, PCI-compliant, and robust.

### Secure Payment Flow
1.  **Initiate Payment**: The frontend calls `/api/payments/create-intent` with the `orderId` and `amount`.
2.  **Stripe Intent**: The backend communicates with Stripe to create a `PaymentIntent` and returns a `clientSecret`.
3.  **Client-Side Charge**: The frontend uses the `clientSecret` with Stripe Elements to securely collect payment details. The card data is sent directly to Stripe, never touching our servers.
4.  **Confirmation**: Once the payment is successful on the client side, the frontend calls `/api/payments/confirm` to record the transaction in our database.

## 🛡️ Resilience & Reliability Patterns

To ensure the system remains stable even if external services (like Stripe) are slow or down, we have implemented the following patterns using **Resilience4j**:

### 1. Circuit Breaker Pattern
- **Purpose**: Prevents the system from repeatedly trying to call a failing service, which can lead to resource exhaustion.
- **Configuration**: If 50% of calls to Stripe fail within a window of 10 calls, the circuit "opens," and all subsequent calls are immediately redirected to a fallback method for 10 seconds.
- **Fallback**: Returns a `PAYMENT_SERVICE_UNAVAILABLE` status, allowing the frontend to show a graceful error message.

### 2. Retry Pattern
- **Purpose**: Automatically retries failed network calls that might be due to temporary glitches.
- **Configuration**: The system will retry a failed Stripe call up to **3 times** with a **2-second** delay between attempts.

## 🆔 Transaction Identification System

To provide a clear audit trail, every payment is assigned a unique, human-readable `transactionCode`.

### Format: `useremail-PAY-N`
- **Registered Users**: The code follows the pattern `customer@email.com-PAY-1`, where `1` is the incrementing count of transactions for that specific user.
- **Guest Users**: For guest checkouts, a unique code is generated in the format `GUEST-PAY-[RANDOM_ID]`.

## 🚀 API Reference

### 1. Create Payment Intent
`POST /api/payments/create-intent`
- **Payload**:
  ```json
  {
    "orderId": "UUID",
    "amount": 25.50
  }
  ```
- **Response**: Returns a `clientSecret` or `PAYMENT_SERVICE_UNAVAILABLE`.

### 2. Confirm Transaction
`POST /api/payments/confirm`
- **Payload**:
  ```json
  {
    "orderId": "UUID",
    "amount": 25.50,
    "paymentMethod": "card",
    "paymentId": "pi_3N..."
  }
  ```

## 📊 Database Schema: `transactions`

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `transaction_code` | String | Unique human-readable ID (e.g., `user@email.com-PAY-1`) |
| `order_id` | UUID | Foreign Key to the `orders` table |
| `payment_id` | String | External ID from Stripe (PaymentIntent ID) |
| `amount` | Decimal | Total amount paid |
| `status` | Enum | `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED` |
| `payment_method` | String | e.g., "card", "apple_pay" |

## 🔒 Security & Compliance
- **PCI Compliance**: Backend never handles sensitive card information.
- **Transactional Integrity**: Confirmation process is wrapped in `@Transactional`.

## 🛠 Configuration
```properties
# Resilience4j Settings
resilience4j.circuitbreaker.instances.stripeService.failureRateThreshold=50
resilience4j.retry.instances.stripeService.maxAttempts=3
```
