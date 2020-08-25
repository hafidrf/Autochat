package id.co.kamil.autochat.adapter;

public class ItemPesan {
    String id,nomor,nama,tglpesan,jadwalkirim,pesan,status,error_again;
    boolean checkbox,chkvisible;
    @Override
    public String toString() {
        return "ItemPesan{" +
                "id='" + id + '\'' +
                ", nomor='" + nomor + '\'' +
                ", nama='" + nama + '\'' +
                ", tglpesan='" + tglpesan + '\'' +
                ", jadwalkirim='" + jadwalkirim + '\'' +
                ", pesan='" + pesan + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public ItemPesan(String id, String nomor, String nama, String tglpesan, String jadwalkirim, String pesan, String status) {
        this.id = id;
        this.nomor = nomor;
        this.nama = nama;
        this.tglpesan = tglpesan;
        this.jadwalkirim = jadwalkirim;
        this.pesan = pesan;
        this.status = status;
    }

    public ItemPesan(String id, String nomor, String nama, String tglpesan, String jadwalkirim, String pesan, String status, String error_again, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.nomor = nomor;
        this.nama = nama;
        this.tglpesan = tglpesan;
        this.jadwalkirim = jadwalkirim;
        this.pesan = pesan;
        this.status = status;
        this.error_again = error_again;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getError_again() {
        return error_again;
    }

    public void setError_again(String error_again) {
        this.error_again = error_again;
    }

    public ItemPesan(String id, String nomor, String nama, String tglpesan, String pesan, String status) {
        this.id = id;
        this.nomor = nomor;
        this.nama = nama;
        this.tglpesan = tglpesan;
        this.pesan = pesan;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomor() {
        return nomor;
    }

    public void setNomor(String nomor) {
        this.nomor = nomor;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTglpesan() {
        return tglpesan;
    }

    public void setTglpesan(String tglpesan) {
        this.tglpesan = tglpesan;
    }

    public String getJadwalkirim() {
        return jadwalkirim;
    }

    public void setJadwalkirim(String jadwalkirim) {
        this.jadwalkirim = jadwalkirim;
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
}
