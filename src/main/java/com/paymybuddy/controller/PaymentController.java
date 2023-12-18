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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private OperationService operationService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @GetMapping("/transfer")
    public String displayPayments(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model, PaymentInfo paymentInfo, @RequestParam(required = false) Integer id) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);
        if(userAccount != null) {
            if(id == null) {
                id = 0;
            }
            addDataToModel(model, userAccount, id, "");
            return "/transfer";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/transfer")
    public String sendPayment(@Valid PaymentInfo paymentInfo, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = operationService.sendPayment(userAccount, paymentInfo);
                return "redirect:transfer?success";
            } catch (NullUserException | UserNotFountException | PaymentFailedException e) {
                userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
                addDataToModel(model, userAccount, paymentInfo.getRecipientId(), e.getMessage());
                return "/transfer";
            }
        } else {
            addDataToModel(model, userAccount, 0, "");
            return "/transfer";
        }
    }

    /**
     * Add attributes to the model so they can be displayed
     * @param model to add attributes to
     * @param userAccount user data
     * @param selectedContactId id of contact to send payment to
     */
    private void addDataToModel(Model model, UserAccount userAccount, Integer selectedContactId, String errorMessage) {
        List<OperationDisplay> operationDisplays = userService.getPaymentToDisplay(userAccount);
        List<ContactDisplay> contactDisplays = userService.getContactToDisplay(userAccount);

        model.addAttribute("balance", userAccount.getAccountBalance());
        model.addAttribute("operations", operationDisplays);
        model.addAttribute("contacts", contactDisplays);
        model.addAttribute("selectedContactId", selectedContactId);

        if(!errorMessage.isEmpty()) {
            model.addAttribute("transferError", "Payment failed: " + errorMessage);
        }
    }

}
