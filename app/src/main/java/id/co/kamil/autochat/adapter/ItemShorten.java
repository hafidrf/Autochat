package id.co.kamil.autochat.adapter;

public class ItemShorten {
    String id, domain, subdomaincode, totalklik;
    boolean checkbox, chkvisible;

    @Override
    public String toString() {
        return "ItemShorten{" +
                "id='" + id + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }

    public ItemShorten(String id, String domain, String subdomaincode) {
        this.id = id;
        this.domain = domain;
        this.subdomaincode = subdomaincode;
    }

    public ItemShorten(String id, String domain, String subdomaincode, String totalklik) {
        this.id = id;
        this.domain = domain;
        this.subdomaincode = subdomaincode;
        this.totalklik = totalklik;
    }

    public String getTotalklik() {
        return totalklik;
    }

    public void setTotalklik(String totalklik) {
        this.totalklik = totalklik;
    }

    public String getSubdomaincode() {
        return subdomaincode;
    }

    public void setSubdomaincode(String subdomaincode) {
        this.subdomaincode = subdomaincode;
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

    public ItemShorten(String id, String domain) {
        this.id = id;
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
