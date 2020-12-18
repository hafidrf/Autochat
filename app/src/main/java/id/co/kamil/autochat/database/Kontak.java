package id.co.kamil.autochat.database;

public class Kontak {
    String id, phone, sapaan, firstname, lastname;

    public Kontak() {
    }

    @Override
    public String toString() {
        return "Kontak{" +
                "id='" + id + '\'' +
                ", phone='" + phone + '\'' +
                ", sapaan='" + sapaan + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }

    public Kontak(String id, String phone, String sapaan, String firstname, String lastname) {
        this.id = id;
        this.phone = phone;
        this.sapaan = sapaan;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSapaan() {
        return sapaan;
    }

    public void setSapaan(String sapaan) {
        this.sapaan = sapaan;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
