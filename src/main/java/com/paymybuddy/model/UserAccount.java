package com.paymybuddy.model;

import jakarta.persistence.*;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "firstname")
    private String firstName;
    @Column(name = "lastname")
    private String lastName;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "account_balance")
    private float accountBalance;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "deletion_date")
    private Date deletionDate;

    @Column(name = "openidconnect_user")
    private boolean openidconnectUser;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "id_sender")
    private List<Operation> operations;

    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = { CascadeType.MERGE, CascadeType.PERSIST }
    )
    @JoinTable(
            name = "contact",
            joinColumns = @JoinColumn(name = "id_user_1"),
            inverseJoinColumns = @JoinColumn(name = "id_user_2")
    )
    private List<Contact> contacts;

    public UserAccount() {
        operations = new ArrayList<>();
        contacts = new ArrayList<>();
    }

    public UserAccount(RegisterInfo registerInfo, boolean openidconnectUser) {
        this.email = registerInfo.getEmail();
        this.firstName = registerInfo.getFirstName();
        this.lastName = registerInfo.getLastName();
        this.password = registerInfo.getPassword();
        this.accountBalance = 0;
        this.status = UserStatus.ENABLED;
        this.creationDate = new Date();
        this.openidconnectUser = openidconnectUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(float accountBalance) {
        this.accountBalance = accountBalance;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public boolean isOpenidconnectUser() {
        return openidconnectUser;
    }

    public void setOpenidconnectUser(boolean openidconnectUser) {
        this.openidconnectUser = openidconnectUser;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
