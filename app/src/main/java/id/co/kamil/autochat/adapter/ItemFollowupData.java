package id.co.kamil.autochat.adapter;

public class ItemFollowupData {
    String id, message, schedule, interval;

    public ItemFollowupData(String id, String message, String schedule, String interval) {
        this.id = id;
        this.message = message;
        this.schedule = schedule;
        this.interval = interval;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
