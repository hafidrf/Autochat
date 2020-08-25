package id.co.kamil.autochat.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ItemWaform {
    String id,judul,redaksi,submit_text,dest_number,dest_name,domain_code,domain,created,url;
    JSONArray field;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    public ItemWaform(String id, String judul, String redaksi, String submit_text, String dest_number, String dest_name, String domain_code, String domain, String created, String url, JSONArray field, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.judul = judul;
        this.redaksi = redaksi;
        this.submit_text = submit_text;
        this.dest_number = dest_number;
        this.dest_name = dest_name;
        this.domain_code = domain_code;
        this.domain = domain;
        this.created = created;
        this.url = url;
        this.field = field;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getRedaksi() {
        return redaksi;
    }

    public void setRedaksi(String redaksi) {
        this.redaksi = redaksi;
    }

    public String getSubmit_text() {
        return submit_text;
    }

    public void setSubmit_text(String submit_text) {
        this.submit_text = submit_text;
    }

    public String getDest_number() {
        return dest_number;
    }

    public void setDest_number(String dest_number) {
        this.dest_number = dest_number;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
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

    public JSONArray getField() {
        return field;
    }

    public void setField(JSONArray field) {
        this.field = field;
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
