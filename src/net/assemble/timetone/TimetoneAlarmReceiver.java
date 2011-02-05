package net.assemble.timetone;

import java.util.Calendar;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Receiver for Alarm Intent
 */
public class TimetoneAlarmReceiver extends BroadcastReceiver
{
    private static final String TAG = "Timetone";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(TAG, "received intent: " + intent.getAction());

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
