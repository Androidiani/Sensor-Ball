package it.unina.is2project.sensorgames.pong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.game.entity.GameObject;
import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatOnePlayer;

public class GamePongOnePlayer extends GamePong {

    private final String TAG = "1PlayerGame";

    /**
     * Graphics
     */
    // Text View
    private Text textScore;
    private Text textLvl;
    private Text textEvnt;

    // Life
    private int theme_star;
    private GameObject lifeStar;
    private List<GameObject> lifeStars = new ArrayList<>();

    // Bonus ball
    private BubbleBonus bubble;

    // Life bonus
    private LifeBonus lifeBonus;

    /**
     * Game Data
     */
    private int score = 0;
    private int gain = 0;
    private int level = 1;
    private int oldLevel = 0;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE - 1;
    private int reach_count = 1;
    private static final int MIN_REACH_COUNT = 2;
    private static final int MAX_REACH_COUNT = 6;

    /**
     * Event
     */
    private int game_event;

    /*
    * BONUS
    */
    private static final int NUM_BONUS = 10;
    private static final int NO_EVENT = 0;
    private static final int FIRST_ENEMY = 1;
    private static final int BUBBLE_BONUS = 2;
    private static final int CUT_BAR_30 = 3;
    private static final int LIFE_BONUS = 4;
    private static final int CUT_BAR_50 = 5;
    private static final int BIG_BAR = 6;
    private static final int REVERSE = 7;
    private static final int FREEZE = 8;
    private static final int RUSH_HOUR = 9;

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        // Adding the scoring text to the scene
        textScore = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textScore);
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);

        // Adding the level text to the scene
        textLvl = new Text(10, textScore.getY() + textScore.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textLvl);

        // Adding the level text to the scene
        textEvnt = new Text(10, textLvl.getY() + textLvl.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textEvnt);

        // Adding the life sprites to the scene
        for (int i = 1; i <= life + 1; i++) {
            GameObject lifeTemp = new GameObject(lifeStar);
            lifeTemp.addToScene(scene, 0.05f, 0.05f);
            lifeTemp.setPosition(CAMERA_WIDTH - i * lifeTemp.getObjectWidth(), 0);
            lifeStars.add(lifeTemp);
        }

        // Setting up the physics of the game
        settingPhysics();
        clearGame();
        return scene;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!pause && !animActive)
            pauseGame();
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        switch (theme) {
            case CLASSIC:
                theme_star = R.drawable.star_white;
                break;
            case GOLD:
                theme_star = R.drawable.star_gold;
                break;
            case BLUE:
                theme_star = R.drawable.star_blue;
                break;
        }

        // Life texture loading
        lifeStar = new GameObject(this, theme_star);
        // Life Bonus
        lifeBonus = new LifeBonus(this, theme_star, ball);
        // Bonus ball loading
        bubble = new BubbleBonus(this, ball);
    }

    @Override
    protected void collidesBottom() {
        super.collidesBottom();

        // Decrement and Detach Life
        Log.d(TAG, "Life: " + life);
        lifeStars.get(life).detach();
        life--;
        // Clear current event
        clearEvent();
        Log.d(TAG, "Event Cleared");
        // Setting NO EVENT for 1 reach_count
        game_event = NO_EVENT;
        gameEvent();
        reach_count = 1;
        // Game Over section
        if (life < 0) {
            Log.d(TAG, "Game Over");
            gameOver();
        }
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();

        // Score section
        addScore();
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);
        Log.d(TAG, "Score: " + score);
        changeSpeedByLevel();

        // Game events section
        reach_count--;
        Log.d(TAG, "Reach count " + reach_count);
        if (reach_count == 0) {
            clearEvent();
            Log.d(TAG, "Event Cleared");
            callEvent();
            Log.d(TAG, "New Game Event: " + game_event);
            gameEvent();
            // Generating a new reach count
            Random random = new Random();
            reach_count = MIN_REACH_COUNT + random.nextInt(MAX_REACH_COUNT - MIN_REACH_COUNT + 1);
            Log.d(TAG, "New Reach Count: " + reach_count);
        }
    }

    @Override
    protected void clearGame() {
        super.clearGame();
        // Clear game data
        life = MAX_LIFE - 1;
        score = 0;
        gain = 0;
        level = 1;
        game_event = NO_EVENT;
        reach_count = 1;
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);
    }

    @Override
    public void onBackPressed() {
        if (!animActive) {
            if (!pause)
                pauseGame();

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.text_ttl_oneplayer_leavegame));
            alert.setMessage(getResources().getString(R.string.text_msg_oneplayer_leavegame));
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
        switch (game_event) {
            case FIRST_ENEMY:
                previous_event = firstEnemy.collision(previous_event, touch);
                break;
            case BUBBLE_BONUS:
                score = bubble.collision(score, level, textScore);
                break;
            case LIFE_BONUS:
                life = lifeBonus.collision(life, lifeStars);
                break;
            case RUSH_HOUR:
                rushHour.collision();
                break;
        }
    }

    @Override
    public void addScore() {
        gain = getLevel() * 10;
        score += gain;
        if (game_event == FIRST_ENEMY)
            score += gain * 2;
        if (game_event == CUT_BAR_30)
            score += gain * 4;
        if (game_event == CUT_BAR_50)
            score += gain * 8;
        if (game_event == REVERSE)
            score += gain * 10;
        if (game_event == RUSH_HOUR)
            score += gain * 12;
    }

    protected int getLevel() {
        float a = 0.02567f;
        float b = 1;
        level = (int) Math.round((Math.log(a * score + 1) + b));
        Log.d("LEVEL", "livello : " + level);
        textLvl.setText(getResources().getString(R.string.text_lvl) + " " + level);
        return level;
    }

    private void changeSpeedByLevel() {
        if (oldLevel != getLevel() && getLevel() % 3 == 0) {
            oldLevel = getLevel();
            bar.setBarSpeed(1.5f * bar.getBarSpeed());
            ball.setHandlerSpeed(1.5f * ball.getHandlerSpeedX(), 1.5f * ball.getHandlerSpeedY());
            Log.d(TAG, "Speed changed.");
        }
    }

    @Override
    protected void gameOver() {
        ball.setHandlerSpeed(0f, 0f);
        bar.setBarSpeed(0f);
        textEvnt.setText(getResources().getString(R.string.text_gameover));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Game over dialog

                AlertDialog.Builder alert = new AlertDialog.Builder(GamePongOnePlayer.this);
                alert.setTitle(getResources().getString(R.string.text_ttl_oneplayer_savegame));
                alert.setMessage(getResources().getString(R.string.text_msg_oneplayer_savegame));

                alert.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String user_input_name = sharedPreferences.getString(Constants.PREF_NICKNAME, getString(R.string.txt_no_name));

                        saveGame(user_input_name);
                        finish();
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });

                alert.show();
            }
        });
    }

    @Override
    protected void saveGame(String user_input_name) {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player player = playerDAO.findByNome(user_input_name);
        long idPlayer;

        if (player == null) {
            player = new Player(user_input_name);
            idPlayer = playerDAO.insert(player);
        } else idPlayer = player.getId();

        Calendar calendar = Calendar.getInstance();
        String date = "" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + ((calendar.get(Calendar.MONTH) < 9 ? "0" : "")) + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, date, score);
        StatOnePlayerDAO statOnePlayerDAO = new StatOnePlayerDAO(getApplicationContext());

        statOnePlayerDAO.insert(statOnePlayer);

        playerDAO.close();
        statOnePlayerDAO.close();
    }

    private void gameEvent() {
        switch (game_event) {
            case NO_EVENT:
                textEvnt.setText("");
                break;
            case FIRST_ENEMY:
                textEvnt.setText(getResources().getString(R.string.text_first_enemy));
                firstEnemy.addToScene(scene);
                break;
            case BUBBLE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_bubble));
                bubble.addToScene(scene);
                break;
            case CUT_BAR_30:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_30));
                bar.setObjectWidth(0.7f * bar.getBarWidth());
                break;
            case LIFE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_lifebonus));
                lifeBonus.addToScene(scene, life);
                break;
            case CUT_BAR_50:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_50));
                bar.setObjectWidth(0.5f * bar.getBarWidth());
                break;
            case BIG_BAR:
                textEvnt.setText(getResources().getString(R.string.text_big_bar));
                bar.setObjectWidth(1.5f * bar.getBarWidth());
                break;
            case REVERSE:
                textEvnt.setText(getResources().getString(R.string.text_reverse));
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case FREEZE:
                textEvnt.setText(getResources().getString(R.string.text_freeze));
                ball.setHandlerSpeed(ball.getHandlerSpeedX() / 2, ball.getHandlerSpeedY() / 2);
                break;
            case RUSH_HOUR:
                textEvnt.setText(getResources().getString(R.string.text_rush));
                rushHour.addToScene(scene);
                break;
        }
    }

    private void clearEvent() {
        switch (game_event) {
            case NO_EVENT:
                break;
            case FIRST_ENEMY:
                firstEnemy.clear();
                break;
            case BUBBLE_BONUS:
                bubble.clear();
                break;
            case CUT_BAR_30:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case LIFE_BONUS:
                lifeBonus.clear();
                break;
            case CUT_BAR_50:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case BIG_BAR:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case REVERSE:
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case FREEZE:
                ball.setHandlerSpeed(ball.getHandlerSpeedX() * 2, ball.getHandlerSpeedY() * 2);
                break;
            case RUSH_HOUR:
                rushHour.clear();
                break;
        }
    }

    private void callEvent() {
        // Generating a new event, different from current event
        Random random = new Random();
        int random_int;
        do {
            random_int = random.nextInt(level) % NUM_BONUS;
        }
        while ((random_int == game_event && level > 1) || (random_int == LIFE_BONUS && life == MAX_LIFE - 1));
        game_event = random_int;
    }
}
