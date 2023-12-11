package com.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
    public void userLoginTest_WithOpenIdConnectEmail_Fail() throws Exception {
        mockMvc.perform(formLogin("/login").user("openid.connect@gmail.com").password("password"))
                .andExpect(unauthenticated());
    }

    @Test
    public void userLoginTest_WithOpenIdConnectToken_OK() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claim("email", "openid.connect@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENABLED"))))
                .andExpect(status().isOk());
    }

    @Test
    public void userLoginTest_WithOpenIdConnectTokenDisabled_Fail() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claim("email", "openid.disabled@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_DISABLED"))))
                .andExpect(status().is3xxRedirection());
    }

}
