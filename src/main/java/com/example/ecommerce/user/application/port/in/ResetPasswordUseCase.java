package com.example.ecommerce.user.application.port.in;

public interface ResetPasswordUseCase {

    void resetPassword(String token, String newPassword);
}
