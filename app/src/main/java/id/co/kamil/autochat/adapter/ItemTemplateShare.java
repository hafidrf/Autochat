package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemTemplateShare {
    String id,template,totalshare;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    public ItemTemplateShare(String id, String template, String totalshare, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.template = template;
        this.totalshare = totalshare;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTotalshare() {
        return totalshare;
    }

    public void setTotalshare(String totalshare) {
        this.totalshare = totalshare;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
