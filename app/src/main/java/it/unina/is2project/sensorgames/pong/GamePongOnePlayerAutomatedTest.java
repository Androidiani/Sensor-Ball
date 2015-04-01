package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.input.sensor.acceleration.AccelerationData;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {
    private final String TAG = "1P_Test";

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // do nothing
    }

    @Override
    protected void gameEvents() {
        super.gameEvents();
        barSprite.setX(ballSprite.getX());
    }
}
