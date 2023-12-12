package com.paymybuddy.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PaymentInfo {
    @Min(value = 1, message = "Select a contact")
    private int recipientId;
    @NotNull
    @NotEmpty(message = "Description should not be empty")
    private String description;
    @Min(value = 1, message = "Minimum payment value 1€")
    private int amount;

    public PaymentInfo() {}

    public PaymentInfo(int recipientId, String description, int amount) {
        this.recipientId = recipientId;
        this.description = description;
        this.amount = amount;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
