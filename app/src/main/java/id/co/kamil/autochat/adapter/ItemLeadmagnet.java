package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemLeadmagnet {
    String id,name,field,domain,sub_domain,message,group_contact,status,klik,submit,url_qrcode,url_download;
    JSONObject json;
    boolean checkbox,chkvisible;

    public String getUrl_download() {
        return url_download;
    }

    public void setUrl_download(String url_download) {
        this.url_download = url_download;
    }

    public String getUrl_qrcode() {
        return url_qrcode;
    }

    public void setUrl_qrcode(String url_qrcode) {
        this.url_qrcode = url_qrcode;
    }

    @Override
    public String toString() {
        return "ItemLeadmagnet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", field='" + field + '\'' +
                ", domain='" + domain + '\'' +
                ", sub_domain='" + sub_domain + '\'' +
                ", message='" + message + '\'' +
                ", group_contact='" + group_contact + '\'' +
                ", status='" + status + '\'' +
                ", klik='" + klik + '\'' +
                ", submit='" + submit + '\'' +
                ", url_qrcode='" + url_qrcode + '\'' +
                ", url_download='" + url_download + '\'' +
                ", json=" + json +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public ItemLeadmagnet(String id, String name, String field, String domain, String sub_domain, String message, String group_contact, String status, String klik, String submit, String url_qrcode, String url_download, JSONObject json, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.name = name;
        this.field = field;
        this.domain = domain;
        this.sub_domain = sub_domain;
        this.message = message;
        this.group_contact = group_contact;
        this.status = status;
        this.klik = klik;
        this.submit = submit;
        this.url_qrcode = url_qrcode;
        this.url_download = url_download;
        this.json = json;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public ItemLeadmagnet(String id, String name, String field, String domain, String sub_domain, String message, String group_contact, String status, String klik, String submit, JSONObject json, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.name = name;
        this.field = field;
        this.domain = domain;
        this.sub_domain = sub_domain;
        this.message = message;
        this.group_contact = group_contact;
        this.status = status;
        this.klik = klik;
        this.submit = submit;
        this.json = json;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public ItemLeadmagnet(String id, String name, String field, String domain, String sub_domain, String message, String group_contact, String status, JSONObject json, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.name = name;
        this.field = field;
        this.domain = domain;
        this.sub_domain = sub_domain;
        this.message = message;
        this.group_contact = group_contact;
        this.status = status;
        this.json = json;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getKlik() {
        return klik;
    }

    public void setKlik(String klik) {
        this.klik = klik;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSub_domain() {
        return sub_domain;
    }

    public void setSub_domain(String sub_domain) {
        this.sub_domain = sub_domain;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroup_contact() {
        return group_contact;
    }

    public void setGroup_contact(String group_contact) {
        this.group_contact = group_contact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
