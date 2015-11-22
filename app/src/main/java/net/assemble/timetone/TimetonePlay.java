package net.assemble.timetone;

import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.assemble.timetone.preferences.TimetonePreferences;

/**
 * 時刻読み上げ処理
 */
public class TimetonePlay {
    private static final int RESTORE_VOLUME_RETRIES = 5;
    private static final int RESTORE_VOLUME_RETRY_INTERVAL = 1000; /* ms */
    private static final int[] FLASH_PATTERN = new int[] { 100, 400, 100, 1000, 100, 400, 100 };

    private static MediaPlayer g_Mp = null; // 再生中のMediaPlayer

    private AudioManager mAudioManager;
    private AlarmManager mAlarmManager;
    private Context mCtx;

    private final Handler handler = new Handler();
    private int origVol;
    private int newVol;
    private int retryRestore;

    /**
     * Constructor
     *
     * @param context Context
     */
    public TimetonePlay(Context context) {
        mCtx = context;
        mAudioManager = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
        mAlarmManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * MediaPlayer生成
     * 着信音量をMediaPlayerに設定する。
     *
     * @param resid リソースID
     */
    private MediaPlayer createMediaPlayer(int resid) {
        // 再生中の音声があれば停止する。
        if (g_Mp != null) {
            g_Mp.stop();
            g_Mp.release();
            g_Mp = null;
        }

        // 生成
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mCtx, Uri.parse("android.resource://" + mCtx.getPackageName() + "/" + resid));
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            mp.prepare();
        } catch (Exception e) {
            Log.e(Timetone.TAG, "Failed to create MediaPlayer!");
            return null;
        }
        g_Mp = mp;
        return mp;
    }

    /**
     * 時報再生
     *
     * @param cal
     *            再生日時
     */
    public void play(Calendar cal) {
        // バイブレーション
        if (TimetonePreferences.getVibrate(mCtx)) {
            Vibrator vibrator = (Vibrator) mCtx.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern;
            if (cal.get(Calendar.MINUTE) < 30) {
                pattern = new long[] {500, 200, 100, 200, 500, 200, 100, 200};
            } else {
                pattern = new long[] {500, 200, 100, 200};
            }
            vibrator.vibrate(pattern, -1);

        //  // Receiverからは直接振動させられないため、Notificationを経由する
        //  // ->そんなことはなかった
        //  NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        //  Notification notification = new Notification();
        //  notification.vibrate = pattern;
        //  notificationManager.notify(R.string.app_name, notification);
        }

        // 時報音
        if (!(TimetonePreferences.getSilent(mCtx) &&
                mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)) {
            MediaPlayer mp = createMediaPlayer(getSound(cal));
            if (mp != null) {
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        restoreVolume();
                        mp.release();
                        g_Mp = null;
                    }
                });
                origVol = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                newVol = TimetonePreferences.getVolume(mCtx);
                retryRestore = RESTORE_VOLUME_RETRIES;
                if (Timetone.DEBUG) Log.d(Timetone.TAG, "Changing alarm volume: " + origVol + " -> " + newVol);
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVol, 0);
                mp.start();
            }
        }

        // フラッシュ
        if (TimetonePreferences.getFlash(mCtx) && Build.VERSION.SDK_INT >= 5) {
            new Thread() {
                @TargetApi(5)
                @Override
                public void run() {
                    Camera camera;
                    try {
                        camera = Camera.open();
                    } catch (Exception e) {
                        return;
                    }
                    camera.startPreview();
                    String flashMode = Camera.Parameters.FLASH_MODE_ON;
                    Camera.Parameters params = camera.getParameters();
                    List<String> modes = params.getSupportedFlashModes();
                    if (modes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                        flashMode = Camera.Parameters.FLASH_MODE_TORCH;
                    }
                    boolean on = true;
                    for (int msec : FLASH_PATTERN) {
                        if (on) {
                            params.setFlashMode(flashMode);
                        } else {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        }
                        on = !on;
                        camera.setParameters(params);
                        try {
                            Thread.sleep(msec);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    camera.stopPreview();
                    camera.release();
                }
            }.start();
        }
    }

    /**
     * 音量を元に戻す
     */
    private void restoreVolume() {
        if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) == newVol) {
            // 音量が自分で変更したものと同じ場合のみ復元する
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, origVol, 0);
            if (Timetone.DEBUG) Log.d(Timetone.TAG, "Restored alarm volume: " + newVol + " -> " + origVol);
        } else {
            // 音量が他の要因により変更されていた場合、ちょっと時間を置いてリトライしてみる
            retryRestore--;
            if (retryRestore > 0) {
                if (Timetone.DEBUG) Log.d(Timetone.TAG, "Pending restoring alarm volume: count=" + retryRestore);
                //1.初回実行
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restoreVolume();
                    }
                }, RESTORE_VOLUME_RETRY_INTERVAL);
            }
        }
    }

    /**
     * 現在日時の時報再生
     */
    public void play() {
        Calendar cal = Calendar.getInstance();
        if (TimetonePreferences.issetHour(mCtx, cal)) {
            play(cal);
        }
    }

    /**
     * 時報テスト再生
     */
    public void playTest() {
        if (g_Mp != null) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);
        play(cal);
    }

    /**
     * 現在時刻から音声リソース取得
     *
     * @param cal
     *            日時
     * @return 音声リソースID
     */
    @SuppressWarnings("UnusedParameters")
    private static int getSound(Calendar cal) {
        return R.raw.tone;
    }

    /**
     * タイマ設定
     *
     * @param cal
     *            設定日時
     */
    public void setAlarm(Calendar cal, long interval) {
        mAlarmManager.cancel(pendingIntent());
        long next = cal.getTimeInMillis();
        next -= (next % 1000);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, next, interval,
                pendingIntent());
        Log.d(Timetone.TAG, "set alarm: "
                + DateFormat.getDateTimeInstance().format(cal.getTime())
                + " (msec=" + next + ", interval=" + interval + ")");
    }

    /**
     * 設定に従ってタイマを設定
     */
    public void setAlarm() {
        long interval;
        Calendar cal = Calendar.getInstance();
        if (TimetonePreferences.getPeriod(mCtx).equals(TimetonePreferences.PREF_PERIOD_EACHHOUR)) {
            // each hour
            cal.set(Calendar.MINUTE, 0);
            cal.add(Calendar.HOUR, 1);
            interval = 60 * 60 * 1000/*AlarmManager.INTERVAL_HOUR*/;
        } else {
            // each 30min.
            if (cal.get(Calendar.MINUTE) >= 30) {
                cal.set(Calendar.MINUTE, 0);
                cal.add(Calendar.HOUR, 1);
            } else {
                cal.set(Calendar.MINUTE, 30);
            }
            interval = 30 * 60 * 1000/*AlarmManager.INTERVAL_HALF_HOUR*/;
        }
        cal.set(Calendar.SECOND, 0);
        setAlarm(cal, interval);

        if (TimetonePreferences.getNotificationIcon(mCtx)) {
            TimetoneNotification.showNotification(mCtx);
        } else {
            TimetoneNotification.clearNotification(mCtx);
        }
    }

    /**
     * タイマ解除
     */
    public void resetAlarm() {
        mAlarmManager.cancel(pendingIntent());
        Log.d(Timetone.TAG, "alarm canceled.");
        TimetoneNotification.clearNotification(mCtx);
    }

    /**
     * PendingIntent取得
     */
    public PendingIntent pendingIntent() {
        Intent intent = new Intent(mCtx, TimetoneAlarmReceiver.class);
        return PendingIntent.getBroadcast(mCtx, 0, intent, 0);
    }
}
