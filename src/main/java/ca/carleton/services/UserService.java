package ca.carleton.services;

import ca.carleton.models.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
