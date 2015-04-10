package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.input.sensor.acceleration.AccelerationData;

import java.util.Random;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {

    private static final String TAG = "1P_Test";

    private float random_number = 0;

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // do nothing
    }

    @Override
    protected void gameEventsCollisionLogic() {
        super.gameEventsCollisionLogic();
//        setRandomBarPosition();
        setExactBarPosition();
    }

    /**
     * X of BarSprite can get value between range: [ball_x_pos - (bar_width - ball_width) ; ball_x_pos]
     */
    private void setRandomBarPosition() {
        barSprite.setX(ballSprite.getX() - random_number);
        if (ballSprite.collidesWith(barSprite)) {
            Random random = new Random();
            random_number = random.nextInt((int) (barSprite.getWidth() - ballSprite.getWidth()) + 1);
            Log.d(TAG, "Bar X = " + (ballSprite.getX() - random_number));
            Log.d(TAG, "Bar width = " + barSprite.getWidth() + ", Ball width = " + ballSprite.getWidth());
        }
    }

    private void setExactBarPosition() {
        barSprite.setX(ballSprite.getX());
    }
}
