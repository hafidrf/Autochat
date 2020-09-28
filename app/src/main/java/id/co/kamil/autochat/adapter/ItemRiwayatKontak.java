package id.co.kamil.autochat.adapter;

public class ItemRiwayatKontak {
    private String id, komentar, action, status; // status = pending,sukses,batal ; action = delete, add

    @Override
    public String toString() {
        return "ItemRiwayatKontak{" +
                "id='" + id + '\'' +
                ", komentar='" + komentar + '\'' +
                ", action='" + action + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public ItemRiwayatKontak(String id, String komentar) {
        this.id = id;
        this.komentar = komentar;
    }

    public ItemRiwayatKontak(String id, String komentar, String action, String status) {
        this.id = id;
        this.komentar = komentar;
        this.action = action;
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }
}
