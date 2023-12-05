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
    public String displayLoginForm(Model model) {
        model.addAttribute("userAccount", new UserAccount());
        return "/login";
    }

    @GetMapping("/")
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser) {
        StringBuffer userInfo = new StringBuffer();

        if(user instanceof UsernamePasswordAuthenticationToken) {
            userInfo.append(getUsernamePasswordLoginInfo(user));
        } else if (user instanceof OAuth2AuthenticationToken) {
            userInfo.append(getOAuth2LoginInfo(user, oidcUser));
        }

        return userInfo.toString();
    }

    private StringBuffer getUsernamePasswordLoginInfo(Principal user) {
        StringBuffer userInfo = new StringBuffer();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;

        if(token.isAuthenticated()) {
            User u = (User) token.getPrincipal();
            UserAccount userAccount = userService.findUserByEmail(u.getUsername());
            if(userAccount != null) {
                userInfo.append("Welcome, ").append(userAccount.getFirstName());
                if(!userAccount.getContacts().isEmpty()) {
                    userInfo.append("<ul>");
                    for(Contact contact : userAccount.getContacts()) {
                        userInfo.append("<li>")
                                .append(contact.getFirstName())
                                .append(" ")
                                .append(contact.getLastName())
                                .append(" ")
                                .append(contact.getEmail())
                                .append("</li>");
                    }
                    userInfo.append("</ul>");
                }
                else {
                    userInfo.append("<br>No contact");
                }

                if (!userAccount.getOperations().isEmpty()) {
                    userInfo.append("<ul>");
                    for(Operation operation : userAccount.getOperations()) {
                        userInfo.append("<li>")
                                .append(operation.getRecipientId())
                                .append(" ")
                                .append(operation.getDescription())
                                .append(" ")
                                .append(operation.getAmount())
                                .append(" ")
                                .append(operation.getOperationDate())
                                .append("</li>");
                    }
                    userInfo.append("</ul>");
                }
                else {
                    userInfo.append("<br>No operation");
                }
            } else {
                userInfo.append("Welcome, ").append(u.getUsername());
            }
        } else {
            userInfo.append("NA");
        }

        return userInfo;
    }

    private StringBuffer getOAuth2LoginInfo(Principal user, OidcUser oidcUser) {
        StringBuffer protectedInfo = new StringBuffer();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) user;

        if(authToken.isAuthenticated()) {
            if(oidcUser != null && !oidcUser.getAttributes().isEmpty()) {
                String email = oidcUser.getEmail();
                String firstName = oidcUser.getAttribute("given_name");
                String lastName = oidcUser.getAttribute("family_name");
                String password = oidcUser.getIdToken().getTokenValue();
                Boolean emailVerified = oidcUser.getEmailVerified();

                protectedInfo.append("Welcome, ").append(firstName);

                if(!userService.emailExists(email) && emailVerified) {
                    RegisterInfo registerInfo = new RegisterInfo(email, firstName, lastName, password, password);
                    UserAccount userAccount = userService.registerNewUserAccount(registerInfo, true);
                }
            }
        } else {
            protectedInfo.append("NA");
        }

        return protectedInfo;
    }
}
