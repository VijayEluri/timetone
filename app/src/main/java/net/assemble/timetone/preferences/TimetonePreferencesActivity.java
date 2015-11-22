package net.assemble.timetone.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import net.assemble.android.AboutActivity;
import net.assemble.timetone.R;
import net.assemble.timetone.Timetone;
import net.assemble.timetone.TimetonePlay;
import net.assemble.timetone.TimetoneService;

/**
 * 設定画面
 */
public class TimetonePreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private Preference mFlashPref;
    private Preference mTestPref;
    private Preference mAboutPref;
    private ListPreference mPeriodPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TimetonePreferences.upgrade(this);
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mPeriodPref = (ListPreference)findPreference(TimetonePreferences.PREF_PERIOD_KEY);
        mFlashPref = findPreference(TimetonePreferences.PREF_FLASH_KEY);
        mTestPref = findPreference(TimetonePreferences.PREF_TEST_KEY);
        mAboutPref = findPreference(TimetonePreferences.PREF_ABOUT_KEY);

        if (Build.VERSION.SDK_INT < 5) {
            mFlashPref.setEnabled(false);
        } else if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mFlashPref.setEnabled(false);
        }

        updateSummary();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mFlashPref) {
            final CheckBoxPreference checkbox = (CheckBoxPreference) preference;
            if (checkbox.isChecked()) {
                alertMessage(R.string.pref_flash_warning, null, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        checkbox.setChecked(false);
                    }
                });
            }
        } else if (preference == mTestPref) {
            new TimetonePlay(getApplicationContext()).playTest();
        } else if (preference == mAboutPref) {
            Intent intent = new Intent().setClass(this, AboutActivity.class);
            intent.putExtra("body_asset", "about.txt");
            startActivity(intent);

//          AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//          alertDialogBuilder.setTitle(R.string.about_title);
//          alertDialogBuilder.setMessage(R.string.about_text);
//          alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//              @Override
//              public void onClick(DialogInterface dialog, int which) {
//              }});
//          alertDialogBuilder.setCancelable(true);
//          AlertDialog alertDialog = alertDialogBuilder.create();
//          alertDialog.show();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
//            Toast.makeText(this, R.string.media_volume_zero, Toast.LENGTH_LONG).show();
//        }

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        updateService();
    }

    private void updateSummary() {
        String val = mPeriodPref.getValue();
        String[] entries = getResources().getStringArray(R.array.entries_period);
        String[] entryvalues = getResources().getStringArray(R.array.entryvalues_period);
        for (int i = 0; i < entries.length; i++) {
            if (val.equals(entryvalues[i])) {
                mPeriodPref.setSummary(entries[i]);
            }
        }
    }

    /**
     * 警告をダイアログ表示
     *
     * @param message 表示するメッセージ
     * @param negativeListener キャンセルされた場合のリスナ
     */
    private void alertMessage(String message, DialogInterface.OnClickListener clickListener, DialogInterface.OnCancelListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        if (clickListener != null) {
            builder.setPositiveButton(R.string.ok, clickListener);
        }
        if (negativeListener != null) {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setCancelable(true);
            builder.setOnCancelListener(negativeListener);
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void alertMessage(int msgResId, DialogInterface.OnClickListener clickListener, DialogInterface.OnCancelListener negativeListener) {
        alertMessage(getResources().getString(msgResId), clickListener, negativeListener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary();
    }

    /**
     * 設定に応じて処理をおこなう
     */
    private void updateService() {
        // ライセンスフラグ設定
        //  有料版を使ったことがある場合は購入メニューを表示させない
        //noinspection PointlessBooleanExpression
        if (!Timetone.FREE_VERSION) {
            TimetonePreferences.setLicensed(this, true);
        }

        // 有効期限チェック
        if (!Timetone.checkExpiration(this)) {
            TimetonePreferences.setEnabled(this, false);
        }

        if (TimetonePreferences.getEnabled(this)) {
            TimetoneService.startService(this);
        } else {
            TimetoneService.stopService(this);
        }
    }

}
