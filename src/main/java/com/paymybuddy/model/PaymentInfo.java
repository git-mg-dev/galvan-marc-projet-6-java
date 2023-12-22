package com.paymybuddy.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {

    @Min(value = 1, message = "Select a contact")
    private int recipientId;
    @NotNull
    @NotEmpty(message = "Description should not be empty")
    private String description;
    @NotNull(message = "Minimum payment value 1€")
    @Min(value = 1, message = "Minimum payment value 1€")
    private Integer amount;

}
