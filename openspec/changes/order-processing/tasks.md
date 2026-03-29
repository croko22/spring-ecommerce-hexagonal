# Tasks: Order Processing

## Phase 1: Domain Layer

- [x] 1.1 Create `OrderStatus` enum with PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
- [x] 1.2 Create `ShippingAddress` value object
- [x] 1.3 Create `OrderItem` value object with productId, productName, quantity, unitPrice, subtotal
- [x] 1.4 Create `Order` entity (aggregate root) with validation in constructor
- [x] 1.5 Create `OrderNotFoundException` exception class

## Phase 2: Application Layer (Ports)

- [x] 2.1 Create `OrderRepositoryPort` interface (output port)
- [x] 2.2 Create `CartPort` interface for cart access (output port)
- [x] 2.3 Create `CreateOrderUseCase` interface (input port)
- [x] 2.4 Create `GetOrderUseCase` interface (input port)
- [x] 2.5 Create `GetUserOrdersUseCase` interface (input port)
- [x] 2.6 Create `UpdateOrderStatusUseCase` interface (input port)

## Phase 3: Application Layer (Service)

- [x] 3.1 Create `OrderService` with CreateOrderUseCase implementation
- [x] 3.2 Add getOrder method to OrderService (GetOrderUseCase)
- [x] 3.3 Add getUserOrders method to OrderService (GetUserOrdersUseCase)
- [x] 3.4 Add updateOrderStatus method to OrderService (UpdateOrderStatusUseCase)

## Phase 4: Infrastructure - Persistence

- [x] 4.1 Create `OrderEntity` JPA entity
- [x] 4.2 Create `OrderItemEntity` JPA entity
- [x] 4.3 Create `OrderJpaRepository` interface
- [x] 4.4 Create `OrderPersistenceAdapter` implementing OrderRepositoryPort

## Phase 5: Infrastructure - Web Layer

- [x] 5.1 Create `CreateOrderRequest` DTO
- [x] 5.2 Create `UpdateOrderStatusRequest` DTO
- [x] 5.3 Create `OrderResponse` DTO
- [x] 5.4 Create `OrderController` with POST, GET, PUT endpoints
- [x] 5.5 Create `OrderConfig` for dependency injection

## Phase 6: Unit Tests

- [x] 6.1 Write Order domain tests (creation, validation, status transitions)
- [x] 6.2 Write OrderService unit tests with mocked dependencies

## Phase 7: Build and Verify

- [x] 7.1 Run `./mvnw compile` to verify compilation
- [x] 7.2 Run `./mvnw test` to verify all tests pass
