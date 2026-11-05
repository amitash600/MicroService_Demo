# Food Delivery Microservices

Microservices conversion of the `spring-boot-final-project` monolith.

## Architecture

```
                          ┌──────────────┐
                          │  api-gateway │  :8080  (Spring Cloud Gateway, WebFlux)
                          └──────┬───────┘
                                 │ lb:// (Eureka)
        ┌──────────┬─────────────┼─────────────┬──────────┐
        ▼          ▼             ▼             ▼          ▼
 ┌────────────┐ ┌──────────┐ ┌───────────┐ ┌────────┐ ┌───────────┐
 │ restaurant │ │   menu   │ │ customer  │ │ order  │ │  payment  │
 │  :8081     │ │  :8082   │ │   :8083   │ │ :8084  │ │   :8085   │
 └────────────┘ └──────────┘ └───────────┘ └────────┘ └───────────┘
       │              │             │            │            │
 restaurant_db    menu_db      customer_db   order_db    payment_db
```

- **eureka-server** `:8761` — service discovery (Netflix Eureka)
- **api-gateway** `:8080` — single entry point, routes by path prefix
- Each service has its **own PostgreSQL database** and only owns its tables.

## Inter-service communication (Spring Cloud OpenFeign)

| Caller | Callee | Purpose |
|---|---|---|
| order-service | customer-service | validate customer on `placeOrder`, enrich order DTOs |
| order-service | menu-service | price/availability on `placeOrder`, live price on qty update |
| order-service | payment-service | create payment on `placeOrder`, fetch payment for DTOs |
| menu-service | restaurant-service | validate restaurant on menu item save/update |
| menu-service | order-service | block delete if item is used in orders |
| customer-service | order-service | block delete if customer has orders |
| restaurant-service | menu-service | block delete if restaurant has menu items |

## Database setup

Create 5 databases in your local PostgreSQL (same credentials as the monolith: `postgres` / `root`):

```sql
CREATE DATABASE restaurant_db;
CREATE DATABASE menu_db;
CREATE DATABASE customer_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
```

Tables are auto-created by Hibernate (`ddl-auto=update`). **Existing data in `FoodDB` is not migrated** — recreate it through the APIs.

## Run

Start in this order (each in its own terminal):

```bash
# 1. Discovery server
cd eureka-server && mvn spring-boot:run

# 2. Business services
cd restaurant-service && mvn spring-boot:run
cd menu-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run

# 3. Gateway (last)
cd api-gateway && mvn spring-boot:run
```

Verify registration at http://localhost:8761

All APIs are reachable through the gateway on `http://localhost:8080` — same paths as the monolith:

- `/restaurant/**` → restaurant-service
- `/menuItem/**` → menu-service
- `/customer/**` → customer-service
- `/order/**`, `/orderItem/**` → order-service
- `/payment/**` → payment-service

## Changes vs the monolith (important)

- `Order` no longer has JPA relations to `Customer`/`Payment` — stores `customerId`/`paymentId`.
- `OrderItem` snapshots `menuItemId`, `itemName`, `price`, `restaurantId` at order time (no cross-service FK).
- `MenuItem` stores `restaurantId` instead of a `Restaurant` entity.
- `Payment` stores `orderId` instead of an `Order` entity.
- `POST /order` accepts the **same JSON shape** as before, e.g.:

```json
{
  "customer": { "customerId": 1 },
  "orderItems": [
    { "menuItem": { "itemId": 2 }, "quantity": 2 }
  ],
  "payment": { "paymentMethod": "UPI", "paymentStatus": "SUCCESS" }
}
```

- `POST`/`PUT /menuItem` accepts either `"restaurantId": 1` or the old nested `"restaurant": {"restaurantId": 1}`.
- Menu item responses now return `restaurantId` instead of the nested `restaurant` object.
- New internal endpoints used by Feign clients:
  - `GET /order/exists/customer/{customerId}`
  - `GET /orderItem/exists/menuItem/{menuItemId}`
  - `GET /menuItem/exists/restaurant/{restaurantId}`
  - `POST /payment` and `GET /payment/order/{orderId}`

## Known limitations

- Order list endpoints make 2 Feign calls per order (customer + payment) — N+1 pattern, fine for learning scale.
- No distributed transactions: if payment-service is down after an order is saved, the order exists without a payment (order keeps `paymentId = null`).
- Same behavior quirk as the monolith: adding/updating order items does not recalculate `order.totalAmount`.
