package com.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.InstantSource;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
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
        mockMvc.perform(get("/index")
                        .with(oidcLogin()
                                .idToken(token -> token.claim("email", "openid.connect@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENABLED"))))
                .andExpect(status().isOk());
    }

    @Test
    public void userLoginTest_WithNewOpenIdConnectToken_OK() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "newopenid.connect@gmail.com");
        attributes.put("given_name", "newopenid");
        attributes.put("family_name", "connect");
        attributes.put("sub", "111678394972363534681");

        Instant start = Instant.now();
        Instant end = Instant.now().plus(1, ChronoUnit.HOURS);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList("ROLE_ENABLED"),
                attributes, "email"
        );

        OidcIdToken oidcIdToken = new OidcIdToken("tokenvalue", start, end, Collections.singletonMap("email", "newopenid.connect@gmail.com"));
        OidcUserInfo oidcUserInfo = new OidcUserInfo(attributes);
        OidcUser oidcUser = new DefaultOidcUser(AuthorityUtils.createAuthorityList("ROLE_ENABLED"), oidcIdToken, oidcUserInfo);

        mockMvc.perform(get("/index").with(oidcLogin().oidcUser(oidcUser)))
                .andExpect(status().isOk());
    }

    @Test
    public void userLoginTest_WithOpenIdConnectTokenDisabled_Fail() throws Exception {
        mockMvc.perform(get("/index")
                        .with(oidcLogin()
                                .idToken(token -> token.claim("email", "openid.disabled@gmail.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_DISABLED"))))
                .andExpect(status().is3xxRedirection());
    }

}
