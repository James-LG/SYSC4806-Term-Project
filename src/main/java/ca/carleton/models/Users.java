package ca.carleton.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class Users {
    private String username;
    private String name;
    private String password;
    private boolean subscription;

    public Users(String username, String name, String password) {
        this.username = username;
        this.name = name;
        this.password = password;
        subscription = false;
    }

    public Users() {
    }

    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getSubscription(){ return subscription; }

    public void setSubscription(boolean sub) {subscription = sub;}

    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}
