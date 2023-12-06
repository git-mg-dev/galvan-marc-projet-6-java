package com.paymybuddy.controller;

import com.paymybuddy.exceptions.UserAlreadyExistException;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
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
                    return "/";
                } else {
                    //TODO: user account creation failed
                    model.addAttribute("registerForm", registerInfo);
                    return "/register";
                }
            } catch (UserAlreadyExistException userAlreadyExistException) {
                //TODO: error message, An account with that email already exists + link to /login
                model.addAttribute("registerForm", registerInfo);
                return "/register";
            }
        } else {
            model.addAttribute("registerForm", registerInfo);
            return "/register";
        }
    }


}
