package ca.carleton.services;

import ca.carleton.models.User;

import java.util.List;

public interface UserService {
    void save(User user);

    User findByUsername(String username);

    Iterable<User> findAll();

    List<User> userAll();
}
