package com.paymybuddy.service;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.OperationFailedException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.repository.OperationRepository;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Date;

@Service
@Transactional
public class OperationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OperationRepository operationRepository;

    public UserAccount makeDeposit(UserAccount userAccount, float deposit) throws OperationFailedException {
        if(userAccount != null && deposit > 0 && userAccount.getStatus() != UserStatus.DISABLED) {
            float chargedAmount = getChargedAmount(deposit);
            Operation operation = new Operation();
            operation.setAmount(deposit);
            operation.setDescription("Test deposit");
            operation.setOperationDate(new Date());
            operation.setOperationType(OperationType.DEPOSIT);
            operation.setChargedAmount(chargedAmount);
            operation.setSenderId(userAccount.getId());
            operation.setRecipientId(userAccount.getId());

            userAccount.setAccountBalance(userAccount.getAccountBalance() + deposit - chargedAmount);
            userAccount.getOperations().add(operation);

            return userRepository.save(userAccount);
        } else {
            throw new OperationFailedException("Invalid user information or deposit amount");
        }
    }

    @Transactional(rollbackFor = PaymentFailedException.class)
    public UserAccount sendPayment(UserAccount sender, String recipientEmail, float paymentAmount, String description)
            throws NullUserException, UserNotFountException, PaymentFailedException {
        if(sender != null) {
            UserAccount recipient = userRepository.findByEmail(recipientEmail);

            if (recipient != null && recipient.getStatus() == UserStatus.ENABLED) {
                float chargedAmount = getChargedAmount(paymentAmount);
                Date operationDate = new Date();
                Operation operation = new Operation();
                operation.setAmount(paymentAmount);
                operation.setDescription(description);
                operation.setOperationDate(operationDate);
                operation.setOperationType(OperationType.PAYMENT);
                operation.setChargedAmount(chargedAmount);
                operation.setSenderId(sender.getId());
                operation.setRecipientId(recipient.getId());
                operation.setStatus(OperationStatus.PROCESSING);

                sender.setAccountBalance(sender.getAccountBalance() - paymentAmount - chargedAmount);
                sender.getOperations().add(operation);
                recipient.setAccountBalance(recipient.getAccountBalance() + paymentAmount);

                UserAccount savedSender = userRepository.save(sender);
                UserAccount savedRecipient = userRepository.save(recipient);

                if(savedRecipient != null) {
                    operation = savedSender.getOperations().get(savedSender.getOperations().size()-1);
                    operation.setStatus(OperationStatus.SUCCEEDED);
                    operationRepository.save(operation);

                    return savedSender;

                } else {
                    throw new PaymentFailedException("Payment failed from " + sender.getEmail() + " to " +
                            recipientEmail + " (" + description + ": " + paymentAmount + "€)");
                }
            } else {
                throw new UserNotFountException("No user was found with this email");
            }
        } else {
            throw new NullUserException("Invalid user account");
        }
    }

    private float getChargedAmount(float operationAmount) {
        return operationAmount * 0.005f;
    }

}
