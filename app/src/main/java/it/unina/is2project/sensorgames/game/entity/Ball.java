package it.unina.is2project.sensorgames.game.entity;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class Ball extends GameObject {

    // Ball Field
    private float ballSpeed;

    // Handler
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

    public void onBallLost() {
        this.detach();
        this.setPosition(Ball.MIDDLE);
        this.attach();
    }

    public void createHandler() {
        this.handler = new PhysicsHandler(this.gSprite);
        this.gSprite.registerUpdateHandler(handler);
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
