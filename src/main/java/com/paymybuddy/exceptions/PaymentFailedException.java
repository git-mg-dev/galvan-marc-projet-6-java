package com.paymybuddy.exceptions;

public class PaymentFailedException extends Throwable {
    public PaymentFailedException(String message) {
        super(message);
    }
}
