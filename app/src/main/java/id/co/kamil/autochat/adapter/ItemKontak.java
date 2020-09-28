package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemKontak {
    String id, judul, nomorhp;
    boolean checkbox, chkvisible;
    JSONObject jsonObject;

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", judul:'" + judul + '\'' +
                ", nomorhp:'" + nomorhp + '\'' +
                ", checkbox:" + checkbox +
                '}';
    }

    public ItemKontak(String id, String judul, String nomorhp, boolean checkbox) {
        this.id = id;
        this.judul = judul;
        this.nomorhp = nomorhp;
        this.checkbox = checkbox;
    }

    public ItemKontak(String id, String judul, String nomorhp, boolean checkbox, JSONObject jsonObject) {
        this.id = id;
        this.judul = judul;
        this.nomorhp = nomorhp;
        this.checkbox = checkbox;
        this.jsonObject = jsonObject;
    }

    public ItemKontak(String id, String judul, String nomorhp, boolean checkbox, boolean chkvisible, JSONObject jsonObject) {
        this.id = id;
        this.judul = judul;
        this.nomorhp = nomorhp;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
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

    public String getNomorhp() {
        return nomorhp;
    }

    public void setNomorhp(String nomorhp) {
        this.nomorhp = nomorhp;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }
}
