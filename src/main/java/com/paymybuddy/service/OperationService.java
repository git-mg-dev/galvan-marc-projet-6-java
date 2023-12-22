package com.paymybuddy.service;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.OperationFailedException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.repository.OperationRepository;
import com.paymybuddy.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

@Log4j2
@Service
@Transactional
public class OperationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OperationRepository operationRepository;

    /**
     * Creates an operation with type DEPOSIT
     * @param userAccount user making the deposit
     * @param deposit amount of deposit
     * @return updated user account
     * @throws OperationFailedException if user account isn't found or is disabled, or the deposit amount is 0
     */
    @Transactional(rollbackFor = OperationFailedException.class)
    public UserAccount makeDeposit(UserAccount userAccount, float deposit) throws OperationFailedException {
        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED && deposit > 0) {
            log.info("Making deposit of " + deposit + "€ for user " + userAccount.getId());

            float chargedAmount = getChargedAmount(deposit);

            //Charged amount is taken from the balance
            Operation operation = new Operation(new Date(), OperationType.DEPOSIT,
                    "Deposit", deposit, chargedAmount, userAccount.getId(),
                    userAccount.getId(), OperationStatus.PROCESSING);

            userAccount.setAccountBalance(getFloat2Decimal(userAccount.getAccountBalance() + deposit - chargedAmount));
            userAccount.getOperations().add(operation);

            UserAccount savedUser = userRepository.save(userAccount);

            if(savedUser != null) {
                operation = savedUser.getOperations().get(savedUser.getOperations().size()-1);
                updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                return savedUser;
            } else {
                String message = "Deposit for " + userAccount.getEmail() + " failed";
                log.error("Error while making deposit: " + message);
                throw new OperationFailedException(message);
            }
        } else {
            String message = "Invalid user information or deposit amount";
            log.error("Error while making deposit: " + message);
            throw new OperationFailedException(message);
        }
    }

    /**
     * Creates an operation with type TRANSFER
     * @param userAccount user transferring money
     * @param transferAmount amount of transfer
     * @param iban to transfer money to
     * @return updated user account
     * @throws OperationFailedException if user account isn't found or is disabled, or iban is empty or transfer
     * amount is 0
     */
    @Transactional(rollbackFor = OperationFailedException.class)
    public UserAccount makeTransfer(UserAccount userAccount, float transferAmount, String iban) throws OperationFailedException {
        float chargedAmount = getChargedAmount(transferAmount);

        if(userAccount != null && userAccount.getStatus() == UserStatus.ENABLED && !iban.isEmpty() &&
                transferAmount > 0 && transferAmount <= userAccount.getAccountBalance()) {
            log.info("Making transfer of " + transferAmount + "€ for user " + userAccount.getId() + " to IBAN " + iban);

            //Charged amount is taken from the transfer amount
            Operation operation = new Operation(new Date(), OperationType.TRANSFER,
                    "Transfer", transferAmount - chargedAmount, chargedAmount,
                    userAccount.getId(), userAccount.getId(), OperationStatus.PROCESSING);
            operation.setIban(iban);

            userAccount.setAccountBalance(getFloat2Decimal(userAccount.getAccountBalance()) - transferAmount);
            userAccount.getOperations().add(operation);

            UserAccount savedUser = userRepository.save(userAccount);

            if(savedUser != null) {
                operation = savedUser.getOperations().get(savedUser.getOperations().size()-1);
                updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                return savedUser;
            } else {
                String message = "Transfer for " + userAccount.getEmail() + " failed";
                log.error("Error while making transfer: " + message);
                throw new OperationFailedException(message);
            }

        } else {
            String message = "Invalid user information or transfer amount";
            log.error("Error while making transfer: " + message);
            throw new OperationFailedException(message);
        }
    }

    /**
     * Creates an operation with type PAYMENT
     * @param sender user sending payment
     * @param paymentInfo information concerning payment (recipient, description, amount)
     * @return updated user account of sender
     * @throws NullUserException if send account isn't found or payment amount is 0
     * @throws UserNotFountException if recipient account isn't found or is disabled
     * @throws PaymentFailedException if sender doesn't have enough money
     */
    @Transactional(rollbackFor = PaymentFailedException.class)
    public UserAccount sendPayment(UserAccount sender, PaymentInfo paymentInfo)
            throws NullUserException, UserNotFountException, PaymentFailedException {
        if(sender != null && paymentInfo.getAmount() > 0) {
            log.info("Making payment of " + paymentInfo.getAmount() + "€ from user " + sender.getId() + " to user " + paymentInfo.getRecipientId());

            Optional<UserAccount> optionalRecipient = userRepository.findById(paymentInfo.getRecipientId());
            UserAccount recipient = null;
            if(optionalRecipient.isPresent()) {
                recipient = optionalRecipient.get();
            }

            float paymentAmountFloat = getFloatFromInt(paymentInfo.getAmount());
            float chargedAmount = getChargedAmount(paymentInfo.getAmount());

            if(sender.getAccountBalance() >= paymentAmountFloat + chargedAmount) {
                if (recipient != null && recipient.getStatus() == UserStatus.ENABLED) {

                    //Charged amount is taken from the balance of the sender
                    Operation operation = new Operation(new Date(), OperationType.PAYMENT, paymentInfo.getDescription(),
                            paymentAmountFloat, chargedAmount, sender.getId(), recipient.getId(), OperationStatus.PROCESSING);

                    sender.setAccountBalance(getFloat2Decimal(sender.getAccountBalance() - paymentAmountFloat - chargedAmount));
                    sender.getOperations().add(operation);
                    recipient.setAccountBalance(getFloat2Decimal(recipient.getAccountBalance() + paymentAmountFloat));

                    UserAccount savedSender = userRepository.save(sender);
                    UserAccount savedRecipient = userRepository.save(recipient);

                    if (savedRecipient != null) {
                        operation = savedSender.getOperations().get(savedSender.getOperations().size() - 1);
                        updateOperationStatus(operation, OperationStatus.SUCCEEDED);

                        return savedSender;

                    } else {
                        String message = "Payment failed from " + sender.getEmail() + " to user with id " +
                                paymentInfo.getRecipientId() + " (" + paymentInfo.getDescription() + ": " +
                                paymentInfo.getAmount() + "€)";
                        log.error("Error while sending payment: " + message);
                        throw new PaymentFailedException(message);
                    }
                } else {
                    String message = "No user was found with this email";
                    log.error("Error while sending payment: " + message);
                    throw new UserNotFountException(message);
                }
            } else {
                String message = "Not enough money on account balance";
                log.error("Error while sending payment: " + message);
                throw new PaymentFailedException(message);
            }
        } else {
            String message = "Invalid user account or amount value";
            log.error("Error while sending payment: " + message);
            throw new NullUserException(message);
        }
    }

    /**
     * Caculates charged amount from operation amount
     * @param operationAmount
     * @return charged amount
     */
    public float getChargedAmount(float operationAmount) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(operationAmount * 0.005f));
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.floatValue();
    }

    /**
     * Rounds floats to 2 decimals
     * @param value to round
     * @return rounded value
     */
    public float getFloat2Decimal(float value) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(value));
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.floatValue();
    }

    /**
     * Change operation status and save it
     * @param operation operation which status will be changed
     * @param operationStatus new status
     */
    private void updateOperationStatus(Operation operation, OperationStatus operationStatus) {
        operation.setStatus(operationStatus);
        operationRepository.save(operation);
    }

    /**
     * Gets a float value from an int value
     * @param value as int
     * @return result as float
     */
    private float getFloatFromInt(int value) {
        Integer integer = (Integer) value;
        return integer.floatValue();
    }
}
