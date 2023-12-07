package com.paymybuddy.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "operation")
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private int id;
    @Column(name = "operation_date")
    private Date operationDate;
    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;
    @Column(name = "description")
    private String description;
    @Column(name = "amount")
    private float amount;
    @Column(name = "charged_amount")
    private float chargedAmount;
    @Column(name = "id_sender")
    private int senderId;
    @Column(name = "id_recipient")
    private int recipientId;
    @Column(name = "iban")
    private String iban;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    public Operation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getChargedAmount() {
        return chargedAmount;
    }

    public void setChargedAmount(float chargedAmount) {
        this.chargedAmount = chargedAmount;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }
}
