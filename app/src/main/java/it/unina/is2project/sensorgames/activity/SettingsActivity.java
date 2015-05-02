package it.unina.is2project.sensorgames.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import it.unina.is2project.sensorgames.R;

public class SettingsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.user_settings);
    }
}
