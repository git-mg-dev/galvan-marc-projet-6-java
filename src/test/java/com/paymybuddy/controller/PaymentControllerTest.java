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
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void displayPaymentFormTest() throws Exception {
        mockMvc.perform(get("/transfer").with(user("pauline.test@mail.com").password("password")))
                .andExpect(model().attributeExists("balance"))
                .andExpect(model().attributeExists("operations"))
                .andExpect(model().attributeExists("contacts"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().toString().equals("/transfer");
    }

    @Test
    public void registerPaymentTest_OK() throws Exception {
        mockMvc.perform(post("/transfer").with(user("pauline.test@mail.com").password("password"))
                        .param("recipientId", "2")
                        .param("description", "test")
                        .param("amount", "10"))
                .andDo(print())
                .andReturn().toString().equals("redirect:transfer?success");
    }

    @Test
    public void registerTransferTest_WithInsufficientBalance_Fail() throws Exception {
        mockMvc.perform(post("/transfer").with(user("pauline.test@mail.com").password("password"))
                        .param("recipientId", "2")
                        .param("description", "test")
                        .param("amount", "1000"))
                .andReturn().toString().equals("/transfer");
    }

    @Test
    public void registerTransferTest_WithNullAmount_Fail() throws Exception {
        mockMvc.perform(post("/transfer").with(user("pauline.test@mail.com").password("password"))
                        .param("recipientId", "2")
                        .param("description", "test")
                        .param("amount", "0"))
                .andReturn().toString().equals("/transfer");
    }

    @Test
    public void registerTransferTest_ToDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/transfer").with(user("pauline.test@mail.com").password("password"))
                        .param("recipientId", "3")
                        .param("description", "test")
                        .param("amount", "10"))
                .andReturn().toString().equals("/transfer");
    }

    @Test
    public void registerTransferTest_WithDisabledUser_Fail() throws Exception {
        mockMvc.perform(post("/transfer").with(user("mr@mail.com").password("password"))
                        .param("recipientId", "2")
                        .param("description", "test")
                        .param("amount", "10"))
                .andReturn().toString().equals("/transfer");
    }

}
