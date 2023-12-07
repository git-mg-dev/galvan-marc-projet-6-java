package com.paymybuddy.controller;

import com.paymybuddy.model.*;
import com.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    private final OAuth2AuthorizedClientService authorizedClientService;

    public LoginController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/login")
    public String displayLoginForm() {
        return "/login";
    }

    //TODO: ajouter remember me

    @GetMapping("/*")
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {

        UserAccount userAccount = getUserInfo(user, oidcUser);
        if (userAccount == null) {
            return ("/error");
        } else {
            model.addAttribute("firstName", userAccount.getFirstName());
            model.addAttribute("balance", userAccount.getAccountBalance()+"€");
            return ("/index");
        }
    }

    private UserAccount getUserInfo(Principal user, OidcUser oidcUser) {

        if(user instanceof UsernamePasswordAuthenticationToken) {
            return getLoggedUserInfo(user);
        } else if (user instanceof OAuth2AuthenticationToken) {
            return getAndRegisterOAuthUser(user, oidcUser);
        } else {
            return null;
        }
    }

    private UserAccount getAndRegisterOAuthUser(Principal user, OidcUser oidcUser) {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) user;

        if (authToken.isAuthenticated()) {
            if (oidcUser != null && !oidcUser.getAttributes().isEmpty()) {
                String email = oidcUser.getEmail();

                //User not registered
                if (!userService.emailExists(email)) {
                    String firstName = oidcUser.getAttribute("given_name");
                    String lastName = oidcUser.getAttribute("family_name");
                    String password = oidcUser.getIdToken().getTokenValue();

                    RegisterInfo registerInfo = new RegisterInfo(email, firstName, lastName, password, password);
                    return userService.registerNewUserAccount(registerInfo, true);

                } else {
                    //User already registered
                    return userService.findUserByEmail(email);
                }
            } else {
                return null; //User info not available
            }
        } else {
            return null; //User not authenticated
        }
    }

    private UserAccount getLoggedUserInfo(Principal user) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;

        if(token.isAuthenticated()) {
            User u = (User) token.getPrincipal();
            return userService.findUserByEmail(u.getUsername());
        } else {
            return null;
        }
    }

}
