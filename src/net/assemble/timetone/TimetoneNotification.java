package net.assemble.timetone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.assemble.timetone.preferences.TimetonePreferencesActivity;

public class TimetoneNotification {
    private static final int NOTIFICATIONID_ICON = 1;
    private static final int NOTIFICATIONID_EXPIRED = 2;

    public static boolean g_Icon;       // ステータスバーアイコン状態

    /**
     * ノーティフィケーションバーにアイコンを表示
     */
    public static void showNotification(Context ctx) {
        if (g_Icon != false) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon, ctx.getResources().getString(R.string.app_name), System.currentTimeMillis());
        Intent intent = new Intent(ctx, TimetonePreferencesActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        notification.setLatestEventInfo(ctx, ctx.getResources().getString(R.string.app_name), ctx.getResources().getString(R.string.app_description), contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTIFICATIONID_ICON, notification);
        g_Icon = true;
    }

    /**
     * ノーティフィケーションバーのアイコンを消去
     */
    public static void clearNotification(Context ctx) {
        if (g_Icon == false) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATIONID_ICON);
        g_Icon = false;
    }

    /**
     * 期限切れ通知
     */
    public static void showExpiredNotify(Context ctx) {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon,
                      ctx.getResources().getString(R.string.app_name),
                      System.currentTimeMillis());
        Intent intent = new Intent(ctx, TimetonePreferencesActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        String message = ctx.getResources().getString(R.string.notify_expired);
        notification.setLatestEventInfo(ctx,
                ctx.getResources().getString(R.string.app_name),
                message, contentIntent);
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        notificationManager.notify(NOTIFICATIONID_EXPIRED, notification);
    }

}
