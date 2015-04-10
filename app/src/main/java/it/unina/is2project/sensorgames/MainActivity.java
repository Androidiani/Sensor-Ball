package it.unina.is2project.sensorgames;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import it.unina.is2project.sensorgames.pong.GamePongOnePlayer;
import it.unina.is2project.sensorgames.pong.GamePongTraining;
import it.unina.is2project.sensorgames.stats.activity.StatsActivity;

public class MainActivity extends Activity {

    private int CAMERA_WIDTH;

    // Views on screen declaration
    private Button btnOnePlayer;
    private Button btnTwoPlayer;
    private Button btnTraining;
    private Button btnStats;
    private Button btnAboutUs;
    private LinearLayout mLinearLayout;
    private TextView txtAppName;
    private ImageView homeBar;

    // Ball
    private BallView mBallView;
    private Handler redrawHandler = new Handler();
    private static final int BALL_RADIUS = 50;
    private static final int BALL_COLOR = Color.WHITE;
    private int x_pos;
    private int x_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        findViews();
        // Set-up typeface
        setTypeface();
        // Set the listners
        setListners();
        // Place the ball
        placeBall();
        // Get screen dimensions
        Point dim = getScreenDimensions();
        CAMERA_WIDTH = dim.x;
        // Move bar
        Animation animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.movebar);
        homeBar.setAnimation(animMove);

        // Sensor Manager
        ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        x_speed = (int) -event.values[0];
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                },
                ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                        .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Find views in activity_main.xml
     */
    public void findViews() {
        txtAppName = (TextView) findViewById(R.id.txt_App_Name);
        homeBar = (ImageView) findViewById(R.id.homeBar);
        mLinearLayout = (LinearLayout) findViewById(R.id.mLinearLayout);
        btnOnePlayer = (Button) findViewById(R.id.btn_p1);
        btnTwoPlayer = (Button) findViewById(R.id.btn_p2);
        btnTraining = (Button) findViewById(R.id.btn_trng);
        btnStats = (Button) findViewById(R.id.btn_sts);
        btnAboutUs = (Button) findViewById(R.id.btn_about);
    }

    /**
     * Set the typeface
     */
    public void setTypeface() {
        // Load the font
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");
        // Set the typeface
        txtAppName.setTypeface(typeFace);
        btnOnePlayer.setTypeface(typeFace);
        btnTwoPlayer.setTypeface(typeFace);
        btnTraining.setTypeface(typeFace);
        btnStats.setTypeface(typeFace);
        btnAboutUs.setTypeface(typeFace);
    }

    /**
     * Set listners for buttons
     */
    public void setListners() {
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
        // Stats Button
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStatsClick();
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
    private void placeBall() {
        // Make the Ball View
        mBallView = new BallView(this, x_pos, BALL_RADIUS, BALL_RADIUS, BALL_COLOR);
        // Attach mBallView to mLinearLayout
        mLinearLayout.addView(mBallView);
        // Draw ball
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BALL_RADIUS * 2);
        mLinearLayout.setLayoutParams(parms);
        mBallView.invalidate();
    }

    /**
     * Get the screen dimensions
     */
    private Point getScreenDimensions() {
        Point mPoint = new Point();
        // Get screen dimensions
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
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on stats button.
     */
    private void btnStatsClick() {
        Intent i = new Intent(MainActivity.this, StatsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on aboutUs button.
     */
    private void btnAboutUsClick() {
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setTitle(getApplicationContext().getResources().getString(R.string.ttl_main_adv));
        alert.setMessage(getApplicationContext().getResources().getString(R.string.txt_main_adv));

        alert.setPositiveButton(getApplicationContext().getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.setNegativeButton(getApplicationContext().getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });

        alert.show();
    }

    @Override
    public void onResume() {
        // Create timer to move ball to new position
        Timer mTmr = new Timer();
        TimerTask mTsk = new TimerTask() {
            public void run() {
                x_pos += x_speed;
                if (x_pos > CAMERA_WIDTH + 2 * BALL_RADIUS)
                    x_pos = 0;
                if (x_pos < -2 * BALL_RADIUS)
                    x_pos = CAMERA_WIDTH;

                mBallView.x = x_pos;

                // Redraw ball
                redrawHandler.post(new Runnable() {
                    public void run() {
                        mBallView.invalidate();
                    }
                });
            }
        };
        mTmr.schedule(mTsk, 10, 10);
        super.onResume();
    }
}
