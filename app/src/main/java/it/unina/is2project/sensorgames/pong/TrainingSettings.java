package it.unina.is2project.sensorgames.pong;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import it.unina.is2project.sensorgames.R;

public class TrainingSettings extends ActionBarActivity {

    // Font typeface
    private Typeface typeFace;

    // Spinner
    Spinner ball_speed_spinner;
    Spinner bar_speed_spinner;
    Spinner event_spinner;

    // TextView
    TextView title;
    TextView ball_speed_textview;
    TextView bar_speed_textview;
    TextView event_textview;

    // Array adapter
    ArrayAdapter ball_speed_adapter;
    ArrayAdapter bar_speed_adapter;
    ArrayAdapter event_adapter;

    // Button
    Button back;

    // Data
    private int ball_speed;
    private int bar_speed;
    private int event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_settings);

        /** Set the fullscreen window */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        /** Find view */
        findView();

        /** Populate spinner */
        populateSpinner();

        /** Set-up the typeface */
        setTypeface();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GamePongTraining.class);
                intent.putExtra("ballspeed", ball_speed);
                intent.putExtra("barspeed", bar_speed);
                intent.putExtra("event", event);
                startActivity(intent);
                finish();
            }
        });

    }

    private void findView(){
        // Spinner
        ball_speed_spinner = (Spinner) findViewById(R.id.spinner_ball_speed);
        bar_speed_spinner = (Spinner) findViewById(R.id.spinner_bar_speed);
        event_spinner = (Spinner) findViewById(R.id.spinner_event);

        // TextView
        ball_speed_textview = (TextView) findViewById(R.id.txt_ball_speed);
        bar_speed_textview = (TextView) findViewById(R.id.txt_bar_speed);
        event_textview = (TextView) findViewById(R.id.txt_event_selection);
        title = (TextView) findViewById(R.id.title_setting_training);

        // Button
        back = (Button) findViewById(R.id.btn_back);
    }

    private void setTypeface(){
        /** Load the font */
        typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");

        /** Set the typeface */
        ball_speed_textview.setTypeface(typeFace);
        bar_speed_textview.setTypeface(typeFace);
        event_textview.setTypeface(typeFace);
        title.setTypeface(typeFace);
    }

    private void populateSpinner(){
        // Ball Speed Spinner
        ball_speed_adapter = ArrayAdapter.createFromResource(this, R.array.ball_speed_spinner, android.R.layout.simple_spinner_item);
        ball_speed_spinner.setAdapter(ball_speed_adapter);
        ball_speed_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ball_speed = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ball_speed = 1;
            }
        });

        // Bar Speed Spinner
        bar_speed_adapter = ArrayAdapter.createFromResource(this, R.array.bar_speed_spinner, android.R.layout.simple_spinner_item);
        bar_speed_spinner.setAdapter(bar_speed_adapter);
        bar_speed_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bar_speed = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bar_speed = 1;
            }
        });

        // Event Spinner
        event_adapter = ArrayAdapter.createFromResource(this, R.array.event_spinner, android.R.layout.simple_spinner_item);
        event_spinner.setAdapter(event_adapter);
        event_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                event = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                event = 0;
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training_settings, menu);
        return true;
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

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
