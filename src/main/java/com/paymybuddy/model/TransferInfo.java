package com.paymybuddy.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransferInfo {

    @NotNull
    @NotEmpty(message = "IBAN should not be empty")
    @Size(min = 5, max = 34, message = "IBAN must have 5 to 34 characters")
    private String iban;

}
