package it.unina.is2project.sensorpong.game.entity;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class Ball extends GameObject {

    // Ball Field
    private float ballSpeed;
    // Handler Field
    private PhysicsHandler handler;

    public Ball(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable) {
        super(simpleBaseGameActivity, idDrawable);
    }

    public Ball(Ball ball) {
        super(ball);
    }

    public float getBallSpeed() {
        return ballSpeed;
    }

    public void setBallSpeed(float ballSpeed) {
        this.ballSpeed = ballSpeed;
    }

    @Override
    public void addToScene(Scene scene, float xRatio, float yRatio) {
        super.addToScene(scene, xRatio, yRatio);
        this.handler = new PhysicsHandler(this.gSprite);
        this.gSprite.registerUpdateHandler(handler);
    }

    public void onBallLost() {
        this.detach();
        this.setPosition(MIDDLE);
        this.attach();
    }

    public void setHandlerSpeed(float xSpeed, float ySpeed) {
        this.handler.setVelocity(xSpeed, ySpeed);
    }

    public void setHandlerSpeedX(float xSpeed) {
        this.handler.setVelocityX(xSpeed);
    }

    public void setHandlerSpeedY(float ySpeed) {
        this.handler.setVelocityY(ySpeed);
    }

    public float getHandlerSpeedX() {
        return this.handler.getVelocityX();
    }

    public float getHandlerSpeedY() {
        return this.handler.getVelocityY();
    }
}