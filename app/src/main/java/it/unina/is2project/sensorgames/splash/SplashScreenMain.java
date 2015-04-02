package it.unina.is2project.sensorgames.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    Animation animFadein;
    ImageView splashAnim;
    ImageView gameName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_main);

        animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        splashAnim = (ImageView) findViewById(R.id.splashAnim);
        splashAnim.startAnimation(animMove);

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        gameName = (ImageView) findViewById(R.id.appNameSplash);
        gameName.startAnimation(animFadein);

        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (splashActive && ms < splashTime) {
                        if(!paused)
                            ms=ms+100;
                        sleep(100);
                    }
                } catch(Exception e) {}
                finally {
                    Intent intent = new Intent(SplashScreenMain.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mythread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen_main, menu);
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
}
