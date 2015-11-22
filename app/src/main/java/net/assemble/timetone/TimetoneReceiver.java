package net.assemble.timetone;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.util.Log;

import net.assemble.timetone.preferences.TimetonePreferences;

/**
 * Receiver for Broadcast Intent
 *
 * 装置起動時や時刻変更時のタイマ再設定など
 */
public class TimetoneReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(Timetone.TAG, "received intent: " + intent.getAction());

        TimetonePreferences.upgrade(ctx);
        if (!TimetonePreferences.getEnabled(ctx)) {
            return;
        }

        // 有効期限チェック
        if (!Timetone.checkExpiration(ctx)) {
            TimetonePreferences.setEnabled(ctx, false);
            return;
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Restart service
                Log.i(Timetone.TAG, "Timetone restarted. (at boot)");
                TimetoneService.startService(ctx);
            } else if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED"/*Intent.ACTION_PACKAGE_REPLACED*/)) {
                if (intent.getData() != null &&
                        intent.getData().equals(Uri.fromParts("package", ctx.getPackageName(), null))) {
                    // Restart service
                    Log.i(Timetone.TAG, "Timetone restarted. (package replaced)");
                    TimetoneService.startService(ctx);
                }
            } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
                    || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                // Restart alarm
                TimetoneService.startService(ctx);
            }
        }
    }
}
