import com.opencsv.bean.CsvBindByName;

public class SubscriberBean {

    @CsvBindByName
    private String Name;

    @CsvBindByName
    private String Email;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    @Override
    public String toString() {
        return "SubscriberBean{" + "Name=" + Name + ", Email=" + Email + '}';
    }
}
