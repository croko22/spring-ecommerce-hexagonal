# AGENTS.md - Development Guidelines for E-commerce Application

## Project Overview

This is a **Spring Boot 4.0.5** application with Java 21 using **Clean Architecture / Hexagonal Architecture** pattern.

### Technology Stack

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Validation
- Spring Web MVC
- PostgreSQL (via Docker)
- JUnit 5 + Mockito for testing

---

## Build, Lint, and Test Commands

### Build Commands

```bash
# Compile the project
./mvnw compile

# Package the application (creates JAR)
./mvnw package

# Run the application
./mvnw spring-boot:run

# Clean build artifacts
./mvnw clean
```

### Database

```bash
# Start PostgreSQL container
docker-compose up -d

# Stop PostgreSQL container
docker-compose down

# Check database status
docker-compose ps
```

### Test Commands

```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ProductTest

# Run a single test method
./mvnw test -Dtest=ProductTest#shouldCreateProductWithValidData

# Run tests with verbose output
./mvnw test -Dsurefire.useFile=false

# Run tests and skip integration tests (if tagged)
./mvnw test -DskipIntegrationTests=true
```

---

## Code Style Guidelines

### General Principles

- Follow **Clean Architecture** with clear separation of concerns
- Use **Ports and Adapters** (Hexagonal Architecture) pattern
- Prefer **constructor injection** over field injection
- Keep classes small and focused on single responsibility

### Package Structure

```
src/main/java/com/example/ecommerce/
├── product/
│   ├── domain/
│   │   ├── model/          # Domain entities (Product, etc.)
│   │   └── exception/      # Domain exceptions (InvalidProductException)
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/         # Input ports (use cases)
│   │   │   └── out/        # Output ports (repository interfaces)
│   │   └── service/        # Application services
│   └── infrastructure/
│       ├── adapter/
│       │   ├── in/
│       │   │   └── web/    # REST controllers
│       │   │       └── dto/# Request/Response DTOs
│       │   └── out/
│       │       └── persistence/ # JPA adapters
│       └── config/         # Configuration classes
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `ProductService`, `ProductController` |
| Interfaces | PascalCase with suffix | `CreateProductUseCase`, `ProductRepositoryPort` |
| Methods | camelCase | `createProduct()`, `findById()` |
| Variables | camelCase | `productRepositoryPort`, `productRequest` |
| Packages | lowercase | `com.example.ecommerce.product.domain.model` |
| Test Classes | ClassName + Test | `ProductTest`, `ProductServiceTest` |
| Test Methods | shouldXxx / shouldThrowExceptionWhenXxx | `shouldCreateProductWithValidData()` |

### Java Type Conventions

- **Domain Models**: Use primitive `double` for prices (or `BigDecimal` for financial data if precision required)
- **IDs**: Use `Long` for database IDs (nullable for new entities)
- **Strings**: Use `String` with validation for required fields

### Imports Organization

```java
// 1. Java/JDK imports
import java.util.List;

// 2. Spring framework imports
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Service;

// 3. Third-party libraries
import jakarta.persistence.Entity;

// 4. Project imports - domain
import com.example.ecommerce.product.domain.model.Product;

// 5. Project imports - application layer
import com.example.ecommerce.product.application.port.in.CreateProductUseCase;

// 6. Project imports - infrastructure
import com.example.ecommerce.product.infrastructure.adapter.out.persistence;

// 7. Static imports
import static org.junit.jupiter.api.Assertions.assertEquals;
```

### Class Structure

```java
// Domain Model Example
public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;

    // Validation in constructor
    public Product(Long id, String name, String description, double price) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("Product name cannot be blank");
        }
        if (price < 0) {
            throw new InvalidProductException("Price cannot be negative");
        }
        // assignments
    }

    // Getters and Setters with validation in setters
}
```

### Error Handling

- **Domain Exceptions**: Extend `RuntimeException` for business rule violations
- **Exception Naming**: `{Entity}Exception` or `{Domain}Exception`
- **Error Messages**: Clear, user-friendly messages
- **Global Exception Handling**: Consider adding `@ControllerAdvice` for REST APIs

```java
public class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) {
        super(message);
    }
}
```

### DTOs

- Use simple POJOs for request/response objects
- Place in `infrastructure.adapter.in.web.dto` package
- Use manual getters/setters (no Lombok)

```java
public class ProductRequest {
    private String name;
    private String description;
    private double price;

    // Getters and Setters
}
```

---

## Testing Guidelines

### Test Structure (Arrange-Act-Assert)

```java
@Test
void shouldCreateProductWithValidData() {
    // Arrange (Given)
    Product product = new Product(1L, "Laptop", "Gaming Laptop", 1500.0);

    // Act (When)
    // ... perform action

    // Assert (Then)
    assertEquals(1L, product.getId());
}
```

### Unit Test Patterns

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepositoryPort);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        // Arrange
        Product inputProduct = new Product(null, "Test", "Desc", 10.0);
        Product savedProduct = new Product(1L, "Test", "Desc", 10.0);
        when(productRepositoryPort.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(inputProduct);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepositoryPort).save(any(Product.class));
    }
}
```

### Test Naming

- `shouldXxx` - for positive test cases
- `shouldThrowExceptionWhenXxx` - for negative/error cases

---

## Git Conventions

- Make small, focused commits
- Write meaningful commit messages
- Do NOT commit secrets, credentials, or `.env` files

---

## Additional Notes

- **No Lombok**: This project uses manual getters/setters (Lombok dependency is present but not used)
- **Java Version**: Requires Java 21+
- **Database**: PostgreSQL on localhost:5432 (configured via docker-compose)

---

## Project Management

- **Kanban Board**: [Ecommerce App Kanban](https://www.notion.so/38e2eae4512d44908b6a99c6e8644999)
- **Current / Next Task**: Implement User Authentication (Status: To Do)
- **Upcoming Tasks**:
  - Create Shopping Cart Domain
  - Set up Order Processing
