package net.assemble.timetone;

import android.annotation.TargetApi;
import android.app.Service;
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

    private TimetonePlay mPlay;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlay = new TimetonePlay(this);
        Log.d(TAG, "Service started.");
        Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "Service stopped.");
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    /**
     * サービス開始
     *
     * @param ctx Context
     */
    public static void startService(Context ctx) {
        ctx.startService(new Intent(ctx, TimetoneService.class));
    }

    /**
     * サービス停止
     *
     * @param ctx Context
     */
    public static void stopService(Context ctx) {
        ctx.stopService(new Intent(ctx, TimetoneService.class));
    }
}
