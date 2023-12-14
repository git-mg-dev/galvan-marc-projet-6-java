package com.paymybuddy.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepositInfo {

    @NotNull
    @Min(value = 1, message = "Deposit should be at least 1€")
    private int amount;

}
