package id.co.kamil.autochat.adapter;

public class ItemRecyclerTag {
    private String id, title, tipe, firebase;

    public ItemRecyclerTag(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public ItemRecyclerTag(String id, String title, String tipe) {
        this.id = id;
        this.title = title;
        this.tipe = tipe;
    }

    public ItemRecyclerTag(String id, String title, String tipe, String firebase) {
        this.id = id;
        this.title = title;
        this.tipe = tipe;
        this.firebase = firebase;
    }

    public String getFirebase() {
        return firebase;
    }

    public void setFirebase(String firebase) {
        this.firebase = firebase;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
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

    @Override
    public String toString() {
        return "ItemRecyclerTag{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", tipe='" + tipe + '\'' +
                '}';
    }
}
