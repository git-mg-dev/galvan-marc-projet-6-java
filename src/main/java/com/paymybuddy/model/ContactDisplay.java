package com.paymybuddy.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactDisplay {
    private int id;

    @NotNull
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be a valid email address")
    private String email;

    @NotNull
    @NotEmpty(message = "Firstname should not be empty")
    private String firstName;

    @NotNull
    @NotEmpty(message = "Lastname should not be empty")
    private String lastName;

    private boolean openidconnectUser;
}
