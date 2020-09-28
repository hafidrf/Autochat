package id.co.kamil.autochat.adapter;

public class ItemField {

    String judul;
    boolean checkbox, chkvisible;

    public ItemField(String judul, boolean checkbox, boolean chkvisible) {
        this.judul = judul;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
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
