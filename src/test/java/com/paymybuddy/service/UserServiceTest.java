package com.paymybuddy.service;

import com.paymybuddy.exceptions.*;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    @Sql("/reinit_db.sql")
    public static void setDataBase() {
        // script executed by @Sql
    }

    @Test
    public void findUserByEmailTest_WithExistingEmail_OK() {
        // GIVEN
        String email = "pauline.test@mail.com";

        // WHEN
        UserAccount userAccount = userService.findUserByEmail(email);

        // THEN
        assertNotNull(userAccount);
    }

    @Test
    public void findUserByEmailTest_WithNonExistingEmail_Fail() {
        // GIVEN
        String email = "paul.test@mail.com";

        // WHEN
        UserAccount userAccount = userService.findUserByEmail(email);

        // THEN
        assertNull(userAccount);
    }

    @Test
    public void registerUser_OK() throws InvalidRegisterInformation {
        // GIVEN
        RegisterInfo registerInfo = new RegisterInfo("eneko.etxegoyen@mail.com", "Eneko", "Etxegoyen", "password", "password");

        // WHEN
        UserAccount userAccount = userService.registerNewUserAccount(registerInfo, false);

        // THEN
        assertNotNull(userAccount);
    }

    @Test
    public void registerUser_UserAlreadyExists_Fail() {
        // GIVEN
        RegisterInfo registerInfo = new RegisterInfo("pauline.test@mail.com", "whatever", "whatever", "whatever", "whatever");

        // WHEN & THEN
        assertThrows(UserAlreadyExistException.class, () -> userService.registerNewUserAccount(registerInfo, false));
    }

    @Test
    public void registerUser_InvalidInfo_Fail() {
        // GIVEN
        RegisterInfo registerInfo = new RegisterInfo("", "", "", "", "");

        // WHEN & THEN
        assertThrows(InvalidRegisterInformation.class, () -> userService.registerNewUserAccount(registerInfo, false));
    }

    @Test
    public void updateUserInfo_OK() throws UserNotFountException, NullUserException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");
        userAccount.setLastName("Harrington");

        // WHEN
        UserAccount savedUser = userService.updateUserInfo(userAccount, false);

        // THEN
        assertNotNull(savedUser);
        assertEquals(userAccount.getLastName(), savedUser.getLastName());
    }

    @Test
    public void updateUserInfo_NullUser_Fail() {
        // WHEN & THEN
        assertThrows(NullUserException.class, () -> userService.updateUserInfo(null, false));
    }

    @Test
    public void updatePassword_OK() throws NullUserException, UserNotFountException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");
        userAccount.setPassword("Harrington");

        // WHEN
        UserAccount savedUser = userService.updateUserInfo(userAccount, true);

        // THEN
        assertNotNull(savedUser);
        assertEquals(userAccount.getPassword(), savedUser.getPassword());
    }

    @Test
    public void updatePassword_NullUser_Fail() {
        // WHEN & THEN
        assertThrows(NullUserException.class, () -> userService.updateUserInfo(null, true));
    }

}
