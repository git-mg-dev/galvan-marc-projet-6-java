package com.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // User info
    @Test
    public void displayProfileFormTest() throws Exception {
        mockMvc.perform(get("/profile").with(user("pauline.test@mail.com").password("password")))
                .andExpect(model().attributeExists("contactDisplay"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/profile");
    }

    @Test
    public void updateProfileTest_OK() throws Exception {
        mockMvc.perform(post("/profile").with(user("pauline.test@mail.com").password("password"))
                        .param("email", "pauline.newname@mail.com")
                        .param("firstname", "Pauline")
                        .param("lastname", "Newname"))
                .andReturn().toString().equals("redirect:profile?success");
    }

    @Test
    public void updateProfile_WithDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/profile").with(user("mr@mail.com").password("password"))
                        .param("email", "mr@mail.com")
                        .param("firstname", "Marius")
                        .param("lastname", "IsBack"))
                .andReturn().toString().equals("/profile");
    }

    // Change password
    @Test
    public void displayPasswordFormTest() throws Exception {
        mockMvc.perform(get("/profile_password").with(user("pauline.test@mail.com").password("password")))
                .andExpect(model().attributeExists("passwordChange"))
                .andExpect(model().attributeExists("openidconnectUser"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/profile_password");
    }

    @Test
    public void changePasswordTest_OK() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("pauline.test@mail.com").password("password"))
                        .param("currentPassword", "password")
                        .param("newPassword", "NewPa$$w0rd")
                        .param("confirmPassword", "NewPa$$w0rd"))
                .andReturn().toString().equals("redirect:profile_password?success");
    }

    @Test
    public void changePasswordTest_WithWrongPassword_Fail() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("pauline.test@mail.com").password("password"))
                        .param("currentPassword", "wrongpassword")
                        .param("newPassword", "NewPa$$w0rd")
                        .param("confirmPassword", "NewPa$$w0rd"))
                .andReturn().toString().equals("/profile_password");
    }

    @Test
    public void changePasswordTest_WithDifferentPasswords_Fail() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("pauline.test@mail.com").password("password"))
                        .param("currentPassword", "password")
                        .param("newPassword", "NewPa$$w0rd")
                        .param("confirmPassword", "whatever"))
                .andReturn().toString().equals("/profile_password");
    }

    @Test
    public void changePasswordTest_WithDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("mr@mail.com").password("password"))
                        .param("currentPassword", "password")
                        .param("newPassword", "whatever")
                        .param("confirmPassword", "whatever"))
                .andReturn().toString().equals("/profile_password");
    }

    // Close account
    @Test
    public void displayCloseAccountFormTest() throws Exception {
        mockMvc.perform(get("/profile_close").with(user("pauline.test@mail.com").password("password")))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/profile_close");
    }

    @Test
    public void closeAccountTest_OK() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("jrocher@mail.com").password("password")))
                .andReturn().toString().equals("redirect:logout");
    }

    @Test
    public void closeAccountTest_WithBalanceNotNull_Fail() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("pauline.test@mail.com").password("password")))
                .andReturn().toString().equals("/profile_close");
    }

    @Test
    public void closeAccountTest_WithAlreadyDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/profile_password").with(user("mr@mail.com").password("password")))
                .andReturn().toString().equals("/profile_close");
    }

}
