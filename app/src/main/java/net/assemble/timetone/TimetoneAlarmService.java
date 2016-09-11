package net.assemble.timetone;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Receiver for Alarm Intent
 */
public class TimetoneAlarmService extends IntentService
{
    private static final String TAG = TimetoneAlarmService.class.getSimpleName();

    public TimetoneAlarmService(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public TimetoneAlarmService() {
        this(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d(Timetone.TAG, "received intent: " + intent.getAction());

        if (Calendar.getInstance().get(Calendar.MINUTE) % 30 == 0) {
            TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tel.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                // 通話中は抑止
                return;
            }
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Timetone.TAG);
            wl.acquire(3000);
            new TimetonePlay(this).play();
        }
    }
}
