package id.co.kamil.autochat.autoreply.models;

import android.annotation.SuppressLint;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.ScheduledFuture;

import id.co.kamil.autochat.autoreply.VersionUtils;

/**
 * Created by JJ on 05/06/15.
 */
public class PendingNotification {

    private ScheduledFuture<?> scheduledFuture;
    private StatusBarNotification sbn;
    private String key;

    @SuppressLint("NewApi")
    public PendingNotification(StatusBarNotification sbn) {
        this.sbn = sbn;
        this.key = VersionUtils.isLollipop() ? sbn.getKey() : null;
    }

    public void setDismissKey(String key) {
        this.key = key;
    }

    public String getDismissKey() {
        return key;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    @Override
    public boolean equals(Object o) {
        if(VersionUtils.isJellyBean()) {
            String group = NotificationCompat.getGroup(((PendingNotification) o).getSbn().getNotification());
            String thisGroup = NotificationCompat.getGroup(sbn.getNotification());
            if(group == null || thisGroup == null)
                return false;
            return group.equals(thisGroup);
        } else
            return ((PendingNotification) o).getSbn().getPackageName().equals(sbn.getPackageName());
    }

}
