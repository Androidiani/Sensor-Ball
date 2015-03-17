package it.unina.is2project.sensorgames.pong;

import android.util.Log;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;

public class GamePongTraining extends GamePong {

    @Override
    protected Scene onCreateScene() {
        /** Making a new scene */
        scene = new Scene();

        /** Setting up the background color */
        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        /** Adding the ballSprite to the scene */
        ballSprite = new Sprite((CAMERA_WIDTH - ballTexture.getWidth())/2,
                (CAMERA_HEIGHT - ballTexture.getHeight())/2,
                ballTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(ballSprite);


        /** Setting up physics
         * - A physics handler is linked to the ballSprite */
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        handler.setVelocity(BALL_SPEED,-BALL_SPEED);

        /** The Update Handler is linked to the scene. It evalutates the condition of the scene
         *  every frame.
         */
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                /** Border variables */
                int rL = CAMERA_WIDTH - (int)ballSprite.getWidth()/2;
                int bL = CAMERA_HEIGHT - (int)ballSprite.getHeight()/2;

                /** Edge's condition
                 *  The direction of the ball changes depending on the
                 *  affected side
                 */
                if((ballSprite.getX() > rL - (int)ballSprite.getWidth()/2) && previous_event != RIGHT){
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    Log.d("", "Right. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    previous_event = RIGHT;
                }
                if(ballSprite.getX() < 0 && previous_event != LEFT){
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    Log.d("","Left. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = LEFT;
                }
                if((ballSprite.getY() > bL - (int)ballSprite.getHeight()/2) && previous_event != BOTTOM){
                    /** If the previous_event is "SIDE" it will reduce the undeserved points. */
                    if(previous_event == SIDE)
                        remScore();
                    Log.d("","Bottom. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = BOTTOM;
                    /** If the game is not over it's restarted */
                    restart_game();
                }
                if(ballSprite.getY() < 0 && previous_event != TOP && !game_over){
                    handler.setVelocityY(-handler.getVelocityY());
                    touch.play();
                    Log.d("","Top. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = TOP;
                }

                /** When the barSprite and the ballSprite collides */
                if(ballSprite.collidesWith(barSprite)){
                    /** Condition variable who understand if the ball hit the bar side or front
                     *
                     * - ya: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     *
                     * - yb: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     */
                    float ya = ballSprite.getY() - ballSprite.getHeight()/2;
                    float yb = barSprite.getY() - barSprite.getHeight()/2;

                    /** The ball hit the bar's top surface */
                    if(ya <= yb && previous_event != OVER && previous_event != SIDE){
                        Log.d("","Top event");
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                    }
                    /** The ball hit the bar's side surface */
                    else if(previous_event != SIDE && previous_event != OVER){
                        handler.setVelocityX(-handler.getVelocityX());
                        Log.d("","Side event");
                        previous_event = SIDE;
                    }
                    touch.play();
                }
            }
            @Override
            public void reset() {

            }
        });

        /** Adding the barSprite to the scene */
        barSprite = new Sprite((CAMERA_WIDTH - barTexture.getWidth())/2,
                (CAMERA_HEIGHT - 2*barTexture.getHeight()),
                barTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(barSprite);

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.enableAccelerationSensor(this);
        mAccelerationOptions = new AccelerationSensorOptions(SensorDelay.GAME);

        /** Return the completed scene */
        return scene;
    }

    @Override
    protected void restart_game(){
        /** This procedure restart the game.
         *
         *  The ballSprite is detached.
         */
        scene.detachChild(ballSprite);

        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth())/2,
                (CAMERA_HEIGHT - ballSprite.getHeight())/2);

        /** Set the direction upward */
        handler.setVelocityY(-handler.getVelocityY());
        scene.attachChild(ballSprite);
    }
}

