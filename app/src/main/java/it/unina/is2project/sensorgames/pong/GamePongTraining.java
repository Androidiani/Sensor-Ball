package it.unina.is2project.sensorgames.pong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.game.entity.GameObject;

public class GamePongTraining extends GamePong {

    private final String TAG = "TrainingGame";

    /**
     * Graphics
     */
    // Setting button
    private GameObject setting;

    // Text
    private Text textHit;
    private Text textEvent;

    /**
     * Game Data
     */
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

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        // Adding the textHit to the scene
        textHit = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textHit);
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);

        // Adding the textEvent to the scene
        textEvent = new Text(10, textHit.getY() + textHit.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textEvent);

        // Adding the settingSprite to the scene
        setting.addToScene(scene, 0.1f, 0.1f);
        setting.setPosition(CAMERA_WIDTH - setting.getObjectWidth(), 0);
        scene.registerTouchArea(setting.getSprite());

        // Setting up the physics of the game
        settingPhysics();

        clearGame();

        return scene;
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        setting = new GameObject(this, R.drawable.setting) {
            @Override
            public void onTouch() {
                super.onTouch();
                clearEvent();
                Intent intent = new Intent(getBaseContext(), TrainingSettings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
    }

    @Override
    protected void setBallVelocity() {
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
        Log.d(TAG, "Hit: " + hit_count);
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();
        hit_count++;
        textHit.setText(getResources().getString(R.string.text_hit) + ": " + hit_count);
        Log.d(TAG, "Hit: " + hit_count);
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
                previous_event = firstEnemy.collision(previous_event, touch);
                break;
            case RUSH_HOUR:
                rushHour.collision();
                break;
        }
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
    protected void gameOver() {
        //do nothing
    }

    @Override
    protected void saveGame(String s) {
        //do nothing
    }

    private void setTrainingMode(int ball_speed, int bar_speed) {
        // Setting up the ball speed
        ball.setHandlerSpeed(0, -ball_speed * ball.getBallSpeed());
        Log.d(TAG, "Ball Speed Selected: " + ball_speed * ball.getBallSpeed());

        // Setting up the bar speed
        bar.setBarSpeed(bar_speed * bar.getBarSpeed());
        Log.d(TAG, "Bar Speed Selected: " + bar_speed * bar.getBarSpeed());

        // Setting up the game event
        gameEvent();
    }

    private void gameEvent() {
        switch (event) {
            case NO_EVENT:
                textEvent.setText("");
                break;
            case FIRST_ENEMY:
                textEvent.setText(getResources().getString(R.string.text_first_enemy));
                firstEnemy.addToScene(scene);
                break;
            case CUT_30:
                textEvent.setText(getResources().getString(R.string.text_cut_bar_30));
                bar.setObjectWidth(0.7f * bar.getBarWidth());
                break;
            case CUT_50:
                textEvent.setText(getResources().getString(R.string.text_cut_bar_50));
                bar.setObjectWidth(0.5f * bar.getBarWidth());
                break;
            case REVERSE:
                textEvent.setText(getResources().getString(R.string.text_reverse));
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case RUSH_HOUR:
                textEvent.setText(getResources().getString(R.string.text_rush));
                rushHour.addToScene(scene);
                break;
        }
    }

    private void clearEvent() {
        switch (event) {
            case NO_EVENT:
                break;
            case FIRST_ENEMY:
                firstEnemy.clear();
                break;
            case CUT_30:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case CUT_50:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case REVERSE:
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case RUSH_HOUR:
                rushHour.clear();
                break;
        }
    }
}