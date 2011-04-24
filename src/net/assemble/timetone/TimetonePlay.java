package net.assemble.timetone;

import java.util.Calendar;
import java.text.DateFormat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.assemble.timetone.R;

/**
 * 時刻読み上げ処理
 */
public class TimetonePlay {
    public static MediaPlayer g_Mp; // 再生中のMediaPlayer

    private AudioManager mAudioMananger;
    private AlarmManager mAlarmManager;
    private Context mCtx;
    private Calendar mCal;

    /**
     * Constructor
     *
     * @param context
     */
    public TimetonePlay(Context context) {
        mCtx = context;
        mAudioMananger = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
        mAlarmManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * MediaPlayer生成
     * 着信音量をMediaPlayerに設定する。
     *
     * @param mp 設定するMediaPlayer
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
        mCal = cal;

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

        MediaPlayer mp = createMediaPlayer(getSound(mCal));
        if (mp == null) {
            return;
        }
        final int origVol = mAudioMananger.getStreamVolume(AudioManager.STREAM_ALARM);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioMananger.setStreamVolume(AudioManager.STREAM_ALARM, origVol, 0);
                mp.release();
                g_Mp = null;
            }
        });
        mAudioMananger.setStreamVolume(AudioManager.STREAM_ALARM, TimetonePreferences.getVolume(mCtx), 0);
        mp.start();
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
        PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent, 0);
        return sender;
    }
}
