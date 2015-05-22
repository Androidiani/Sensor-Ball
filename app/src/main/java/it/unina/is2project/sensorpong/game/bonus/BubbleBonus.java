package it.unina.is2project.sensorpong.game.bonus;

import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorpong.R;
import it.unina.is2project.sensorpong.game.entity.Ball;
import it.unina.is2project.sensorpong.game.entity.GameObject;

public class BubbleBonus {

    //Necessario per getResources() (settare lo score)
    private final SimpleBaseGameActivity simpleBaseGameActivity;

    //Necessario per le collisioni della palla con le bubble
    private final Ball ball;

    // Bubble Bonus
    private final GameObject bubble;
    private final List<GameObject> bubbles = new ArrayList<>();
    private static final int BONUS_BALL_MIN_NUM = 3;
    private static final int BONUS_BALL_MAX_NUM = 5;

    public BubbleBonus(SimpleBaseGameActivity simpleBaseGameActivity, Ball ball) {
        this.simpleBaseGameActivity = simpleBaseGameActivity;
        this.ball = ball;
        this.bubble = new GameObject(simpleBaseGameActivity, R.drawable.ball_petrol);
    }

    public void addToScene(Scene scene) {
        Random random = new Random();
        int BONUS_BALL_NUM = BONUS_BALL_MIN_NUM + random.nextInt(BONUS_BALL_MAX_NUM - BONUS_BALL_MIN_NUM + 1);
        for (int i = 0; i < BONUS_BALL_NUM; i++) {
            GameObject bubbleTemp = new GameObject(bubble);
            bubbleTemp.addToScene(scene, 0.1f, 0.1f);
            bubbleTemp.setPosition(bubbleTemp.getObjectWidth() + random.nextInt(bubble.getDisplaySize().x - (int) (bubbleTemp.getObjectWidth() * 2)), (bubbleTemp.getObjectHeight() * 2) * (i + 1));
            bubbles.add(bubbleTemp);
        }
        Log.d("Bubble Bonus", "BONUS_BALL_NUM: " + BONUS_BALL_NUM + " bonusBalls.size(): " + bubbles.size());
    }

    public int collision(int score, int level, Text text) {
        for (int i = 0; i < bubbles.size(); i++) {
            if (ball.collidesWith(bubbles.get(i))) {
                Log.d("Bubble Bonus", "Bonus Ball " + i + " removed");
                bubbles.get(i).detach();
                bubbles.remove(i);
                score += 20 * (level + 1);
                text.setText(simpleBaseGameActivity.getResources().getString(R.string.text_score) + ": " + score);
                break;
            }
        }
        return score;
    }

    public void clear() {
        while (!bubbles.isEmpty()) {
            bubbles.get(0).detach();
            bubbles.remove(0);
        }
    }
}
