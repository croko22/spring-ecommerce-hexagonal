package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;
import com.example.ecommerce.payment.infrastructure.adapter.in.web.dto.PaymentWebhookReceiptResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {

    private final HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

    public PaymentWebhookController(HandlePaymentWebhookUseCase handlePaymentWebhookUseCase) {
        this.handlePaymentWebhookUseCase = handlePaymentWebhookUseCase;
    }

    @PostMapping("/provider")
    public ResponseEntity<PaymentWebhookReceiptResponse> handleProviderWebhook(
            @RequestHeader("X-Provider-Event-Id") String providerEventId,
            @RequestHeader("X-Provider-Signature") String signature,
            @RequestBody String payload
    ) {
        HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand command =
                new HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand(providerEventId, signature, payload);
        HandlePaymentWebhookUseCase.HandlePaymentWebhookResult result = handlePaymentWebhookUseCase.handleWebhook(command);
        return ResponseEntity.accepted().body(PaymentWebhookReceiptResponse.fromResult(result));
    }
}
