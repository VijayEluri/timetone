package net.assemble.timetone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.assemble.timetone.preferences.TimetonePreferencesActivity;

public class TimetoneNotification {
    private static final int NOTIFICATIONID_ICON = 1;
    private static final int NOTIFICATIONID_EXPIRED = 2;

    public static boolean g_Icon;       // ステータスバーアイコン状態

    /**
     * ノーティフィケーションバーにアイコンを表示
     */
    public static void showNotification(Context ctx) {
        if (g_Icon) {
            return;
        }

        Intent intent = new Intent(ctx, TimetonePreferencesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(ctx.getResources().getString(R.string.app_name))
                .setContentText(ctx.getResources().getString(R.string.app_description))
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATIONID_ICON, notification);

        g_Icon = true;
    }

    /**
     * ノーティフィケーションバーのアイコンを消去
     */
    public static void clearNotification(Context ctx) {
        if (!g_Icon) {
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
        Intent intent = new Intent(ctx, TimetonePreferencesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(ctx.getResources().getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentText(ctx.getResources().getString(R.string.notify_expired))
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATIONID_EXPIRED, notification);
    }
}
