package co.yoprice.nextgenchat.singleton;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import java.sql.Date;

import co.yoprice.nextgenchat.MainChatActivity;
import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.data.callbacks.OnMessageListener;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.utils.aLog;

/**
 * Created by cj on 4/30/16.
 */
public class NotifSingleton implements OnMessageListener {
    private static NotifSingleton ourInstance = new NotifSingleton();
    private boolean onFront;
    private boolean canVibrate;
    private boolean constantNotif = true;
    private boolean mediaActivityOpened;

    public boolean isMediaActivityOpened() {
        return mediaActivityOpened;
    }

    public boolean isConstantNotif() {
        return constantNotif;
    }

    public void setConstantNotif(boolean constantNotif) {
        this.constantNotif = constantNotif;
    }

    public boolean isOnFront() {
        return onFront;
    }

    public static NotifSingleton getInstance() {
        return ourInstance;
    }

    private NotifSingleton() {
    }


    long sinceLastUpdate;

    private boolean canNotifyUser = true;
    private NotificationCompat.Builder notification;
    private NotificationManager notificationManager;

    public void ShowNotification(String title, String message, Context context, PendingIntent pendingIntent, int id) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (!isConstantNotif()) {
            if (sinceLastUpdate == 0) {
                sinceLastUpdate = System.currentTimeMillis();
            } else {
                if (MinuteDifferent(new Date(System.currentTimeMillis()), new Date(sinceLastUpdate))< 5){
                    return;
                }else{
                    sinceLastUpdate = System.currentTimeMillis();
                }
            }
        }
        notification = new NotificationCompat.Builder(context);
        notification.setOnlyAlertOnce(false);
        notification.setAutoCancel(true);
        notification.setCategory(Notification.CATEGORY_PROMO);
        notification.setVisibility(View.VISIBLE);
        if (id == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.setColor(context.getColor(R.color.md_green_500));
            } else {
                notification.setColor(context.getResources().getColor(R.color.md_green_500));
            }
        } else if (id == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.setColor(context.getColor(R.color.md_red_500));
            } else {
                notification.setColor(context.getResources().getColor(R.color.md_red_500));
            }
        }
        notification.setSmallIcon(R.drawable.ic_notifications_active_black_24dp);
        if (canVibrate) {
            notification.setVibrate(new long[]{500, 500, 500});
        } else {
            notification.setVibrate(new long[]{500});
        }
        notification.setPriority(Notification.PRIORITY_HIGH);
        notification.addAction(R.drawable.ic_notifications_active_black_24dp, "OPEN", pendingIntent);
        notificationManager.notify(id, notification.setContentTitle(title).setContentText(message).build());
    }

    public long MinuteDifferent(Date newD, Date oldD) {
        long diff = newD.getTime() - oldD.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        aLog.w(getClass().getSimpleName(),diffDays + " days, ");
        aLog.w(getClass().getSimpleName(),diffHours + " hours, ");
        aLog.w(getClass().getSimpleName(),diffMinutes + " minutes, ");
        aLog.w(getClass().getSimpleName(),diffSeconds + " seconds.");
        return diffMinutes;
    }

    @Override
    public void OnMessageReceived(final Message message, Context context) {
        if (canNotifyUser && !onFront && !isMediaActivityOpened()) {
            ShowNotification(message.getUser(), message.getMessage(), context, PendingIntent.getActivity(context, 0, new Intent(context, MainChatActivity.class), PendingIntent.FLAG_UPDATE_CURRENT), 1);
        }
    }

    @Override
    public void OnMyNameDiscussed(final Message message, Context context) {
        if (canNotifyUser && !onFront && !isMediaActivityOpened()) {
            ShowNotification(message.getUser() + " said your name!", message.getMessage(), context, PendingIntent.getActivity(context, 0, new Intent(context, MainChatActivity.class), PendingIntent.FLAG_UPDATE_CURRENT), 0);
        }
    }

    public void setOnFront(boolean onFront) {
        this.onFront = onFront;
    }

    public void setOnMediaActivityOpened(boolean mediaActivityOpened){
        this.mediaActivityOpened = mediaActivityOpened;
    }

    public void setCanNotify(boolean canNotify) {
        this.canNotifyUser = canNotify;
    }

    public void setCanVibrate(boolean canVibrate) {
        this.canVibrate = canVibrate;
    }
}
