package com.paymybuddy.model;

public class OperationDisplay {
    private int id;
    private String contactName;
    private String description;
    private String amount;

    public OperationDisplay(int id, String contactName, String description, String amount) {
        this.id = id;
        this.contactName = contactName;
        this.description = description;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
