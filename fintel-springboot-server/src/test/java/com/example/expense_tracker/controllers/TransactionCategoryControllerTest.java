package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.TransactionCategoryService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionCategoryService transactionCategoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;
    private TransactionCategory sampleCategory;
    private UsernamePasswordAuthenticationToken authPrincipalUser1;
    private UsernamePasswordAuthenticationToken authPrincipalUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Alice", "alice@example.com", "password", null);
        sampleCategory = new TransactionCategory(5, sampleUser, "Travel", "#123456");

        UserPrincipal principal1 = new UserPrincipal(1, "Alice", "alice@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser1 = new UsernamePasswordAuthenticationToken(principal1, null, principal1.getAuthorities());

        UserPrincipal principal2 = new UserPrincipal(2, "Bob", "bob@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser2 = new UsernamePasswordAuthenticationToken(principal2, null, principal2.getAuthorities());
    }

    @Test
    void testGetCategories_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/transaction-category/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCategories_WithAuth_Success() throws Exception {
        when(transactionCategoryService.getAllTransactionCategoriesByUserId(1))
                .thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/v1/transaction-category/user/1")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Travel"));
    }

    @Test
    void testGetCategories_CrossUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/transaction-category/user/1")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateCategory_Success() throws Exception {
        when(transactionCategoryService.createTransactionCategory(eq(1), eq("Travel"), eq("#123456")))
                .thenReturn(sampleCategory);

        mockMvc.perform(post("/api/v1/transaction-category")
                        .with(authentication(authPrincipalUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Travel"));
    }

    @Test
    void testDeleteCategory_Success() throws Exception {
        when(transactionCategoryService.getTransactionCategoryById(5)).thenReturn(Optional.of(sampleCategory));
        when(transactionCategoryService.deleteTransactionCategoryById(5)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/transaction-category/5")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk());
    }
}
