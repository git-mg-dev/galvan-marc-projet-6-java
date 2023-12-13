package com.paymybuddy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperationDisplay {
    private int id;
    private String contactName;
    private String description;
    private String amount;
}
