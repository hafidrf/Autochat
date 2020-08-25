package id.co.kamil.autochat.adapter;

import org.json.JSONObject;

public class ItemTemplatePromosi {
    String id,created_at,tags,content,picture,name,owner_name,status_image;
    boolean owner;
    JSONObject jsonObject;
    boolean checkbox,chkvisible;

    public ItemTemplatePromosi(String id, String created_at, String tags, String content, String picture, String name, String owner_name, String status_image, boolean owner, JSONObject jsonObject, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.created_at = created_at;
        this.tags = tags;
        this.content = content;
        this.picture = picture;
        this.name = name;
        this.owner_name = owner_name;
        this.status_image = status_image;
        this.owner = owner;
        this.jsonObject = jsonObject;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getStatus_image() {
        return status_image;
    }

    public void setStatus_image(String status_image) {
        this.status_image = status_image;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ItemTemplatePromosi{" +
                "id='" + id + '\'' +
                ", created_at='" + created_at + '\'' +
                ", tags='" + tags + '\'' +
                ", content='" + content + '\'' +
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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
