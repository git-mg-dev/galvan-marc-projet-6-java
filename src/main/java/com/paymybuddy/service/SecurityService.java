package com.paymybuddy.service;

import com.paymybuddy.exceptions.InvalidRegisterInformation;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Service
public class SecurityService {
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;

    /**
     * Gets user info from logged-in user
     * @param user
     * @param oidcUser
     * @return
     */
    public UserAccount getUserInfo(Principal user, OidcUser oidcUser) {

        if(user instanceof UsernamePasswordAuthenticationToken) {
            return getLoggedUserInfo(user);
        } else if (user instanceof OAuth2AuthenticationToken) {
            return getAndRegisterOAuthUser(user, oidcUser);
        } else {
            return null;
        }
    }

    /**
     * Get info for OAuth user and register them in database if they're not registered yet
     * @param user principal
     * @param oidcUser open id connect user info
     * @return user account
     */
    private UserAccount getAndRegisterOAuthUser(Principal user, OidcUser oidcUser) {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) user;

        if (authToken.isAuthenticated()) {
            if (oidcUser != null && !oidcUser.getAttributes().isEmpty()) {
                String email = oidcUser.getEmail();
                log.debug("Getting OAuth user: user " + email + " is logging in");

                //User not registered
                if (!userService.emailExists(email)) {
                    String firstName = oidcUser.getAttribute("given_name");
                    String lastName = oidcUser.getAttribute("family_name");
                    String password = oidcUser.getIdToken().getTokenValue();

                    try {
                        RegisterInfo registerInfo = new RegisterInfo(email, firstName, lastName, password, password);
                        return userService.registerNewUserAccount(registerInfo, true);
                    } catch (InvalidRegisterInformation e) {
                        log.error("Getting OAuth user: " + e.getMessage());
                        return null;
                    }
                } else {
                    log.debug("Getting OAuth user: user is already registered");
                    return userService.findUserByEmail(email);
                }
            } else {
                log.error("Error while getting OAuth user: user info are not available");
                return null;
            }
        } else {
            log.error("Error while getting OAuth user: user not authenticated");
            return null;
        }
    }

    /**
     * Get info for logged-in
     * @param user principal
     * @return user account
     */
    private UserAccount getLoggedUserInfo(Principal user) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;

        if(token.isAuthenticated()) {
            User u = (User) token.getPrincipal();
            log.debug("Getting logged user info: user " + u.getUsername() + " is logging in");

            return userService.findUserByEmail(u.getUsername());
        } else {
            log.error("Getting logged user info failed");
            return null;
        }
    }

}
