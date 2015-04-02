package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.input.sensor.acceleration.AccelerationData;

import java.util.Random;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {

    private static final String TAG = "1P_Test";

    private float random_number = 0;
    private Random random;

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // do nothing
    }

    @Override
    protected void gameEvents() {
        super.gameEvents();
        setBarPosition();
    }

    /**
     * BarSprite can get value between range: [ball_x_pos - (bar_width - ball_width) ; ball_x_pos]
     */
    private void setBarPosition() {
        barSprite.setX(ballSprite.getX() - random_number);
        if (ballSprite.collidesWith(barSprite)) {
            random = new Random();
            random_number = random.nextInt((int)(barSprite.getWidth() - ballSprite.getWidth()) + 1);
            Log.d(TAG, "Bar X = " + (ballSprite.getX() - random_number));
            Log.d(TAG, "Bar width = " + barSprite.getWidth() + ", Ball width = " + ballSprite.getWidth());
        }
    }
}
