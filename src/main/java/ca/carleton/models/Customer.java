package ca.carleton.models;

import javax.persistence.Entity;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Customer extends User {
    private Date accessExpiration;
    private boolean subscription;

    public Customer(String username, String name, String password) {
        super(username, name, password);
        subscription = false;
    }

    public Customer() {
    }

    public boolean getSubscription(){ return subscription; }

    public void setSubscription(boolean sub) {
        subscription = sub;
    }

    public Date getAccessExpiration() {
        return accessExpiration;
    }

    public void setAccessExpiration(Date accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public void startExpiration() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        accessExpiration = cal.getTime();
    }

}
