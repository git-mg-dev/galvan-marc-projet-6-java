package com.paymybuddy.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactInfo {
    @NotNull
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be a valid email address")
    private String email;

}
