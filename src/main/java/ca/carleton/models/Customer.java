package ca.carleton.models;

import javax.persistence.Entity;
import java.sql.Date;

@Entity
public class Customer extends Users {
    private Date accessExpiration;

    public Customer(String username, String name, String password, Date accessExpiration) {
        super(username, name, password);
        this.accessExpiration = accessExpiration;
    }

    public Customer() {
    }

    public Date getAccessExpiration() {
        return accessExpiration;
    }

    public void setAccessExpiration(Date accessExpiration) {
        this.accessExpiration = accessExpiration;
    }
}
