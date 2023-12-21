package com.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class RegisterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void displayRegisterFormTest() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().size(1))
                .andExpect(model().attributeExists("registerInfo"));
    }

    @Test
    public void registerTest_WithValidInformation_OK() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "pauline.second@mail.com")
                        .param("firstName", "Pauline")
                        .param("lastName", "Second")
                        .param("password", "password")
                        .param("passwordConfirm", "password"))
                .andReturn().toString().equals("redirect:register?success");
    }

    @Test
    public void registerTest_UserAlreadyExists_Fail() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "pauline.test@mail.com")
                        .param("firstName", "Pauline")
                        .param("lastName", "Test")
                        .param("password", "password")
                        .param("passwordConfirm", "password"))
                .andReturn().toString().equals("/register");
    }

    @Test
    public void registerTest_InvalidRegisterInfo_Fail() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "pauline.test@mail.com")
                        .param("firstName", "Pauline")
                        .param("lastName", "Test")
                        .param("password", "password")
                        .param("passwordConfirm", "pass"))
                .andReturn().toString().equals("/register");
    }
}
