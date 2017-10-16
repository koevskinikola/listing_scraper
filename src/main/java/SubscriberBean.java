import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;

public class SubscriberBean implements Serializable {

    @CsvBindByName
    String email;

    @CsvBindByName
    String name;

    public SubscriberBean() {}

    public SubscriberBean(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SubscriberBean [" + "Email=" + email + ", Name=" + name + ']';
    }

    public Boolean isEmpty() {
        return (this.name.equals("") || this.email.equals("") || this.name == null || this.email == null);
    }
}
