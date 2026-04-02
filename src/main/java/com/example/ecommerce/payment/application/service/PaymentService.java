package com.example.ecommerce.payment.application.service;

import com.example.ecommerce.payment.application.exception.OrderNotPayableException;
import com.example.ecommerce.payment.application.exception.PaymentAccessDeniedException;
import com.example.ecommerce.payment.application.exception.IdempotencyInProgressException;
import com.example.ecommerce.payment.application.exception.PaymentNotFoundException;
import com.example.ecommerce.payment.application.exception.PaymentWebhookSignatureInvalidException;
import com.example.ecommerce.payment.application.port.in.GetPaymentUseCase;
import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;
import com.example.ecommerce.payment.application.port.out.OrderPaymentTransitionPort;
import com.example.ecommerce.payment.application.port.out.OrderSnapshotPort;
import com.example.ecommerce.payment.application.port.out.PaymentIdempotencyPort;
import com.example.ecommerce.payment.application.port.out.PaymentProviderPort;
import com.example.ecommerce.payment.application.port.out.PaymentRepositoryPort;
import com.example.ecommerce.payment.application.port.out.PaymentWebhookReceiptPort;
import com.example.ecommerce.payment.application.port.out.PaymentWebhookSignaturePort;
import com.example.ecommerce.payment.application.port.out.PrincipalAccessPort;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.Payment;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import com.example.ecommerce.payment.domain.model.ProviderReference;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
public class PaymentService implements InitiatePaymentUseCase, GetPaymentUseCase, HandlePaymentWebhookUseCase {

    private final PrincipalAccessPort principalAccessPort;
    private final OrderSnapshotPort orderSnapshotPort;
    private final PaymentIdempotencyPort paymentIdempotencyPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PaymentProviderPort paymentProviderPort;
    private final OrderPaymentTransitionPort orderPaymentTransitionPort;
    private final PaymentWebhookSignaturePort paymentWebhookSignaturePort;
    private final PaymentWebhookReceiptPort paymentWebhookReceiptPort;
    private final OrderToPaymentMapper orderToPaymentMapper;

    public PaymentService(
            PrincipalAccessPort principalAccessPort,
            OrderSnapshotPort orderSnapshotPort,
            PaymentIdempotencyPort paymentIdempotencyPort,
            PaymentRepositoryPort paymentRepositoryPort,
            PaymentProviderPort paymentProviderPort,
            OrderPaymentTransitionPort orderPaymentTransitionPort,
            PaymentWebhookSignaturePort paymentWebhookSignaturePort,
            PaymentWebhookReceiptPort paymentWebhookReceiptPort
    ) {
        this.principalAccessPort = principalAccessPort;
        this.orderSnapshotPort = orderSnapshotPort;
        this.paymentIdempotencyPort = paymentIdempotencyPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.paymentProviderPort = paymentProviderPort;
        this.orderPaymentTransitionPort = orderPaymentTransitionPort;
        this.paymentWebhookSignaturePort = paymentWebhookSignaturePort;
        this.paymentWebhookReceiptPort = paymentWebhookReceiptPort;
        this.orderToPaymentMapper = new OrderToPaymentMapper();
    }

    @Override
    public InitiatePaymentResult initiatePayment(InitiatePaymentCommand command) {
        Long principalUserId = principalAccessPort.getCurrentUserId();
        IdempotencyKey idempotencyKey = new IdempotencyKey(command.idempotencyKey());

        OrderSnapshotPort.OrderSnapshot orderSnapshot = orderSnapshotPort.getOrderSnapshot(command.orderId());
        validateOwnership(principalUserId, orderSnapshot.userId(), command.orderId());
        assertOrderPayable(command.orderId(), orderSnapshot.status());

        String actorScope = buildActorScope(principalUserId, orderSnapshot.orderId());
        String requestHash = requestHash(command);

        PaymentIdempotencyPort.AcquireOutcome outcome = paymentIdempotencyPort.acquireOrReplay(
                PaymentOperation.INITIATE,
                actorScope,
                idempotencyKey,
                requestHash
        );

        if (outcome instanceof PaymentIdempotencyPort.Replay replay) {
            return parseStoredResponse(replay.storedResponse().responseBody());
        }

        if (outcome instanceof PaymentIdempotencyPort.InProgress) {
            throw new IdempotencyInProgressException(
                    "Payment request is already in progress for key " + idempotencyKey.getValue()
            );
        }

        // External provider invocation intentionally runs outside idempotency persistence transactions.
        // Reserve/update operations are isolated in adapter-level short transactions.
        Payment payment = Payment.initiate(
                orderSnapshot.orderId(),
                principalUserId,
                orderToPaymentMapper.toMoney(orderSnapshot)
        );
        payment = paymentRepositoryPort.save(payment);

        PaymentProviderPort.ProviderAuthorizeCaptureResult providerResult = paymentProviderPort.authorizeAndCapture(
                orderSnapshot.orderId(),
                principalUserId,
                command.paymentMethodToken()
        );

        if (providerResult.success()) {
            ProviderReference providerReference = new ProviderReference(providerResult.providerReference());
            payment.authorize(providerReference);
            payment.capture(providerReference);
            payment = paymentRepositoryPort.save(payment);
            orderPaymentTransitionPort.markPaidAfterPayment(orderSnapshot.orderId(), payment.getId());
        } else {
            payment.fail(providerResult.failureCode());
            payment = paymentRepositoryPort.save(payment);
        }

        InitiatePaymentResult result = new InitiatePaymentResult(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency().getValue(),
                payment.getOrderId(),
                payment.getProviderReference() != null ? payment.getProviderReference().getValue() : null
        );

        paymentIdempotencyPort.complete(
                PaymentOperation.INITIATE,
                actorScope,
                idempotencyKey,
                requestHash,
                payment.getId(),
                result.status(),
                formatStoredResponse(result)
        );

        return result;
    }

    @Override
    public PaymentDetails getPaymentById(Long paymentId) {
        Long principalUserId = principalAccessPort.getCurrentUserId();
        Payment payment = paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        validateOwnership(principalUserId, payment.getUserId(), payment.getOrderId());

        return new PaymentDetails(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getStatus().name(),
                payment.getAmount().getAmount(),
                payment.getAmount().getCurrency().getValue(),
                payment.getProviderReference() != null ? payment.getProviderReference().getValue() : null,
                payment.getFailureCode()
        );
    }

    @Override
    public HandlePaymentWebhookResult handleWebhook(HandlePaymentWebhookCommand command) {
        String providerEventId = required(command.providerEventId(), "providerEventId");
        String signature = required(command.signature(), "signature");
        String payload = required(command.payload(), "payload");

        boolean validSignature = paymentWebhookSignaturePort.isValidSignature(signature, payload);
        if (!validSignature) {
            throw new PaymentWebhookSignatureInvalidException("Invalid payment webhook signature");
        }

        PaymentWebhookReceiptPort.PaymentWebhookReceipt existing = paymentWebhookReceiptPort
                .findByProviderEventId(providerEventId)
                .orElse(null);

        if (existing != null) {
            return new HandlePaymentWebhookResult(
                    existing.id(),
                    existing.providerEventId(),
                    "DUPLICATE",
                    existing.reconciliationStatus()
            );
        }

        PaymentWebhookReceiptPort.PaymentWebhookReceipt created = paymentWebhookReceiptPort.save(
                new PaymentWebhookReceiptPort.PaymentWebhookReceipt(
                        null,
                        providerEventId,
                        signature,
                        payload,
                        true,
                        "NO_OP",
                        null,
                        null
                )
        );

        return new HandlePaymentWebhookResult(
                created.id(),
                created.providerEventId(),
                "ACCEPTED",
                created.reconciliationStatus()
        );
    }

    private void validateOwnership(Long principalUserId, Long ownerUserId, Long orderId) {
        if (!principalUserId.equals(ownerUserId)) {
            throw new PaymentAccessDeniedException("Access denied to payment resource");
        }
    }

    private void assertOrderPayable(Long orderId, String orderStatus) {
        if (!"PENDING".equals(orderStatus)) {
            throw new OrderNotPayableException(orderId, orderStatus);
        }
    }

    private String buildActorScope(Long userId, Long orderId) {
        return userId + ":" + orderId;
    }

    private String requestHash(InitiatePaymentCommand command) {
        String payload = command.orderId() + "|" + command.paymentMethodToken();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash idempotency payload", e);
        }
    }

    private String formatStoredResponse(InitiatePaymentResult result) {
        String reference = result.providerReference() == null ? "" : result.providerReference();
        return result.paymentId()
                + "|"
                + result.status()
                + "|"
                + result.amount()
                + "|"
                + result.currency()
                + "|"
                + result.orderId()
                + "|"
                + reference;
    }

    private InitiatePaymentResult parseStoredResponse(String responseBody) {
        String[] parts = responseBody.split("\\|", -1);
        Long paymentId = Long.parseLong(parts[0]);
        String status = parts[1];
        java.math.BigDecimal amount = new java.math.BigDecimal(parts[2]);
        String currency = parts[3];
        Long orderId = Long.parseLong(parts[4]);
        String providerReference = parts.length > 5 && !parts[5].isBlank() ? parts[5] : null;
        return new InitiatePaymentResult(paymentId, status, amount, currency, orderId, providerReference);
    }

    private String required(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is required");
        }
        return value;
    }
}
