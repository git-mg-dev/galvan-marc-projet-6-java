package com.paymybuddy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OperationDisplay {
    private int id;
    private String contactName;
    private String description;
    private String amount;
}
