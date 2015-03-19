package it.unina.is2project.sensorgames;

import it.unina.is2project.sensorgames.pong.GamePongOnePlayer;
import it.unina.is2project.sensorgames.pong.GamePongTraining;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class MainActivity extends ActionBarActivity {

    private int CAMERA_WIDTH;

    // Views on screen declaration
    private Button btnOnePlayer;
    private Button btnTwoPlayer;
    private Button btnTraining;
    private Button btnStats;
    private Button btnAboutUs;
    private LinearLayout mLinearLayout;

    // Font typeface
    private Typeface typeFace;

    // Ball
    private BallView mBallView;
    private Handler redrawHandler = new Handler();
    private static final int BALL_RADIUS = 50;
    private static final int BALL_COLOR = Color.WHITE;
    private int x_pos;
    private int x_speed;

    // Timer
    private Timer mTmr;
    private TimerTask mTsk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Set the fullscreen window */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        /** Load the font */
        typeFace = Typeface.createFromAsset(getAssets(),"font/secrcode.ttf");

        setContentView(R.layout.activity_main);

        // Get screen dimensions
        Point dim = getScreenDimensions();
        CAMERA_WIDTH = dim.x;

        // Get the object id
        findViews();

        // Set the button fonts
        buttonFonts();

        // Set the listners
        setListners();

        // Place the ball
        placeBall();

        // Sensor Manager
        ((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        x_speed = (int)-event.values[0];
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                },
                ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                        .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * Find views in activity_main.xml routine.
     */
    public void findViews(){
        btnOnePlayer = (Button)findViewById(R.id.btn_p1);
        btnTwoPlayer = (Button)findViewById(R.id.btn_p2);
        btnTraining = (Button)findViewById(R.id.btn_trng);
        btnStats = (Button)findViewById(R.id.btn_sts);
        btnAboutUs = (Button)findViewById(R.id.btn_about);
        mLinearLayout = (LinearLayout) findViewById(R.id.mLinearLayout);
    }

    /**
     * Set the font face to buttons
     */
    public void buttonFonts(){
        btnOnePlayer.setTypeface(typeFace);
        btnTwoPlayer.setTypeface(typeFace);
        btnTraining.setTypeface(typeFace);
        btnStats.setTypeface(typeFace);
        btnAboutUs.setTypeface(typeFace);
    }

    /**
     * Set listners for buttons' event.
     */
    public void setListners(){

        // 1 Player Button
        btnOnePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnePlayerClick();
            }
        });

        // 2 Player Button
        btnTwoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTwoPlayerClick();
            }
        });

        // Training Button
        btnTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTrainingClick();
            }
        });

        // About Us Button
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAboutUsClick();
            }
        });
    }

    /**
     * Place the ball over the layout
     */
    private void placeBall(){
        // Make the Ball View
        mBallView = new BallView(this,x_pos,BALL_RADIUS,BALL_RADIUS,BALL_COLOR);

        // Attach mBallView to mLinearLayout
        mLinearLayout.addView(mBallView);
        mBallView.invalidate();
    }

    /**
     * Get the screen dimensions
     */
    private Point getScreenDimensions(){
        Point mPoint = new Point();

        //get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();

        display.getSize(mPoint);

        return mPoint;
    }

    /**
     * Manage click on onePlayer button.
     */
    private void btnOnePlayerClick() {
        Intent i = new Intent(MainActivity.this, GamePongOnePlayer.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on twoPlayer button.
     */
    private void btnTwoPlayerClick() {
        Intent i = new Intent(MainActivity.this, TwoPlayerActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on training button.
     */
    private void btnTrainingClick() {
        Intent i = new Intent(MainActivity.this, GamePongTraining.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on aboutUs button.
     */
    private void btnAboutUsClick(){
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {
        //create timer to move ball to new position
        mTmr = new Timer();
        mTsk = new TimerTask() {
            public void run() {

                x_pos += x_speed;
                if (x_pos > CAMERA_WIDTH + BALL_RADIUS) x_pos = 0;
                if (x_pos < -BALL_RADIUS) x_pos = CAMERA_WIDTH;

                mBallView.x = x_pos;

                //redraw ball
                redrawHandler.post(new Runnable() {
                    public void run() {
                        mBallView.invalidate();
                    }
                });
            }
        };
        mTmr.schedule(mTsk,10,10);
        super.onResume();
    }

}
