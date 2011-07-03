package net.assemble.timetone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import net.assemble.android.AboutActivity;

/**
 * 設定画面
 */
public class TimetonePreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
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
        mTestPref = (Preference)findPreference(TimetonePreferences.PREF_TEST_KEY);
        mAboutPref = (Preference)findPreference(TimetonePreferences.PREF_ABOUT_KEY);

        updateSummary();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mPeriodPref) {
        } else if (preference == mTestPref) {
            new TimetonePlay(getApplicationContext()).playTest();
        } else if (preference == mAboutPref) {
            //Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent().setClass(this, AboutActivity.class);
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
                mPeriodPref.setSummary(getResources().getString(R.string.pref_period_summary) +
                    ": " + entries[i]);
            }
        }
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
