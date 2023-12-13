package com.paymybuddy.service;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.OperationFailedException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.PaymentInfo;
import com.paymybuddy.model.UserAccount;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class OperationServiceTest {
    @Autowired
    private OperationService operationService;
    @Autowired
    private UserService userService;

    @BeforeAll
    @Sql("/init_db.sql")
    public static void reinitDB() {
        // script executed by @Sql
    }

    @Test
    public void depositTest_OK() throws OperationFailedException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("jrocher@mail.com");
        float balanceBeforeDeposit = userAccount.getAccountBalance();
        float deposit = 50.0f;
        float chargedAmount = operationService.getChargedAmount(deposit);

        // WHEN
        UserAccount saveUser = operationService.makeDeposit(userAccount, deposit);

        // THEN
        assertNotNull(saveUser);
        assertEquals(balanceBeforeDeposit + deposit - chargedAmount, saveUser.getAccountBalance());
    }

    @Test
    public void depositTest_WithInvalidAmount_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("mr@mail.com");
        float deposit = 50.0f;

        // WHEN & THEN
        assertThrows(OperationFailedException.class, () -> operationService.makeDeposit(userAccount, deposit));
    }

    @Test
    public void paymentTest_OK() throws NullUserException, UserNotFountException, PaymentFailedException {
        // GIVEN
        UserAccount sender = userService.findUserByEmail("pauline.test@mail.com");
        UserAccount recipient = userService.findUserByEmail("jrocher@mail.com");
        float paymentAmount = 15.0f;
        float chargedAmount = operationService.getChargedAmount(paymentAmount);
        PaymentInfo paymentInfo = new PaymentInfo(recipient.getId(), "Restaurant", (int)paymentAmount);

        float senderBalanceBeforePayment = sender.getAccountBalance();
        float recipientBalanceBeforePayment = recipient.getAccountBalance();

        // WHEN
        UserAccount saveSender = operationService.sendPayment(sender, paymentInfo);
        UserAccount saveRecipient = userService.findUserByEmail("jrocher@mail.com");

        // THEN
        assertNotNull(saveSender);
        assertEquals(senderBalanceBeforePayment - paymentAmount - chargedAmount, saveSender.getAccountBalance());
        assertEquals(recipientBalanceBeforePayment + paymentAmount, saveRecipient.getAccountBalance());
    }

    @Test
    public void paymentTest_NullSender_Fail() {
        // GIVEN
        PaymentInfo paymentInfo = new PaymentInfo(0, "whatever", 0);

        // WHEN & THEN
        assertThrows(NullUserException.class, () -> operationService.sendPayment(null, paymentInfo));
    }

    @Test
    public void paymentTest_InvalidRecipient_Fail() {
        // GIVEN
        UserAccount sender = userService.findUserByEmail("pauline.test@mail.com");
        PaymentInfo paymentInfo = new PaymentInfo(3, "whatever", 1);

        // WHEN & THEN
        assertThrows(UserNotFountException.class, () -> operationService.sendPayment(sender, paymentInfo));
    }

    @Test
    public void paymentTest_InvalidAmount_Fail() {
        // GIVEN
        UserAccount sender = userService.findUserByEmail("pauline.test@mail.com");
        PaymentInfo paymentInfo = new PaymentInfo(2, "whatever", 1000);

        // WHEN & THEN
        assertThrows(PaymentFailedException.class, () -> operationService.sendPayment(sender, paymentInfo));
    }

    @Test
    public void paymentTest_PaymentFailed_Fail() throws NullUserException, UserNotFountException, PaymentFailedException {
        //TODO: il faudrait mocker le UserRepository et indiquer qu'il retourne null quand on appelle Save
        // mais comment l'injecter dans OperationService ?
        // Hint: https://lkrnac.net/blog/2015/12/mock-spring-bean-v2/
        /*
        // GIVEN
        UserAccount sender = userService.findUserByEmail("pauline.test@mail.com");
        UserAccount recipient = userService.findUserByEmail("jrocher@mail.com");
        float paymentAmount = 15.35f;
        float chargedAmount = paymentAmount * 0.005f;
        String description = "Restaurant";

        when(operationService.sendPayment(any(), any(), any(), any())).thenReturn(null);

        // WHEN
        UserAccount saveSender = operationService.sendPayment(sender, recipient.getEmail(), paymentAmount, description);
        UserAccount saveRecipient = userService.findUserByEmail("jrocher@mail.com");

        // THEN
        assertNull(saveSender);
        assertEquals(saveRecipient.getAccountBalance(), recipient.getAccountBalance());*/
        fail();
    }

    @Test
    public void transferTest_OK() throws OperationFailedException {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("pauline.test@mail.com");
        float balanceBeforeDeposit = userAccount.getAccountBalance();
        float transferAmount = 50.0f;
        float chargedAmount = operationService.getChargedAmount(transferAmount);
        String iban = "FR8914508000708675176486S61";

        // WHEN
        UserAccount saveUser = operationService.makeTransfer(userAccount, transferAmount, iban);

        // THEN
        assertNotNull(saveUser);
        assertEquals(balanceBeforeDeposit - transferAmount - chargedAmount, saveUser.getAccountBalance());
    }

    @Test
    public void transferTest_InvalidUser_Fail() {
        // WHEN & THEN
        assertThrows(OperationFailedException.class,
                () -> operationService.makeTransfer(null, 0.00f, "whatever"));
    }

    @Test
    public void transferTest_InvalidAmount_Fail() {
        // GIVEN
        UserAccount userAccount = userService.findUserByEmail("jrocher@mail.com");

        // WHEN & THEN
        assertThrows(OperationFailedException.class,
                () -> operationService.makeTransfer(userAccount, 100000.00f, "whatever"));
    }

    @Test
    public void transferTest_PaymentFailed_Fail() {
        //TODO: mocker le userRepository pour que le save retourne null
        fail();
    }
}
