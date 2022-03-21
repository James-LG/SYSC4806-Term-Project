package ca.carleton.controllers;

import ca.carleton.models.Customer;
import ca.carleton.models.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void signUpShouldSaveCustomer() throws Exception {
        // arrange
        Customer customer = new Customer("bob", "Bob", "password", Date.valueOf("2022-01-01"));

        when(userRepository.save(customer))
                .thenReturn(customer);

        // act
        this.mockMvc.perform(
                post("/signup")
                        .flashAttr("customer", customer)).andReturn();

        // assert
        verify(userRepository, times(1)).save(customer);
    }
}
