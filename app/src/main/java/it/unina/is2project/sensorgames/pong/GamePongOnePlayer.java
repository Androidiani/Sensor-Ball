package it.unina.is2project.sensorgames.pong;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromAsset;
import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public class GamePongOnePlayer extends GamePong {

    /*
        Scene
    */
    private boolean game_over = false;

    /*
        Graphics
    */
    // Text View
    private Text txtScore;
    private Text txtEvnt;

    // Life
    private BitmapTextureAtlas lifeTexture;
    private ITextureRegion lifeTextureRegion;
    private List<Sprite> lifeSprites = new ArrayList<Sprite>();

    /*
        Game data
    */
    private int score = 0;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE - 1;

    // Events
    private boolean x2_ballspeed = false;
    private static final int X2_BALLSPEED = 7;
    private boolean x2_barspeed = false;
    private static final int X2_BARSPEED = 4;
    private boolean x3_barspeed = false;
    private static final int X3_BARSPEED = 11;
    private boolean x4_ballspeed = false;
    private static final int X4_BALLSPEED = 235;
    private boolean reduce_bar = false;
    private static final int REDUCE_BAR = 45;

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        /** Life texture loading */
        Drawable starDraw = getResources().getDrawable(R.drawable.life);
        lifeTexture = new BitmapTextureAtlas(getTextureManager(), starDraw.getIntrinsicWidth(), starDraw.getIntrinsicHeight());
        lifeTextureRegion = createFromResource(lifeTexture, this, R.drawable.life, 0, 0);
        lifeTexture.load();
    }

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        /** Adding the scoring text to the scene */
        txtScore = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtScore);

        /** Adding the event text to the scene */
        txtEvnt = new Text(10, 45, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtEvnt);

        /** Adding the life sprites to the scene */
        for ( int i = 1 ; i <= MAX_LIFE ; i++ ){
            Sprite lifeSprite = new Sprite(0, 0, lifeTextureRegion,getVertexBufferObjectManager());
            lifeSprite.setX(CAMERA_WIDTH - i*lifeSprite.getWidth());
            lifeSprites.add(lifeSprite);
            scene.attachChild(lifeSprites.get(i-1));
        }

        /** The score text is updated to the current value */
        txtScore.setText("Score: " + score);

        /** Setting up the physics of the game */
        settingPhysics();

        return scene;
    }

    @Override
    public void settingPhysics() {
        /** A physics handler is linked to the ballSprite */
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        handler.setVelocity(BALL_SPEED, -BALL_SPEED);

        /** The Update Handler is linked to the scene. It evalutates the condition of the scene every frame */
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                /** Border variables */
                int rL = CAMERA_WIDTH - (int) ballSprite.getWidth() / 2;
                int bL = CAMERA_HEIGHT - (int) ballSprite.getHeight() / 2;

                /** Edge's condition
                 *  The direction of the ball changes depending on the affected side
                 */
                if(!game_over) {
                    if ((ballSprite.getX() > rL - (int) ballSprite.getWidth() / 2) && previous_event != RIGHT) {
                        handler.setVelocityX(-handler.getVelocityX());
                        touch.play();
                        previous_event = RIGHT;
                        Log.d("", "Right. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    }
                    if (ballSprite.getX() < 0 && previous_event != LEFT) {
                        handler.setVelocityX(-handler.getVelocityX());
                        touch.play();
                        previous_event = LEFT;
                        Log.d("", "Left. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    }
                    if (ballSprite.getY() < 0 && previous_event != TOP) {
                        handler.setVelocityY(-handler.getVelocityY());
                        touch.play();
                        previous_event = TOP;
                        Log.d("", "Top. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    }
                    if ((ballSprite.getY() > bL - (int) ballSprite.getHeight() / 2) && previous_event != BOTTOM) {
                        /** If the previous_event is "SIDE" it will reduce the undeserved points */
                        if (previous_event == SIDE)
                            remScore();
                        previous_event = BOTTOM;
                        restartOnBallLost();
                        Log.d("", "Bottom. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    }
                }

                /** When the barSprite and the ballSprite collides */
                if (ballSprite.collidesWith(barSprite)) {
                    /** Condition variable who understand if the ball hit the bar side or front
                     * - ya: is the relative position of the ball according to the CAMERA_HEIGHT
                     * - yb: is the relative position of the ball according to the CAMERA_HEIGHT
                     */
                    float ya = ballSprite.getY() - ballSprite.getHeight() / 2;
                    float yb = barSprite.getY() - barSprite.getHeight() / 2;

                    /** The ball hit the bar's top surface */
                    if (ya <= yb && previous_event != OVER && previous_event != SIDE) {
                        Log.d("", "Top event");
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                        addScore();
                    }
                    /** The ball hit the bar's side surface */
                    else if (previous_event != SIDE && previous_event != OVER) {
                        Log.d("", "Side event");
                        handler.setVelocityX(-handler.getVelocityX());
                        previous_event = SIDE;
                        addScore();
                    }
                    touch.play();
                }

                /** The score text is updated to the current value */
                txtScore.setText("Score: " + score);

                /** Game events section */
                gameEvents();
            }

            @Override
            public void reset() {

            }
        });
    }


    @Override
    public void restartOnBallLost() {
        /**  The ballSprite is detached */
        scene.detachChild(ballSprite);

        /** The lifeSprite is detached */
        scene.detachChild(lifeSprites.get(life));

        /** Life count is decremented */
        life--;

        /** If the life count is less equal than 0, the game is over */
        if(life < 0){
            game_over = true;
            txtEvnt.setText("Game Over");
        }
        /** Else replace the ball */
        else{
            /** Setting the position on centre of screen */
            ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth())/2, (CAMERA_HEIGHT - ballSprite.getHeight())/2);

            /** Set the direction upward */
            handler.setVelocityY(-handler.getVelocityY());

            /** The ballSprite is attached */
            scene.attachChild(ballSprite);
        }
    }

    @Override
    public void addScore() {
        /** This procedure increase the score according to the current score. */
        if (score >= 0 && score <= 9) {
            if (previous_event == SIDE)
                score += 3;
            else score++;
        }
        if (score >= 10 && score <= 30) {
            if (previous_event == SIDE)
                score += 9;
            else score += 3;
        }
        if (score >= 31) {
            if (previous_event == SIDE)
                score += 27;
            else score += 9;
        }
    }

    @Override
    public void remScore() {
        /** If the previous_event is "SIDE" it will reduce the undeserved points. */
        if (previous_event == SIDE) {
            if (score >= 0 && score <= 9)
                score -= 3;
            if (score >= 10 && score <= 30)
                score -= 9;
            if (score >= 31)
                score -= 27;
        }
    }

    private void gameEvents(){
        /** This procedure understand what modifier needs according to the score */

        /** Increasing x2 tha bar speed */
        if(score >= X2_BARSPEED && !x2_barspeed){
            GAME_VELOCITY *= 2;
            x2_barspeed = true;
            txtEvnt.setText("2X Bar Speed");
        }

        /** Increasing x2 the ball speed */
        if(score >= X2_BALLSPEED && !x2_ballspeed){
            handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
            x2_ballspeed = true;
            txtEvnt.setText("2X Ball Speed");
        }

        /** Increasing x3 the bar speed */
        if(score >= X3_BARSPEED && !x3_barspeed){
            GAME_VELOCITY *= 1.5f;
            x3_barspeed = true;
            txtEvnt.setText("3X Bar Speed");
        }

        /** Increasing x4 the ball speed */
        if(score >= X4_BALLSPEED && !x4_ballspeed){
            handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
            x4_ballspeed = true;
            txtEvnt.setText("4X Ball Speed");
        }

        /** Scale the bar dimensions to small */
        if(score >= REDUCE_BAR && !reduce_bar){
            barSprite.setWidth(CAMERA_WIDTH*0.2f);
            reduce_bar = true;
            txtEvnt.setText("Bar dimension: small");
        }
    }

}
