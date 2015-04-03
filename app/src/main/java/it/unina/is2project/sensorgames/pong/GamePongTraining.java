package it.unina.is2project.sensorgames.pong;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public class GamePongTraining extends GamePong {

    private final String TAG = "TrainingGame";

    // Setting button
    private BitmapTextureAtlas settingTexture;
    private ITextureRegion settingTextureRegion;
    private Sprite settingSprite;

    // Easy mode button
    private BitmapTextureAtlas easyTexture;
    private ITextureRegion easyTextureRegion;
    private Sprite easySprite;

    // Normal mode button
    private BitmapTextureAtlas normalTexture;
    private ITextureRegion normalTextureRegion;
    private Sprite normalSprite;

    // Insane mode button
    private BitmapTextureAtlas insaneTexture;
    private ITextureRegion insaneTextureRegion;
    private Sprite insaneSprite;

    // Text info
    private Text textHit;
    private Text textEvent;

    // Game's mode
    private int hit_count = 0;
    private static final int EASY_MODE = 0;
    private static final int NORMAL_MODE = 1;
    private static final int INSANE_MODE = 2;

    // Events
    private boolean enableModes = false;

    // Pause utils
    private static final int PAUSE = -1;
    private float old_x_speed;
    private float old_y_speed;
    private int old_game_speed;
    private long firstTap;
    private long secondTap;

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        float spriteRate = easyTextureRegion.getHeight() / easyTextureRegion.getWidth();

        // Adding the textHit to the scene
        textHit = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textHit);
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);

        // Adding the textEvnt to the scene
        textEvent = new Text(10, textHit.getY() + textHit.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textEvent);

        // Adding the settingSprite to the scene
        settingSprite = new Sprite(0, 0, settingTextureRegion, getVertexBufferObjectManager());
        settingSprite.setWidth(CAMERA_WIDTH * 0.1f);
        settingSprite.setHeight(CAMERA_WIDTH * 0.1f);
        settingSprite.setX(CAMERA_WIDTH - settingSprite.getWidth());
        scene.attachChild(settingSprite);

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

        // Setting
        Drawable settingDrawable = getResources().getDrawable(R.drawable.setting);
        settingTexture = new BitmapTextureAtlas(getTextureManager(), settingDrawable.getIntrinsicWidth(), settingDrawable.getIntrinsicHeight());
        settingTextureRegion = createFromResource(settingTexture, this, R.drawable.setting, 0, 0);
        settingTexture.load();

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
    protected void collidesBottom() {
        super.collidesBottom();
        hit_count = 0;
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();
        hit_count++;
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);
    }

    @Override
    protected void actionDownEvent(float x, float y) {
        if (!pause) {
            pauseGame();
            if (checkTouchOnSprite(x, y) && !enableModes) {
                showMenu();
            }
        }
        if (pause && (System.currentTimeMillis() - firstTap > 500)) {
            restartGameAfterPause();
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

    @Override
    protected void onStop() {
        super.onStop();
        pauseGame();
    }

    private void pauseGame() {
        Log.d(TAG, "Game Paused");
        textEvent.setText(getResources().getString(R.string.text_pause));
        // Saving Game Data
        old_x_speed = handler.getVelocityX();
        old_y_speed = handler.getVelocityY();
        old_game_speed = GAME_VELOCITY;
        // Stop the Game
        handler.setVelocity(0);
        GAME_VELOCITY = 0;
        firstTap = System.currentTimeMillis();
        previous_event = PAUSE;
        touch.stop();
        pause = true;
    }

    private void restartGameAfterPause() {
        textEvent.setText("");
        handler.setVelocity(old_x_speed, old_y_speed);
        GAME_VELOCITY = old_game_speed;
        pause = false;
    }

    private boolean checkTouchOnSprite(float x, float y) {
        boolean checkTouchSpriteStatus = false;
        if (x <= settingSprite.getX() + settingSprite.getWidth() && x >= settingSprite.getX() && y >= settingSprite.getY() && y <= settingSprite.getY() + settingSprite.getHeight())
            checkTouchSpriteStatus = true;
        return checkTouchSpriteStatus;
    }

    /**
     * Show the mode's menu
     */
    private void showMenu() {
        // Register Touch Area
        scene.registerTouchArea(easySprite);
        scene.registerTouchArea(normalSprite);
        scene.registerTouchArea(insaneSprite);

        // Attach the menu's children
        scene.attachChild(easySprite);
        scene.attachChild(normalSprite);
        scene.attachChild(insaneSprite);

        enableModes = true;
    }

    /**
     * Hide the mode's menu
     */
    private void hideMenu() {
        // Unregister Touch Area
        scene.unregisterTouchArea(easySprite);
        scene.unregisterTouchArea(normalSprite);
        scene.unregisterTouchArea(insaneSprite);

        // Detach menu's children
        scene.detachChild(easySprite);
        scene.detachChild(normalSprite);
        scene.detachChild(insaneSprite);

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
                barSprite.setWidth(CAMERA_WIDTH * 0.21f);
                break;
            }

            case INSANE_MODE: {
                GAME_VELOCITY = 4 * DEVICE_RATIO;
                handler.setVelocity(BALL_SPEED * 4, -BALL_SPEED * 4);
                barSprite.setWidth(CAMERA_WIDTH * 0.15f);
                break;
            }
        }
        hideMenu();
    }

}
