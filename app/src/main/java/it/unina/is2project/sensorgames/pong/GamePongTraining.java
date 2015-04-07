package it.unina.is2project.sensorgames.pong;

import android.content.Intent;
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

    // Text Hit
    private Text textHit;

    // Hit count
    private int hit_count = 0;

    // Events
    private long secondTap;

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        // Adding the textHit to the scene
        textHit = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textHit);
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);

        // Adding the settingSprite to the scene
        settingSprite = new Sprite(0, 0, settingTextureRegion, getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Intent intent = new Intent(getBaseContext(), TrainingSettings.class);
                startActivity(intent);
                finish();
                return true;
            }
        };
        settingSprite.setWidth(CAMERA_WIDTH * 0.1f);
        settingSprite.setHeight(CAMERA_WIDTH * 0.1f);
        settingSprite.setX(CAMERA_WIDTH - settingSprite.getWidth());
        scene.registerTouchArea(settingSprite);
        scene.attachChild(settingSprite);

        // Setting up the physics of the game
        settingPhysics();

        // Get options by training settings
        Intent i = getIntent();
        int ballSpeed = i.getIntExtra("ballspeed", 1);
        int barSpeed = i.getIntExtra("barspeed", 1);
        int event = i.getIntExtra("event", 0);

        setTrainingMode(ballSpeed, barSpeed, event);

        return scene;
    }

    private void setTrainingMode(int ball_speed, int bar_speed, int event){
        handler.setVelocity(ball_speed * BALL_SPEED, -ball_speed * BALL_SPEED);
        GAME_VELOCITY = bar_speed * GAME_VELOCITY;
        //TODO logica con event una volta creata la classe dei bonus
    }


    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Setting
        Drawable settingDrawable = getResources().getDrawable(R.drawable.setting);
        settingTexture = new BitmapTextureAtlas(getTextureManager(), settingDrawable.getIntrinsicWidth(), settingDrawable.getIntrinsicHeight());
        settingTextureRegion = createFromResource(settingTexture, this, R.drawable.setting, 0, 0);
        settingTexture.load();
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
        }
        if (pause && (System.currentTimeMillis() - firstTap > 500) && !checkTouchOnSettingSprite(x, y)) {
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

    private boolean checkTouchOnSettingSprite(float x, float y) {
        boolean checkTouchSpriteStatus = false;
        if (x <= settingSprite.getX() + settingSprite.getWidth() && x >= settingSprite.getX() && y >= settingSprite.getY() && y <= settingSprite.getY() + settingSprite.getHeight())
            checkTouchSpriteStatus = true;
        return checkTouchSpriteStatus;
    }

}
