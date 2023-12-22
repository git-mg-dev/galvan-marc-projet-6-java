package com.paymybuddy.controller;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.exceptions.WrongPasswordException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Date;

@Log4j2
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
            ContactDisplay contactDisplay = new ContactDisplay(userAccount.getId(), userAccount.getEmail(),
                    userAccount.getFirstName(), userAccount.getLastName(), userAccount.isOpenidconnectUser());
            model.addAttribute("contactDisplay", contactDisplay);
            return "/profile";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid ContactDisplay contactDisplay, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount.setEmail(contactDisplay.getEmail());
                userAccount.setFirstName(contactDisplay.getFirstName());
                userAccount.setLastName(contactDisplay.getLastName());

                userAccount = userService.updateUserInfo(userAccount, false, false);
                return "redirect:profile?success";
            } catch (UserNotFountException | NullUserException e) {
                userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
                model.addAttribute("contactDisplay", contactDisplay);
                model.addAttribute("updateProfileError", "Update profile failed: " + e.getMessage());
                return "/profile";
            }
        }
        model.addAttribute("contactDisplay", contactDisplay);
        return "/profile";
    }

    @GetMapping("/profile_password")
    public String displayPasswordForm(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            model.addAttribute("passwordChange", new PasswordChange());
            model.addAttribute("openidconnectUser", userAccount.isOpenidconnectUser());
            return "/profile_password";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/profile_password")
    public String updatePassword(@Valid PasswordChange passwordChange, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(!bindingResult.hasErrors()) {
            try {
                userAccount = userService.changePassword(userAccount, passwordChange);
                return "redirect:profile_password?success";

            } catch (WrongPasswordException | NullUserException | UserNotFountException e) {
                userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
                model.addAttribute("passwordChange", passwordChange);
                model.addAttribute("openidconnectUser", userAccount.isOpenidconnectUser());
                model.addAttribute("changePasswordError", "Changing password failed: " + e.getMessage());
                return "/profile_password";

            }
        } else {
            model.addAttribute("passwordChange", passwordChange);
            model.addAttribute("openidconnectUser", userAccount.isOpenidconnectUser());
            return "/profile_password";
        }
    }

    @GetMapping("/profile_close")
    public String displayCloseProfile(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            return "/profile_close";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/profile_close")
    public String disableUser(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (userAccount.getAccountBalance() == 0) {
            try {
                userAccount = userService.updateUserInfo(userAccount, false, true);

                log.info("User account " + userAccount.getId() + " has just been disabled");
                return "redirect:logout";
            } catch (UserNotFountException | NullUserException e) {
                userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
                model.addAttribute("closeProfileError", "Closing account failed: " + e.getMessage());
                return "/profile_close";
            }
        } else {
            userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
            model.addAttribute("closeProfileError", "Closing account failed: Balance must be 0€ before closing ");
            return "/profile_close";
        }
    }
}
