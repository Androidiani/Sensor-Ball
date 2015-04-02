package it.unina.is2project.sensorgames.pong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatOnePlayer;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public class GamePongOnePlayer extends GamePong {

    private final String TAG = "1PlayerGame";

    /**
     * Graphics
     */
    // Text View
    private Text txtScore;
    private Text txtLvl;
    private Text txtEvnt;

    // Life
    private BitmapTextureAtlas lifeTexture;
    private ITextureRegion lifeTextureRegion;
    private List<Sprite> lifeSprites = new ArrayList<>();

    // First enemy
    private Sprite firstEnemy;

    // Bonus ball
    private BitmapTextureAtlas bonusBallTexture;
    private ITextureRegion bonusBallTextureRegion;
    private List<Sprite> bonusBalls = new ArrayList<>();

    // Life bonus
    private Sprite lifeBonus;

    // Rush Hour
    private List<Sprite> rushHour = new ArrayList<>();
    private List<PhysicsHandler> rushHourHandlers = new ArrayList<>();

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
    private boolean no_event = false;
    private boolean first_enemy = false;
    private boolean bubble_bonus = false;
    private boolean cut_bar_30 = false;
    private boolean life_bonus = false;
    private boolean cut_bar_50 = false;
    private boolean big_bar = false;
    private boolean reverse = false;
    private boolean freeze = false;
    private boolean rush_hour = false;

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
    private static final int RUSH_HOUR_MIN_NUM = 15;
    private static final int RUSH_HOUR_MAX_NUM = 30;
    private boolean life_detached = false;
    private boolean allBonusDetached = false;

    // Pause utils
    private static final int PAUSE = 10;
    private float old_x_speed;
    private float old_y_speed;
    private int old_game_speed;
    private String old_event = "";
    private long tap;

    // Game over utils
    private boolean restart_game = false;


    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Life texture loading
        Drawable starDraw = getResources().getDrawable(R.drawable.life);
        lifeTexture = new BitmapTextureAtlas(getTextureManager(), starDraw.getIntrinsicWidth(), starDraw.getIntrinsicHeight());
        lifeTextureRegion = createFromResource(lifeTexture, this, R.drawable.life, 0, 0);
        lifeTexture.load();

        // Bonus ball loading
        Drawable bonusBallDraw = getResources().getDrawable(R.drawable.ball_petrol);
        bonusBallTexture = new BitmapTextureAtlas(getTextureManager(), bonusBallDraw.getIntrinsicWidth(), bonusBallDraw.getIntrinsicHeight());
        bonusBallTextureRegion = createFromResource(bonusBallTexture, this, R.drawable.ball_petrol, 0, 0);
        bonusBallTexture.load();

    }

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        // Adding the scoring text to the scene
        txtScore = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtScore);

        // Adding the level text to the scene
        txtLvl = new Text(10, txtScore.getY() + txtScore.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtLvl);

        // Adding the level text to the scene
        txtEvnt = new Text(10, txtLvl.getY() + txtLvl.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(txtEvnt);

        // Adding the life sprites to the scene
        addLifeSpritesToScene();

        // Setting the text score
        txtScore.setText(getResources().getString(R.string.text_score) + ": " + score);

        // Setting up the physics of the game
        settingPhysics();

        return scene;
    }

    @Override
    protected void collidesBottom() {
        Log.d(TAG, "BOTTOM EDGE. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = BOTTOM;

        ballSprite.detachSelf();
        lifeSprites.get(life).detachSelf();
        life--;
        Log.d(TAG, "Life: " + life);
        if (life < 0) {
            gameOver();
        } else {
            ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth()) / 2, (CAMERA_HEIGHT - ballSprite.getHeight()) / 2);
            handler.setVelocityY(-handler.getVelocityY());
            attachBall();
        }

    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();

        // Set score section
        addScore();
        txtScore.setText(getResources().getString(R.string.text_score) + ": " + score);

        reach_count--;
        Log.d(TAG, "Reach count " + reach_count);
        Log.d(TAG, "Level " + level);
        if (reach_count == 0) {
            clearEvent();
            callEvent();
            // Generating a new reach count and reset the hit count
            Random random = new Random();
            reach_count = MIN_REACH_COUNT + random.nextInt(MAX_REACH_COUNT - MIN_REACH_COUNT + 1);
            Log.d(TAG, "New Reach count " + reach_count);
        }
    }

    @Override
    protected void clearGame() {
        super.clearGame();
        clearEvent();
        // Clear game data
        life = MAX_LIFE - 1;
        old_life = 0;
        score = 0;
        gain = 0;
        level = LEVEL_ONE;
        game_event = NO_EVENT;
        reach_count = 1;
        txtScore.setText(getResources().getString(R.string.text_score) + ": " + score);
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
        // Setting true level_one
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
        //Adding life sprites to the scene
        addLifeSpritesToScene();
    }

    @Override
    public void actionDownEvent() {
        if (!pause) {
            pauseGame();
        }
        if (pause && (System.currentTimeMillis() - tap > 500)) {
            restartGameAfterPause();
        }
    }

    @Override
    protected void bluetoothExtra() {
        //do nothing
    }

    @Override
    public void addScore() {
        // This procedure increase the score according to the current score
        if (score < BARRIER_ONE && level_one) {
            score += 10;
            gain = 10;
        }
        if (score < BARRIER_TWO && level_two) {
            score += 15;
            gain = 15;
        }
        if (score < BARRIER_THREE && level_three) {
            score += 20;
            gain = 20;
        }
        if (score < BARRIER_FOUR && level_four) {
            score += 40;
            gain = 40;
        }
        if (score < BARRIER_FIVE && level_five) {
            score += 60;
            gain = 60;
        }
        if (score < BARRIER_SIX && level_six) {
            score += 100;
            gain = 100;
        }
        if (score < BARRIER_SEVEN && level_seven) {
            score += 200;
            gain = 200;
        }
        if (score < BARRIER_EIGHT && level_eight) {
            score += 300;
            gain = 300;
        }
        if (score < BARRIER_NINE && level_nine) {
            score += 400;
            gain = 400;
        }
        if (score < BARRIER_TEN && level_ten) {
            score += 500;
            gain = 500;
        }
        if (score < BARRIER_ELEVEN && level_eleven) {
            score += 1000;
            gain = 1000;
        }
        if (score < BARRIER_TWELVE && level_twelve) {
            score += 2000;
            gain = 2000;
        }
        if (score > BARRIER_TWELVE && level_max) {
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

        Log.d(TAG, "Score: " + score);
    }

    @Override
    protected void gameLevels() {
        // This procedure understand what modifier needs according to the score
        if (score >= 0 && score < BARRIER_ONE && level_one) {
            level = LEVEL_ONE;
            level_one = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv1));
        }
        if (score >= BARRIER_ONE && score < BARRIER_TWO && !level_two) {
            level = LEVEL_TWO;
            level_two = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv2));
        }
        if (score >= BARRIER_TWO && score < BARRIER_THREE && !level_three) {
            level = LEVEL_THREE;
            level_three = true;
            GAME_VELOCITY *= 2;
            handler.setVelocity(handler.getVelocityX() * 1.5f, handler.getVelocityY() * 1.5f);
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv3));
        }
        if (score >= BARRIER_THREE && score < BARRIER_FOUR && !level_four) {
            level = LEVEL_FOUR;
            level_four = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv4));
        }
        if (score >= BARRIER_FOUR && score < BARRIER_FIVE && !level_five) {
            level = LEVEL_FIVE;
            level_five = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv5));
        }
        if (score >= BARRIER_FIVE && score < BARRIER_SIX && !level_six) {
            level = LEVEL_SIX;
            level_six = true;
            handler.setVelocity(handler.getVelocityX() * 1.5f, handler.getVelocityY() * 1.5f);
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv6));
        }
        if (score >= BARRIER_SIX && score < BARRIER_SEVEN && !level_seven) {
            level = LEVEL_SEVEN;
            level_seven = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv7));
        }
        if (score >= BARRIER_SEVEN && score < BARRIER_EIGHT && !level_eight) {
            level = LEVEL_EIGHT;
            level_eight = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv8));
        }
        if (score >= BARRIER_EIGHT && score < BARRIER_NINE && !level_nine) {
            level = LEVEL_NINE;
            level_nine = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv9));
        }
        if (score >= BARRIER_NINE && score < BARRIER_TEN && !level_ten) {
            level = LEVEL_TEN;
            level_ten = true;
            GAME_VELOCITY *= 2;
            handler.setVelocity(handler.getVelocityX() * 1.5f, handler.getVelocityY() * 1.5f);
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv10));
        }
        if (score >= BARRIER_TEN && score < BARRIER_ELEVEN && !level_eleven) {
            level = LEVEL_ELEVEN;
            level_eleven = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv11));
        }
        if (score >= BARRIER_ELEVEN && score < BARRIER_TWELVE && !level_twelve) {
            level = LEVEL_TWELVE;
            level_twelve = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv12));
        }
        if (score >= BARRIER_TWELVE && !level_max) {
            level = LEVEL_MAX;
            level_max = true;
            txtLvl.setText(getApplicationContext().getString(R.string.text_lv13));
        }
    }

    @Override
    protected void gameEvents() {
        // Handling game events collisions
        gameEventsCollisionLogic();

        // Handling events logic
        switch (game_event) {
            case NO_EVENT:
                if (!no_event) {
                    txtEvnt.setText("");
                    no_event = true;
                }
                break;
            case FIRST_ENEMY:
                if (!first_enemy) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_first_enemy));
                    firstEnemyLogic();
                }
                break;
            case BUBBLE_BONUS:
                if (!bubble_bonus) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_bubble));
                    bubbleBonusLogic();
                }
                break;
            case CUT_BAR_30:
                if (!cut_bar_30) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_cut_bar_30));
                    cutBar30Logic();
                }
                break;
            case LIFE_BONUS:
                if (!life_bonus) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_lifebonus));
                    lifeBonusLogic();
                }
                break;
            case CUT_BAR_50:
                if (!cut_bar_50) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_cut_bar_50));
                    cutBar50Logic();
                }
                break;
            case BIG_BAR:
                if (!big_bar) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_big_bar));
                    bigBarLogic();
                }
                break;
            case REVERSE:
                if (!reverse) {
                    txtEvnt.setText(getResources().getString(R.string.text_reverse));
                    reverseLogic();
                }
                break;
            case FREEZE:
                if (!freeze) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_freeze));
                    freezeLogic();
                }
                break;
            case RUSH_HOUR:
                if (!rush_hour) {
                    txtEvnt.setText(getApplicationContext().getString(R.string.text_rush));
                    rushHourLogic();
                }
                break;
        }

        // Handling game restarting
        if (restart_game) {
            Log.d(TAG, "Game restarted");
            restartAfterGameOver();
            restart_game = false;
        }

    }

    @Override
    protected void gameOver() {
        game_over = true;
        handler.setVelocity(0f);
        GAME_VELOCITY = 0;
        touch.stop();
        txtEvnt.setText(getApplicationContext().getString(R.string.text_gameover));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Game over dialog

                AlertDialog.Builder alert = new AlertDialog.Builder(GamePongOnePlayer.this);

                alert.setTitle(getApplicationContext().getResources().getString(R.string.text_ttl_oneplayer_savegame));
                alert.setMessage(getApplicationContext().getResources().getString(R.string.text_msg_oneplayer_savegame));

                // Set an EditText view to get user input
                final EditText input = new EditText(GamePongOnePlayer.this);
                alert.setView(input);

                alert.setPositiveButton(getApplicationContext().getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String user_input_name = input.getText().toString();

                        if (!user_input_name.equals("")) {
                            saveGame(user_input_name);

                            restart_game = true;
                            game_over = false;
                        } else {
                            Toast toast = Toast.makeText(getApplication(), getResources().getString(R.string.text_no_user_input), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();
                            run();
                        }
                    }
                });

                alert.setNegativeButton(getApplicationContext().getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        restart_game = true;
                        game_over = false;
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

        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, new Date().toString(), score);
        StatOnePlayerDAO statOnePlayerDAO = new StatOnePlayerDAO(getApplicationContext());

        statOnePlayerDAO.insert(statOnePlayer);

        playerDAO.close();
        statOnePlayerDAO.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseGame();
    }

    @Override
    public void onBackPressed() {
        if (!pause)
            pauseGame();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.text_msg_oneplayer_dialog)).setTitle(getResources().getString(R.string.text_msg_oneplayer_leavegame)).setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked YES button
                finish();
            }
        }).setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                restartGameAfterPause();
            }
        }).show();

        AlertDialog dialog = builder.create();
    }

    private void pauseGame() {
        Log.d(TAG, "Game Paused");
        old_event = (String) txtEvnt.getText();
        txtEvnt.setText(getResources().getString(R.string.text_pause));
        old_x_speed = handler.getVelocityX();
        old_y_speed = handler.getVelocityY();
        old_game_speed = GAME_VELOCITY;
        tap = System.currentTimeMillis();
        handler.setVelocity(0);
        GAME_VELOCITY = 0;
        touch.stop();
        previous_event = PAUSE;
        pause = true;
    }

    private void addLifeSpritesToScene() {
        for (int i = 1; i <= life + 1; i++) {
            Sprite lifeSprite = new Sprite(0, 0, lifeTextureRegion, getVertexBufferObjectManager());
            lifeSprite.setX(CAMERA_WIDTH - i * lifeSprite.getWidth());
            lifeSprites.add(lifeSprite);
            scene.attachChild(lifeSprites.get(i - 1));
        }
    }

    private void restartAfterGameOver() {
        clearGame();
        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth()) / 2, (CAMERA_HEIGHT - ballSprite.getHeight()) / 2);
        handler.setVelocity(BALL_SPEED, -BALL_SPEED);
        scene.attachChild(ballSprite);
    }

    private void restartGameAfterPause() {
        txtEvnt.setText(old_event);
        handler.setVelocity(old_x_speed, old_y_speed);
        GAME_VELOCITY = old_game_speed;
        pause = false;
    }

    private void firstEnemyLogic() {
        first_enemy = true;
        firstEnemy = new Sprite(0, CAMERA_HEIGHT / 3, barTextureRegion, getVertexBufferObjectManager());
        firstEnemy.setWidth(CAMERA_WIDTH);
        scene.attachChild(firstEnemy);
    }

    private void clearFirstEnemy() {
        firstEnemy.detachSelf();
        first_enemy = false;
    }

    private void bubbleBonusLogic() {
        bubble_bonus = true;

        Random random = new Random();
        int BONUS_BALL_NUM = BONUS_BALL_MIN_NUM + random.nextInt(BONUS_BALL_MAX_NUM - BONUS_BALL_MIN_NUM + 1);

        // Adding the bonus ball sprites to the scene
        for (int i = 0; i < BONUS_BALL_NUM; i++) {
            random = new Random();
            Sprite bonusSprite = new Sprite(0, 0, bonusBallTextureRegion, getVertexBufferObjectManager());
            int ballRadius = (int) bonusSprite.getHeight() / 2;
            int bonusSpriteX = ballRadius + random.nextInt(CAMERA_WIDTH - ballRadius * 2);
            bonusSprite.setPosition(bonusSpriteX, (bonusSprite.getHeight() / 2) * (i + 1));
            bonusSprite.setWidth(CAMERA_WIDTH * 0.1f);
            bonusSprite.setHeight(CAMERA_WIDTH * 0.1f);
            bonusBalls.add(bonusSprite);
            scene.attachChild(bonusBalls.get(i));
        }
        Log.d(TAG, "BONUS_BALL_NUM: " + BONUS_BALL_NUM + " bonusBalls.size(): " + bonusBalls.size());
    }

    private void clearBubbleBonus() {
        if (!allBonusDetached) {
            Log.d(TAG, "Not all bonus ball detached");
            do {
                bonusBalls.get(0).detachSelf();
                bonusBalls.remove(0);
                Log.d(TAG, "Bonus ball detached in clear");
            } while (bonusBalls.size() > 0);
        }
        allBonusDetached = false;
        bubble_bonus = false;
    }

    private void cutBar30Logic() {
        cut_bar_30 = true;
        barSprite.setWidth(0.21f * CAMERA_WIDTH);
    }

    private void clearCutBar30() {
        barSprite.setWidth(0.3f * CAMERA_WIDTH);
        cut_bar_30 = false;
    }

    private void lifeBonusLogic() {
        life_bonus = true;
        old_life = life;
        if (life < MAX_LIFE - 1) {
            Random random = new Random();
            lifeBonus = new Sprite(0, 0, lifeTextureRegion, getVertexBufferObjectManager());
            lifeBonus.setWidth(CAMERA_WIDTH * 0.1f);
            lifeBonus.setHeight(CAMERA_WIDTH * 0.1f);
            lifeBonus.setPosition(random.nextInt(CAMERA_WIDTH - (int) lifeBonus.getWidth()), random.nextInt(CAMERA_HEIGHT - 2 * (int) ballSprite.getHeight()));
            scene.attachChild(lifeBonus);
        }
    }

    private void clearLifeBonus() {
        if (!life_detached && life < MAX_LIFE - 1)
            lifeBonus.detachSelf();
        life_detached = false;
        life_bonus = false;
    }

    private void cutBar50Logic() {
        cut_bar_50 = true;
        barSprite.setWidth(0.15f * CAMERA_WIDTH);
    }

    private void clearCutBar50() {
        barSprite.setWidth(0.3f * CAMERA_WIDTH);
        cut_bar_50 = false;
    }

    private void bigBarLogic() {
        big_bar = true;
        barSprite.setWidth(0.45f * CAMERA_WIDTH);
    }

    private void clearBigBar() {
        barSprite.setWidth(0.3f * CAMERA_WIDTH);
        big_bar = false;
    }

    private void reverseLogic() {
        reverse = true;
        GAME_VELOCITY = (-1) * GAME_VELOCITY;
    }

    private void clearReverseLogic() {
        GAME_VELOCITY = (-1) * GAME_VELOCITY;
        reverse = false;
    }

    private void freezeLogic() {
        freeze = true;
        handler.setVelocity(handler.getVelocityX() / 2, handler.getVelocityY() / 2);
    }

    private void clearFreeze() {
        handler.setVelocity(handler.getVelocityX() * 2, handler.getVelocityY() * 2);
        freeze = false;
    }

    private void rushHourLogic() {
        rush_hour = true;

        Random random = new Random();
        int RUSH_HOUR_NUM = RUSH_HOUR_MIN_NUM + random.nextInt(RUSH_HOUR_MAX_NUM - RUSH_HOUR_MIN_NUM + 1);

        for (int i = 0; i < RUSH_HOUR_NUM; i++) {
            Sprite rush = new Sprite(0, 0, ballTextureRegion, getVertexBufferObjectManager());
            rush.setPosition(random.nextInt(CAMERA_WIDTH), random.nextInt(CAMERA_HEIGHT) - ballSprite.getHeight() * 2);
            rush.setWidth(CAMERA_WIDTH * 0.1f);
            rush.setHeight(CAMERA_WIDTH * 0.1f);
            rushHour.add(rush);

            PhysicsHandler physicsHandler = new PhysicsHandler(rushHour.get(i));
            physicsHandler.setVelocity(BALL_SPEED * (random.nextFloat() - random.nextFloat()), BALL_SPEED * (random.nextFloat() - random.nextFloat()));
            rushHourHandlers.add(physicsHandler);

            rushHour.get(i).registerUpdateHandler(rushHourHandlers.get(i));

            scene.attachChild(rushHour.get(i));
        }
    }

    private void clearRushHour() {
        do {
            rushHour.get(0).detachSelf();
            rushHour.remove(0);
            rushHourHandlers.remove(0);
        } while (rushHour.size() > 0);

        rush_hour = false;
    }

    private void gameEventsCollisionLogic() {
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

    private void firstEnemyCollisions() {
        if (ballSprite.collidesWith(firstEnemy) && first_enemy && ballSprite.getY() < CAMERA_HEIGHT / 2 && previous_event != TOP) {
            previous_event = TOP;
            handler.setVelocityY(-handler.getVelocityY());
            touch.play();
        }
    }

    private void bubbleBonusCollisions() {
        for (int i = 0; i < bonusBalls.size(); i++) {
            if (ballSprite.collidesWith(bonusBalls.get(i))) {
                Log.d(TAG, "Bonus Ball " + i + " removed");
                bonusBalls.get(i).detachSelf();
                bonusBalls.remove(i);
                score += 20 * (level + 1);
                txtScore.setText(getResources().getString(R.string.text_score) + ": " + score);
                if (bonusBalls.size() == 0) {
                    allBonusDetached = true;
                    Log.d(TAG, "All bonus ball detached by player");
                }
            }
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

    private void lifeBonusCollisions() {
        if (ballSprite.collidesWith(lifeBonus) && life < MAX_LIFE - 1 && old_life == life) {
            lifeBonus.detachSelf();
            life++;
            scene.attachChild(lifeSprites.get(life));
            life_detached = true;
            Log.d(TAG, "Star collision. Life: " + life);
        }
    }


    private void clearEvent() {
        Log.d(TAG, "Clear Event called");

        switch (game_event) {
            case NO_EVENT:
                no_event = false;
                break;
            case FIRST_ENEMY:
                clearFirstEnemy();
                break;
            case BUBBLE_BONUS:
                clearBubbleBonus();
                break;
            case CUT_BAR_30:
                clearCutBar30();
                break;
            case LIFE_BONUS:
                clearLifeBonus();
                break;
            case CUT_BAR_50:
                clearCutBar50();
                break;
            case BIG_BAR:
                clearBigBar();
                break;
            case REVERSE:
                clearReverseLogic();
                break;
            case FREEZE:
                clearFreeze();
                break;
            case RUSH_HOUR:
                clearRushHour();
                break;
        }
    }

    private void callEvent() {
        Log.d(TAG, "Call Event called");

        // Generating a new event different from current event
        Random random = new Random();
        int random_int = random.nextInt(level + 1);
        while ((random_int == game_event && level > LEVEL_ONE) || (random_int == LIFE_BONUS && life == MAX_LIFE - 1) || (random_int == 10) || (random_int == 11) || (random_int == 12)) {
            random_int = random.nextInt(level + 1);
        }
        game_event = random_int;
        Log.d(TAG, "Game Event " + game_event);
    }
}
