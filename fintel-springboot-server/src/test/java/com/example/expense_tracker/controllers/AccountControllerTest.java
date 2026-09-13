package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.Account;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;
    private Account sampleAccount;
    private UsernamePasswordAuthenticationToken authPrincipalUser1;
    private UsernamePasswordAuthenticationToken authPrincipalUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "password", LocalDateTime.now());
        sampleAccount = new Account(1, sampleUser, "Checking", "CHECKING", BigDecimal.valueOf(5000), "INR", true);

        UserPrincipal principalUser1 = UserPrincipal.create(sampleUser);
        authPrincipalUser1 = new UsernamePasswordAuthenticationToken(principalUser1, null, principalUser1.getAuthorities());

        User user2 = new User(2, "Other", "other@example.com", "password", LocalDateTime.now());
        UserPrincipal principalUser2 = UserPrincipal.create(user2);
        authPrincipalUser2 = new UsernamePasswordAuthenticationToken(principalUser2, null, principalUser2.getAuthorities());
    }

    @Test
    void testGetAccounts_Success() throws Exception {
        when(accountService.getAccountsByUserId(1)).thenReturn(List.of(sampleAccount));

        mockMvc.perform(get("/api/v1/accounts/user/1")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Checking"))
                .andExpect(jsonPath("$[0].balance").value(5000));
    }

    @Test
    void testGetAccounts_CrossUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/user/1")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        when(accountService.createAccount(eq(1), any(Account.class))).thenReturn(sampleAccount);

        mockMvc.perform(post("/api/v1/accounts/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleAccount))
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Checking"));
    }
}
