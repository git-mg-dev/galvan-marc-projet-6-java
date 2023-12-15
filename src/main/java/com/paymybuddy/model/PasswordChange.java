package com.paymybuddy.model;

import com.paymybuddy.validation.PasswordsChange;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@PasswordsChange
public class PasswordChange {
    @NotNull
    @NotEmpty(message = "Enter current password")
    private String currentPassword;

    @NotNull
    @NotEmpty(message = "New password should not be empty")
    @Size(min = 8, message = "New password should be at least 8 characters long")
    private String newPassword;

    @NotNull
    @NotEmpty(message = "Password confirmation should not be empty")
    private String confirmPassword;

    public PasswordChange() {
        currentPassword = "";
        newPassword = "";
        confirmPassword = "";
    }
}
