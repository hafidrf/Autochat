package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemNotif {
    String id,title,body,url;
    JSONObject json;
    boolean checkbox,chkvisible;

    @Override
    public String toString() {
        return "ItemNotif{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", url='" + url + '\'' +
                ", json=" + json +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public ItemNotif(String id, String title, String body, String url, JSONObject json, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.url = url;
        this.json = json;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public ItemNotif(String id, String title, String body, String url) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.url = url;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
