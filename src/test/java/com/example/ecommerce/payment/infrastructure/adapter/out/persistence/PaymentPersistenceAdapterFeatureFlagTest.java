package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.exception.PaymentFeatureDisabledException;
import com.example.ecommerce.payment.domain.model.CurrencyCode;
import com.example.ecommerce.payment.domain.model.Money;
import com.example.ecommerce.payment.domain.model.Payment;
import com.example.ecommerce.shared.infrastructure.PostgresContainerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "feature.payment.enabled=false")
@Tag("integration")
class PaymentPersistenceAdapterFeatureFlagTest extends PostgresContainerIntegrationTest {

    @Autowired
    private PaymentPersistenceAdapter paymentPersistenceAdapter;

    @Test
    void shouldBlockPaymentWritesWhenFeatureFlagDisabled() {
        Payment payment = Payment.initiate(100L, 200L, new Money(new BigDecimal("12.30"), new CurrencyCode("USD")));

        assertThrows(PaymentFeatureDisabledException.class, () -> paymentPersistenceAdapter.save(payment));
    }
}
