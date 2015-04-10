package it.unina.is2project.sensorgames;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;


public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        TextView titolo = (TextView) findViewById(R.id.lblNameApp);
        TextView descrizione = (TextView) findViewById(R.id.lblDescription);
        // Load font and set typeface
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");
        titolo.setTypeface(typeFace);
        descrizione.setTypeface(typeFace);
    }
}
