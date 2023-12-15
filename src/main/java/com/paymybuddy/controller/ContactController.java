package com.paymybuddy.controller;

import com.paymybuddy.exceptions.ContactAlreadyExistsException;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.ContactService;
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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ContactController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @GetMapping("/contact")
    public String displayContacts(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model, ContactInfo contactInfo) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if(userAccount != null) {
            addDataToModel(model, userAccount);
            return "/contact";
        } else {
            return "redirect:index?error";
        }
    }

    @GetMapping("/contact-remove")
    public String removeContact(@RequestParam int id, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);
        try{
            userAccount = contactService.removeContact(userAccount, id);
        } catch (NullUserException | UserNotFountException e) {
            //TODO log
            model.addAttribute("contactError", "Remove failed: " + e.getMessage());
            addDataToModel(model, userAccount);
            return "/contact";
        }

        return "redirect:contact";
    }

    @PostMapping("/contact")
    public String addContact(@Valid ContactInfo contactInfo, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = contactService.addContact(userAccount, contactInfo.getEmail());

                // Reset email field
                model.addAttribute("contactInfo", new ContactInfo());
                return "redirect:contact?success";

            } catch (UserNotFountException | NullUserException | ContactAlreadyExistsException e) {
                //TODO log
                model.addAttribute("contactError", "Invalid email: " + e.getMessage());
                addDataToModel(model, userAccount);
                return "/contact";
            }
        }

        addDataToModel(model, userAccount);
        return "/contact";
    }

    private void addDataToModel(Model model, UserAccount userAccount) {
        List<ContactDisplay> contactDisplays = userService.getContactToDisplay(userAccount);
        model.addAttribute("contacts", contactDisplays);
    }
}
