package it.unina.is2project.sensorpong.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import it.unina.is2project.sensorpong.R;

public class SettingsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.user_settings);
    }
}
