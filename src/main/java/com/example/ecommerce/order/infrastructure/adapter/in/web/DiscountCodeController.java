package com.example.ecommerce.order.infrastructure.adapter.in.web;

import com.example.ecommerce.order.application.port.out.DiscountCodePort;
import com.example.ecommerce.order.domain.model.DiscountCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/discount")
public class DiscountCodeController {

    private final DiscountCodePort discountCodePort;

    public DiscountCodeController(DiscountCodePort discountCodePort) {
        this.discountCodePort = discountCodePort;
    }

    @GetMapping("/{code}")
    public ResponseEntity<DiscountCodeResponse> validateDiscountCode(@PathVariable String code) {
        return discountCodePort.findByCode(code)
                .map(dc -> ResponseEntity.ok(DiscountCodeResponse.from(dc)))
                .orElse(ResponseEntity.notFound().build());
    }

    public record DiscountCodeResponse(
            String code,
            String type,
            double value
    ) {
        public static DiscountCodeResponse from(DiscountCode dc) {
            return new DiscountCodeResponse(
                    dc.getCode(),
                    dc.getType().name().toLowerCase(),
                    dc.getValue()
            );
        }
    }
}