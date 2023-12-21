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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class OperationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // Transfer to bank account
    @Test
    public void displaySendFormTest() throws Exception {
        mockMvc.perform(get("/send").with(user("pauline.test@mail.com").password("password")))
                .andExpect(model().attributeExists("balance"))
                .andExpect(model().attributeExists("transferInfo"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/send");
    }

    @Test
    public void registerTransferTest_OK() throws Exception {
        mockMvc.perform(post("/send").with(user("pauline.test@mail.com").password("password"))
                        .param("iban", "FR6114508000403618798166B64"))
                .andReturn().toString().equals("redirect:send?success");
    }

    @Test
    public void registerTransferTest_WithEmptyBalance_Fail() throws Exception {
        mockMvc.perform(post("/send").with(user("jrocher@mail.com").password("password"))
                        .param("iban", "FR6114508000403618798166B64"))
                .andReturn().toString().equals("/send");
    }

    @Test
    public void registerTransferTest_WithDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/send").with(user("mr@mail.com").password("password"))
                        .param("iban", "FR6114508000403618798166B64"))
                .andReturn().toString().equals("/send");
    }

    // Deposit on user account
    @Test
    public void displayDepositFormTest() throws Exception {
        mockMvc.perform(get("/deposit").with(user("pauline.test@mail.com").password("password")))
                .andExpect(model().attributeExists("balance"))
                .andExpect(model().attributeExists("depositInfo"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/deposit");
    }

    @Test
    public void registerDepositTest_OK() throws Exception {
        mockMvc.perform(post("/deposit").with(user("pauline.test@mail.com").password("password"))
                        .param("amount", "50"))
                .andReturn().toString().equals("redirect:deposit?success");
    }

    @Test
    public void registerDepositTest_WithNullAmount_Fail() throws Exception {
        mockMvc.perform(post("/deposit").with(user("pauline.test@mail.com").password("password"))
                        .param("amount", "0"))
                .andReturn().toString().equals("/deposit");
    }

    @Test
    public void registerDepositTest_WithDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/deposit").with(user("mr@mail.com").password("password"))
                        .param("amount", "50"))
                .andReturn().toString().equals("/deposit");
    }

}
