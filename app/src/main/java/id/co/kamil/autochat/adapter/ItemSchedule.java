package id.co.kamil.autochat.adapter;

public class ItemSchedule {
    String id, nama, jadwalkirim, tipe, pesan, status;
    boolean checkbox, chkvisible, group;

    public ItemSchedule(String id, String nama, String jadwalkirim, String tipe, String pesan, String status, boolean group, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.nama = nama;
        this.jadwalkirim = jadwalkirim;
        this.tipe = tipe;
        this.pesan = pesan;
        this.status = status;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
        this.group = group;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getJadwalkirim() {
        return jadwalkirim;
    }

    public void setJadwalkirim(String jadwalkirim) {
        this.jadwalkirim = jadwalkirim;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isChkvisible() {
        return chkvisible;
    }

    public void setChkvisible(boolean chkvisible) {
        this.chkvisible = chkvisible;
    }

    @Override
    public String toString() {
        return "ItemSchedule{" +
                "id='" + id + '\'' +
                ", nama='" + nama + '\'' +
                ", jadwalkirim='" + jadwalkirim + '\'' +
                ", tipe='" + tipe + '\'' +
                ", pesan='" + pesan + '\'' +
                ", status='" + status + '\'' +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }
}
