package com.paymybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user")
public class Contact {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "firstname")
    private String firstName;
    @Column(name = "lastname")
    private String lastName;
    @Column(name = "email")
    private String email;

    public Contact() {
    }

    public Contact(UserAccount userAccount) {
        this.id = userAccount.getId();
        this.firstName = userAccount.getFirstName();
        this.lastName = userAccount.getLastName();
        this.email = userAccount.getEmail();
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
