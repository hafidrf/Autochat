package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemOperator {
    String id,nama,email;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    @Override
    public String toString() {
        return "ItemOperator{" +
                "id='" + id + '\'' +
                ", nama='" + nama + '\'' +
                ", email='" + email + '\'' +
                ", jsonObject=" + jsonObject +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public ItemOperator(String id, String nama, String email, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.jsonObject = jsonObject;
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

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
