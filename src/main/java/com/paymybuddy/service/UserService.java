package com.paymybuddy.service;

import com.paymybuddy.exceptions.InvalidRegisterInformation;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserAlreadyExistException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserAccount findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) { return userRepository.findByEmail(email) != null; }

    public UserAccount registerNewUserAccount(RegisterInfo registerInfo, boolean openIdConnectUser) throws InvalidRegisterInformation {
        if(!registerInfo.getEmail().isEmpty() && !registerInfo.getFirstName().isEmpty() &&
        !registerInfo.getLastName().isEmpty() && !registerInfo.getPassword().isEmpty() &&
        registerInfo.getPassword().equals(registerInfo.getPasswordConfirm())) {

            if (emailExists(registerInfo.getEmail())) {
                throw new UserAlreadyExistException("An account with that email address already exists");
            }

            UserAccount userAccount = new UserAccount(registerInfo, openIdConnectUser);
            encodePassword(userAccount, registerInfo);

            return userRepository.save(userAccount);
        } else {
            throw new InvalidRegisterInformation("Register failed, invalid information");
        }
    }

    public UserAccount updateUserInfo(UserAccount userAccount, boolean withNewPassword) throws UserNotFountException, NullUserException {
        if(userAccount != null) {
            if (findUserByEmail(userAccount.getEmail()) != null) {
                if(withNewPassword) {
                    userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
                }
                return userRepository.save(userAccount);
            } else {
                throw new UserNotFountException("Update user failed, user " + userAccount.getEmail() + " not found");
            }
        } else {
            throw new NullUserException("Invalid user account");
        }
    }

    private void encodePassword(UserAccount userAccount, RegisterInfo registerInfo){
        userAccount.setPassword(passwordEncoder.encode(registerInfo.getPassword()));
    }
}
