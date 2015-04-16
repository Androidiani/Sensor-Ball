package it.unina.is2project.sensorgames.game.entity;

import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

/**
 * Created by Giovanni on 09/04/2015.
 */
public class Ball extends GameObject {

    public Ball(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable) {
        super(simpleBaseGameActivity, idDrawable);
    }

    @Override
    public void addToScene(Scene scene, float spriteRatio) {
        super.addToScene(scene, spriteRatio);
        gSprite.setHeight(displaySize.x * spriteRatio);
    }
}
