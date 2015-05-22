package it.unina.is2project.sensorpong.game.bonus;

import android.util.Log;

import org.andengine.entity.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorpong.game.entity.Ball;

public class RushHourBonus {

    // Rush Hour
    private final Ball ball;
    private final List<Ball> rushHour = new ArrayList<>();
    private static final int RUSH_HOUR_MIN_NUM = 15;
    private static final int RUSH_HOUR_MAX_NUM = 30;
    private final List<Float> oldRushSpeed_x = new ArrayList<>();
    private final List<Float> oldRushSpeed_y = new ArrayList<>();

    public RushHourBonus(Ball ball) {
        //RushHour ha la stessa texture di ball
        this.ball = ball;
    }

    public void addToScene(Scene scene) {
        Random random = new Random();
        int RUSH_HOUR_NUM = RUSH_HOUR_MIN_NUM + random.nextInt(RUSH_HOUR_MAX_NUM - RUSH_HOUR_MIN_NUM + 1);
        for (int i = 0; i < RUSH_HOUR_NUM; i++) {
            Ball rushTemp = new Ball(ball);
            rushTemp.addToScene(scene, 0.1f, 0.1f);
            rushTemp.setRandomPosition();
            rushTemp.setHandlerSpeed(ball.getBallSpeed() * (random.nextFloat() - random.nextFloat()), ball.getBallSpeed() * (random.nextFloat() - random.nextFloat()));
            rushHour.add(rushTemp);
        }
        Log.d("Rush Hour", "RUSH_HOUR_NUM: " + RUSH_HOUR_NUM + ", rushHour.size(): " + rushHour.size());
    }

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
