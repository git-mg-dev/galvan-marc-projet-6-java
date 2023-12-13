package com.paymybuddy.service;

import com.paymybuddy.exceptions.*;
import com.paymybuddy.model.*;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserAccount findUserById(int userId) {
        Optional<UserAccount> optionalUserAccount = userRepository.findById((Integer) userId);
        return optionalUserAccount.orElse(null);
    }

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

    public UserAccount changePassword(UserAccount userAccount, PasswordChange passwordChange) throws NullUserException, WrongPasswordException, UserNotFountException {
        if(userAccount != null) {
            if(passwordEncoder.matches(passwordChange.getCurrentPassword(), userAccount.getPassword())) {
                userAccount.setPassword(passwordChange.getNewPassword());
                return updateUserInfo(userAccount, true);
            } else {
                throw new WrongPasswordException("Invalid password");
            }
        } else {
            throw new NullUserException("Invalid user account");
        }
    }

    /**
     * Get a list of PAYMENT operations to display from a user account
     * @param userAccount user data
     * @return the list of operations to display
     */
    public List<OperationDisplay> getPaymentToDisplay(UserAccount userAccount) {
        List<OperationDisplay> operationDisplays = new ArrayList<>();
        for (Operation operation : userAccount.getOperations()) {
            if(operation.getOperationType() == OperationType.PAYMENT) {
                Optional<UserAccount> optionalUserAccount = userRepository.findById(operation.getRecipientId());
                UserAccount recipient = optionalUserAccount.orElse(null);

                if (recipient != null) {
                    OperationDisplay operationDisplay = new OperationDisplay(
                            operation.getRecipientId(),
                            recipient.getFirstName() + " " + recipient.getLastName(),
                            operation.getDescription(), Float.toString(operation.getAmount())
                    );
                    operationDisplays.add(operationDisplay);
                }
            }
        }

        return operationDisplays;
    }

    /**
     * Get a list of ENABLED contacts to display from a user account
     * @param userAccount user data
     * @return the list of contact to display
     */
    public List<ContactDisplay> getContactToDisplay(UserAccount userAccount) {
        List<ContactDisplay> result = new ArrayList<>();
        for (Contact contact : userAccount.getContacts()) {
            Optional<UserAccount> optionalUserAccount = userRepository.findById(contact.getId());
            UserAccount contactUser = optionalUserAccount.orElse(null);

            if(contactUser != null && contactUser.getStatus() == UserStatus.ENABLED) {
                result.add(new ContactDisplay(contactUser.getId(), contactUser.getEmail(), contactUser.getFirstName(), contactUser.getLastName(), contactUser.isOpenidconnectUser()));
            }
        }
        return result;
    }

    private void encodePassword(UserAccount userAccount, RegisterInfo registerInfo){
        userAccount.setPassword(passwordEncoder.encode(registerInfo.getPassword()));
    }

}
