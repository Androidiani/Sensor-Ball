package it.unina.is2project.sensorgames.pong;

import org.andengine.audio.sound.Sound;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import it.unina.is2project.sensorgames.game.entity.Ball;
import it.unina.is2project.sensorgames.game.entity.GameObject;

public class FirstEnemyBonus implements IBonusMalus {

    private static final int ENEMY = 100;

    //Campo ball per le collisioni della palla con first enemy
    private Ball ball;

    // First Enemy
    private GameObject firstEnemy;

    public FirstEnemyBonus(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable, Ball ball) {
        this.ball = ball;
        this.firstEnemy = new GameObject(simpleBaseGameActivity, idDrawable);
    }

    @Override
    public void addToScene(Scene scene) {
        firstEnemy.addToScene(scene, 1f);
        firstEnemy.setPosition(GameObject.TOP);
    }

    @Override
    public void collision() {
    }

    public int collision(int previous_event, Sound touch) {
        if (ball.collidesWith(firstEnemy) && previous_event != ENEMY) {
            previous_event = ENEMY;
            ball.setHandlerSpeedY(-ball.getHandlerSpeedY());
            touch.play();
        }
        return previous_event;
    }

    @Override
    public void clear() {
        firstEnemy.detach();
    }
}
