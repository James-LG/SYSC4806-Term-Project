package ca.carleton.models;

import javax.persistence.Entity;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Customer extends User {
    private String accessExpiration;
    private String subscription;
    private boolean subBol;

    public Customer(String username, String name, String password) {
        super(username, name, password);
        subBol = false;
    }

    public Customer() {
    }

    public boolean getSubBol() { return subBol;}

    public void setSubBol(boolean subBol) {this.subBol = subBol;}

    public String getSubscription(){ return subscription; }

    public void setSubscription(String sub) {
        subscription = sub;
        if (sub == "PAID") {
            subBol = true;
        }
        if (sub == "TRIAL") {
            subBol = false;
        }
    }

    public String getAccessExpiration() {
        return accessExpiration;
    }

    public void setAccessExpiration(String accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public void startExpiration() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        Date expiration = cal.getTime();
        DateFormat expirationFormat = new SimpleDateFormat("MM/dd/yyyy");
        accessExpiration = expirationFormat.format(expiration);
    }

}
