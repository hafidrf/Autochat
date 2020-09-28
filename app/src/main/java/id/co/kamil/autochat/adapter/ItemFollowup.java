package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemFollowup {
    String id, judul;
    JSONObject jsonObject;
    boolean checkbox, chkvisible;

    @Override
    public String toString() {
        return "ItemFollowup{" +
                "id='" + id + '\'' +
                ", judul='" + judul + '\'' +
                ", jsonObject=" + jsonObject +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public ItemFollowup(String id, String judul, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.judul = judul;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
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
}
