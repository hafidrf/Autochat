package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemGrupNew {
    String id, judul, deskripsi;
    JSONObject jsonObject;
    boolean checkbox, chkvisible;

    public ItemGrupNew() {

    }

    public ItemGrupNew(String id, String judul, String deskripsi) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
    }

    public ItemGrupNew(String id, String judul, String deskripsi, JSONObject jsonObject) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.jsonObject = jsonObject;
    }

    public ItemGrupNew(String id, String judul, String deskripsi, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
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

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", judul:'" + judul + '\'' +
                ", deskripsi:'" + deskripsi + '\'' +
                ", checkbox:" + checkbox +
                ", chkvisible:" + chkvisible +
                '}';
    }
}
