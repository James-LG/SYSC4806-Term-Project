package ca.carleton.controllers;

import ca.carleton.AuthenticationConfig;
import ca.carleton.WebSecurityConfig;
import ca.carleton.models.Customer;
import ca.carleton.services.SecurityService;
import ca.carleton.services.SecurityServiceImpl;
import ca.carleton.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Date;


import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void signUpShouldSaveCustomer() throws Exception {
        // arrange
        Customer customer = new Customer("bob", "Bob", "password", Date.valueOf("2022-01-01"));

//        when(userService.save(customer))
//                .thenReturn(customer);

        // act
        MvcResult mvcResult = this.mockMvc.perform(
                post("/signup")
                        .with(csrf())
                        .flashAttr("customer", customer)).andReturn();

        // assert
        verify(userService, times(1)).save(customer);
    }
}
