package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.game.entity.Ball;
import it.unina.is2project.sensorgames.game.entity.GameObject;

public class BubbleBonus implements IBonusMalus {

    //Per getResources()
    private SimpleBaseGameActivity simpleBaseGameActivity;

    //Campo ball per le collisioni del bonus con la palla
    private Ball ball;

    // Bubble Bonus
    private GameObject bubble;
    private List<GameObject> bubbles = new ArrayList<>();
    private static final int BONUS_BALL_MIN_NUM = 3;
    private static final int BONUS_BALL_MAX_NUM = 5;

    public BubbleBonus(SimpleBaseGameActivity simpleBaseGameActivity, Ball ball) {
        this.bubble = new GameObject(simpleBaseGameActivity, R.drawable.ball_petrol);
        this.simpleBaseGameActivity = simpleBaseGameActivity;
        this.ball = ball;
    }

    @Override
    public void addToScene(Scene scene) {
        Random random = new Random();
        int BONUS_BALL_NUM = BONUS_BALL_MIN_NUM + random.nextInt(BONUS_BALL_MAX_NUM - BONUS_BALL_MIN_NUM + 1);

        // Adding the bonus ball sprites to the scene
        for (int i = 0; i < BONUS_BALL_NUM; i++) {
            GameObject bubbleTemp = new GameObject(bubble);
            bubbleTemp.addToScene(scene, 0.1f);
            bubbleTemp.setObjectHeight(bubble.getDisplaySize().x * 0.1f);
            bubbleTemp.setPosition(bubbleTemp.getObjectWidth() + random.nextInt(bubble.getDisplaySize().x - (bubbleTemp.getObjectWidth() * 2)), (bubbleTemp.getObjectHeight() * 2) * (i + 1));
            bubbles.add(bubbleTemp);
        }
        Log.d("Bubble Bonus", "BONUS_BALL_NUM: " + BONUS_BALL_NUM + " bonusBalls.size(): " + bubbles.size());
    }

    @Override
    public void collision() {
    }

    public int collision(Integer score, int level, Text text) {
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

    @Override
    public void clear() {
        while (bubbles.size() > 0) {
            bubbles.get(0).detach();
            bubbles.remove(0);
        }
    }
}
