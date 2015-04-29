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

    //===========================================
    // DEBUG
    //===========================================
    private final String TAG = "TrainingGame";

    //===========================================
    // GRAPHICS
    //===========================================
    private GameObject setting;     // Setting button
    private Text textHit;           // TextView for hit count
    private Text textEvent;         // TextView or selected event

    //===========================================
    // GAME DATA
    //===========================================
    private int hit_count = 0;                  // Counter of Hit
    private int event;                          // Indicates the current event
    private static final int NO_EVENT = 0;      // ID for no event
    private static final int FIRST_ENEMY = 1;   // ID fot event first enemy
    private static final int CUT_30 = 2;        // ID fot event cut bar 30%
    private static final int CUT_50 = 3;        // ID fot event cut bar 50%
    private static final int REVERSE = 4;       // ID fot event reverse
    private static final int RUSH_HOUR = 5;     // ID fot event rush hour

    int ballSpeed;
    int barSpeed;

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

        // Adding the setting button to the scene
        setting.addToScene(scene, 0.1f, 0.1f);
        setting.setPosition(setting.getDisplaySize().x - setting.getObjectWidth(), 0);
        setting.registerTouch();

        // Setting up the physics of the game
        settingPhysics();

        clearGame();

        return scene;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!pause && !animActive)
            pauseGame();
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        setting = new GameObject(this, R.drawable.setting) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (!animActive) {
                    clearEvent();
                    Intent intent = new Intent(getBaseContext(), TrainingSettings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    intent.putExtra("ballSpeed", ballSpeed);
                    intent.putExtra("barSpeed", barSpeed);
                    intent.putExtra("event", event);

                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void setBallVelocity() {
        // Get options by training settings
        Intent i = getIntent();
//        int ballSpeed = i.getIntExtra("ballSpeed", 1);
//        int barSpeed = i.getIntExtra("barSpeed", 1);
        ballSpeed = i.getIntExtra("ballSpeed", 1);
        barSpeed = i.getIntExtra("barSpeed", 1);
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
                    // do nothing
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
    protected void gameOver() {
        //do nothing
    }

    @Override
    protected void saveGame(String s) {
        //do nothing
    }

    @Override
    protected void addScore() {
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