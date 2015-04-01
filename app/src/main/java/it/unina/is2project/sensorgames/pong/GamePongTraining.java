package it.unina.is2project.sensorgames.pong;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public class GamePongTraining extends GamePong {

    private final String TAG = "TrainingGame";

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

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        float spriteRate = easyTextureRegion.getHeight() / easyTextureRegion.getWidth();

        // Adding the easySprite to the scene
        easySprite = new Sprite(0, 0, easyTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d(TAG, "Easy mode");
                        setMode(EASY_MODE);
                    }
                }
                return true;
            }
        };
        easySprite.setWidth(CAMERA_WIDTH * 0.5f);
        easySprite.setHeight(easySprite.getWidth() * spriteRate);
        easySprite.setX((CAMERA_WIDTH - easySprite.getWidth()) / 2); // Position set after scaling
        easySprite.setY(((CAMERA_HEIGHT - easySprite.getHeight()) / 2) - easySprite.getHeight());

        // Adding the normalSprite to the scene
        normalSprite = new Sprite(0, 0, normalTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d(TAG, "Normal mode");
                        setMode(NORMAL_MODE);
                    }
                }
                return true;
            }
        };
        normalSprite.setWidth(CAMERA_WIDTH * 0.5f);
        normalSprite.setHeight(normalSprite.getWidth() * spriteRate);
        normalSprite.setX((CAMERA_WIDTH - normalSprite.getWidth()) / 2); // Position set after scaling
        normalSprite.setY((CAMERA_HEIGHT - normalSprite.getHeight()) / 2);

        // Adding the insaneSprite to the scene
        insaneSprite = new Sprite(0, 0, insaneTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (enableModes) {
                    secondTap = System.currentTimeMillis();
                    if (secondTap - firstTap > 500) {
                        Log.d(TAG, "Insane mode");
                        setMode(INSANE_MODE);
                    }
                }
                return true;
            }
        };
        insaneSprite.setWidth(CAMERA_WIDTH * 0.5f);
        insaneSprite.setHeight(insaneSprite.getWidth() * spriteRate);
        insaneSprite.setX((CAMERA_WIDTH - insaneSprite.getWidth()) / 2); // Position set after scaling
        insaneSprite.setY(((CAMERA_HEIGHT - insaneSprite.getHeight()) / 2) + insaneSprite.getHeight());

        // Setting up the physics of the game
        settingPhysics();

        return scene;
    }


    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Easy mode
        Drawable easyDrawable = getResources().getDrawable(R.drawable.training_setting_easy);
        easyTexture = new BitmapTextureAtlas(getTextureManager(), easyDrawable.getIntrinsicWidth(), easyDrawable.getIntrinsicHeight());
        easyTextureRegion = createFromResource(easyTexture, this, R.drawable.training_setting_easy, 0, 0);
        easyTexture.load();

        // Normal mode
        Drawable normalDrawable = getResources().getDrawable(R.drawable.training_setting_normal);
        normalTexture = new BitmapTextureAtlas(getTextureManager(), normalDrawable.getIntrinsicWidth(), normalDrawable.getIntrinsicHeight());
        normalTextureRegion = createFromResource(normalTexture, this, R.drawable.training_setting_normal, 0, 0);
        normalTexture.load();

        // Insane mode
        Drawable insaneDrawable = getResources().getDrawable(R.drawable.training_setting_insane);
        insaneTexture = new BitmapTextureAtlas(getTextureManager(), insaneDrawable.getIntrinsicWidth(), insaneDrawable.getIntrinsicHeight());
        insaneTextureRegion = createFromResource(insaneTexture, this, R.drawable.training_setting_insane, 0, 0);
        insaneTexture.load();
    }

    @Override
    protected void actionDownEvent() {
        if (!enableModes) {
            Log.i(TAG, "Game Paused");
            showMenu();
        }
    }

    @Override
    protected void bluetoothExtra() {
        //do nothing
    }

    @Override
    protected void addScore() {
        //do nothing
    }

    @Override
    protected void gameLevels() {
        //do nothing
    }

    @Override
    protected void gameEvents() {
        //do nothing
    }

    @Override
    protected void gameOver() {
        //do nothing
    }

    @Override
    protected void saveGame(String s) {
        //do nothing
    }

    /**
     * Show the mode's menu
     */
    private void showMenu() {
        // Stop the Game
        GAME_VELOCITY = 0;
        handler.setVelocity(0f);

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
     * Hide the mode's menu
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
                GAME_VELOCITY = 2 * DEVICE_RATIO;
                handler.setVelocity(BALL_SPEED, -BALL_SPEED);
                barSprite.setWidth(CAMERA_WIDTH * 0.3f);
                break;
            }

            case NORMAL_MODE: {
                GAME_VELOCITY = 3 * DEVICE_RATIO;
                handler.setVelocity(BALL_SPEED * 2, -BALL_SPEED * 2);
                barSprite.setWidth(CAMERA_WIDTH * 0.2f);
                break;
            }

            case INSANE_MODE: {
                GAME_VELOCITY = 4 * DEVICE_RATIO;
                handler.setVelocity(BALL_SPEED * 4, -BALL_SPEED * 4);
                barSprite.setWidth(CAMERA_WIDTH * 0.1f);
                break;
            }
        }
        hideMenu();
    }

}
