package id.co.kamil.autochat.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLinkpage {
    String id,name,domain_code,domain,created,url;
    JSONArray data;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    public ItemLinkpage(String id, String name, String domain_code, String domain, String created, String url, JSONArray data, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.name = name;
        this.domain_code = domain_code;
        this.domain = domain;
        this.created = created;
        this.url = url;
        this.data = data;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain_code() {
        return domain_code;
    }

    public void setDomain_code(String domain_code) {
        this.domain_code = domain_code;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
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
