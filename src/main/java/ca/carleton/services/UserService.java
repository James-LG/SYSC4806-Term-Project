package ca.carleton.services;

import ca.carleton.models.Customer;
import ca.carleton.models.User;

import java.util.List;

public interface UserService {
    void save(User user);

    User findByUsername(String username);

    List<Customer> allCustomer();
}
