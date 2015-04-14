package it.unina.is2project.sensorgames;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO vedere se esiste un metodo migliore non deprecato
        addPreferencesFromResource(R.xml.user_settings);
    }
}
