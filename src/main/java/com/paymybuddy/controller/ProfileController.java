package com.paymybuddy.controller;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.exceptions.WrongPasswordException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
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
import java.util.Date;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;

    @GetMapping("/profile")
    public String displayProfile(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            addDataToModel(model, userAccount);
            return "/profile";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid ContactDisplay userData, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount.setEmail(userData.getEmail());
                userAccount.setFirstName(userData.getFirstName());
                userAccount.setLastName(userData.getLastName());

                userAccount = userService.updateUserInfo(userAccount, false);
                return "redirect:profile?success";
            } catch (UserNotFountException | NullUserException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:profile?error";
            }
        }
        addDataToModel(model, userAccount);
        return "/profile";
    }

    @PostMapping("/profile_password")
    public String changePassword(@Valid PasswordChange passwordChange, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = userService.changePassword(userAccount, passwordChange);
                return "redirect:profile?success";

            } catch (WrongPasswordException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:profile?wrongpassword";

            } catch (NullUserException | UserNotFountException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:profile?error";
            }
        }
        addDataToModel(model, userAccount);
        return "/profile";
    }

    @PostMapping("/profile_close")
    public String disableUser(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (userAccount.getAccountBalance() == 0) {
            try {
                userAccount.setStatus(UserStatus.DISABLED);
                userAccount.setDeletionDate(new Date());
                userAccount = userService.updateUserInfo(userAccount, false);

                //TODO log
                return "redirect:logout";
            } catch (UserNotFountException | NullUserException e) {
                //TODO log
                return "redirect:profile?error";
            }
        } else {
            addDataToModel(model, userAccount);
            return "redirect:profile?balance";
        }
    }

    private void addDataToModel(Model model, UserAccount userAccount) {
        ContactDisplay contactDisplay = new ContactDisplay(userAccount.getId(), userAccount.getEmail(), userAccount.getFirstName(), userAccount.getLastName(), userAccount.isOpenidconnectUser());
        PasswordChange passwordChange = new PasswordChange();

        model.addAttribute("userInfo", contactDisplay);
        model.addAttribute("passwordInfo", passwordChange);
    }
}
