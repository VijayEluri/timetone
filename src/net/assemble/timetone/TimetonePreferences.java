package net.assemble.timetone;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * 設定管理
 */
public class TimetonePreferences
{
    private static final String PREF_PREFERENCE_VERSION_KEY = "preferences_version";
    private static final int CURRENT_PREFERENCE_VERSION = 2;

    public static final String PREF_LICENSED_KEY = "licensed";
    public static final boolean PREF_LICENSED_DEFAULT = false;

    public static final String PREF_ENABLED_KEY = "enabled";
    public static final boolean PREF_ENABLED_DEFAULT = true;

    public static final String PREF_PERIOD_KEY = "period";
    public static final String PREF_PERIOD_DEFAULT = "0";
    public static final String PREF_PERIOD_EACHHOUR = "0";
    public static final String PREF_PERIOD_EACH30MIN = "1";

    public static final String PREF_VIBRATE_KEY = "vibrate";
    public static final boolean PREF_VIBRATE_DEFAULT = false;

    public static final String PREF_HOURS_KEY = "hours";
    public static final int PREF_HOURS_DEFAULT = 0x00ffffff;

    public static final String PREF_SILENT_KEY = "silent";
    public static final boolean PREF_SILENT_DEFAULT = false;

    public static final String PREF_VOLUME_KEY = "volume";
    public static final int PREF_VOLUME_DEFAULT = 4;

    public static final String PREF_NOTIFICATION_ICON_KEY = "notification_icon";
    public static final boolean PREF_NOTIFICATION_ICON_DEFAULT = false;

    public static final String PREF_TEST_KEY = "test";

    public static final String PREF_ABOUT_KEY = "about";

    SharedPreferences mPref;

    public static boolean getLicensed(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
                PREF_LICENSED_KEY, PREF_LICENSED_DEFAULT);
    }

    public static void setLicensed(Context ctx, boolean val) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        e.putBoolean(PREF_LICENSED_KEY, val);
        e.commit();
    }

    public static boolean getEnabled(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
                PREF_ENABLED_KEY, PREF_ENABLED_DEFAULT);
    }

    public static void setEnabled(Context ctx, boolean val) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        e.putBoolean(PREF_ENABLED_KEY, val);
        e.commit();
    }

    public static boolean getVibrate(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
                PREF_VIBRATE_KEY, PREF_VIBRATE_DEFAULT);
    }

    public static String getPeriod(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(
                PREF_PERIOD_KEY, PREF_PERIOD_DEFAULT);
    }

    public static int getHours(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(
                PREF_HOURS_KEY, PREF_HOURS_DEFAULT);
    }

    public static boolean issetHour(Context ctx, Calendar cal) {
        Hours hours = new Hours(getHours(ctx));
        return hours.isSet(cal.get(Calendar.HOUR_OF_DAY));
    }

    public static boolean getSilent(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
                PREF_SILENT_KEY, PREF_SILENT_DEFAULT);
    }

    public static int getVolume(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(
                PREF_VOLUME_KEY, PREF_VOLUME_DEFAULT);
    }

    public static boolean getNotificationIcon(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
                PREF_NOTIFICATION_ICON_KEY, PREF_NOTIFICATION_ICON_DEFAULT);
    }

    /*
     * hours code as a single integer.
     */
    static final class Hours {

        // Bit mask of all hours
        private int mHours;

        Hours(int hours) {
            mHours = hours;
        }

        public boolean isSet(int hour) {
            return ((mHours & (1 << hour)) > 0);
        }

        public void set(int hour, boolean set) {
            if (set) {
                mHours |= (1 << hour);
            } else {
                mHours &= ~(1 << hour);
            }
        }

        public void set(Hours hours) {
            mHours = hours.mHours;
        }

        public int getCoded() {
            return mHours;
        }

        // Returns hours encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[24];
            for (int i = 0; i < 24; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }
    }

    /**
     * プリファレンスのアップグレード
     */
    public static void upgrade(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int ver = pref.getInt(PREF_PREFERENCE_VERSION_KEY, 0);
        if (ver < CURRENT_PREFERENCE_VERSION) {
            Editor e = pref.edit();
            if (ver <= 1) {
                e.putBoolean(PREF_ENABLED_KEY, pref.getBoolean("Enabled", true));
                e.remove("Enabled");
                e.putString(PREF_PERIOD_KEY, pref.getString("Period", "0"));
                e.remove("Period");
                e.putBoolean(PREF_VIBRATE_KEY, pref.getBoolean("Vibrate", false));
                e.remove("Vibrate");
                e.putInt(PREF_HOURS_KEY, pref.getInt("Hours", 0x00ffffff));
                e.remove("Hours");
                e.putInt(PREF_VOLUME_KEY, pref.getInt("Volume", 3));
                e.remove("Volume");
                if (pref.contains("UseRingVolume")) {
                    e.putBoolean(PREF_SILENT_KEY, pref.getBoolean("UseRingVolume", false));
                    e.remove(PREF_VOLUME_KEY);  // デフォルトに戻す
                    e.remove("UseRingVolume");
                }
                e.putBoolean(PREF_NOTIFICATION_ICON_KEY, pref.getBoolean("NotificationIcon", false));
                e.remove("NotificationIcon");
            }
            e.putInt(PREF_PREFERENCE_VERSION_KEY, CURRENT_PREFERENCE_VERSION);
            e.commit();
        }
    }

}
