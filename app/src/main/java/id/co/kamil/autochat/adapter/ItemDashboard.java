package id.co.kamil.autochat.adapter;

public class ItemDashboard {
    String title,val,color;

    public ItemDashboard(String title, String val, String color) {
        this.title = title;
        this.val = val;
        this.color = color;
    }

    @Override
    public String toString() {
        return "ItemDashboard{" +
                "title='" + title + '\'' +
                ", val='" + val + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
