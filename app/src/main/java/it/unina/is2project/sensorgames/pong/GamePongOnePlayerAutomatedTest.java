package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import java.util.Random;

public class GamePongOnePlayerAutomatedTest extends GamePongOnePlayer {

    private static final String TAG = "1P_Test";

    private int random_number = 0;

    @Override
    protected void gameEventsCollisionLogic() {
        super.gameEventsCollisionLogic();
//        setRandomBarPosition();
        setExactBarPosition();
    }

    /**
     * X of Bar can get value between range: [ball_x_pos - (bar_width - ball_width) ; ball_x_pos]
     */
    private void setRandomBarPosition() {
        bar.setPosition(ball.getXCoordinate() - random_number, bar.getYCoordinate());
        if (ball.collidesWith(bar)) {
            Random random = new Random();
            random_number = random.nextInt((int)(bar.getObjectWidth() - ball.getObjectWidth()) + 1);
            Log.d(TAG, "Bar X = " + (ball.getXCoordinate() - random_number));
            Log.d(TAG, "Bar width = " + bar.getObjectWidth() + ", Ball width = " + ball.getObjectWidth());
        }
    }

    private void setExactBarPosition() {
        bar.setPosition(ball.getXCoordinate(), bar.getYCoordinate());
//        barSprite.setX(ballSprite.getX());
    }
}
