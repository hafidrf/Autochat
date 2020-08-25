package id.co.kamil.autochat.adapter;

public class ItemRecyclerKontak {
    private int icon;
    private String link;
    private String tipe;
    private boolean enabled;

    @Override
    public String toString() {
        return "ItemRecyclerKontak{" +
                "icon=" + icon +
                ", link='" + link + '\'' +
                '}';
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ItemRecyclerKontak(int icon, String link, String tipe, boolean enabled) {
        this.icon = icon;
        this.link = link;
        this.tipe = tipe;
        this.enabled = enabled;
    }

    public ItemRecyclerKontak(int icon, String link, String tipe) {
        this.icon = icon;
        this.link = link;
        this.tipe = tipe;
    }

    public ItemRecyclerKontak(int icon, String link) {
        this.icon = icon;
        this.link = link;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
