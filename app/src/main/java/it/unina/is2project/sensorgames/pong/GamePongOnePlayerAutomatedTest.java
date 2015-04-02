package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.input.sensor.acceleration.AccelerationData;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {

    private static final String TAG = "1P_Test";

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // do nothing
    }

    @Override
    protected void gameEvents() {
        super.gameEvents();
        setBarPosition();
    }

    private void setBarPosition() {
        barSprite.setX(ballSprite.getX());
        Log.d(TAG, "Bar X = " + ballSprite.getX());
    }
}
