package it.unina.is2project.sensorgames.pong;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;

public class GamePongTraining extends GamePong {

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        /** Setting up the physics of the game */
        settingPhysics();

        return scene;
    }

    @Override
    public void settingPhysics() {
        /** Setting up physics
         * - A physics handler is linked to the ballSprite
         */
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        handler.setVelocity(BALL_SPEED, -BALL_SPEED);

        /** The Update Handler is linked to the scene. It evalutates the condition of the scene
         *  every frame.
         */
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                /** Border variables */
                int rL = CAMERA_WIDTH - (int) ballSprite.getWidth() / 2;
                int bL = CAMERA_HEIGHT - (int) ballSprite.getHeight() / 2;

                /** Edge's condition
                 *  The direction of the ball changes depending on the
                 *  affected side
                 */
                if ((ballSprite.getX() > rL - (int) ballSprite.getWidth() / 2) && previous_event != RIGHT) {
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    previous_event = RIGHT;
                }
                if (ballSprite.getX() < 0 && previous_event != LEFT) {
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    previous_event = LEFT;
                }
                if (ballSprite.getY() < 0 && previous_event != TOP) {
                    handler.setVelocityY(-handler.getVelocityY());
                    touch.play();
                    previous_event = TOP;
                }
                if ((ballSprite.getY() > bL - (int) ballSprite.getHeight() / 2) && previous_event != BOTTOM) {
                    previous_event = BOTTOM;
                    restartOnBallLost();
                }

                /** When the barSprite and the ballSprite collides */
                if (ballSprite.collidesWith(barSprite)) {
                    /** Condition variable who understand if the ball hit the bar side or front
                     *
                     * - ya: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     * - yb: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     */
                    float ya = ballSprite.getY() - ballSprite.getHeight() / 2;
                    float yb = barSprite.getY() - barSprite.getHeight() / 2;

                    /** The ball hit the bar's top surface */
                    if (ya <= yb && previous_event != OVER && previous_event != SIDE) {
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                    }
                    /** The ball hit the bar's side surface */
                    if (previous_event != SIDE && previous_event != OVER) {
                        handler.setVelocityX(-handler.getVelocityX());
                        previous_event = SIDE;
                    }
                    touch.play();
                }
            }

            @Override
            public void reset() {
            }
        });
    }

    @Override
    public void restartOnBallLost() {
        /** The ballSprite is detached */
        scene.detachChild(ballSprite);

        /** Setting the position on centre of screen */
        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth()) / 2, (CAMERA_HEIGHT - ballSprite.getHeight()) / 2);

        /** Set the direction upward */
        handler.setVelocityY(-handler.getVelocityY());

        /** The ballSprite is attached */
        scene.attachChild(ballSprite);
    }

    @Override
    public void addScore() {
        //do nothing
    }

    @Override
    public void remScore() {
        //do nothing
    }
}
