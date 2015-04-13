package it.unina.is2project.sensorgames.pong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // Game events
    private int event;
    private static final int NO_EVENT = 0;
    private static final int FIRST_ENEMY = 1;
    private static final int CUT_30 = 2;
    private static final int CUT_50 = 3;
    private static final int REVERSE = 4;
    private static final int RUSH_HOUR = 5;

    // First enemy
    private Sprite firstEnemy;

    // Rush Hour
    private List<Sprite> rushHour = new ArrayList<>();
    private List<PhysicsHandler> rushHourHandlers = new ArrayList<>();
    private static final int RUSH_HOUR_MIN_NUM = 15;
    private static final int RUSH_HOUR_MAX_NUM = 30;
    private List<Float> oldRushSpeed_x = new ArrayList<>();
    private List<Float> oldRushSpeed_y = new ArrayList<>();

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
                if (event != NO_EVENT)
                    clearEvent();
                Intent intent = new Intent(getBaseContext(), TrainingSettings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        clearGame();

        return scene;
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Setting texture loading
        Drawable settingDrawable = getResources().getDrawable(R.drawable.setting);
        settingTexture = new BitmapTextureAtlas(getTextureManager(), settingDrawable.getIntrinsicWidth(), settingDrawable.getIntrinsicHeight());
        settingTextureRegion = createFromResource(settingTexture, this, R.drawable.setting, 0, 0);
        settingTexture.load();
    }

    @Override
    protected void setBallVeloctity() {
        // Get options by training settings
        Intent i = getIntent();
        int ballSpeed = i.getIntExtra("ballSpeed", 1);
        int barSpeed = i.getIntExtra("barSpeed", 1);
        event = i.getIntExtra("event", 0);

        setTrainingMode(ballSpeed, barSpeed);
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
    protected void pauseGame() {
        super.pauseGame();
        if (event == RUSH_HOUR) {
            for (int i = 0; i < rushHour.size(); i++) {
                oldRushSpeed_x.add(rushHourHandlers.get(i).getVelocityX());
                oldRushSpeed_y.add(rushHourHandlers.get(i).getVelocityY());
                rushHourHandlers.get(i).setVelocity(0);
            }
        }
    }

    @Override
    protected void restartGameAfterPause() {
        super.restartGameAfterPause();
        if (event == RUSH_HOUR) {
            for (int i = 0; i < rushHour.size(); i++) {
                rushHourHandlers.get(i).setVelocity(oldRushSpeed_x.get(i), oldRushSpeed_y.get(i));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!animActive) {
            if (!pause)
                pauseGame();

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.text_ttl_training_leave));
            alert.setMessage(getResources().getString(R.string.text_msg_training_leave));
            alert.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    restartGameAfterPause();
                }
            });
            alert.show();
        }
    }

    @Override
    protected void bluetoothExtra() {
        //do nothing
    }

    @Override
    protected void gameEventsCollisionLogic() {
        switch (event) {
            case FIRST_ENEMY:
                firstEnemyCollisions();
                break;
            case RUSH_HOUR:
                rushHourCollisions();
                break;
        }
    }

    @Override
    protected void addScore() {
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

    private void setTrainingMode(int ball_speed, int bar_speed) {
        // Setting up the ball speed
        handler.setVelocity(0, -ball_speed * BALL_SPEED);
        Log.d(TAG, "Ball Speed Selected: " + ball_speed * BALL_SPEED);

        // Setting up the bar speed
        BAR_SPEED = bar_speed * BAR_SPEED;
        Log.d(TAG, "Bar Speed Selected: " + bar_speed * BAR_SPEED);

        // Setting up the game event
        switch (event) {
            case FIRST_ENEMY:
                firstEnemyLogic();
                break;
            case CUT_30:
                cutBar30Logic();
                break;
            case CUT_50:
                cutBar50Logic();
                break;
            case REVERSE:
                reverseLogic();
                break;
            case RUSH_HOUR:
                rushHourLogic();
                break;
        }
    }

    private void clearEvent() {
        switch (event) {
            case FIRST_ENEMY:
                clearFirstEnemy();
                break;
            case CUT_30:
                clearCutBar30();
                break;
            case CUT_50:
                clearCutBar50();
                break;
            case REVERSE:
                clearReverse();
                break;
            case RUSH_HOUR:
                clearRushHour();
                break;
        }
    }

    private void firstEnemyLogic() {
        firstEnemy = new Sprite(0, CAMERA_HEIGHT / 3, barTextureRegion, getVertexBufferObjectManager());
        firstEnemy.setWidth(CAMERA_WIDTH);
        scene.attachChild(firstEnemy);
    }

    private void clearFirstEnemy() {
        firstEnemy.detachSelf();
    }

    private void cutBar30Logic() {
        barSprite.setWidth(0.21f * CAMERA_WIDTH);
    }

    private void clearCutBar30() {
        barSprite.setWidth(0.3f * CAMERA_WIDTH);
    }

    private void cutBar50Logic() {
        barSprite.setWidth(0.15f * CAMERA_WIDTH);
    }

    private void clearCutBar50() {
        barSprite.setWidth(0.3f * CAMERA_WIDTH);
    }

    private void reverseLogic() {
        BAR_SPEED *= -1;
    }

    private void clearReverse() {
        BAR_SPEED *= -1;
    }

    private void rushHourLogic() {
        Random random = new Random();
        int RUSH_HOUR_NUM = RUSH_HOUR_MIN_NUM + random.nextInt(RUSH_HOUR_MAX_NUM - RUSH_HOUR_MIN_NUM + 1);

        for (int i = 0; i < RUSH_HOUR_NUM; i++) {
            Sprite rush = new Sprite(0, 0, ballTextureRegion, getVertexBufferObjectManager());
            rush.setWidth(CAMERA_WIDTH * 0.1f);
            rush.setHeight(CAMERA_WIDTH * 0.1f);
            rush.setPosition((int) rush.getWidth() + random.nextInt(CAMERA_WIDTH - (int) rush.getWidth() * 4), (int) rush.getHeight() + random.nextInt(CAMERA_HEIGHT - (int) rush.getHeight() * 4));
            rushHour.add(rush);

            PhysicsHandler physicsHandler = new PhysicsHandler(rushHour.get(i));
            physicsHandler.setVelocity(BALL_SPEED * (random.nextFloat() - random.nextFloat()), BALL_SPEED * (random.nextFloat() - random.nextFloat()));
            rushHourHandlers.add(physicsHandler);

            rushHour.get(i).registerUpdateHandler(rushHourHandlers.get(i));

            scene.attachChild(rushHour.get(i));
        }
        Log.d(TAG, "RUSH_HOUR_NUM: " + RUSH_HOUR_NUM + ", rushHour.size(): " + rushHour.size());
    }

    private void clearRushHour() {
        while (rushHour.size() > 0) {
            rushHour.get(0).detachSelf();
            rushHour.remove(0);
            rushHourHandlers.remove(0);
        }
    }

    private void firstEnemyCollisions() {
        if (ballSprite.collidesWith(firstEnemy) && previous_event != TOP) {
            previous_event = TOP;
            handler.setVelocityY(-handler.getVelocityY());
            touch.play();
        }
    }

    private void rushHourCollisions() {
        for (int i = 0; i < rushHour.size(); i++) {
            if (rushHour.get(i).getX() < 0) {
                rushHourHandlers.get(i).setVelocityX(-rushHourHandlers.get(i).getVelocityX());
            }
            if (rushHour.get(i).getX() > CAMERA_WIDTH - (int) ballSprite.getWidth()) {
                rushHourHandlers.get(i).setVelocityX(-rushHourHandlers.get(i).getVelocityX());
            }
            if (rushHour.get(i).getY() < 0) {
                rushHourHandlers.get(i).setVelocityY(-rushHourHandlers.get(i).getVelocityY());
            }
            if (rushHour.get(i).getY() > CAMERA_HEIGHT - (int) ballSprite.getHeight()) {
                rushHourHandlers.get(i).setVelocityY(-rushHourHandlers.get(i).getVelocityY());
            }
        }
    }
}
