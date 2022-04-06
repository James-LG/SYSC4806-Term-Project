package ca.carleton.models;

import javax.persistence.Entity;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Customer extends User {
    private Date accessExpiration;
    private boolean subscription;
    private int accessLimit;

    public static final int TRIAL_ACCESS_LIMIT = 1000;
    public static final int PAID_ACCESS_LIMIT = 2000;

    public Customer(String username, String name, String password) {
        super(username, name, password);
        subscription = false;
        accessLimit = TRIAL_ACCESS_LIMIT;
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

    public int getAccessLimit() {
        return accessLimit;
    }

    public void setAccessLimit(int accessLimit) {
        this.accessLimit = accessLimit;
    }

    public void startExpiration() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        accessExpiration = cal.getTime();
    }

}
