package it.unina.is2project.sensorgames.pong;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public class GamePongTraining extends GamePong {

    // Settings button
    protected BitmapTextureAtlas settingsTexture;
    protected ITextureRegion settingsTextureRegion;
    protected Sprite settingsSprite;

    // Easy mode button
    protected BitmapTextureAtlas easyTexture;
    protected ITextureRegion easyTextureRegion;
    protected Sprite easySprite;

    // Normal mode button
    protected BitmapTextureAtlas normalTexture;
    protected ITextureRegion normalTextureRegion;
    protected Sprite normalSprite;

    // Insane mode button
    protected BitmapTextureAtlas insaneTexture;
    protected ITextureRegion insaneTextureRegion;
    protected Sprite insaneSprite;

    // Game's mode
    private static final int EASY_MODE = 0;
    private static final int NORMAL_MODE = 1;
    private static final int INSANE_MODE = 2;

    // Events
    private boolean enableModes = false;
    private long firstTap;
    private long secondTap;

    // Speed directions
    private Point DIRECTIONS;

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        /** Adding the easySprite to the scene */
        easySprite = new Sprite(0, 0, easyTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d("", "Easy mode");
                        setMode(EASY_MODE);
                    }
                }
                return true;
            }
        };
        easySprite.setWidth(CAMERA_WIDTH * 0.5f);
        easySprite.setHeight(easySprite.getWidth() * 0.581f); // 0.581 is the rate between image width and height
        easySprite.setX((CAMERA_WIDTH - easySprite.getWidth()) / 2); // Position set after scaling
        easySprite.setY(((CAMERA_HEIGHT - easySprite.getHeight()) / 2) - easySprite.getHeight());

        /** Adding the normalSprite to the scene */
        normalSprite = new Sprite(0, 0, normalTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d("", "Normal mode");
                        setMode(NORMAL_MODE);
                    }
                }
                return true;
            }
        };
        normalSprite.setWidth(CAMERA_WIDTH * 0.5f);
        normalSprite.setHeight(normalSprite.getWidth() * 0.581f); // 0.581 is the rate between image width and height
        normalSprite.setX((CAMERA_WIDTH - normalSprite.getWidth()) / 2); // Position set after scaling
        normalSprite.setY((CAMERA_HEIGHT - normalSprite.getHeight()) / 2);

        /** Adding the insaneSprite to the scene */
        insaneSprite = new Sprite(0, 0, insaneTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d("", "Insane mode");
                        setMode(INSANE_MODE);
                    }
                }
                return true;
            }
        };
        insaneSprite.setWidth(CAMERA_WIDTH * 0.5f);
        insaneSprite.setHeight(insaneSprite.getWidth() * 0.581f); // 0.581 is the rate between image width and height
        insaneSprite.setX((CAMERA_WIDTH - insaneSprite.getWidth()) / 2); // Position set after scaling
        insaneSprite.setY(((CAMERA_HEIGHT - insaneSprite.getHeight()) / 2) + insaneSprite.getHeight());

        /** Setting up the physics of the game */
        settingPhysics();

        return scene;
    }


    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        /** Easy mode */
        Drawable easyDrawable = getResources().getDrawable(R.drawable.training_setting_easy);
        easyTexture = new BitmapTextureAtlas(getTextureManager(), easyDrawable.getIntrinsicWidth(), easyDrawable.getIntrinsicHeight());
        easyTextureRegion = createFromResource(easyTexture, this, R.drawable.training_setting_easy, 0, 0);
        easyTexture.load();

        /** Normal mode */
        Drawable normalDrawable = getResources().getDrawable(R.drawable.training_setting_normal);
        normalTexture = new BitmapTextureAtlas(getTextureManager(), normalDrawable.getIntrinsicWidth(), normalDrawable.getIntrinsicHeight());
        normalTextureRegion = createFromResource(normalTexture, this, R.drawable.training_setting_normal, 0, 0);
        normalTexture.load();

        /** Insane mode */
        Drawable insaneDrawable = getResources().getDrawable(R.drawable.training_setting_insane);
        insaneTexture = new BitmapTextureAtlas(getTextureManager(), insaneDrawable.getIntrinsicWidth(), insaneDrawable.getIntrinsicHeight());
        insaneTextureRegion = createFromResource(insaneTexture, this, R.drawable.training_setting_insane, 0, 0);
        insaneTexture.load();
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

    @Override
    public void actionDownEvent() {
        if (!enableModes) {
            Log.i("", "Game Paused");
            showMenu();
        }
    }


    /**
     * Show the mode's menù
     */
    private void showMenu() {
        // Store directions data
        DIRECTIONS = getDirections();
        Log.d("", "Directions:" + DIRECTIONS.x + " " + DIRECTIONS.y);

        // Stop the ball
        handler.setVelocity(0f);
        GAME_VELOCITY = 0;

        // Register Touch Area
        scene.registerTouchArea(easySprite);
        scene.registerTouchArea(normalSprite);
        scene.registerTouchArea(insaneSprite);

        // Attach the menu's children
        scene.attachChild(easySprite);
        scene.attachChild(normalSprite);
        scene.attachChild(insaneSprite);

        firstTap = System.currentTimeMillis();

        enableModes = true;
    }

    /**
     * Hide the mode's menù
     */
    private void hideMenu() {
        // Detach menu's children
        scene.detachChild(easySprite);
        scene.detachChild(normalSprite);
        scene.detachChild(insaneSprite);

        // Unregister Touch Area
        scene.unregisterTouchArea(easySprite);
        scene.unregisterTouchArea(normalSprite);
        scene.unregisterTouchArea(insaneSprite);

        enableModes = false;
    }

    /**
     * Set the game's mode
     */
    private void setMode(int mode) {
        switch (mode) {
            case EASY_MODE: {
                handler.setVelocityX(BALL_SPEED * DIRECTIONS.x);
                handler.setVelocityY(BALL_SPEED * DIRECTIONS.y);
                GAME_VELOCITY = 2;
                barSprite.setWidth(CAMERA_WIDTH * 0.3f);
                break;
            }

            case NORMAL_MODE: {
                handler.setVelocityX(BALL_SPEED * 2 * DIRECTIONS.x);
                handler.setVelocityY(BALL_SPEED * 2 * DIRECTIONS.y);
                GAME_VELOCITY = 4;
                barSprite.setWidth(CAMERA_WIDTH * 0.2f);
                break;
            }

            case INSANE_MODE: {
                handler.setVelocityX(BALL_SPEED * 4 * DIRECTIONS.x);
                handler.setVelocityY(BALL_SPEED * 4 * DIRECTIONS.y);
                GAME_VELOCITY = 4;
                barSprite.setWidth(CAMERA_WIDTH * 0.15f);
                break;
            }
        }
        hideMenu();
    }

    /**
     * Get the directions of the ball
     *
     * @return
     */
    private Point getDirections() {
        Point mPoint = new Point();
        if (handler.getVelocityX() < 0 && handler.getVelocityY() < 0) {
            mPoint.set(-1, -1);
        }
        if (handler.getVelocityX() < 0 && handler.getVelocityY() > 0) {
            mPoint.set(-1, 1);
        }
        if (handler.getVelocityX() > 0 && handler.getVelocityY() < 0) {
            mPoint.set(1, -1);
        }
        if (handler.getVelocityX() > 0 && handler.getVelocityY() > 0) {
            mPoint.set(1, 1);
        }
        return mPoint;
    }
}
