package net.assemble.timetone;

import java.util.Calendar;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.assemble.timetone.TimetonePlay;

/**
 * Receiver for Broadcast Intent
 *
 * 装置起動時や時刻変更時のタイマ再設定など
 */
public class TimetoneAlarmReceiver extends BroadcastReceiver
{
    private static final String TAG = "Timetone";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(TAG, "received intent: " + intent.getAction());

        if (TimetonePreferences.getEnabled(ctx) == false) {
            return;
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Restore alarm
                Log.i(TAG, "Timetone restarted.");
                //new TimetonePlay(ctx).setAlarm();
                TimetoneService.startService(ctx);
            } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
                    || intent.getAction()
                            .equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                // Restart alarm
                //new TimetonePlay(ctx).setAlarm();
                TimetoneService.startService(ctx);
            }
            return;
        }

        if (Calendar.getInstance().get(Calendar.MINUTE) % 30 == 0) {
            TelephonyManager tel = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tel.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 通話中は抑止
                return;
            }
            new TimetonePlay(ctx).play();
        }
    }
}
