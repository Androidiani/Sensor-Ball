package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import it.unina.is2project.sensorgames.R;

public class TrainingSettings extends Activity {

    // Spinner
    Spinner ball_speed_spinner;
    Spinner bar_speed_spinner;
    Spinner event_spinner;

    // Array adapter
    ArrayAdapter ball_speed_adapter;
    ArrayAdapter bar_speed_adapter;
    ArrayAdapter event_adapter;

    // TextView
    TextView title;
    TextView ball_speed_textview;
    TextView bar_speed_textview;
    TextView event_textview;

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

        // Get intent extras
        Intent intent = getIntent();
        ball_speed = intent.getIntExtra("ballSpeed", 1);
        bar_speed = intent.getIntExtra("barSpeed", 1);
        event = intent.getIntExtra("event", 0);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        findView();
        // Set-up typeface
        setTypeface();
        // Populate spinner
        populateSpinner();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GamePongTraining.class);
                intent.putExtra("ballSpeed", ball_speed);
                intent.putExtra("barSpeed", bar_speed);
                intent.putExtra("event", event);
                startActivity(intent);
                finish();
            }
        });
    }

    private void findView() {
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

    private void setTypeface() {
        // Load the font
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");

        // Set the typeface
        title.setTypeface(typeFace);
        ball_speed_textview.setTypeface(typeFace);
        bar_speed_textview.setTypeface(typeFace);
        event_textview.setTypeface(typeFace);
    }

    private void populateSpinner() {
        // Ball Speed Spinner
        ball_speed_adapter = ArrayAdapter.createFromResource(this, R.array.ball_speed_spinner, R.layout.spinner_training_item);
        ball_speed_adapter.setDropDownViewResource(R.layout.spinner_training_item);
        ball_speed_spinner.setAdapter(ball_speed_adapter);
        ball_speed_spinner.setSelection(ball_speed-1);
        ball_speed_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ball_speed = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // Bar Speed Spinner
        bar_speed_adapter = ArrayAdapter.createFromResource(this, R.array.bar_speed_spinner, R.layout.spinner_training_item);
        bar_speed_adapter.setDropDownViewResource(R.layout.spinner_training_item);
        bar_speed_spinner.setAdapter(bar_speed_adapter);
        bar_speed_spinner.setSelection(bar_speed-1);
        bar_speed_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bar_speed = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // Event Spinner
        event_adapter = ArrayAdapter.createFromResource(this, R.array.event_spinner, R.layout.spinner_training_item);
        event_adapter.setDropDownViewResource(R.layout.spinner_training_item);
        event_spinner.setAdapter(event_adapter);
        event_spinner.setSelection(event);
        event_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                event = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }
}
