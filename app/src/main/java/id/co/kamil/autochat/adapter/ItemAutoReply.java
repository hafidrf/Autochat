package id.co.kamil.autochat.adapter;

public class ItemAutoReply {
    String id, created_at, keyword, reply, status;
    boolean checkbox, chkvisible;

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

    @Override
    public String toString() {
        return "ItemAutoReply{" +
                "created_at='" + created_at + '\'' +
                ", keyword='" + keyword + '\'' +
                ", reply='" + reply + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public ItemAutoReply(String id, String created_at, String keyword, String reply, String status, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.created_at = created_at;
        this.keyword = keyword;
        this.reply = reply;
        this.status = status;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
