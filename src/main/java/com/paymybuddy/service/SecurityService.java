package com.paymybuddy.service;

import com.paymybuddy.exceptions.InvalidRegisterInformation;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class SecurityService {
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;

    public void autoLogin(String email, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(token);

        if(authentication.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserAccount getUserInfo(Principal user, OidcUser oidcUser) {

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

                    try {
                        RegisterInfo registerInfo = new RegisterInfo(email, firstName, lastName, password, password);
                        return userService.registerNewUserAccount(registerInfo, true);
                    } catch (InvalidRegisterInformation e) {
                        return null;
                    }
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
