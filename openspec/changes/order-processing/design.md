# Design: Order Processing

## Technical Approach

Implement Order Processing using Clean Architecture (Hexagonal) pattern following existing project conventions. The solution will include domain entities, value objects, application use cases, and infrastructure adapters for REST API and JPA persistence.

## Architecture Decisions

### Decision: Package Structure

**Choice**: Follow existing project structure: `com.example.ecommerce.order`
**Alternatives considered**: Separate modules per layer
**Rationale**: Maintains consistency with existing product, cart, and user modules

### Decision: Order-Cart Integration

**Choice**: Use Cart port interfaces to create orders, keeping domains loosely coupled
**Alternatives considered**: Direct Cart entity dependency
**Rationale**: Follows hexagonal architecture - domain should not depend on other domains

### Decision: Order Number Generation

**Choice**: UUID-based order number with "ORD-" prefix
**Alternatives considered**: Database sequence, timestamp-based
**Rationale**: UUID ensures uniqueness without database coordination

### Decision: Order Status Enum

**Choice**: Enum with PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
**Alternatives considered**: String-based status
**Rationale**: Type-safe, IDE auto-complete, prevents invalid states

## Data Flow

```
User Request → OrderController → OrderService (Use Cases)
    → OrderRepository (Port) → OrderPersistenceAdapter (JPA)
    → OrderEntity → Order (Domain)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/example/ecommerce/order/domain/model/Order.java` | Create | Order entity (aggregate root) |
| `src/main/java/com/example/ecommerce/order/domain/model/OrderItem.java` | Create | OrderItem value object |
| `src/main/java/com/example/ecommerce/order/domain/model/OrderStatus.java` | Create | Order status enum |
| `src/main/java/com/example/ecommerce/order/domain/exception/OrderNotFoundException.java` | Create | Exception for order not found |
| `src/main/java/com/example/ecommerce/order/application/port/in/CreateOrderUseCase.java` | Create | Input port interface |
| `src/main/java/com/example/ecommerce/order/application/port/in/GetOrderUseCase.java` | Create | Input port interface |
| `src/main/java/com/example/ecommerce/order/application/port/in/GetUserOrdersUseCase.java` | Create | Input port interface |
| `src/main/java/com/example/ecommerce/order/application/port/in/UpdateOrderStatusUseCase.java` | Create | Input port interface |
| `src/main/java/com/example/ecommerce/order/application/port/out/OrderRepositoryPort.java` | Create | Output port interface |
| `src/main/java/com/example/ecommerce/order/application/port/out/CartPort.java` | Create | Cart access port |
| `src/main/java/com/example/ecommerce/order/application/service/OrderService.java` | Create | Application service |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/in/web/OrderController.java` | Create | REST controller |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/in/web/dto/CreateOrderRequest.java` | Create | Request DTO |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/in/web/dto/OrderResponse.java` | Create | Response DTO |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/in/web/dto/UpdateOrderStatusRequest.java` | Create | Request DTO |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/out/persistence/OrderEntity.java` | Create | JPA entity |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/out/persistence/OrderItemEntity.java` | Create | JPA entity |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/out/persistence/OrderJpaRepository.java` | Create | JPA repository |
| `src/main/java/com/example/ecommerce/order/infrastructure/adapter/out/persistence/OrderPersistenceAdapter.java` | Create | Persistence adapter |
| `src/main/java/com/example/ecommerce/order/infrastructure/config/OrderConfig.java` | Create | Configuration |
| `src/test/java/com/example/ecommerce/order/domain/model/OrderTest.java` | Create | Domain tests |
| `src/test/java/com/example/ecommerce/order/application/service/OrderServiceTest.java` | Create | Service tests |

## Interfaces / Contracts

### Order Entity
```java
public class Order {
    private Long id;
    private String orderNumber;
    private Long userId;
    private List<OrderItem> items;
    private double totalAmount;
    private OrderStatus status;
    private ShippingAddress shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CreateOrderUseCase
```java
public interface CreateOrderUseCase {
    Order createOrder(Long userId, CreateOrderRequest request);
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Order entity creation, validation, status transitions | JUnit 5 + Mockito |
| Unit | OrderService use cases | JUnit 5 + Mockito |
| Integration | OrderController endpoints | MockMvc |

## Migration / Rollout

No migration required. This is a new feature adding new tables.
