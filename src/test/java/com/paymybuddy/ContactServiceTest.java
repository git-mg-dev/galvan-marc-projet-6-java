package com.paymybuddy;

import com.paymybuddy.exceptions.ContactAlreadyExistsException;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.ContactService;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class ContactServiceTest {
    @Autowired
    private ContactService contactService;
    @Autowired
    private UserService userService;

    @BeforeAll
    public static void setUpContactTable() {
        //TODO : reset contact table
    }

    @Test
    public void addContactTest_OK() throws UserNotFountException, NullUserException, ContactAlreadyExistsException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("jrocher@mail.com");

        // WHEN
        UserAccount saveUser = contactService.addContact(userAccount, "pauline.test@mail.com");

        // THEN
        assertEquals(userAccount.getContacts().size()+1, saveUser.getContacts().size());
    }

    @Test
    public void addContactTest_AlreadyInContactList_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");

        // WHEN & THEN
        assertThrows(ContactAlreadyExistsException.class, () -> contactService.addContact(userAccount, "jrocher@mail.com"));
    }

    @Test
    public void addContactTest_EmailNotFound_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");

        // WHEN & THEN
        assertThrows(UserNotFountException.class, () -> contactService.addContact(userAccount, "unexistinguser@mail.com"));
    }

    @Test
    public void addContactTest_NullUser_Fail() {
        // WHEN & THEN
        assertThrows(NullUserException.class, () -> contactService.addContact(null, "whatever"));
    }

    @Test
    public void removeContactTest_OK() throws UserNotFountException, NullUserException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");

        // WHEN
        UserAccount saveUser = contactService.removeContact(userAccount, "jrocher@mail.com");

        // THEN
        assertEquals(userAccount.getContacts().size()-1, saveUser.getContacts().size());
    }

    @Test
    public void removeContactTest_EmailNotFound_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");

        // WHEN & THEN
        assertThrows(UserNotFountException.class, () -> contactService.removeContact(userAccount, "unexistinguser@mail.com"));
    }

    @Test
    public void removeContactTest_NullUser_Fail() {
        // WHEN & THEN
        assertThrows(NullUserException.class, () -> contactService.addContact(null, "whatever"));
    }
}
