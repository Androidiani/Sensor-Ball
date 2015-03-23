package it.unina.is2project.sensorgames.pong;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
    private Text txtLvl;

    // Life
    private BitmapTextureAtlas lifeTexture;
    private ITextureRegion lifeTextureRegion;
    private List<Sprite> lifeSprites = new ArrayList<Sprite>();

    /*
        Game data
    */
    private int score = 0;
    private int gain;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE - 1;

    // Levels
    private int level;
    private boolean level_one = true;
    private static final int LEVEL_ONE = 0;
    private static final int BARRIER_ONE = 150;
    private boolean level_two = false;
    private static final int LEVEL_TWO = 0;
    private static final int BARRIER_TWO = 300;
    private boolean level_three = false;
    private static final int LEVEL_THREE = 2;
    private static final int BARRIER_THREE = 900;
    private boolean level_four = false;
    private static final int LEVEL_FOUR = 3;
    private static final int BARRIER_FOUR = 1800;
    private boolean level_five = false;
    private static final int LEVEL_FIVE = 4;
    private static final int BARRIER_FIVE = 3600;
    private boolean level_six = false;
    private static final int LEVEL_SIX = 5;
    private static final int BARRIER_SIX = 5200;
    private boolean level_seven = false;
    private static final int LEVEL_SEVEN = 6;
    private static final int BARRIER_SEVEN = 9200;

    // Events
    private int game_event;
    private boolean new_event = true;
    private static final int NO_EVENT = 0;
    private static final int RUSH_HOUR = 1;
    private static final int PARTY_TIME = 2;
    private static final int FREEZE = 3;


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

        /** Adding the level text to the scene */
        txtLvl = new Text(10, txtScore.getY() + txtScore.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtLvl);

        /** Adding the level text to the scene */
        txtEvnt = new Text(10, txtLvl.getY() + txtLvl.getHeight(), font, "", 20, getVertexBufferObjectManager());
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
        GAME_VELOCITY = 2;

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

                /** Game levels section */
                gameLevels();

                /** Setting up random events generator */
                randomEventsGenerator();

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
            txtLvl.setText("Game Over");
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
        if(score >= 0 && score < BARRIER_ONE) {
            score += 2;
            gain = 2;
        }

        if(score >= BARRIER_ONE && score < BARRIER_TWO) {
            score += 5;
            gain = 5;
        }

        if(score >= BARRIER_TWO && score < BARRIER_THREE) {
            score += 15;
            gain = 15;
        }

        if(score >= BARRIER_THREE && score < BARRIER_FOUR) {
            score += 30;
            gain = 30;
        }

        if(score >= BARRIER_FOUR && score < BARRIER_FIVE) {
            score += 60;
            gain = 60;
        }

        if(score >= BARRIER_FIVE && score < BARRIER_SIX) {
            score += 90;
            gain = 90;
        }

        if(score >= BARRIER_SEVEN) {
            score += 150;
            gain = 150;
        }

        if(game_event == PARTY_TIME)
            score += gain;
    }

    @Override
    public void remScore() {
        // do nothing
    }

    @Override
    public void actionDownEvent() {
        // do nothing
    }


    private void gameLevels(){
        /** This procedure understand what modifier needs according to the score */
        if(score < BARRIER_ONE && level_one){
            level = LEVEL_ONE;
            txtLvl.setText("Level one");
        }

        if(score >= BARRIER_ONE && score < BARRIER_TWO && !level_two) {
            GAME_VELOCITY *= 2;
            level_two = true;
            txtLvl.setText("Level two");
        }

        if(score >= BARRIER_TWO && score < BARRIER_THREE && !level_three) {
            handler.setVelocity(handler.getVelocityX() * 2, handler.getVelocityY() * 2);
            level_three = true;
            txtLvl.setText("Level three");
        }

        if(score >= BARRIER_THREE && score < BARRIER_FOUR && !level_four) {
            barSprite.setWidth(0.2f * CAMERA_WIDTH);
            level_four = true;
            txtLvl.setText("Level four");
        }

        if(score >= BARRIER_FOUR && score < BARRIER_FIVE && !level_five) {
            handler.setVelocity(handler.getVelocityX() * 1.5f, handler.getVelocityY() * 1.5f);
            level_five = true;
            txtLvl.setText("Level five");
        }

        if(score >= BARRIER_FIVE && score < BARRIER_SIX && !level_six) {
            barSprite.setWidth(0.15f * CAMERA_WIDTH);
            level_six = true;
            txtLvl.setText("Level six");
        }

        if(score >= BARRIER_SEVEN && !level_seven) {
            handler.setVelocity(handler.getVelocityX() * 1.5f, handler.getVelocityY() * 1.5f);
            level_seven = true;
            txtLvl.setText("Level seven");
        }
    }

    private void randomEventsGenerator(){
        if(new_event) {
            Timer timer = new Timer();
            Random random = new Random();
            timer.schedule(new SetGameEvents(), 5000 + random.nextInt(5000));
            new_event = false;
        }
    }

    private void gameEvents(){
        switch(game_event){
            case NO_EVENT:
                txtEvnt.setText("");
                break;

            case RUSH_HOUR:
                txtEvnt.setText("Rush Hour");
                break;

            case PARTY_TIME:
                txtEvnt.setText("Party Time");
                break;

            case FREEZE:
                txtEvnt.setText("Freeze");
                handler.setVelocity(handler.getVelocityX()/2, handler.getVelocityY()/2);
                break;
        }
    }

    class SetGameEvents extends TimerTask {

        @Override
        public void run() {
            Random random = new Random();
            if(game_event == FREEZE)
                handler.setVelocity(handler.getVelocityX()*2, handler.getVelocityY()*2); // If previous event was FREEZE
            game_event = random.nextInt(3);
            new_event = true;
        }
    }

}
