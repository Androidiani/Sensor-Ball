package it.unina.is2project.sensorgames;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TwoPlayerActivity extends ActionBarActivity {

    // Font typeface
    private Typeface typeFace;

    // TextView
    private TextView bluetooth;
    private TextView labelEnemy;
    private TextView txtEnemy;

    // Button
    private Button play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2p);

        /** Set the fullscreen window */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        /** Load the font */
        typeFace = Typeface.createFromAsset(getAssets(),"font/secrcode.ttf");

        // Find Activity's View
        findView();

        // Set "secrcode.ttf" font
        setFont();
    }

    private void findView(){
        bluetooth = (TextView) findViewById(R.id.lblBluetooth);
        labelEnemy = (TextView) findViewById(R.id.lblEnemy);
        txtEnemy = (TextView) findViewById(R.id.txtEnemy);
        play = (Button) findViewById(R.id.btnPlay);
    }

    private void setFont(){
        bluetooth.setTypeface(typeFace);
        labelEnemy.setTypeface(typeFace);
        txtEnemy.setTypeface(typeFace);
        play.setTypeface(typeFace);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
