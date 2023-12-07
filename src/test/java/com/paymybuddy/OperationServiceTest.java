package com.paymybuddy;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.OperationFailedException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.Contact;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.OperationService;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class OperationServiceTest {
    @Autowired
    private OperationService operationService;
    @Autowired
    private UserService userService;

    @Test
    public void depositTest_OK() throws OperationFailedException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("jrocher@mail.com");
        float balanceBeforeDeposit = userAccount.getAccountBalance();
        float deposit = 50.0f;
        float chargedAmount = deposit * 0.005f;

        // WHEN
        UserAccount saveUser = operationService.makeDeposit(userAccount, deposit);

        // THEN
        assertNotNull(saveUser);
        assertEquals(balanceBeforeDeposit + deposit - chargedAmount, saveUser.getAccountBalance());
    }

    @Test
    public void depositTest_WithInvalidAmount_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("jrocher@mail.com");
        float deposit = 50.0f;

        // WHEN & THEN
        assertThrows(OperationFailedException.class, () -> operationService.makeDeposit(userAccount, deposit));
    }

    @Test
    public void paymentTest_OK() throws NullUserException, UserNotFountException, PaymentFailedException {
        // GIVEN
        UserAccount sender = userService.findUserByEmail("pauline.test@mail.com");
        UserAccount recipient = userService.findUserByEmail("jrocher@mail.com");
        float paymentAmount = 15.35f;
        float chargedAmount = paymentAmount * 0.005f;
        String description = "Restaurant";

        float senderBalanceBeforePayment = sender.getAccountBalance();
        float recipientBalanceBeforePayment = recipient.getAccountBalance();

        // WHEN
        UserAccount saveSender = operationService.sendPayment(sender, recipient.getEmail(), paymentAmount, description);
        UserAccount saveRecipient = userService.findUserByEmail("jrocher@mail.com");

        // THEN
        assertNotNull(saveSender);
        assertEquals(senderBalanceBeforePayment - paymentAmount - chargedAmount, saveSender.getAccountBalance());
        assertEquals(recipientBalanceBeforePayment + paymentAmount, saveRecipient.getAccountBalance());
    }


    //TODO
    //Send payment fail (3)
    //Transfer money
    //Transfer money fail
}
