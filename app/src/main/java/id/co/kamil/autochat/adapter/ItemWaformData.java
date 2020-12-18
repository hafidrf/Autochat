package id.co.kamil.autochat.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ItemWaformData {
    String id, type, label;
    JSONObject attr;
    JSONArray list;

    @Override
    public String toString() {
        return "ItemWaformData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", label='" + label + '\'' +
                ", attr=" + attr +
                ", list=" + list +
                '}';
    }

    public ItemWaformData(String id, String type, String label, JSONObject attr, JSONArray list) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.attr = attr;
        this.list = list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public JSONObject getAttr() {
        return attr;
    }

    public void setAttr(JSONObject attr) {
        this.attr = attr;
    }

    public JSONArray getList() {
        return list;
    }

    public void setList(JSONArray list) {
        this.list = list;
    }
}
