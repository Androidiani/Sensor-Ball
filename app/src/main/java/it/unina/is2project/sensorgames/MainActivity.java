package it.unina.is2project.sensorgames;

import it.unina.is2project.sensorgames.pong.GamePongOnePlayer;
import it.unina.is2project.sensorgames.pong.GamePongTraining;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.logging.Handler;

public class MainActivity extends ActionBarActivity {

    // Views on screen declaration
    private Button btnOnePlayer;
    private Button btnTwoPlayer;
    private Button btnTraining;
    private Button btnAboutUs;

    private LinearLayout mLinearLayout;
    private ImageView mTopImage;
    private int x_pos;
    private int y_pos;

    // Font typeface
    private Typeface typeFace;

    // Ball
    private BallView mBallView;
    private Handler redrawHandler;
    private static final int BALL_RADIUS = 50;
    private static final int BALL_COLOR = Color.WHITE;


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

        // Get the object id
        findViews();

        // Set the button fonts
        buttonFonts();

        // Set the listners
        setListners();

        // Place the ball
        placeBall();
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
        btnAboutUs = (Button)findViewById(R.id.btn_about);
        mLinearLayout = (LinearLayout) findViewById(R.id.mLinearLayout);
        mTopImage = (ImageView) findViewById(R.id.topView);
    }

    /**
     * Set the font face to buttons
     */
    public void buttonFonts(){
        btnOnePlayer.setTypeface(typeFace);
        btnTwoPlayer.setTypeface(typeFace);
        btnTraining.setTypeface(typeFace);
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
        mBallView = new BallView(this,BALL_RADIUS,BALL_RADIUS,BALL_RADIUS,BALL_COLOR);
        Log.d("", "Ball placed in (X,Y) = " + x_pos + "," + y_pos);

        // Attach mBallView to mLinearLayout
        mLinearLayout.addView(mBallView);

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

}
