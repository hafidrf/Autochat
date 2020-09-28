package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemTemplateDictionary {
    String id, keyword, nilai;
    JSONObject jsonObject;
    boolean checkbox, chkvisible;

    public ItemTemplateDictionary(String id, String keyword, String nilai, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.keyword = keyword;
        this.nilai = nilai;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    @Override
    public String toString() {
        return "ItemTemplateDictionary{" +
                "id='" + id + '\'' +
                ", keyword='" + keyword + '\'' +
                ", nilai='" + nilai + '\'' +
                ", jsonObject=" + jsonObject +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getNilai() {
        return nilai;
    }

    public void setNilai(String nilai) {
        this.nilai = nilai;
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
