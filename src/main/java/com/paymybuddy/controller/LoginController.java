package com.paymybuddy.controller;

import com.paymybuddy.model.*;
import com.paymybuddy.service.SecurityService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Log4j2
@Controller
public class LoginController {

    @Autowired
    private SecurityService securityService;

    @GetMapping("/login")
    public String displayLoginForm() {
        return "/login";
    }

    @GetMapping("/index")
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {

        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);
        if (userAccount == null || userAccount.getStatus() == UserStatus.DISABLED) {
            return "redirect:login?error";
        } else {
            log.info("User " + userAccount.getId() + " just logged in");

            model.addAttribute("firstName", userAccount.getFirstName());
            model.addAttribute("balance", userAccount.getAccountBalance());
            return ("/index");
        }
    }

    @GetMapping("/*")
    public String redirectToHome() {
        return "redirect:index";
    }

}
