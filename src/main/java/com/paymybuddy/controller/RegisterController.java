package com.paymybuddy.controller;

import com.paymybuddy.exceptions.InvalidRegisterInformation;
import com.paymybuddy.exceptions.UserAlreadyExistException;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;

    @GetMapping("/register")
    public String displayRegisterForm(Model model) {

        model.addAttribute("registerInfo", new RegisterInfo());
        return "/register";
    }

    @PostMapping("/register")
    public String submitRegisterForm(@Valid RegisterInfo registerInfo, BindingResult bindingResult, Model model) {
        if(!bindingResult.hasErrors()) {
            try {
                UserAccount newUserAccount = userService.registerNewUserAccount(registerInfo, false);

                if (newUserAccount != null) {
                    securityService.autoLogin(registerInfo.getEmail(), registerInfo.getPassword());
                    model.addAttribute("firstName", newUserAccount.getFirstName());
                    model.addAttribute("balance", newUserAccount.getAccountBalance()+"€");
                    return "/index";
                } else {
                    model.addAttribute("registerForm", registerInfo);
                    bindingResult.addError(new ObjectError("global", "User account creation failed, please try again."));
                    return "/register";
                }
            } catch (UserAlreadyExistException userAlreadyExistException) {
                model.addAttribute("registerForm", registerInfo);
                ObjectError error = new ObjectError("global", "An account with that email already exists, please log in.");
                bindingResult.addError(error);
                return "/register";
            } catch (InvalidRegisterInformation e) {
                model.addAttribute("registerForm", registerInfo);
                ObjectError error = new ObjectError("global", "Invalid information, please correct your information.");
                bindingResult.addError(error);
                return "/register";
            }
        } else {
            model.addAttribute("registerForm", registerInfo);
            return "/register";
        }
    }


}
