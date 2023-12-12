package com.paymybuddy.controller;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.OperationService;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private OperationService operationService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @GetMapping("/send")
    public String displayPayments(Principal user, Model model, PaymentInfo paymentInfo, @RequestParam(required = false) Integer id) {
        UserAccount userAccount = securityService.getUserInfo(user, null);

        if(userAccount != null) {
            // TODO: don't display disabled contacts

            if(id == null) {
                id = 0;
            }
            addDataToModel(model, userAccount, id);
            return "/send";
        } else {
            return "/index";
        }
    }

    @PostMapping("/send")
    public String sendPayment(@Valid PaymentInfo paymentInfo, BindingResult bindingResult, Principal user, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, null);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = operationService.sendPayment(userAccount, paymentInfo);
            } catch (NullUserException | UserNotFountException | PaymentFailedException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:send?error";
            }
        }

        // Reset payment fields
        if(model.containsAttribute("paymentInfo")) {
            model.addAttribute("paymentInfo", new PaymentInfo());
        }

        addDataToModel(model, userAccount, 0);
        return "/send";
    }

    private void addDataToModel(Model model, UserAccount userAccount, Integer selectedContactId) {
        List<OperationDisplay> operationDisplays = getOperations(userAccount.getOperations());

        model.addAttribute("selectedContactId", selectedContactId);
        model.addAttribute("operations", operationDisplays);
        model.addAttribute("contacts", userAccount.getContacts());
    }

    private List<OperationDisplay> getOperations(List<Operation> operations) {
        List<OperationDisplay> operationDisplays = new ArrayList<>();
        for (Operation operation : operations) {
            if(operation.getOperationType() == OperationType.PAYMENT) {
                UserAccount recipient = userService.findUserById(operation.getRecipientId());
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

        return operationDisplays;
    }

}
