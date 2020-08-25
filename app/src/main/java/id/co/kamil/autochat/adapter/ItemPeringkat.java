package id.co.kamil.autochat.adapter;

public class ItemPeringkat {
    String peringkat, email,nama,downline;

    public ItemPeringkat(String peringkat,String email, String nama, String downline) {
        this.peringkat = peringkat;
        this.email = email;
        this.nama = nama;
        this.downline = downline;
    }

    @Override
    public String toString() {
        return "ItemPeringkat{" +
                "peringkat='" + peringkat + '\'' +
                ", email='" + email + '\'' +
                ", nama='" + nama + '\'' +
                ", downline='" + downline + '\'' +
                '}';
    }

    public String getPeringkat() {
        return peringkat;
    }

    public void setPeringkat(String peringkat) {
        this.peringkat = peringkat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDownline() {
        return downline;
    }

    public void setDownline(String downline) {
        this.downline = downline;
    }
}
