package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.util.List;

import it.unina.is2project.sensorgames.game.entity.Ball;
import it.unina.is2project.sensorgames.game.entity.GameObject;

public class LifeBonus implements IBonusMalus {

    //Necessario per le collisioni della palla con life bonus
    private Ball ball;

    // Life Bonus
    private GameObject lifeBonus;
    private int old_life = 0;

    public LifeBonus(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable, Ball ball) {
        this.ball = ball;
        this.lifeBonus = new GameObject(simpleBaseGameActivity, idDrawable);
    }


    @Override
    public void addToScene(Scene scene) {
    }

    public void addToScene(Scene scene, int life) {
        old_life = life;
        lifeBonus.addToScene(scene, 0.1f, 0.1f);
        lifeBonus.setRandomPosition();
    }

    @Override
    public void collision() {
    }

    public int collision(int life, List<GameObject> lifeStars) {
        if (ball.collidesWith(lifeBonus) && old_life == life) {
            lifeBonus.detach();
            life++;
            lifeStars.get(life).attach();
            Log.d("Life Bonus", "Life Collision. Current Life: " + life);
        }
        return life;
    }

    @Override
    public void clear() {
        lifeBonus.detach();
    }
}
