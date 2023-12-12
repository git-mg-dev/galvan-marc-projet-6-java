package com.paymybuddy.controller;

import com.paymybuddy.exceptions.ContactAlreadyExistsException;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.ContactService;
import com.paymybuddy.service.SecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class ContactController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private SecurityService securityService;

    @GetMapping("/contact")
    public String displayContacts(Principal user, Model model, ContactInfo contactInfo) {
        UserAccount userAccount = securityService.getUserInfo(user, null);

        if(userAccount != null) {
            // TODO: don't display disabled contacts
            model.addAttribute("contacts", userAccount.getContacts());
            return "/contact";
        } else {
            return "/index";
        }
    }

    @GetMapping("/contact-remove")
    public String removeContact(@RequestParam int id, Principal user) {
        UserAccount userAccount = securityService.getUserInfo(user, null);
        try{
            userAccount = contactService.removeContact(userAccount, id);
        } catch (NullUserException | UserNotFountException e) {
            //TODO log
            return "redirect:contact";
        }

        return "redirect:contact";
    }

    @PostMapping("/contact")
    public String addContact(@Valid ContactInfo contactInfo, BindingResult bindingResult, Principal user, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, null);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = contactService.addContact(userAccount, contactInfo.getEmail());
            } catch (UserNotFountException | NullUserException | ContactAlreadyExistsException e) {
                //TODO log
                ObjectError error = new ObjectError("error", e.getMessage());
                bindingResult.addError(error);
                return "redirect:contact?error";
            }
        }

        // Reset email field
        if(model.containsAttribute("contactInfo")) {
            model.addAttribute("contactInfo", new ContactInfo());
        }

        model.addAttribute("contacts", userAccount.getContacts());
        return "/contact";
    }

}
