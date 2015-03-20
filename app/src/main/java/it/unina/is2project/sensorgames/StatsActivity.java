package it.unina.is2project.sensorgames;

import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;


public class StatsActivity extends ActionBarActivity {

    // Font typeface
    private Typeface typeFace;

    // TextView
    private TextView top;
    private TextView single;
    private TextView multi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        /** Set the fullscreen window */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        /** Load the font */
        typeFace = Typeface.createFromAsset(getAssets(),"font/secrcode.ttf");

        // Find View by ID
        findView();

        // Set Font Typeface
        setFont();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    public void findView(){
        top = (TextView) findViewById(R.id.txt_stat);
        single = (TextView) findViewById(R.id.txt_single);
        multi = (TextView) findViewById(R.id.txt_multi);
    }

    public void setFont(){
        top.setTypeface(typeFace);
        single.setTypeface(typeFace);
        multi.setTypeface(typeFace);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
