package com.paymybuddy.service;

import com.paymybuddy.exceptions.*;
import com.paymybuddy.model.*;
import com.paymybuddy.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Gets user info by email
     * @param email of user
     * @return user info
     */
    public UserAccount findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Checks if user with email exists in database
     * @param email of user
     * @return true or false
     */
    public boolean emailExists(String email) { return userRepository.findByEmail(email) != null; }

    public UserAccount registerNewUserAccount(RegisterInfo registerInfo, boolean openIdConnectUser) throws InvalidRegisterInformation {
        if(!registerInfo.getEmail().isEmpty() && !registerInfo.getFirstName().isEmpty() &&
        !registerInfo.getLastName().isEmpty() && !registerInfo.getPassword().isEmpty() &&
        registerInfo.getPassword().equals(registerInfo.getPasswordConfirm())) {
            log.info("Registering new account for " + registerInfo.getEmail());

            if (emailExists(registerInfo.getEmail())) {
                String message = "Registering account failed for " + registerInfo.getEmail() + ", an account already exists";
                log.error("Error while registering new account: " + message);
                throw new UserAlreadyExistException(message);
            }

            UserAccount userAccount = new UserAccount(registerInfo, openIdConnectUser);
            userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));

            return userRepository.save(userAccount);
        } else {
            String message = "Registering account failed, invalid information";
            log.error("Error while registering new account: " + message);
            throw new InvalidRegisterInformation(message);
        }
    }

    /**
     * Updates user info in database
     * @param userAccount user account to update
     * @param withNewPassword true if the password must be updated too
     * @return updated user info
     * @throws UserNotFountException if user is not found
     * @throws NullUserException if user is not found
     */
    public UserAccount updateUserInfo(UserAccount userAccount, boolean withNewPassword) throws UserNotFountException, NullUserException {
        if(userAccount != null) {
            if (findUserByEmail(userAccount.getEmail()) != null) {
                log.info("Updating user info for account " + userAccount.getId());

                if(withNewPassword) {
                    userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
                }
                return userRepository.save(userAccount);
            } else {
                String message = "Update user info failed, user " + userAccount.getEmail() + " not found";
                log.error("Error while updating user info: " + message);
                throw new UserNotFountException(message);
            }
        } else {
            String message = "Invalid user account";
            log.error("Error while updating user info: " + message);
            throw new NullUserException(message);
        }
    }

    /**
     * Saves a new password for a user
     * @param userAccount user whose password must be changed
     * @param passwordChange current password (not encoded) and new password (not encoded)
     * @return updated user info
     * @throws NullUserException if user is not found
     * @throws WrongPasswordException if current password doesn't match user password
     * @throws UserNotFountException if user is not found
     */
    public UserAccount changePassword(UserAccount userAccount, PasswordChange passwordChange) throws NullUserException, WrongPasswordException, UserNotFountException {
        if(userAccount != null) {
            log.info("Updating user password for account " + userAccount.getId());

            if(passwordEncoder.matches(passwordChange.getCurrentPassword(), userAccount.getPassword())) {
                userAccount.setPassword(passwordChange.getNewPassword());
                return updateUserInfo(userAccount, true);
            } else {
                String message = "Invalid password";
                log.error("Error while updating user password: " + message);
                throw new WrongPasswordException(message);
            }
        } else {
            String message = "Invalid user account";
            log.error("Error while updating user password: " + message);
            throw new NullUserException(message);
        }
    }

    /**
     * Get a list of PAYMENT operations to display from a user account
     * @param userAccount user data
     * @return the list of operations to display
     */
    public List<OperationDisplay> getPaymentToDisplay(UserAccount userAccount) {
        List<OperationDisplay> operationDisplays = new ArrayList<>();

        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED) {
            log.info("Getting payment operations for user " + userAccount.getId());

            for (Operation operation : userAccount.getOperations()) {
                if (operation.getOperationType() == OperationType.PAYMENT) {
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
        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED) {
            log.info("Getting contact list for user " + userAccount.getId());

            for (Contact contact : userAccount.getContacts()) {
                Optional<UserAccount> optionalUserAccount = userRepository.findById(contact.getId());
                UserAccount contactUser = optionalUserAccount.orElse(null);

                if (contactUser != null && contactUser.getStatus() == UserStatus.ENABLED) {
                    result.add(new ContactDisplay(contactUser.getId(), contactUser.getEmail(), contactUser.getFirstName(), contactUser.getLastName(), contactUser.isOpenidconnectUser()));
                }
            }
        }
        return result;
    }
}
