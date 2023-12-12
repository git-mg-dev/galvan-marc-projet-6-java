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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class OperationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OperationRepository operationRepository;

    @Transactional(rollbackFor = OperationFailedException.class)
    public UserAccount makeDeposit(UserAccount userAccount, float deposit) throws OperationFailedException {
        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED && deposit > 0) {
            float chargedAmount = getChargedAmount(deposit);

            //Charged amount is taken from the balance
            Operation operation = new Operation(new Date(), OperationType.DEPOSIT,
                    "Deposit", deposit, chargedAmount, userAccount.getId(),
                    userAccount.getId(), OperationStatus.PROCESSING);

            userAccount.setAccountBalance(userAccount.getAccountBalance() + deposit - chargedAmount);
            userAccount.getOperations().add(operation);

            UserAccount savedUser = userRepository.save(userAccount);

            if(savedUser != null) {
                operation = savedUser.getOperations().get(savedUser.getOperations().size()-1);
                updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                return savedUser;
            } else {
                throw new OperationFailedException("Deposit for " + userAccount.getEmail() + " failed");
            }
        } else {
            throw new OperationFailedException("Invalid user information or deposit amount");
        }
    }

    @Transactional(rollbackFor = OperationFailedException.class)
    public UserAccount makeTransfer(UserAccount userAccount, float transferAmount, String iban) throws OperationFailedException {
        float chargedAmount = getChargedAmount(transferAmount);

        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED && !iban.isEmpty() &&
                transferAmount > 0 && transferAmount + chargedAmount <= userAccount.getAccountBalance()) {

            //Charged amount is taken from the transfer amount
            Operation operation = new Operation(new Date(), OperationType.TRANSFER,
                    "Transfer", transferAmount - chargedAmount, chargedAmount,
                    userAccount.getId(), userAccount.getId(), OperationStatus.PROCESSING);
            operation.setIban(iban);

            userAccount.setAccountBalance(userAccount.getAccountBalance() - transferAmount - chargedAmount);
            userAccount.getOperations().add(operation);

            UserAccount savedUser = userRepository.save(userAccount);

            if(savedUser != null) {
                operation = savedUser.getOperations().get(savedUser.getOperations().size()-1);
                updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                return savedUser;
            } else {
                throw new OperationFailedException("Transfer for " + userAccount.getEmail() + " failed");
            }

        } else {
            throw new OperationFailedException("Invalid user information or transfer amount");
        }
    }

    @Transactional(rollbackFor = PaymentFailedException.class)
    public UserAccount sendPayment(UserAccount sender, PaymentInfo paymentInfo)
            throws NullUserException, UserNotFountException, PaymentFailedException {
        if(sender != null && paymentInfo.getAmount() > 0) {
            Optional<UserAccount> optionalRecipient = userRepository.findById(paymentInfo.getRecipientId());
            UserAccount recipient = null;
            if(optionalRecipient.isPresent()) {
                recipient = optionalRecipient.get();
            }

            float paymentAmountFloat = getFloatFromInt(paymentInfo.getAmount());

            if (recipient != null && recipient.getStatus() == UserStatus.ENABLED) {
                float chargedAmount = getChargedAmount(paymentInfo.getAmount());

                //Charged amount is taken from the balance of the sender
                Operation operation = new Operation(new Date(), OperationType.PAYMENT, paymentInfo.getDescription(),
                        paymentAmountFloat, chargedAmount, sender.getId(), recipient.getId(), OperationStatus.PROCESSING);

                sender.setAccountBalance(sender.getAccountBalance() - paymentAmountFloat - chargedAmount);
                sender.getOperations().add(operation);
                recipient.setAccountBalance(recipient.getAccountBalance() + paymentAmountFloat);

                UserAccount savedSender = userRepository.save(sender);
                UserAccount savedRecipient = userRepository.save(recipient);

                if(savedRecipient != null) {
                    operation = savedSender.getOperations().get(savedSender.getOperations().size()-1);
                    updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                    return savedSender;

                } else {
                    throw new PaymentFailedException("Payment failed from " + sender.getEmail() + " to user with id " +
                            paymentInfo.getRecipientId() + " (" + paymentInfo.getDescription() + ": " +
                            paymentInfo.getAmount() + "€)");
                }
            } else {
                throw new UserNotFountException("No user was found with this email");
            }
        } else {
            throw new NullUserException("Invalid user account");
        }
    }

    public float getChargedAmount(float operationAmount) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(operationAmount * 0.005f));
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.floatValue();
    }

    private void updateOperationStatus(Operation operation, OperationStatus operationStatus) {
        operation.setStatus(operationStatus);
        operationRepository.save(operation);
    }

    private float getFloatFromInt(int value) {
        Integer integer = (Integer) value;
        return integer.floatValue();
    }
}
