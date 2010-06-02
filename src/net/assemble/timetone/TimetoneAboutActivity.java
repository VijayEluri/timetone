package net.assemble.timetone;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import net.assemble.timetone.R;

/**
 * 「柚子時計について」画面
 */
public class TimetoneAboutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        setContentView(R.layout.about_activity);

        setTitle(getResources().getString(R.string.app_name) + " "
        		+ getResources().getString(R.string.app_version));
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.drawable.icon);
    }
}
