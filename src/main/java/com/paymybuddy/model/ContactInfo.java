package com.paymybuddy.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ContactInfo {
    @NotNull
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be a valid email address")
    private String email;

    public ContactInfo() {
    }

    public ContactInfo(Integer id, String email) {
        this.email = email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
