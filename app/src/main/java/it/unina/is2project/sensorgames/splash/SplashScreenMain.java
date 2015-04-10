package it.unina.is2project.sensorgames.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import it.unina.is2project.sensorgames.MainActivity;
import it.unina.is2project.sensorgames.R;

public class SplashScreenMain extends Activity {

    protected long ms = 0;
    protected long splashTime = 3000;
    protected boolean splashActive = true;
    protected boolean paused = false;

    Animation animMove;
    Animation animFadeIn;
    ImageView splashAnim;
    ImageView gameName;

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

        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (splashActive && ms < splashTime) {
                        if(!paused)
                            ms=ms+100;
                        sleep(100);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(SplashScreenMain.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mythread.start();
    }
}
