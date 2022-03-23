package ca.carleton.models;

import javax.persistence.Entity;

@Entity
public class Admin extends Users {
    public Admin(String username, String name, String password) {
        super(username, name, password);
    }

    public Admin() {}
}
