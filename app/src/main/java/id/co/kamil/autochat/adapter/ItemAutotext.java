package id.co.kamil.autochat.adapter;

public class ItemAutotext {

    String id,shorcut,template,group_id,group_name,group_description;
    boolean checkbox,chkvisible;

    @Override
    public String toString() {
        return "ItemAutotext{" +
                "id='" + id + '\'' +
                ", shorcut='" + shorcut + '\'' +
                ", template='" + template + '\'' +
                ", group_id='" + group_id + '\'' +
                ", group_name='" + group_name + '\'' +
                ", group_description='" + group_description + '\'' +
                ", checkbox=" + checkbox +
                ", chkvisible=" + chkvisible +
                '}';
    }

    public ItemAutotext(String id, String shorcut, String template, String group_id, String group_name, String group_description, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.shorcut = shorcut;
        this.template = template;
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_description = group_description;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShorcut() {
        return shorcut;
    }

    public void setShorcut(String shorcut) {
        this.shorcut = shorcut;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_description() {
        return group_description;
    }

    public void setGroup_description(String group_description) {
        this.group_description = group_description;
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
