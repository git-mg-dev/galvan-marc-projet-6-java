package com.paymybuddy.controller;

import com.paymybuddy.exceptions.InvalidRegisterInformation;
import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.UserAlreadyExistException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void userLoginTest_WithValidUser_OK() throws Exception {
        mockMvc.perform(formLogin("/login").user("pauline.test@mail.com").password("password"))
                .andExpect(authenticated());
    }

    @Test
    public void userLoginTest_WithWrongPassword_Fail() throws Exception {
        mockMvc.perform(formLogin("/login").user("pauline.test@mail.com").password("azerty"))
                .andExpect(unauthenticated());
    }

    @Test
    public void userLoginTest_WithDisabledUser() throws Exception {
        mockMvc.perform(formLogin("/login").user("mr@mail.com").password("password"))
                .andExpect(unauthenticated());
    }

    @Test
    public void userLoginTest_WithOpenIdConnectEmail() throws Exception {
        mockMvc.perform(formLogin("/login").user("openid.connect@gmail.com").password("password"))
                .andExpect(unauthenticated());
    }


    //OpenIdConnect login
    /*@Test
    public void openIdConnectLoginTest() throws Exception {
        OAuth2User principal = createOAuth2User("Oidc User", "openid.connect@gmail.com");

        mockMvc.perform(get("/")
                        .with((RequestPostProcessor)authentication(getOauthAuthenticationFor(principal))))
                .andExpect(status().isOk());
    }

    private static OAuth2User createOAuth2User(String name, String email) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ENABLED"));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234567890");
        attributes.put("name", name);
        attributes.put("email", email);

        return new DefaultOAuth2User(authorities, attributes, "sub");
    }

    private static Authentication getOauthAuthenticationFor(OAuth2User principal) {
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        String authorizedClientRegistrationId = "my-oauth-client";

        return new OAuth2AuthenticationToken(principal, authorities, authorizedClientRegistrationId);
    }*/

    //TODO:
    //OpenIdConnect login fail
    //Logout
    //Logout fail

}
