package net.assemble.timetone;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Receiver for Alarm Intent
 */
public class TimetoneAlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(Timetone.TAG, "received intent: " + intent.getAction());

        if (Calendar.getInstance().get(Calendar.MINUTE) % 30 == 0) {
            TelephonyManager tel = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tel.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 通話中は抑止
                return;
            }
            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Timetone.TAG);
            wl.acquire(3000);
            new TimetonePlay(ctx).play();
        }
    }

}
