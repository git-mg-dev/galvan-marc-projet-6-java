package com.paymybuddy.model;

import com.paymybuddy.validation.Passwords;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Passwords
public class RegisterInfo {

    @NotNull
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be a valid email address")
    private String email;
    @NotNull
    @NotEmpty(message = "Firstname should not be empty")
    private String firstName;
    @NotNull
    @NotEmpty(message = "Lastname should not be empty")
    private String lastName;
    @NotNull
    @NotEmpty(message = "Password should not be empty")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String password;
    @NotNull
    @NotEmpty(message = "Password confirmation should not be empty")
    private String passwordConfirm;

    public RegisterInfo() {
    }

    public RegisterInfo(String email, String firstName, String lastName, String password, String passwordConfirm) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
