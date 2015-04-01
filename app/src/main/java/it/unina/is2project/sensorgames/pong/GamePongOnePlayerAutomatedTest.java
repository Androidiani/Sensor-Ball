package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.input.sensor.acceleration.AccelerationData;

import java.util.Random;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {
    private final String TAG = "1P_Test";
    private float random_number = 0;

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // do nothing
    }

    @Override
    protected void gameEvents() {
        super.gameEvents();
        setBarPosition();
    }

    private void setBarPosition(){
        barSprite.setX(ballSprite.getX() - random_number);
        if(ballSprite.collidesWith(barSprite)) {
            Random random = new Random();
            random_number = random.nextInt((int)(barSprite.getWidth() - ballSprite.getWidth()));
        }
    }
}
