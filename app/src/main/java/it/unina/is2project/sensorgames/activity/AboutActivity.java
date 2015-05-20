package it.unina.is2project.sensorgames.activity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.WindowManager;
import android.widget.TextView;

import it.unina.is2project.sensorgames.R;


public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        TextView descrizione = (TextView) findViewById(R.id.lblDescription);
        // Load font and set typeface
//        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");
//        descrizione.setTypeface(typeFace);
        descrizione.setText(Html.fromHtml(getResources().getString(R.string.text_app_about_description)));
        descrizione.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
