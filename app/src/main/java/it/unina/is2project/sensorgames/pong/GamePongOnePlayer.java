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
    private GameObject lifeStar;
    private List<GameObject> lifeStars = new ArrayList<>();

    // Bonus ball
    private GameObject bonusBall;
    private List<GameObject> bonusBalls = new ArrayList<>();

    // Life bonus
    private GameObject lifeBonus;

    /**
     * Game data
     */
    private long score = 0;
    private int gain;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE - 1;
    private int old_life = 0;
    private int reach_count = 1;
    private static final int MIN_REACH_COUNT = 2;
    private static final int MAX_REACH_COUNT = 6;

    // Levels
    private int level;
    private boolean level_one = true;
    private static final int LEVEL_ONE = 0;
    private static final int BARRIER_ONE = 100;
    private boolean level_two = false;
    private static final int LEVEL_TWO = 1;
    private static final int BARRIER_TWO = 200;
    private boolean level_three = false;
    private static final int LEVEL_THREE = 2;
    private static final int BARRIER_THREE = 500;
    private boolean level_four = false;
    private static final int LEVEL_FOUR = 3;
    private static final int BARRIER_FOUR = 1000;
    private boolean level_five = false;
    private static final int LEVEL_FIVE = 4;
    private static final int BARRIER_FIVE = 2000;
    private boolean level_six = false;
    private static final int LEVEL_SIX = 5;
    private static final int BARRIER_SIX = 5000;
    private boolean level_seven = false;
    private static final int LEVEL_SEVEN = 6;
    private static final int BARRIER_SEVEN = 10000;
    private boolean level_eight = false;
    private static final int LEVEL_EIGHT = 7;
    private static final int BARRIER_EIGHT = 20000;
    private boolean level_nine = false;
    private static final int LEVEL_NINE = 8;
    private static final int BARRIER_NINE = 50000;
    private boolean level_ten = false;
    private static final int LEVEL_TEN = 9;
    private static final int BARRIER_TEN = 100000;
    private boolean level_eleven = false;
    private static final int LEVEL_ELEVEN = 10;
    private static final int BARRIER_ELEVEN = 200000;
    private boolean level_twelve = false;
    private static final int LEVEL_TWELVE = 11;
    private static final int BARRIER_TWELVE = 500000;
    private boolean level_max = false;
    private static final int LEVEL_MAX = 12;

    /**
     * Events
     */
    private int game_event;

    // Events' enable
    private boolean bubble_bonus = false;
    private boolean life_bonus = false;
    private boolean big_bar = false;
    private boolean freeze = false;

    // Events' number
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

    // Events' data
    private static final int BONUS_BALL_MAX_NUM = 5;
    private static final int BONUS_BALL_MIN_NUM = 3;
    private boolean life_detached = false;
    private boolean allBonusDetached = false;

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
        textLvl.setText(getResources().getString(R.string.text_lv1));

        // Adding the level text to the scene
        textEvnt = new Text(10, textLvl.getY() + textLvl.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textEvnt);

        // Adding the life sprites to the scene
        for (int i = 1; i <= life + 1; i++) {
            lifeStar.addToScene(scene, 0.05f);
            lifeStar.getSprite().setHeight(CAMERA_WIDTH * 0.05f);
            lifeStar.setPosition(CAMERA_WIDTH - i * lifeStar.getObjectWidth(), 0);
            lifeStars.add(lifeStar);
        }

        // Setting up the physics of the game
        settingPhysics();

        clearGame();

        return scene;
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Life texture loading
        lifeStar = new GameObject(this, R.drawable.life);
        lifeBonus = new GameObject(this, R.drawable.life);

        // Bonus ball loading
//        bonusBall = new GameObject(this, R.drawable.ball_petrol);
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

        // Game levels section
        gameLevels();
        Log.d(TAG, "Level " + level);

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
        old_life = 0;
        score = 0;
        gain = 0;
        level = LEVEL_ONE;
        game_event = NO_EVENT;
        reach_count = 1;
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);
        textLvl.setText(getResources().getString(R.string.text_lv1));
        // Setting false all events
        no_event = false;
        first_enemy = false;
        bubble_bonus = false;
        cut_bar_30 = false;
        life_bonus = false;
        cut_bar_50 = false;
        big_bar = false;
        reverse = false;
        freeze = false;
        rush_hour = false;
        // Setting true level one
        level_one = true;
        // Setting false other levels
        level_two = false;
        level_three = false;
        level_four = false;
        level_five = false;
        level_six = false;
        level_seven = false;
        level_eight = false;
        level_nine = false;
        level_ten = false;
        level_eleven = false;
        level_twelve = false;
        level_max = false;
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
                firstEnemyCollisions();
                break;
            case BUBBLE_BONUS:
                bubbleBonusCollisions();
                break;
            case LIFE_BONUS:
                lifeBonusCollisions();
                break;
            case RUSH_HOUR:
                rushHourCollisions();
                break;
        }
    }

    @Override
    public void addScore() {
        if (score >= 0 && score < BARRIER_ONE && level_one) {
            score += 10;
            gain = 10;
        } else if (score >= BARRIER_ONE && score < BARRIER_TWO && level_two) {
            score += 15;
            gain = 15;
        } else if (score >= BARRIER_TWO && score < BARRIER_THREE && level_three) {
            score += 20;
            gain = 20;
        } else if (score >= BARRIER_THREE && score < BARRIER_FOUR && level_four) {
            score += 30;
            gain = 30;
        } else if (score >= BARRIER_FOUR && score < BARRIER_FIVE && level_five) {
            score += 40;
            gain = 40;
        } else if (score >= BARRIER_FIVE && score < BARRIER_SIX && level_six) {
            score += 50;
            gain = 50;
        } else if (score >= BARRIER_SIX && score < BARRIER_SEVEN && level_seven) {
            score += 100;
            gain = 100;
        } else if (score >= BARRIER_SEVEN && score < BARRIER_EIGHT && level_eight) {
            score += 200;
            gain = 200;
        } else if (score >= BARRIER_EIGHT && score < BARRIER_NINE && level_nine) {
            score += 300;
            gain = 300;
        } else if (score >= BARRIER_NINE && score < BARRIER_TEN && level_ten) {
            score += 600;
            gain = 600;
        } else if (score >= BARRIER_TEN && score < BARRIER_ELEVEN && level_eleven) {
            score += 1000;
            gain = 1000;
        } else if (score >= BARRIER_ELEVEN && score < BARRIER_TWELVE && level_twelve) {
            score += 2000;
            gain = 2000;
        } else if (score >= BARRIER_TWELVE && level_max) {
            score += 3000;
            gain = 3000;
        }

        if (game_event == FIRST_ENEMY)
            score += gain * 2;
        if (game_event == CUT_BAR_30)
            score += gain * 4;
        if (game_event == CUT_BAR_50)
            score += gain * 6;
        if (game_event == REVERSE)
            score += gain * 8;
        if (game_event == RUSH_HOUR)
            score += gain * 10;
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

    private void gameLevels() {
        if (score >= 0 && score < BARRIER_ONE && level_one) {
            level = LEVEL_ONE;
            level_one = true;
            textLvl.setText(getResources().getString(R.string.text_lv1));
        } else if (score >= BARRIER_ONE && score < BARRIER_TWO && !level_two) {
            level = LEVEL_TWO;
            level_two = true;
            textLvl.setText(getResources().getString(R.string.text_lv2));
        } else if (score >= BARRIER_TWO && score < BARRIER_THREE && !level_three) {
            level = LEVEL_THREE;
            level_three = true;
            textLvl.setText(getResources().getString(R.string.text_lv3));
            bar.setBarSpeed(1.5f * bar.getBarSpeed());
            ball.setHandlerSpeed(1.5f * ball.getHandlerSpeedX(), 1.5f * ball.getHandlerSpeedY());
        } else if (score >= BARRIER_THREE && score < BARRIER_FOUR && !level_four) {
            level = LEVEL_FOUR;
            level_four = true;
            textLvl.setText(getResources().getString(R.string.text_lv4));
        } else if (score >= BARRIER_FOUR && score < BARRIER_FIVE && !level_five) {
            level = LEVEL_FIVE;
            level_five = true;
            textLvl.setText(getResources().getString(R.string.text_lv5));
        } else if (score >= BARRIER_FIVE && score < BARRIER_SIX && !level_six) {
            level = LEVEL_SIX;
            level_six = true;
            textLvl.setText(getResources().getString(R.string.text_lv6));
            ball.getHandler().setVelocity(ball.getHandler().getVelocityX() * 1.5f, ball.getHandler().getVelocityY() * 1.5f);
        } else if (score >= BARRIER_SIX && score < BARRIER_SEVEN && !level_seven) {
            level = LEVEL_SEVEN;
            level_seven = true;
            textLvl.setText(getResources().getString(R.string.text_lv7));
        } else if (score >= BARRIER_SEVEN && score < BARRIER_EIGHT && !level_eight) {
            level = LEVEL_EIGHT;
            level_eight = true;
            textLvl.setText(getResources().getString(R.string.text_lv8));
        } else if (score >= BARRIER_EIGHT && score < BARRIER_NINE && !level_nine) {
            level = LEVEL_NINE;
            level_nine = true;
            textLvl.setText(getResources().getString(R.string.text_lv9));
            BAR_SPEED *= 1.5;
            ball.getHandler().setVelocity(ball.getHandler().getVelocityX() * 1.5f, ball.getHandler().getVelocityY() * 1.5f);
        } else if (score >= BARRIER_NINE && score < BARRIER_TEN && !level_ten) {
            level = LEVEL_TEN;
            level_ten = true;
            textLvl.setText(getResources().getString(R.string.text_lv10));
        } else if (score >= BARRIER_TEN && score < BARRIER_ELEVEN && !level_eleven) {
            level = LEVEL_ELEVEN;
            level_eleven = true;
            textLvl.setText(getResources().getString(R.string.text_lv11));
        } else if (score >= BARRIER_ELEVEN && score < BARRIER_TWELVE && !level_twelve) {
            level = LEVEL_TWELVE;
            level_twelve = true;
            textLvl.setText(getResources().getString(R.string.text_lv12));
        } else if (score >= BARRIER_TWELVE && !level_max) {
            level = LEVEL_MAX;
            level_max = true;
            textLvl.setText(getResources().getString(R.string.text_lv13));
            BAR_SPEED *= 1.5;
            ball.getHandler().setVelocity(ball.getHandler().getVelocityX() * 1.5f, ball.getHandler().getVelocityY() * 1.5f);
        }
    }

    private void gameEvent() {
        switch (game_event) {
            case NO_EVENT:
                textEvnt.setText("");
                no_event = true;
                break;
            case FIRST_ENEMY:
                textEvnt.setText(getResources().getString(R.string.text_first_enemy));
                first_enemy = true;
                firstEnemyLogic();
                break;
            case BUBBLE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_bubble));
                bubble_bonus = true;
                bubbleBonusLogic();
                break;
            case CUT_BAR_30:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_30));
                cut_bar_30 = true;
                cutBar30Logic();
                break;
            case LIFE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_lifebonus));
                life_bonus = true;
                lifeBonusLogic();
                break;
            case CUT_BAR_50:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_50));
                cut_bar_50 = true;
                cutBar50Logic();
                break;
            case BIG_BAR:
                textEvnt.setText(getResources().getString(R.string.text_big_bar));
                big_bar = true;
                bigBarLogic();
                break;
            case REVERSE:
                textEvnt.setText(getResources().getString(R.string.text_reverse));
                reverse = true;
                reverseLogic();
                break;
            case FREEZE:
                textEvnt.setText(getResources().getString(R.string.text_freeze));
                freeze = true;
                freezeLogic();
                break;
            case RUSH_HOUR:
                textEvnt.setText(getResources().getString(R.string.text_rush));
                rush_hour = true;
                rushHourLogic();
                break;
        }
    }

    private void clearEvent() {
        switch (game_event) {
            case NO_EVENT:
                no_event = false;
                break;
            case FIRST_ENEMY:
                clearFirstEnemy();
                first_enemy = false;
                break;
            case BUBBLE_BONUS:
                clearBubbleBonus();
                bubble_bonus = false;
                break;
            case CUT_BAR_30:
                clearCutBar30();
                cut_bar_30 = false;
                break;
            case LIFE_BONUS:
                clearLifeBonus();
                life_bonus = false;
                break;
            case CUT_BAR_50:
                clearCutBar50();
                cut_bar_50 = false;
                break;
            case BIG_BAR:
                clearBigBar();
                big_bar = false;
                break;
            case REVERSE:
                clearReverse();
                reverse = false;
                break;
            case FREEZE:
                clearFreeze();
                freeze = false;
                break;
            case RUSH_HOUR:
                clearRushHour();
                rush_hour = false;
                break;
        }
    }

    private void callEvent() {
//        // Generating a new event, different from current event
//        Random random = new Random();
//        int random_int;
//        do {
//            random_int = random.nextInt(level + 1);
//        }
//        while ((random_int == game_event && level > LEVEL_ONE) || (random_int == LIFE_BONUS && life == MAX_LIFE - 1) || (random_int == 10) || (random_int == 11) || (random_int == 12));
//        game_event = random_int;
        game_event = NO_EVENT;
    }

    private void bubbleBonusLogic() {
        Random random = new Random();
        int BONUS_BALL_NUM = BONUS_BALL_MIN_NUM + random.nextInt(BONUS_BALL_MAX_NUM - BONUS_BALL_MIN_NUM + 1);

        // Adding the bonus ball sprites to the scene
        for (int i = 0; i < BONUS_BALL_NUM; i++) {
            bonusBall = new GameObject(this, R.drawable.ball_petrol);
            bonusBall.addToScene(scene, 0.1f);
            bonusBall.getSprite().setHeight(CAMERA_WIDTH * 0.1f);
            bonusBall.setPosition(bonusBall.getObjectWidth() + random.nextInt(CAMERA_WIDTH - (bonusBall.getObjectWidth() * 2)), (bonusBall.getObjectHeight() * 2) * (i + 1));
            bonusBalls.add(bonusBall);
        }
        Log.d(TAG, "BONUS_BALL_NUM: " + BONUS_BALL_NUM + " bonusBalls.size(): " + bonusBalls.size());
    }

    private void clearBubbleBonus() {
        if (!allBonusDetached) {
            while (bonusBalls.size() > 0) {
                bonusBalls.get(0).detach();
                bonusBalls.remove(0);
            }
        }
        allBonusDetached = false;
    }

    private void lifeBonusLogic() {
        old_life = life;
        lifeBonus.addToScene(scene, 0.1f);
        lifeBonus.getSprite().setHeight(CAMERA_WIDTH * 0.1f);
        lifeBonus.setRandomPosition();
    }

    private void clearLifeBonus() {
        if (!life_detached)
            lifeBonus.detach();
        life_detached = false;
    }

    private void bigBarLogic() {
        bar.getSprite().setWidth(0.45f * CAMERA_WIDTH);
    }

    private void clearBigBar() {
        bar.getSprite().setWidth(0.3f * CAMERA_WIDTH);
    }

    private void freezeLogic() {
        ball.getHandler().setVelocity(ball.getHandler().getVelocityX() / 2, ball.getHandler().getVelocityY() / 2);
    }

    private void clearFreeze() {
        ball.getHandler().setVelocity(ball.getHandler().getVelocityX() * 2, ball.getHandler().getVelocityY() * 2);
    }

    private void bubbleBonusCollisions() {
        for (int i = 0; i < bonusBalls.size(); i++) {
            if (ball.getSprite().collidesWith(bonusBalls.get(i).getSprite())) {
                Log.d(TAG, "Bonus Ball " + i + " removed");
//                bonusBalls.get(i).detachSelf();
                bonusBalls.get(i).detach();
                bonusBalls.remove(i);
                score += 20 * (level + 1);
                textScore.setText(getResources().getString(R.string.text_score) + ": " + score);
                gameLevels();
                if (bonusBalls.size() == 0) {
                    allBonusDetached = true;
                    Log.d(TAG, "All bonus ball detached by player");
                }
            }
        }
    }

    private void lifeBonusCollisions() {
        if (ball.getSprite().collidesWith(lifeBonus.getSprite()) && old_life == life) {
            lifeBonus.detach();
            life++;
            lifeStars.get(life).attach();
            life_detached = true;
            Log.d(TAG, "Life Collision. Current Life: " + life);
        }
    }
}
