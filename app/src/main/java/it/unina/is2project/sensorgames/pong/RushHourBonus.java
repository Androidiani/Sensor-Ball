package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.entity.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorgames.game.entity.Ball;

public class RushHourBonus implements IBonusMalus {
    // Rush Hour
    protected Ball ball;
    protected List<Ball> rushHour = new ArrayList<>();
    protected static final int RUSH_HOUR_MIN_NUM = 15;
    protected static final int RUSH_HOUR_MAX_NUM = 30;
    protected List<Float> oldRushSpeed_x = new ArrayList<>();
    protected List<Float> oldRushSpeed_y = new ArrayList<>();

    public RushHourBonus(Ball ball) {
        //Duplicate ball in rush
        //RushHour ha la stessa texture di ball
        this.ball = ball;
    }

    @Override
    public void addToScene(Scene scene) {
        Random random = new Random();
        int RUSH_HOUR_NUM = RUSH_HOUR_MIN_NUM + random.nextInt(RUSH_HOUR_MAX_NUM - RUSH_HOUR_MIN_NUM + 1);
        for (int i = 0; i < RUSH_HOUR_NUM; i++) {
            Ball rushTemp = new Ball(ball);
            rushTemp.addToScene(scene, 0.1f);
            rushTemp.setRandomPosition();
            rushTemp.createHandler();
            rushTemp.setHandlerSpeed(ball.getBallSpeed() * (random.nextFloat() - random.nextFloat()), ball.getBallSpeed() * (random.nextFloat() - random.nextFloat()));
            rushHour.add(rushTemp);
        }
        Log.d("Rush Hour", "RUSH_HOUR_NUM: " + RUSH_HOUR_NUM + ", rushHour.size(): " + rushHour.size());
    }

    @Override
    public void collision() {
        for (int i = 0; i < rushHour.size(); i++) {
            Ball rush = rushHour.get(i);
            if (rush.getXCoordinate() < 0) {
                rush.setHandlerSpeedX(-rush.getHandlerSpeedX());
            }
            if (rush.getXCoordinate() > ball.getDisplaySize().x - ball.getObjectWidth()) {
                rush.setHandlerSpeedX(-rush.getHandlerSpeedX());
            }
            if (rush.getYCoordinate() < 0) {
                rush.setHandlerSpeedY(-rush.getHandlerSpeedY());
            }
            if (rush.getYCoordinate() > ball.getDisplaySize().y - ball.getObjectHeight()) {
                rush.setHandlerSpeedY(-rush.getHandlerSpeedY());
            }
        }
    }

    @Override
    public void clear() {
        while (!rushHour.isEmpty()) {
            rushHour.get(0).detach();
            rushHour.remove(0);
        }
    }

    public void pause() {
        for (int i = 0; i < rushHour.size(); i++) {
            oldRushSpeed_x.add(rushHour.get(i).getHandlerSpeedX());
            oldRushSpeed_y.add(rushHour.get(i).getHandlerSpeedY());
            rushHour.get(i).setHandlerSpeed(0f, 0f);
        }
    }

    public void restartAfterPause() {
        for (int i = 0; i < rushHour.size(); i++) {
            rushHour.get(i).setHandlerSpeed(oldRushSpeed_x.get(i), oldRushSpeed_y.get(i));
        }
    }
}
