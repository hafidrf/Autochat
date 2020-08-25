package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemTemplateShareDetail {
    String id,email;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    public ItemTemplateShareDetail(String id, String email, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
