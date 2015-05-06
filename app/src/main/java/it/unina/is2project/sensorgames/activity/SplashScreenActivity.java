package it.unina.is2project.sensorgames.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import it.unina.is2project.sensorgames.R;

public class SplashScreenActivity extends Activity {

    private long ms = 0;
    private final long splashTime = 3000;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Animation animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        ImageView splashAnim = (ImageView) findViewById(R.id.splashAnim);
        splashAnim.startAnimation(animMove);

        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        ImageView gameName = (ImageView) findViewById(R.id.appNameSplash);
        gameName.startAnimation(animFadeIn);

        // Nickname from Shared Preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        nickname = sharedPreferences.getString("prefNickname", getString(R.string.txt_no_name));

        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (ms < splashTime) {
                        ms = ms + 100;
                        sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent intent;
                    if (nickname.compareTo(getString(R.string.txt_no_name)) != 0) {
                        intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    } else {
                        intent = new Intent(SplashScreenActivity.this, FirstAccess.class);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        };
        mythread.start();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
