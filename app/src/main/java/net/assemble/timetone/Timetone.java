package net.assemble.timetone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class Timetone {
    public static final String TAG = "Timetone";

    public static final boolean FREE_VERSION = false;
    public static final String FREE_EXPIRES = "2010/10/30";

    /**
     * 有効期限チェック
     *
     * @param ctx Context
     * @return true:期限内 false:期限切れ
     */
    @SuppressWarnings("unused")
    public static boolean checkExpiration(Context ctx) {
        //noinspection PointlessBooleanExpression
        if (FREE_VERSION && FREE_EXPIRES != null) {
            Date today = new Date();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date expire_date = sdf.parse(FREE_EXPIRES);
                if (today.compareTo(expire_date) > 0) {
                    TimetoneNotification.showExpiredNotify(ctx);
                    Log.d(TAG, "Expired.");
                    return false;
                } else {
                    Log.d(TAG, "Expires on " + expire_date.toLocaleString());
                }
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        }
        return true;
    }

}
