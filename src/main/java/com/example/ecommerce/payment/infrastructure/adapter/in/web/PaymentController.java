package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.payment.application.port.in.GetPaymentUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;
import com.example.ecommerce.payment.infrastructure.adapter.in.web.dto.InitiatePaymentRequest;
import com.example.ecommerce.payment.infrastructure.adapter.in.web.dto.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final InitiatePaymentUseCase initiatePaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    public PaymentController(
            InitiatePaymentUseCase initiatePaymentUseCase,
            GetPaymentUseCase getPaymentUseCase
    ) {
        this.initiatePaymentUseCase = initiatePaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initiatePayment(@RequestBody InitiatePaymentRequest request) {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                request.getOrderId(),
                request.getIdempotencyKey(),
                request.getPaymentMethodToken()
        );

        InitiatePaymentUseCase.InitiatePaymentResult result = initiatePaymentUseCase.initiatePayment(command);
        return PaymentResponse.fromInitiateResult(result);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        GetPaymentUseCase.PaymentDetails details = getPaymentUseCase.getPaymentById(id);
        return PaymentResponse.fromDetails(details);
    }
}
