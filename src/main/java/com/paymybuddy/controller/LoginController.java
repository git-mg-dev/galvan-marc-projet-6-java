package com.paymybuddy.controller;

import com.paymybuddy.model.*;
import com.paymybuddy.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class LoginController {

    @Autowired
    private SecurityService securityService;
/*
    private final OAuth2AuthorizedClientService authorizedClientService;

    public LoginController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }
*/
    @GetMapping("/login")
    public String displayLoginForm() {
        return "/login";
    }

    @GetMapping("/")
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {

        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);
        if (userAccount == null || userAccount.getStatus() == UserStatus.DISABLED) {
            return "redirect:login?error";
        } else {
            model.addAttribute("firstName", userAccount.getFirstName());
            model.addAttribute("balance", userAccount.getAccountBalance()+"€");
            return ("/index");
        }
    }

}
