package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemGrup {
    String id, judul, deskripsi, member;
    JSONObject jsonObject;
    boolean checkbox, chkvisible;

    public ItemGrup() {

    }

    public ItemGrup(String id, String judul, String deskripsi, String member) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.member = member;
    }

    public ItemGrup(String id, String judul, String deskripsi, String member, JSONObject jsonObject) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.member = member;
        this.jsonObject = jsonObject;
    }

    public ItemGrup(String id, String judul, String deskripsi, String member, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.member = member;
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
        this.id =id;
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

    public String getMember() {
        return member;
    }

    public void setMember ( String member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", judul:'" + judul + '\'' +
                ", deskripsi:'" + deskripsi + '\'' +
                ", member:'" + member + '\'' +
                ", checkbox:" + checkbox +
                ", chkvisible:" + chkvisible +
                '}';
    }
}
