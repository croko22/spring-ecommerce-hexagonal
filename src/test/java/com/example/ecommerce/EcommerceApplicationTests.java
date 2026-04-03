package com.example.ecommerce;

import com.example.ecommerce.shared.infrastructure.PostgresContainerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("integration")
class EcommerceApplicationTests extends PostgresContainerIntegrationTest {

	@Test
	void contextLoads() {
	}

}
