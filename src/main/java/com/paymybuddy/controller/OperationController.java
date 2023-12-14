package com.paymybuddy.controller;

import com.paymybuddy.exceptions.OperationFailedException;
import com.paymybuddy.model.DepositInfo;
import com.paymybuddy.model.TransferInfo;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.OperationService;
import com.paymybuddy.service.SecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class OperationController {
    @Autowired
    private OperationService operationService;
    @Autowired
    private SecurityService securityService;

    @GetMapping("/send")
    public String displayForm(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model, TransferInfo transferInfo) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            model.addAttribute("balance", userAccount.getAccountBalance());
            model.addAttribute("transferInfo", transferInfo);
            return "/send";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/send")
    public String registerTransfer(@Valid TransferInfo transferInfo, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = operationService.makeTransfer(userAccount, userAccount.getAccountBalance(), transferInfo.getIban());
                return "redirect:send?success";

            } catch (OperationFailedException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:send?error";
            }
        }

        model.addAttribute("balance", userAccount.getAccountBalance());
        model.addAttribute("transferInfo", transferInfo);
        return "/send";
    }

    @GetMapping("/deposit")
    public String displayPage(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model, DepositInfo depositInfo) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            model.addAttribute("balance", userAccount.getAccountBalance());
            model.addAttribute("depositInfo", depositInfo);
        }
        return "/deposit";
    }

    @PostMapping("/deposit")
    public String makeDeposit(@Valid DepositInfo depositInfo, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = operationService.makeDeposit(userAccount, depositInfo.getAmount());
                return "redirect:deposit?success";

            } catch (OperationFailedException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:deposit?error";
            }
        }

        model.addAttribute("balance", userAccount.getAccountBalance());
        model.addAttribute("depositInfo", depositInfo);
        return "/deposit";
    }
}
