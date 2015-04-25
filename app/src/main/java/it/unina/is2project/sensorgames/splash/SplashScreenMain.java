package it.unina.is2project.sensorgames.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import it.unina.is2project.sensorgames.FirstAccess;
import it.unina.is2project.sensorgames.MainActivity;
import it.unina.is2project.sensorgames.R;

public class SplashScreenMain extends Activity {

    protected long ms = 0;
    protected long splashTime = 3000;
    protected boolean splashActive = true;

    Animation animMove;
    Animation animFadeIn;
    ImageView splashAnim;
    ImageView gameName;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_main);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        splashAnim = (ImageView) findViewById(R.id.splashAnim);
        splashAnim.startAnimation(animMove);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        gameName = (ImageView) findViewById(R.id.appNameSplash);
        gameName.startAnimation(animFadeIn);

        // Shared Preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (splashActive && ms < splashTime) {
                        ms = ms + 100;
                        sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent intent;
                    if (sharedPreferences.getString("prefNickname", getString(R.string.txt_no_name)).compareTo(getString(R.string.txt_no_name)) == 0)
                        intent = new Intent(SplashScreenMain.this, FirstAccess.class);
                    else intent = new Intent(SplashScreenMain.this, MainActivity.class);
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
