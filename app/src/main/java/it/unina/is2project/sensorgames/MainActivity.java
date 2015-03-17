package it.unina.is2project.sensorgames;

import it.unina.is2project.sensorgames.pong.GamePong;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends ActionBarActivity {

    // Views on screen declaration
    private ImageButton btnOnePlayer;
    private ImageButton btnTwoPlayer;
    private ImageButton btnTraining;
    private ImageButton btnAboutUs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListners();
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
        btnOnePlayer = (ImageButton)findViewById(R.id.btn_p1);
        btnTwoPlayer = (ImageButton)findViewById(R.id.btn_p2);
        btnTraining = (ImageButton)findViewById(R.id.btn_trng);
        btnAboutUs = (ImageButton)findViewById(R.id.btn_about);
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
     * Manage click on onePlayer button.
     */
    private void btnOnePlayerClick() {
        Intent i = new Intent(MainActivity.this, GamePong.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * Manage click on training button.
     */
    private void btnTrainingClick() {
        Intent i = new Intent(MainActivity.this, GamePong.class);
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
