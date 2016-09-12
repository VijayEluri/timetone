package net.assemble.timetone;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * サービス
 */
public class TimetoneService extends Service {
    private static final String TAG = TimetoneService.class.getSimpleName();

    public static final String ACTION_ALARM = "net.assemble.timetone.action.ALARM";

    private static ComponentName mService;
    private TimetonePlay mPlay;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlay = new TimetonePlay(this);
        mPlay.setAlarm();
    }

    // This is the old onStart method that will be called on the pre-2.0
    // platform.  On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @TargetApi(5)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        if (intent != null && ACTION_ALARM.equals(intent.getAction())) {
            Log.d(TAG, "received intent: " + intent.getAction());

            if (Calendar.getInstance().get(Calendar.MINUTE) % 30 == 0) {
                TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tel.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                    // 通話中は抑止
                    return;
                }
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Timetone.TAG);
                wl.acquire(3000);
                mPlay.play();
            }
        }
        mPlay.setAlarm();
    }

    public void onDestroy() {
        mPlay.resetAlarm();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    /**
     * サービス動作有無取得
     */
    public static boolean isActive() {
        return mService != null;
    }

    /**
     * サービス開始
     */
    public static boolean startService(Context ctx) {
        boolean result;
        boolean restart = isActive();
        mService = ctx.startService(new Intent(ctx, TimetoneService.class));
        if (mService == null) {
            Log.e(Timetone.TAG, "TimetoneService could not start!");
            result = false;
        } else {
            Log.d(Timetone.TAG, "TimetoneService started: " + mService);
            result = true;
        }
        if (!restart && result) {
            Toast.makeText(ctx, R.string.service_started, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    /**
     * サービス停止
     */
    public static void stopService(Context ctx) {
        if (mService != null) {
            Intent i = new Intent();
            i.setComponent(mService);
            boolean res = ctx.stopService(i);
            if (!res) {
                Log.e(Timetone.TAG, "TimetoneService could not stop!");
            } else {
                Log.d(Timetone.TAG, "TimetoneService stopped: " + mService);
                Toast.makeText(ctx, R.string.service_stopped, Toast.LENGTH_SHORT).show();
                mService = null;
            }
        }
    }
}
