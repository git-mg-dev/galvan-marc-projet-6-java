package com.paymybuddy.service;

import com.paymybuddy.exceptions.ContactAlreadyExistsException;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.Contact;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.model.UserStatus;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
        if(userAccount != null) {
            UserAccount contactUser = userRepository.findByEmail(emailContact);

            if (contactUser != null && contactUser.getStatus() == UserStatus.ENABLED) {
                try {
                    if (userAccount.getContacts() == null) {
                        userAccount.setContacts(new ArrayList<>());
                    }

                    userAccount.getContacts().add(new Contact(contactUser));
                    return userRepository.save(userAccount);
                } catch (InvalidDataAccessApiUsageException e) {
                    throw new ContactAlreadyExistsException("Contact already exists");
                }
            } else {
                throw new UserNotFountException("No user was found with this email");
            }
        } else {
            throw new NullUserException("Invalid user account");
        }
    }

    /**
     * Removes a contact to a user contact list
     * @param userAccount user to remove contact to
     * @param email email of the contact to be removed
     * @return user with updated contact list
     * @throws UserNotFountException wrong contact email: user with this email doesn't exist or their
     * account is disabled
     * @throws NullUserException user is null
     */
    public UserAccount removeContact(UserAccount userAccount, String email) throws UserNotFountException, NullUserException {
        if(userAccount != null) {
            for(Contact contact : userAccount.getContacts()) {
                if(email.equals(contact.getEmail())) {
                    userAccount.getContacts().remove(contact);
                    return userRepository.save(userAccount);
                }
            }
            throw new UserNotFountException("No user was found with this email");
        } else {
            throw new NullUserException("Invalid user account");
        }
    }
}
