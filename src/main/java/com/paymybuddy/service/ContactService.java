package com.paymybuddy.service;

import com.paymybuddy.exceptions.ContactAlreadyExistsException;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.Contact;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.model.UserStatus;
import com.paymybuddy.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Log4j2
@Service
public class ContactService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Adds a contact to a user contact list
     * @param userAccount user to add contact to
     * @param emailContact email of the contact to be added
     * @return user with updated contact list
     * @throws UserNotFountException wrong contact email: user with this email doesn't exist or their
     * account is disabled
     * @throws NullUserException user is null
     * @throws ContactAlreadyExistsException user already have this email in their contact list
     */
    public UserAccount addContact(UserAccount userAccount, String emailContact) throws UserNotFountException, NullUserException, ContactAlreadyExistsException {
        if(userAccount != null && !userAccount.getEmail().equals(emailContact)) {
            log.info("Adding contact " + emailContact + " to user " + userAccount.getId());

            UserAccount contactUser = userRepository.findByEmail(emailContact);

            if (contactUser != null && contactUser.getStatus() == UserStatus.ENABLED) {
                try {
                    if (userAccount.getContacts() == null) {
                        userAccount.setContacts(new ArrayList<>());
                    }

                    userAccount.getContacts().add(new Contact(contactUser));
                    return userRepository.save(userAccount);
                } catch (InvalidDataAccessApiUsageException e) {
                    String message = "Contact already exists";
                    log.error("Error while adding contact: " + message);
                    throw new ContactAlreadyExistsException(message);
                }
            } else {
                String message = "No user was found with this email";
                log.error("Error while adding contact: " + message);
                throw new UserNotFountException(message);
            }
        } else {
            String message = "Invalid user account";
            log.error("Error while adding contact: " + message);
            throw new NullUserException(message);
        }
    }

    /**
     * Removes a contact to a user contact list
     * @param userAccount user to remove contact to
     * @param idContact id of the contact to be removed
     * @return user with updated contact list
     * @throws UserNotFountException wrong contact email: user with this email doesn't exist or their
     * account is disabled
     * @throws NullUserException user is null
     */
    public UserAccount removeContact(UserAccount userAccount, int idContact) throws UserNotFountException, NullUserException {
        if(userAccount != null) {
            log.info("Removing user " + idContact + " from contact list of user " + userAccount.getId());

            for(Contact contact : userAccount.getContacts()) {
                if(idContact == contact.getId()) {
                    userAccount.getContacts().remove(contact);
                    return userRepository.save(userAccount);
                }
            }
            String message = "No user was found with this id";
            log.error("Error while removing contact: " + message);
            throw new UserNotFountException(message);
        } else {
            String message = "Invalid user account";
            log.error("Error while removing contact: " + message);
            throw new NullUserException(message);
        }
    }
}
