package com.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void displayContactsTest() throws Exception {
        mockMvc.perform(get("/contact").with(user("pauline.test@mail.com").password("password")))
                //.andDo(print())
                .andExpect(model().attributeExists("contacts"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/contact");
    }

    @Test
    public void removeContactTest_OK() throws Exception {
        mockMvc.perform(get("/contact_remove").with(user("pauline.test@mail.com").password("password"))
                        .param("id", "2"))
                //.andDo(print())
                .andExpect(status().is3xxRedirection())
                .andReturn().toString().equals("redirect:contact");
    }

    @Test
    public void removeContactTest_Fail() throws Exception {
        mockMvc.perform(get("/contact_remove").with(user("pauline.test@mail.com").password("password"))
                        .param("id", "8"))
                //.andDo(print())
                .andExpect(status().is3xxRedirection())
                .andReturn().toString().equals("/contact");
    }

    @Test
    public void addContactTest_WithValidInformation_OK() throws Exception {
        mockMvc.perform(post("/contact").with(user("jrocher@mail.com").password("password"))
                        .param("email", "pauline.second@mail.com"))
                //.andDo(print())
                .andReturn().toString().equals("redirect:contact?success");
    }

    @Test
    public void addContactTest_WithOwnEmail_Fail() throws Exception {
        mockMvc.perform(post("/contact").with(user("jrocher@mail.com").password("password"))
                        .param("email", "jrocher@mail.com"))
                .andReturn().toString().equals("/contact");
    }

    @Test
    public void addContactTest_ContactDisabled_Fail() throws Exception {
        mockMvc.perform(post("/contact").with(user("pauline.second@mail.com").password("password"))
                        .param("email", "openid.disabled@gmail.com"))
                .andReturn().toString().equals("/contact");
    }

    @Test
    public void addContactTest_ContactAlreadyRegistered_Fail() throws Exception {
        mockMvc.perform(post("/contact").with(user("pauline.second@mail.com").password("password"))
                        .param("email", "jrocher@mail.com"))
                .andReturn().toString().equals("/contact");
    }

}
