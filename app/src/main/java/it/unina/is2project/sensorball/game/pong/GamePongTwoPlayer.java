package it.unina.is2project.sensorball.game.pong;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import it.unina.is2project.sensorball.bluetooth.FSMGame;
import it.unina.is2project.sensorball.R;
import it.unina.is2project.sensorball.activity.TwoPlayerActivity;
import it.unina.is2project.sensorball.bluetooth.service.BluetoothService;
import it.unina.is2project.sensorball.bluetooth.Constants;
import it.unina.is2project.sensorball.bluetooth.message.Serializer;
import it.unina.is2project.sensorball.bluetooth.message.AppMessage;
import it.unina.is2project.sensorball.game.bonus.BonusManager;
import it.unina.is2project.sensorball.game.entity.GameObject;
import it.unina.is2project.sensorball.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorball.stats.database.dao.StatTwoPlayerDAO;
import it.unina.is2project.sensorball.stats.entity.Player;
import it.unina.is2project.sensorball.stats.entity.StatTwoPlayer;

public class GamePongTwoPlayer extends GamePong {

    //===========================================
    // DEBUG
    //===========================================
    private final String TAG = "2PlayersGame";  // String for logcat debug

    //===========================================
    // BOOLEAN UTILS
    //===========================================
    private boolean haveBall = false;           // Indicates the holding's ball
    private boolean isMaster = false;           // Indicates who starts game
    private boolean transferringBall = false;   // Indicates when ball is passing
    private boolean backPressed = false;        // Indicates who backs activity
    private boolean winner = false;             // Indicates the winner of the match

    //===========================================
    // BLUETOOTH
    //===========================================
    private BluetoothService mBluetoothService = null;  // Service bluetooth
    private String mConnectedDeviceName = "";           // Indicates paired device in game
    private boolean isConnected;                        // Indicates if connection is up

    //===========================================
    // GAME DATA
    //===========================================
    private int points;         // Points to reach
    private int score;          // Score for current player in current game
    private int opponentScore;  // Score for opponent player in current game
    private Text textInfo;      // TextView for game events
    private Text textPoint;     // TextView for show points

    //===========================================
    // FINITE STATE MACHINE
    //===========================================
    private FSMGame fsmGame = null; // FSM to control game flow

    //===========================================
    // INTENT EXTRAS
    //===========================================
    public static final String EXTRA_MASTER = "isMaster_boolean";                 // Returns master state
    public static final String EXTRA_CONNECTION_STATE = "isConnected_boolean";    // Returns connection state
    public static final String EXTRA_DEVICE = "deviceName_string";                // Returns connected device

    //===========================================
    // GAME OBJECTS
    //===========================================
    private GameObject speedX2Bonus;        // Speed X2 Bonus
    private GameObject speedX2BonusIcon;    // Speed X2 Icon
    private GameObject speedX3Bonus;        // Speed X3 Bonus
    private GameObject speedX3BonusIcon;    // Speed X3 Icon
    private GameObject speedX4Bonus;        // Speed X4 Bonus
    private GameObject speedX4BonusIcon;    // Speed X4 Icon
    private GameObject lockFieldBonus;      // Lock Field Bonus
    private GameObject lockFieldBonusIcon;  // Lock Field Icon
    private GameObject cutBar30Bonus;       // Cut Bar 30 Bonus
    private GameObject cutBar30BonusIcon;   // Cut Bar 30 Icon
    private GameObject cutBar50Bonus;       // Cut Bar 50 Bonus
    private GameObject cutBar50BonusIcon;   // Cut Bar 50 Icon
    private GameObject reverseBonus;        // Reverted Bar Bonus
    private GameObject reverseBonusIcon;    // Reverted Bar Icon
    private GameObject rushHourBonus;       // Rush-Hour Bonus
    private GameObject rushHourBonusIcon;   // Rush-Hour Icon

    //===========================================
    // PAUSE UTILS
    //===========================================
    private long tap;                                   // Register current milliseconds time to avoid fastest pause
    private int PROXIMITY_ZONE;                         // Variable to avoid pause when ball is passing
    private boolean proximityRegion;                    // Indicates when ball is in proximity zone
    private boolean resumeAllowed;                      // Indicates that player can resume the game
    private boolean receivedStop;                       // Indicates that player received a stop request

    //===========================================
    // BONUS SECTION
    //===========================================
    private BonusManager bonusManager = null;                   // Manager for created/expired bonus
    private int previous_bonus;                                 // Indicates previous selectable bonus
    private final int SPRITE_NONE = -1;                         // There is no attached sprite
    private static final int SPEED_X2_ICON = 10;                // ID for bonus icon speed x2
    private static final int SPEED_X3_ICON = 11;                // ID for bonus icon speed x3
    private static final int SPEED_X4_ICON = 12;                // ID for bonus icon speed x4
    private static final int LOCK_FIELD_ICON = 13;              // ID for bonus icon lock field
    private static final int CUT_BAR_30_ICON = 14;              // ID for bonus icon cut bar 30%
    private static final int CUT_BAR_50_ICON = 15;              // ID for bonus icon cut bar 50%
    private static final int REVERTED_BAR_ICON = 16;            // ID for bonus icon reverted bar
    private static final int RUSH_HOUR_ICON = 17;               // ID for bonus icon rush hour
    private int activedBonusSprite;                             // Indicates currenct actived and selectable bonus
    private float SPRITE_ICON_SIZE;                             // Size for bonus icons
    private boolean deletedBonusSprite = true;                  // Indicates that current selectable bonus was pressed
    private List<Integer> bonusStatusArray = new ArrayList<>(); // Indicates current activator bonus on scene
    private long scheduleDelay;                                 // Time to wait for next selectable bonus
    private long previousScheduleTime;                          // Register previous schedule time in millis
    private boolean locksField = false;                         // Indicates that bonus lock field is actived
    private boolean rush_hour = false;                          // Indicates that bonus rush hour is actived
    private float mSPEED_X1;                                    // Constant module for velocity x1
    private float mSPEED_X2;                                    // Constant module for velocity x2
    private float mSPEED_X3;                                    // Constant module for velocity x3
    private float mSPEED_X4;                                    // Constant module for velocity x4
    private TimerTask taskBonus;                                // Task for selectable bonus activation
    private Timer timerBonus;                                   // Timer for bonus task activation
    private TimerTask taskTimeout;                              // Task for timeout termination game
    private Timer timerTimeout;                                 // Time for timeout task activation

    //===========================================
    // OVERRIDEN METHODS
    //===========================================
    @Override
    protected Scene onCreateScene() {
        Log.d("LifeCycle", "onCreateScene()");
        super.onCreateScene();

        // Retrieve intent message
        Intent i = getIntent();
        if (i.getIntExtra(TwoPlayerActivity.EXTRA_BALL, 0) == 1) {
            haveBall = true;
        } else {
            haveBall = false;
            ball.detach();
        }
        points = i.getIntExtra(TwoPlayerActivity.EXTRA_POINTS, 0);
        mConnectedDeviceName = i.getStringExtra(TwoPlayerActivity.EXTRA_DEVICE_NAME);

        // Set result in case of failure
        setResult(RESULT_CANCELED);

        // Setting up the physics of the game
        settingPhysics();

        // Setting variables
        isConnected = true;
        resumeAllowed = true;
        proximityRegion = false;
        receivedStop = false;
        score = 0;
        opponentScore = 0;
        scheduleDelay = 2000;
        mSPEED_X1 = (float) Math.sqrt(Math.pow(BALL_SPEED, 2) + Math.pow(BALL_SPEED, 2));
        myModule = mSPEED_X1;
        mSPEED_X2 = mSPEED_X1 * 2;
        mSPEED_X3 = mSPEED_X1 * 3;
        mSPEED_X4 = mSPEED_X1 * 4;
        COS_X = COS_45;
        SIN_X = SIN_45;
        PROXIMITY_ZONE = CAMERA_HEIGHT / 8;
        previous_event = NO_COLL;
        previous_bonus = Constants.NO_BONUS;
        activedBonusSprite = SPRITE_NONE;

        // Attaching textInfo
        textInfo = new Text(10, 10, font, "", 60, getVertexBufferObjectManager());
        scene.attachChild(textInfo);

        // Attachning textPoint
        Text textPointUtils = new Text(10, 10, font, "Score 10-10 [10]", 30, getVertexBufferObjectManager());
        textPoint = new Text(10, CAMERA_HEIGHT - textPointUtils.getHeight(), font, getResources().getString(R.string.sts_score) + " " + score + "-" + opponentScore + " [" + points + "]", 30, getVertexBufferObjectManager());
        scene.attachChild(textPoint);
        SPRITE_ICON_SIZE = textPointUtils.getHeight();

        // Traslating bar
        bar.setPosition(bar.getXCoordinate(), textPoint.getY() - bar.getObjectHeight());

        initializeSprite();

        isMaster = i.getBooleanExtra(TwoPlayerActivity.EXTRA_MASTER, false);
        if (isMaster) {
            // Master Branch Operation
            AppMessage alertMessage = new AppMessage(Constants.MSG_TYPE_ALERT);
            sendBluetoothMessage(alertMessage);
            fsmGame.setState(FSMGame.STATE_IN_GAME_WAITING);
        } else {
            // Non-Master Branch Operation
            AppMessage messageSync = new AppMessage(Constants.MSG_TYPE_SYNC);
            sendBluetoothMessage(messageSync);
            fsmGame.setState(FSMGame.STATE_IN_GAME);
            // Non-Master Increase Played Games.
            increasePartiteGiocate();
        }

        return scene;
    }


    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        // Speed X2 Bonus
        speedX2Bonus = new GameObject(this, R.drawable.speedx2) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.SPEED_X2);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx2Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEED_X2, randNum);
                    sendBluetoothMessage(speedx2Message);
                }
            }
        };
        // Speed X2 Icon
        speedX2BonusIcon = new GameObject(this, R.drawable.speedx2_icon);
        // Speed X3 Bonus
        speedX3Bonus = new GameObject(this, R.drawable.speedx3) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.SPEED_X3);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx3Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEED_X3, randNum);
                    sendBluetoothMessage(speedx3Message);
                }
            }
        };
        // Speed X3 Icon
        speedX3BonusIcon = new GameObject(this, R.drawable.speedx3_icon);
        // Speed X4 Bonus
        speedX4Bonus = new GameObject(this, R.drawable.speedx4) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.SPEED_X4);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx4Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEED_X4, randNum);
                    sendBluetoothMessage(speedx4Message);
                }
            }
        };
        // Speed X4 Icon
        speedX4BonusIcon = new GameObject(this, R.drawable.speedx4_icon);
        // Lock Screen Bonus
        lockFieldBonus = new GameObject(this, R.drawable.firstenemy) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.LOCK_FIELD);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage lockFieldMessage = new AppMessage(Constants.MSG_TYPE_BONUS_LOCK_FIELD, randNum);
                    sendBluetoothMessage(lockFieldMessage);
                }
            }
        };
        // Lock Screen Icon
        lockFieldBonusIcon = new GameObject(this, R.drawable.lockfield_icon);
        // Cut Bar 30% Bonus
        cutBar30Bonus = new GameObject(this, R.drawable.reduce30) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.CUT_BAR_30);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage cutBar30Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUT_BAR_30, randNum);
                    sendBluetoothMessage(cutBar30Message);
                }
            }
        };
        // Cut Bar 30% Icon
        cutBar30BonusIcon = new GameObject(this, R.drawable.cutbar30_icon);
        // Cut Bar 50% Bonus
        cutBar50Bonus = new GameObject(this, R.drawable.reduce50) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.CUT_BAR_50);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage cutBar50Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUT_BAR_50, randNum);
                    sendBluetoothMessage(cutBar50Message);
                }
            }
        };
        // Cut Bar 50% Icon
        cutBar50BonusIcon = new GameObject(this, R.drawable.cutbar50_icon);
        // Reverted Bar Bonus
        reverseBonus = new GameObject(this, R.drawable.revert) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.REVERSE);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage revertedBarMessage = new AppMessage(Constants.MSG_TYPE_BONUS_REVERSE_BAR, randNum);
                    sendBluetoothMessage(revertedBarMessage);
                }
            }
        };
        // Reverted Bar Icon
        reverseBonusIcon = new GameObject(this, R.drawable.revertbar_icon);
        // Rush Hour Bonus
        rushHourBonus = new GameObject(this, R.drawable.rush_hour) {
            @Override
            public void onTouch() {
                super.onTouch();
                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                    detachSprite(Constants.RUSH_HOUR);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage rushHourMessage = new AppMessage(Constants.MSG_TYPE_BONUS_RUSH_HOUR, randNum);
                    sendBluetoothMessage(rushHourMessage);
                }
            }
        };
        // Rush Hour Icon
        rushHourBonusIcon = new GameObject(this, R.drawable.rushhour_icon);
    }

    @Override
    protected void loadAdditionalGraphics() {
        // do nothing
    }

    @Override
    protected void settingPhysics() {
        doPhysics();
    }

    @Override
    protected void onStart() {
        // Getting instance of fsm, service bluetooth and bonus manager
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
        bonusManager = BonusManager.getBonusInstance(bonusHandler);
        super.onStart();
    }

    @Override
    protected synchronized void onResume() {
        backPressed = false;
        if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
            AppMessage resumeMessage = new AppMessage(Constants.MSG_TYPE_ALERT);
            sendBluetoothMessage(resumeMessage);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (!backPressed) {
            if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                saveHandlerState();
            }
            if (fsmGame.getState() == FSMGame.STATE_IN_GAME ||
                    fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                    fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED) {
                AppMessage pauseMessage = new AppMessage(Constants.MSG_TYPE_STOP_REQUEST);
                sendBluetoothMessage(pauseMessage);
                resumeAllowed = true;
                fsmGame.setState(FSMGame.STATE_GAME_PAUSE_STOP);
            } else if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP && receivedStop) {
                AppMessage suspendMessage = new AppMessage(Constants.MSG_TYPE_SUSPEND_REQUEST);
                sendBluetoothMessage(suspendMessage);
                fsmGame.setState(FSMGame.STATE_GAME_SUSPENDED);
                if (timerTimeout != null) timerTimeout.cancel();
                if (taskTimeout != null) taskTimeout.cancel();
            }
            if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                AppMessage noReadyMessage = new AppMessage(Constants.MSG_TYPE_NO_READY);
                sendBluetoothMessage(noReadyMessage);
            }
        }
        super.onStop();
    }

    @Override
    protected void setBallVelocity() {
        old_bar_speed = bar.getBarSpeed();
        old_x_speed = ball.getBallSpeed();
        old_y_speed = -ball.getBallSpeed();
    }

    @Override
    protected boolean topCondition() {
        if (!transferringBall && ball.getYCoordinate() < 0 && previous_event != TOP && haveBall) {
            Log.d(TAG, "topCondition TRUE");
            return true;
        } else return false;
    }

    @Override
    protected void collidesTop() {
        Log.d(TAG, "collidesTop");
        if (!locksField) {
            float xRatio = ball.getXCoordinate() / CAMERA_WIDTH;
            AppMessage messageCoords = new AppMessage(Constants.MSG_TYPE_COORDS,
                    Math.signum(ball.getHandlerSpeedX()),
                    COS_X,
                    SIN_X,
                    xRatio);
            sendBluetoothMessage(messageCoords);
            haveBall = false;
            transferringBall = true;
            previous_event = TOP;
        } else {
            super.collidesTop();
        }
    }

    @Override
    protected void collidesBottom() {
        super.collidesBottom();
        AppMessage pointToEnemyMessage = new AppMessage(Constants.MSG_TYPE_POINT_UP);
        sendBluetoothMessage(pointToEnemyMessage);
        opponentScore++;
        bonusManager.clearAll();
        textPoint.setText(getResources().getString(R.string.sts_score) + " " + score + " - " + opponentScore + " [" + points + "]");
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();
        bonusManager.decrementCount();
    }

    @Override
    protected void bluetoothExtra() {
        // Setting proximityRegion ON
        if (!proximityRegion && ball.getYCoordinate() <= PROXIMITY_ZONE) {
            proximityRegion = true;
        }
        // Al rientro della pallina, proximity regione in ricezione (la metà di quella di invio, più critica)
        if (proximityRegion && ball.getYCoordinate() > PROXIMITY_ZONE / 2) {
            proximityRegion = false;
        }
        // Quando la palla ESCE COMPLETAMENTE dal device
        if (proximityRegion && ball.getYCoordinate() < -ball.getObjectHeight()) {
            ball.detach();
            transferringBall = false;
            proximityRegion = false;
        }
        // Quando la palla ENTRA COMPLETAMENTE nel device
        if (transferringBall && ball.getYCoordinate() > 0) {
            transferringBall = false;
        }
    }

    @Override
    protected void gameEventsCollisionLogic() {
        if (rush_hour) {
            rushHour.collision();
        }
    }

    @Override
    protected void gameOver() {
        Log.d(TAG, "gameOver() - winner: " + winner);
        ball.setHandlerSpeed(0f, 0f);
        bar.setBarSpeed(0f);
        if (timerBonus != null)
            timerBonus.cancel();
        if (taskBonus != null)
            taskBonus.cancel();
        saveGame();
    }

    private void increasePartiteGiocate() {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player currentPlayer = playerDAO.findByNome(nickname);
        long idPlayer;

        if (currentPlayer == null) {
            currentPlayer = new Player(nickname);
            idPlayer = playerDAO.insert(currentPlayer);
        } else idPlayer = currentPlayer.getId();

        StatTwoPlayerDAO statTwoPlayerDAO = new StatTwoPlayerDAO(getApplicationContext());
        StatTwoPlayer currentPlayerStats = statTwoPlayerDAO.findById((int) idPlayer);

        if (currentPlayerStats == null) {
            currentPlayerStats = new StatTwoPlayer((int) idPlayer, 0, 0);
            statTwoPlayerDAO.insert(currentPlayerStats);
        }

        currentPlayerStats.increasePartiteGiocate();

        statTwoPlayerDAO.update(currentPlayerStats);

        playerDAO.close();
        statTwoPlayerDAO.close();
    }

    @Override
    protected void saveGame() {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player currentPlayer = playerDAO.findByNome(nickname);
        long idPlayer;

        idPlayer = currentPlayer.getId();

        StatTwoPlayerDAO statTwoPlayerDAO = new StatTwoPlayerDAO(getApplicationContext());
        StatTwoPlayer currentPlayerStats = statTwoPlayerDAO.findById((int) idPlayer);

        if (winner)
            currentPlayerStats.increasePartiteVinte();

        statTwoPlayerDAO.update(currentPlayerStats);

        playerDAO.close();
        statTwoPlayerDAO.close();
    }

    @Override
    protected void addScore() {
        score++;
        textPoint.setText(getResources().getString(R.string.sts_score) + " " + score + " - " + opponentScore + " [" + points + "]");
        if (score == points) {
            AppMessage gameOverMessage = new AppMessage(Constants.MSG_TYPE_GAME_OVER);
            sendBluetoothMessage(gameOverMessage);
            fsmGame.setState(FSMGame.STATE_GAME_WINNER);
        }
    }

    @Override
    public void actionDownEvent(float x, float y) {
        if (!checkTouchOnSprite(activedBonusSprite, x, y)) {
            if (fsmGame.getState() == FSMGame.STATE_IN_GAME && !proximityRegion) {
                if (haveBall) {
                    tap = System.currentTimeMillis();
                    fsmGame.setState(FSMGame.STATE_GAME_PAUSED);
                    AppMessage pauseMessage = new AppMessage(Constants.MSG_TYPE_PAUSE);
                    sendBluetoothMessage(pauseMessage);
                } else {
                    textInfo.setText(getResources().getString(R.string.text_pause_not_allowed));
                }
            }

            if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSED && (System.currentTimeMillis() - tap > 500)) {
                AppMessage resumeMessage = new AppMessage(Constants.MSG_TYPE_RESUME);
                sendBluetoothMessage(resumeMessage);
                fsmGame.setState(FSMGame.STATE_IN_GAME);
            }

            if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP && resumeAllowed) {
                AppMessage resumeMessage = new AppMessage(Constants.MSG_TYPE_RESUME);
                sendBluetoothMessage(resumeMessage);
                fsmGame.setState(FSMGame.STATE_IN_GAME);
            }

            if (fsmGame.getState() == FSMGame.STATE_GAME_SUSPENDED) {
                AppMessage resumeAfterSuspend = new AppMessage(Constants.MSG_TYPE_RESUME_AFTER_SUSPEND);
                sendBluetoothMessage(resumeAfterSuspend);
                resumeAllowed = false;
                fsmGame.setState(FSMGame.STATE_GAME_PAUSE_STOP);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fsmGame.getState() == FSMGame.STATE_IN_GAME ||
                fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING ||
                fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP ||
                fsmGame.getState() == FSMGame.STATE_GAME_SUSPENDED) {
            backPressed = true;
            AppMessage messageFail = new AppMessage(Constants.MSG_TYPE_FAIL);
            sendBluetoothMessage(messageFail);
            if (fsmGame.getState() != FSMGame.STATE_IN_GAME_WAITING &&
                    fsmGame.getState() != FSMGame.STATE_OPPONENT_NOT_READY) {
                winner = false;
                gameOver();
            }
        }
        if (rush_hour) {
            runOnUpdateThread(new Runnable() {
                @Override
                public void run() {
                    rush_hour = false;
                    rushHour.clear();
                }
            });
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONNECTION_STATE, isConnected);
        intent.putExtra(EXTRA_MASTER, isMaster);
        intent.putExtra(EXTRA_DEVICE, mConnectedDeviceName);
        setResult(RESULT_CANCELED, intent);
        if (timerBonus != null) timerBonus.cancel();
        if (timerTimeout != null) timerTimeout.cancel();
        super.onBackPressed();
    }

    //===========================================
    // MISCELLANEA
    //===========================================

    /**
     * This routine is called to send a bluetooth message between two paired devices
     *
     * @param message Message to send.
     */
    private synchronized void sendBluetoothMessage(AppMessage message) {
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

    /**
     * This routine is called to understand if bonus activator was touched
     *
     * @param bonusID ID of activator bonus
     * @param x       X coordinate of the touch
     * @param y       Y coordinate of the touch
     * @return True if activator touched, false instead
     */
    private boolean checkTouchOnSprite(int bonusID, float x, float y) {
        boolean checkTouchSpriteStatus;
        switch (bonusID) {
            case Constants.SPEED_X2:
                checkTouchSpriteStatus = speedX2Bonus.checkOnTouch(x, y);
                break;
            case Constants.SPEED_X3:
                checkTouchSpriteStatus = speedX3Bonus.checkOnTouch(x, y);
                break;
            case Constants.SPEED_X4:
                checkTouchSpriteStatus = speedX4Bonus.checkOnTouch(x, y);
                break;
            case Constants.LOCK_FIELD:
                checkTouchSpriteStatus = lockFieldBonus.checkOnTouch(x, y);
                break;
            case Constants.CUT_BAR_30:
                checkTouchSpriteStatus = cutBar30Bonus.checkOnTouch(x, y);
                break;
            case Constants.CUT_BAR_50:
                checkTouchSpriteStatus = cutBar50Bonus.checkOnTouch(x, y);
                break;
            case Constants.REVERSE:
                checkTouchSpriteStatus = reverseBonus.checkOnTouch(x, y);
                break;
            case Constants.RUSH_HOUR:
                checkTouchSpriteStatus = rushHourBonus.checkOnTouch(x, y);
                break;
            default:
                checkTouchSpriteStatus = false;
                break;
        }
        return checkTouchSpriteStatus;
    }

    /**
     * Use this to save the state of game's objects
     */
    private void saveHandlerState() {
        Log.d(TAG, "Save Handler State");
        old_x_speed = ball.getHandlerSpeedX();
        old_y_speed = ball.getHandlerSpeedY();
        old_bar_speed = bar.getBarSpeed();
        if (rush_hour) {
            rushHour.pause();
        }
    }

    /**
     * Use this to initialize game's object on scene
     */
    private void initializeSprite() {

        // SPEED X2 BONUS INITIALIZING
        speedX2Bonus.addToScene(scene);
        speedX2Bonus.getSprite().setScale(speedX2Bonus.getSprite().getScaleX() / 2);
        speedX2Bonus.getSprite().setCullingEnabled(true);
        speedX2Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X2 ICON INITIALIZING
        speedX2BonusIcon.addToScene(scene);
        speedX2BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX2BonusIcon.getSprite().setCullingEnabled(true);
        speedX2BonusIcon.setPosition(CAMERA_WIDTH - speedX2BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX2BonusIcon.getObjectHeight() / 2);

        // SPEED X3 BONUS INITIALIZING
        speedX3Bonus.addToScene(scene);
        speedX3Bonus.getSprite().setScale(speedX3Bonus.getSprite().getScaleX() / 2);
        speedX3Bonus.getSprite().setCullingEnabled(true);
        speedX3Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X3 ICON INITIALIZING
        speedX3BonusIcon.addToScene(scene);
        speedX3BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX3BonusIcon.getSprite().setCullingEnabled(true);
        speedX3BonusIcon.setPosition(CAMERA_WIDTH - speedX3BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX3BonusIcon.getObjectHeight() / 2);

        // SPEED X4 BONUS INITIALIZING
        speedX4Bonus.addToScene(scene);
        speedX4Bonus.getSprite().setScale(speedX4Bonus.getSprite().getScaleX() / 2);
        speedX4Bonus.getSprite().setCullingEnabled(true);
        speedX4Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X4 ICON INITIALIZING
        speedX4BonusIcon.addToScene(scene);
        speedX4BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX4BonusIcon.getSprite().setCullingEnabled(true);
        speedX4BonusIcon.setPosition(CAMERA_WIDTH - speedX4BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX4BonusIcon.getObjectHeight() / 2);

        // LOCK FIELD INITIALIZING
        lockFieldBonus.addToScene(scene);
        lockFieldBonus.getSprite().setScale(lockFieldBonus.getSprite().getScaleX() / 2);
        lockFieldBonus.getSprite().setCullingEnabled(true);
        lockFieldBonus.setPosition(GameObject.MIDDLE);

        // LOCK FIELD ICON INITIALIZING
        lockFieldBonusIcon.addToScene(scene);
        lockFieldBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        lockFieldBonusIcon.getSprite().setCullingEnabled(true);
        lockFieldBonusIcon.setPosition(speedX4BonusIcon.getXCoordinate() - speedX4BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - lockFieldBonusIcon.getObjectHeight() / 2);

        // CUT BAR 30 INITIALIZING
        cutBar30Bonus.addToScene(scene);
        cutBar30Bonus.getSprite().setScale(cutBar30Bonus.getSprite().getScaleX() / 2);
        cutBar30Bonus.getSprite().setCullingEnabled(true);
        cutBar30Bonus.setPosition(GameObject.MIDDLE);

        // CUT BAR 30 ICON INITIALIZING
        cutBar30BonusIcon.addToScene(scene);
        cutBar30BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        cutBar30BonusIcon.getSprite().setCullingEnabled(true);
        cutBar30BonusIcon.setPosition(lockFieldBonusIcon.getXCoordinate() - lockFieldBonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - cutBar30BonusIcon.getObjectHeight() / 2);

        // CUT BAR 50 INITIALIZING
        cutBar50Bonus.addToScene(scene);
        cutBar50Bonus.getSprite().setScale(cutBar50Bonus.getSprite().getScaleX() / 2);
        cutBar50Bonus.getSprite().setCullingEnabled(true);
        cutBar50Bonus.setPosition(GameObject.MIDDLE);

        // CUT BAR 50 ICON INITIALIZING
        cutBar50BonusIcon.addToScene(scene);
        cutBar50BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        cutBar50BonusIcon.getSprite().setCullingEnabled(true);
        cutBar50BonusIcon.setPosition(lockFieldBonusIcon.getXCoordinate() - lockFieldBonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - cutBar50BonusIcon.getObjectHeight() / 2);

        // REVERTED BAR INITIALIZING
        reverseBonus.addToScene(scene);
        reverseBonus.getSprite().setScale(reverseBonus.getSprite().getScaleX() / 2);
        reverseBonus.getSprite().setCullingEnabled(true);
        reverseBonus.setPosition(GameObject.MIDDLE);

        // REVERTED BAR ICON INITIALIZING
        reverseBonusIcon.addToScene(scene);
        reverseBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        reverseBonusIcon.getSprite().setCullingEnabled(true);
        reverseBonusIcon.setPosition(cutBar50BonusIcon.getXCoordinate() - cutBar50BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - reverseBonusIcon.getObjectHeight() / 2);

        // RUSH-HOUR INITIALIZING
        rushHourBonus.addToScene(scene);
        rushHourBonus.getSprite().setScale(rushHourBonus.getSprite().getScaleX() / 2);
        rushHourBonus.getSprite().setCullingEnabled(true);
        rushHourBonus.setPosition(GameObject.MIDDLE);

        // RUSH HOUR ICON INITIALIZING
        rushHourBonusIcon.addToScene(scene);
        rushHourBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        rushHourBonusIcon.getSprite().setCullingEnabled(true);
        rushHourBonusIcon.setPosition(reverseBonusIcon.getXCoordinate() - reverseBonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - rushHourBonusIcon.getObjectHeight() / 2);
    }


    //===========================================
    // HANDLERS
    //===========================================
    @SuppressWarnings("all")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "mHandler Called");
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            isConnected = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP ||
                                    fsmGame.getState() == FSMGame.STATE_GAME_SUSPENDED) {
                                winner = true;
                                gameOver();
                            }
                            fsmGame.setState(FSMGame.STATE_DISCONNECTED);
                            break;
                        default:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.i(TAG, "Message Write");
                    break;
                case Constants.MESSAGE_READ:
                    Log.i(TAG, "Message Read");
                    byte[] readBuf = (byte[]) msg.obj;
                    AppMessage recMsg = (AppMessage) Serializer.deserializeObject(readBuf);
                    if (recMsg != null) {
                        switch (recMsg.TYPE) {
                            //------------------------COORDS------------------------
                            case Constants.MSG_TYPE_COORDS:
                                Log.d(TAG, "Received : MSG_TYPE_COORDS");
                                if (!haveBall) {
//                                        float offset = 0;
//                                        if (recMsg.OP1 == 0) {
//                                            offset = ball.getObjectWidth();
//                                        } else if (recMsg.OP1 > 0) {
//                                            offset = 2f * ball.getObjectWidth();
//                                        }
                                    float offset = recMsg.OP1 * ball.getObjectWidth() + ball.getObjectWidth();
                                    float xPos = ((1 - recMsg.OP4) * CAMERA_WIDTH) - offset;
                                    float velX = -recMsg.OP1 * myModule * recMsg.OP2;
                                    float velY = myModule * recMsg.OP3;
                                    COS_X = recMsg.OP2;
                                    SIN_X = recMsg.OP3;
                                    ball.setPosition(xPos, -ball.getObjectHeight());
                                    ball.attach();
                                    ball.setHandlerSpeed(velX, velY);
                                    previous_event = TOP;
                                    transferringBall = true;
                                    haveBall = true;
                                    proximityRegion = true;
                                    textInfo.setText("");
                                }
                                break;
                            //------------------------SYNC------------------------
                            case Constants.MSG_TYPE_SYNC:
                                Log.d(TAG, "Received : MSG_TYPE_SYNC");
                                if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                    fsmGame.setState(FSMGame.STATE_IN_GAME);
                                    // Master Increase Played Games
                                    increasePartiteGiocate();
                                }
                                break;
                            //------------------------FAIL------------------------
                            case Constants.MSG_TYPE_FAIL:
                                Log.d(TAG, "Received : MSG_TYPE_FAIL");
                                if (fsmGame.getState() != FSMGame.STATE_GAME_WINNER &&
                                        fsmGame.getState() != FSMGame.STATE_GAME_LOSER &&
                                        fsmGame.getState() != FSMGame.STATE_DISCONNECTED &&
                                        fsmGame.getState() != FSMGame.STATE_OPPONENT_LEFT) {
                                    fsmGame.setState(FSMGame.STATE_OPPONENT_LEFT);
                                }
                                break;
                            //------------------------PAUSE------------------------
                            case Constants.MSG_TYPE_PAUSE:
                                Log.d(TAG, "Received : MSG_TYPE_PAUSE");
                                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                    fsmGame.setState(FSMGame.STATE_GAME_OPPONENT_PAUSED);
                                }
                                break;
                            //------------------------RESUME------------------------
                            case Constants.MSG_TYPE_RESUME:
                                Log.d(TAG, "Received : MSG_TYPE_RESUME");
                                if (fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                                        fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP) {
                                    fsmGame.setState(FSMGame.STATE_IN_GAME);
                                    if (taskTimeout != null) taskTimeout.cancel();
                                    if (timerTimeout != null) timerTimeout.cancel();
                                }
                                break;
                            //------------------------INTEGER------------------------
                            case Constants.MSG_TYPE_FIRST_START:
                                Log.d(TAG, "Received : MSG_TYPE_FIRST_START");
                                // Never happen
                                break;
                            //------------------------ALERT------------------------
                            case Constants.MSG_TYPE_ALERT:
                                Log.d(TAG, "Received : MSG_TYPE_ALERT");
                                if (fsmGame.getState() == FSMGame.STATE_DISCONNECTED ||
                                        fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                                        fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                                        fsmGame.getState() == FSMGame.STATE_OPPONENT_LEFT ||
                                        fsmGame.getState() == FSMGame.STATE_GAME_WINNER ||
                                        fsmGame.getState() == FSMGame.STATE_GAME_LOSER) {
                                    AppMessage notReadyMessage = new AppMessage(Constants.MSG_TYPE_NO_READY);
                                    sendBluetoothMessage(notReadyMessage);
                                }
                                break;
                            //------------------------STOP REQUEST------------------------
                            case Constants.MSG_TYPE_STOP_REQUEST:
                                Log.d(TAG, "Received : MSG_TYPE_STOP_REQUEST");
                                if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                    saveHandlerState();
                                }
                                if (fsmGame.getState() != FSMGame.STATE_GAME_PAUSE_STOP) {
                                    taskTimeout = new TimerGameTimeoutTask();
                                    timerTimeout = new Timer();
                                    timerTimeout.schedule(taskTimeout, Constants.TIMEOUT_FOR_GAME_OVER);
                                }
                                receivedStop = true;
                                fsmGame.setState(FSMGame.STATE_GAME_PAUSE_STOP);
                                if (!haveBall && ball.getYCoordinate() < 0) {
                                    ball.detach();
                                }
                                break;
                            //------------------------SUSPEND REQUEST------------------------
                            case Constants.MSG_TYPE_SUSPEND_REQUEST:
                                Log.d(TAG, "Received : MSG_TYPE_SUSPEND_REQUEST");
                                if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP) {
                                    fsmGame.setState(FSMGame.STATE_GAME_SUSPENDED);
                                }
                                break;
                            //------------------------RESUME AFTER SUSPEND------------------------
                            case Constants.MSG_TYPE_RESUME_AFTER_SUSPEND:
                                Log.d(TAG, "Received : MSG_TYPE_RESUME_AFTER_SUSPEND");
                                if (fsmGame.getState() == FSMGame.STATE_GAME_SUSPENDED) {
                                    resumeAllowed = true;
                                    fsmGame.setState(FSMGame.STATE_GAME_PAUSE_STOP);
                                }
                                break;
                            //------------------------NOREADY------------------------
                            case Constants.MSG_TYPE_NO_READY:
                                Log.d(TAG, "Received : MSG_TYPE_NO_READY");
                                if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                    fsmGame.setState(FSMGame.STATE_OPPONENT_NOT_READY);
                                }
                                break;
                            //------------------------POINT UP------------------------
                            case Constants.MSG_TYPE_POINT_UP:
                                Log.d(TAG, "Received : MSG_TYPE_POINT_UP");
                                addScore();
                                break;
                            //------------------------TIMEOUT-----------------------
                            case Constants.MSG_TYPE_GAME_TIMEOUT:
                                Log.d(TAG, "Received : MSG_TYPE_GAME_TIMEOUT");
                                fsmGame.setState(FSMGame.STATE_GAME_LOSER);
                                break;
                            //------------------------GAME OVER-----------------------
                            case Constants.MSG_TYPE_GAME_OVER:
                                Log.d(TAG, "Received : MSG_TYPE_GAME_OVER");
                                fsmGame.setState(FSMGame.STATE_GAME_LOSER);
                                break;
                            //------------------------BONUS SPEED X2------------------------
                            case Constants.MSG_TYPE_BONUS_SPEED_X2:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEED_X2");
                                bonusManager.addBonus(Constants.SPEED_X2, recMsg.OP1);
                                break;
                            //------------------------BONUS SPEED X3------------------------
                            case Constants.MSG_TYPE_BONUS_SPEED_X3:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEED_X3");
                                bonusManager.addBonus(Constants.SPEED_X3, recMsg.OP1);
                                break;
                            //------------------------BONUS SPEED X4------------------------
                            case Constants.MSG_TYPE_BONUS_SPEED_X4:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEED_X4");
                                bonusManager.addBonus(Constants.SPEED_X4, recMsg.OP1);
                                break;
                            //------------------------BONUS LOCK_FIELD------------------------
                            case Constants.MSG_TYPE_BONUS_LOCK_FIELD:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_LOCK_FIELD");
                                bonusManager.addBonus(Constants.LOCK_FIELD, recMsg.OP1);
                                break;
                            //------------------------BONUS CUT_BAR_30------------------------
                            case Constants.MSG_TYPE_BONUS_CUT_BAR_30:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_CUT_BAR_30");
                                bonusManager.addBonus(Constants.CUT_BAR_30, recMsg.OP1);
                                break;
                            //------------------------BONUS CUT_BAR_50------------------------
                            case Constants.MSG_TYPE_BONUS_CUT_BAR_50:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_CUT_BAR_50");
                                bonusManager.addBonus(Constants.CUT_BAR_50, recMsg.OP1);
                                break;
                            //------------------------BONUS REVERSE------------------------
                            case Constants.MSG_TYPE_BONUS_REVERSE_BAR:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_REVERSE_BAR");
                                bonusManager.addBonus(Constants.REVERSE, recMsg.OP1);
                                break;
                            //------------------------BONUS RUSH-HOUR------------------------
                            case Constants.MSG_TYPE_BONUS_RUSH_HOUR:
                                Log.d(TAG, "Received : MSG_TYPE_BONUS_RUSH_HOUR");
                                bonusManager.addBonus(Constants.RUSH_HOUR, recMsg.OP1);
                                break;
                            default:
                                Log.d(TAG, "Received : Incorrect Message - Type is " + recMsg.TYPE);
                        }
                    } else {
                        Log.e(TAG, "Received : Null Message");
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    @SuppressWarnings("all")
    private final Handler fsmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        //-----------------------------------------------------
                        // STATE NOT READY
                        //-----------------------------------------------------
                        case FSMGame.STATE_NOT_READY:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            break;
                        //-----------------------------------------------------
                        // STATE CONNECTED
                        //-----------------------------------------------------
                        case FSMGame.STATE_CONNECTED:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            break;
                        //-----------------------------------------------------
                        // STATE IN GAME
                        //-----------------------------------------------------
                        case FSMGame.STATE_IN_GAME:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            ball.setHandlerSpeed(old_x_speed, old_y_speed);
                            bar.setBarSpeed(old_bar_speed);
                            if (rush_hour) {
                                rushHour.restartAfterPause();
                            }
                            if (receivedStop && haveBall && Math.signum(ball.getHandlerSpeedY()) > 0) {
                                ball.setHandlerSpeedX(-ball.getHandlerSpeedX());
                                ball.setHandlerSpeedY(-ball.getHandlerSpeedY());
                                previous_event = NO_COLL;
                            }
                            pause = false;
                            receivedStop = false;
                            resumeAllowed = false;
                            taskBonus = new TimerBonusTask();
                            timerBonus = new Timer();
                            scheduleDelay = scheduleDelay < 0 ? 0 : scheduleDelay;
                            timerBonus.schedule(taskBonus, scheduleDelay, Constants.BONUS_REPEATING_TIME_MILLIS);
                            textInfo.setText(" ");
                            break;
                        //-----------------------------------------------------
                        // STATE IN GAME WAITING
                        //-----------------------------------------------------
                        case FSMGame.STATE_IN_GAME_WAITING:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            textInfo.setText(getResources().getString(R.string.text_waiting));
                            break;
                        //-----------------------------------------------------
                        // STATE GAME PAUSED
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_PAUSED:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            pause = true;
                            textInfo.setText(getResources().getString(R.string.text_pause));
                            saveHandlerState();
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            if (taskBonus.scheduledExecutionTime() != 0) {
                                previousScheduleTime = taskBonus.scheduledExecutionTime();
                            }
                            scheduleDelay = Constants.BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                            if (timerBonus != null) timerBonus.cancel();
                            break;
                        //-----------------------------------------------------
                        // STATE GAME STOP
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_PAUSE_STOP:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            pause = true;
                            textInfo.setText(getResources().getString(R.string.pause_stop));
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            if (taskBonus != null) {
                                if (taskBonus.scheduledExecutionTime() != 0) {
                                    previousScheduleTime = taskBonus.scheduledExecutionTime();
                                }
                                scheduleDelay = Constants.BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                                taskBonus = null;
                            }
                            if (timerBonus != null) timerBonus.cancel();
                            break;
                        //-----------------------------------------------------
                        // STATE GAME OPPONENT PAUSED
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_OPPONENT_PAUSED:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_opponent_pause));
                            saveHandlerState();
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            if (taskBonus.scheduledExecutionTime() != 0) {
                                previousScheduleTime = taskBonus.scheduledExecutionTime();
                            }
                            scheduleDelay = Constants.BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                            if (timerBonus != null) timerBonus.cancel();
                            break;
                        //-----------------------------------------------------
                        // STATE OPPONENT NOT READY
                        //-----------------------------------------------------
                        case FSMGame.STATE_OPPONENT_NOT_READY:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_opponent_not_ready));
                            break;
                        //-----------------------------------------------------
                        // STATE DISCONNECTED
                        //-----------------------------------------------------
                        case FSMGame.STATE_DISCONNECTED:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_disconnected));
                            isConnected = false;
                            mConnectedDeviceName = "";
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            isMaster = false;
                            break;
                        //-----------------------------------------------------
                        // STATE GAME SUSPENDED
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_SUSPENDED:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_game_suspended));
                            break;
                        //-----------------------------------------------------
                        // STATE OPPONENT LEFT
                        //-----------------------------------------------------
                        case FSMGame.STATE_OPPONENT_LEFT:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            ball.setHandlerSpeed(0f, 0f);
                            bar.setBarSpeed(0f);
                            if (rush_hour) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rush_hour = false;
                                        rushHour.clear();
                                    }
                                });
                            }
                            textInfo.setText(getResources().getString(R.string.text_opponent_left));
                            winner = true;
                            gameOver();
                            break;
                        //-----------------------------------------------------
                        // STATE WINNER
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_WINNER:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_you_win));
                            winner = true;
                            gameOver();
                            break;
                        //-----------------------------------------------------
                        // STATE LOSER
                        //-----------------------------------------------------
                        case FSMGame.STATE_GAME_LOSER:
                            Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                            textInfo.setText(getResources().getString(R.string.text_you_lose));
                            winner = false;
                            gameOver();
                            break;
                        default:
                            Log.d(TAG, "Invalid State : " + msg.arg1);
                            break;
                    }
                default:
                    break;
            }
        }
    };

    @SuppressWarnings("all")
    private final Handler bonusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BonusManager.BONUS_CREATED:
                    switch (msg.arg1) {
                        case Constants.SPEED_X2:
                            Log.d(TAG, "Bonus Created : SPEED_X2");
                            setVelocityBonus(Constants.SPEED_X2);
                            safeAttachSpriteIcon(SPEED_X2_ICON);
                            break;
                        case Constants.SPEED_X3:
                            Log.d(TAG, "Bonus Created : SPEED_X3");
                            setVelocityBonus(Constants.SPEED_X3);
                            safeAttachSpriteIcon(SPEED_X3_ICON);
                            break;
                        case Constants.SPEED_X4:
                            Log.d(TAG, "Bonus Created : SPEED_X4");
                            setVelocityBonus(Constants.SPEED_X4);
                            safeAttachSpriteIcon(SPEED_X4_ICON);
                            break;
                        case Constants.LOCK_FIELD:
                            Log.d(TAG, "Bonus Created : LOCK_FIELD");
                            safeAttachSpriteIcon(LOCK_FIELD_ICON);
                            synchronized (this) {
                                locksField = true;
                            }
                            break;
                        case Constants.CUT_BAR_30:
                            Log.d(TAG, "Bonus Created : CUT_BAR_30");
                            bar.setObjectWidth(0.7f * bar.getBarWidth());
                            safeAttachSpriteIcon(CUT_BAR_30_ICON);
                            break;
                        case Constants.CUT_BAR_50:
                            Log.d(TAG, "Bonus Created : CUT_BAR_50");
                            bar.setObjectWidth(0.5f * bar.getBarWidth());
                            safeAttachSpriteIcon(CUT_BAR_50_ICON);
                            break;
                        case Constants.REVERSE:
                            Log.d(TAG, "Bonus Created : REVERSE");
                            safeAttachSpriteIcon(REVERTED_BAR_ICON);
                            if (Math.signum(bar.getBarSpeed()) > 0)
                                bar.setBarSpeed(-bar.getBarSpeed());
                            break;
                        case Constants.RUSH_HOUR:
                            Log.d(TAG, "Bonus Created : RUSH_HOUR");
                            safeAttachSpriteIcon(RUSH_HOUR_ICON);
                            if (!rush_hour) {
                                rush_hour = true;
                                rushHour.addToScene(scene);
                            }
                            break;

                        default:
                            Log.e(TAG, "Error - Invalid Bonus ID Created");
                            break;
                    }
                    break;
                case BonusManager.BONUS_EXPIRED:
                    switch (msg.arg1) {
                        case Constants.SPEED_X2:
                            Log.d(TAG, "Bonus Expired : SPEED_X2");
                            setVelocityBonus(Constants.NO_BONUS);
                            speedX2BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X2_ICON));
                            break;
                        case Constants.SPEED_X3:
                            Log.d(TAG, "Bonus Expired : SPEED_X3");
                            setVelocityBonus(Constants.NO_BONUS);
                            speedX3BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X3_ICON));
                            break;
                        case Constants.SPEED_X4:
                            Log.d(TAG, "Bonus Expired : SPEED_X4");
                            setVelocityBonus(Constants.NO_BONUS);
                            speedX4BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X4_ICON));
                            break;
                        case Constants.LOCK_FIELD:
                            Log.d(TAG, "Bonus Expired : LOCK_FIELD");
                            synchronized (this) {
                                locksField = false;
                            }
                            lockFieldBonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(LOCK_FIELD_ICON));
                            break;
                        case Constants.CUT_BAR_30:
                            Log.d(TAG, "Bonus Expired : CUT_BAR_30");
                            bar.setObjectWidth(bar.getBarWidth());
                            cutBar30BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(CUT_BAR_30_ICON));
                            break;
                        case Constants.CUT_BAR_50:
                            Log.d(TAG, "Bonus Expired : CUT_BAR_50");
                            bar.setObjectWidth(bar.getBarWidth());
                            cutBar50BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(CUT_BAR_50_ICON));
                            break;
                        case Constants.REVERSE:
                            Log.d(TAG, "Bonus Expired : REVERSE");
                            if (Math.signum(bar.getBarSpeed()) < 0)
                                bar.setBarSpeed(-bar.getBarSpeed());
                            reverseBonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(REVERTED_BAR_ICON));
                            break;
                        case Constants.RUSH_HOUR:
                            Log.d(TAG, "Bonus Expired : RUSH_HOUR");
                            runOnUpdateThread(new Runnable() {
                                @Override
                                public void run() {
                                    rush_hour = false;
                                    rushHour.clear();
                                }
                            });
                            rushHourBonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(RUSH_HOUR_ICON));
                            break;

                        default:
                            Log.e(TAG, "Error - Invalid Bonus ID Expired");
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Safetely attach and detach icon sprite
     *
     * @param spriteID ID of sprite to attach
     */
    private void safeAttachSpriteIcon(Integer spriteID) {
        switch (spriteID) {
            case SPEED_X2_ICON:
                if (!bonusStatusArray.contains(SPEED_X2_ICON)) {
                    speedX2BonusIcon.attach();
                    bonusStatusArray.add(SPEED_X2_ICON);
                    if (bonusStatusArray.contains(SPEED_X3_ICON)) {
                        speedX3BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X3_ICON));
                    }
                    if (bonusStatusArray.contains(SPEED_X4_ICON)) {
                        speedX4BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X4_ICON));
                    }
                }
                break;
            case SPEED_X3_ICON:
                if (!bonusStatusArray.contains(SPEED_X3_ICON)) {
                    speedX3BonusIcon.attach();
                    bonusStatusArray.add(SPEED_X3_ICON);
                    if (bonusStatusArray.contains(SPEED_X2_ICON)) {
                        speedX2BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X2_ICON));
                    }
                    if (bonusStatusArray.contains(SPEED_X4_ICON)) {
                        speedX4BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X4_ICON));
                    }
                }
                break;
            case SPEED_X4_ICON:
                if (!bonusStatusArray.contains(SPEED_X4_ICON)) {
                    speedX4BonusIcon.attach();
                    bonusStatusArray.add(SPEED_X4_ICON);
                    if (bonusStatusArray.contains(SPEED_X2_ICON)) {
                        speedX2BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X2_ICON));
                    }
                    if (bonusStatusArray.contains(SPEED_X3_ICON)) {
                        speedX3BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(SPEED_X3_ICON));
                    }
                }
                break;
            case LOCK_FIELD_ICON:
                if (!bonusStatusArray.contains(LOCK_FIELD_ICON)) {
                    lockFieldBonusIcon.attach();
                    bonusStatusArray.add(LOCK_FIELD_ICON);
                }
                break;
            case RUSH_HOUR_ICON:
                if (!bonusStatusArray.contains(RUSH_HOUR_ICON)) {
                    rushHourBonusIcon.attach();
                    bonusStatusArray.add(RUSH_HOUR_ICON);
                }
                break;
            case CUT_BAR_30_ICON:
                if (!bonusStatusArray.contains(CUT_BAR_30_ICON)) {
                    cutBar30BonusIcon.attach();
                    bonusStatusArray.add(CUT_BAR_30_ICON);
                    if (bonusStatusArray.contains(CUT_BAR_50_ICON)) {
                        cutBar50BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(CUT_BAR_50_ICON));
                    }
                }
                break;
            case CUT_BAR_50_ICON:
                if (!bonusStatusArray.contains(CUT_BAR_50_ICON)) {
                    cutBar50BonusIcon.attach();
                    bonusStatusArray.add(CUT_BAR_50_ICON);
                    if (bonusStatusArray.contains(CUT_BAR_30_ICON)) {
                        cutBar30BonusIcon.detachOnUIThread();
                        bonusStatusArray.remove(Integer.valueOf(CUT_BAR_30_ICON));
                    }
                }
                break;
            case REVERTED_BAR_ICON:
                if (!bonusStatusArray.contains(REVERTED_BAR_ICON)) {
                    reverseBonusIcon.attach();
                    bonusStatusArray.add(REVERTED_BAR_ICON);
                }
                break;
        }
    }

    private void setVelocityBonus(int nextBonus) {
        switch (nextBonus) {
            case Constants.NO_BONUS:
                myModule = mSPEED_X1;
                break;
            case Constants.SPEED_X2:
                myModule = mSPEED_X2;
                break;
            case Constants.SPEED_X3:
                myModule = mSPEED_X3;
                break;
            case Constants.SPEED_X4:
                myModule = mSPEED_X4;
                break;
            default:
                break;
        }
        if (haveBall) {
            ball.setHandlerSpeedX(Math.signum(ball.getHandlerSpeedX()) * myModule * COS_X);
            ball.setHandlerSpeedY(Math.signum(ball.getHandlerSpeedY()) * myModule * SIN_X);
        }
    }

    //===========================================
    // TASKS
    //===========================================
    private class TimerBonusTask extends TimerTask {

        private int bonusChoice = Constants.NO_BONUS;

        @Override
        public void run() {
            Random rand = new Random();
            do {
                bonusChoice = rand.nextInt((Constants.RUSH_HOUR - Constants.SPEED_X2) + 1) + Constants.SPEED_X2;
            } while (bonusChoice == previous_bonus);
            previous_bonus = bonusChoice;
            if (deletedBonusSprite) {
                attachSprite(bonusChoice);
            } else {
                detachSprite(activedBonusSprite);
                attachSprite(bonusChoice);
            }
        }

        @Override
        public boolean cancel() {
            if (activedBonusSprite != SPRITE_NONE) {
                detachSprite(activedBonusSprite);
                activedBonusSprite = SPRITE_NONE;
            }
            return super.cancel();
        }
    }

    private class TimerGameTimeoutTask extends TimerTask {
        @Override
        public void run() {
            if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP) {
                AppMessage lostYourGameMessage = new AppMessage(Constants.MSG_TYPE_GAME_TIMEOUT);
                sendBluetoothMessage(lostYourGameMessage);
                fsmGame.setState(FSMGame.STATE_GAME_WINNER);
            }
        }
    }

    //===========================================
    // TASKS UTILS
    //===========================================

    /**
     * Attach activator bonus on screen
     *
     * @param bonusID ID of activator bonus
     */
    private void attachSprite(int bonusID) {
        switch (bonusID) {
            case Constants.SPEED_X2:
                speedX2Bonus.registerTouch();
                speedX2Bonus.attach();
                activedBonusSprite = Constants.SPEED_X2;
                deletedBonusSprite = false;
                break;

            case Constants.SPEED_X3:
                speedX3Bonus.registerTouch();
                speedX3Bonus.attach();
                activedBonusSprite = Constants.SPEED_X3;
                deletedBonusSprite = false;
                break;

            case Constants.SPEED_X4:
                speedX4Bonus.registerTouch();
                speedX4Bonus.attach();
                activedBonusSprite = Constants.SPEED_X4;
                deletedBonusSprite = false;
                break;

            case Constants.LOCK_FIELD:
                lockFieldBonus.registerTouch();
                lockFieldBonus.attach();
                activedBonusSprite = Constants.LOCK_FIELD;
                deletedBonusSprite = false;
                break;

            case Constants.CUT_BAR_30:
                cutBar30Bonus.registerTouch();
                cutBar30Bonus.attach();
                activedBonusSprite = Constants.CUT_BAR_30;
                deletedBonusSprite = false;
                break;

            case Constants.CUT_BAR_50:
                cutBar50Bonus.registerTouch();
                cutBar50Bonus.attach();
                activedBonusSprite = Constants.CUT_BAR_50;
                deletedBonusSprite = false;
                break;

            case Constants.REVERSE:
                reverseBonus.registerTouch();
                reverseBonus.attach();
                activedBonusSprite = Constants.REVERSE;
                deletedBonusSprite = false;
                break;

            case Constants.RUSH_HOUR:
                rushHourBonus.registerTouch();
                rushHourBonus.attach();
                activedBonusSprite = Constants.RUSH_HOUR;
                deletedBonusSprite = false;
                break;

            default:
                Log.e(TAG, "Error in attachSprite(). Invalid ID");
                break;
        }
    }

    /**
     * Detach activator bonus sprite from the scene
     *
     * @param bonusID ID of activator bonus
     */
    private void detachSprite(int bonusID) {
        switch (bonusID) {
            case Constants.SPEED_X2:
                speedX2Bonus.unregisterTouch();
                speedX2Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.SPEED_X3:
                speedX3Bonus.unregisterTouch();
                speedX3Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.SPEED_X4:
                speedX4Bonus.unregisterTouch();
                speedX4Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.LOCK_FIELD:
                lockFieldBonus.unregisterTouch();
                lockFieldBonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.CUT_BAR_30:
                cutBar30Bonus.unregisterTouch();
                cutBar30Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.CUT_BAR_50:
                cutBar50Bonus.unregisterTouch();
                cutBar50Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.REVERSE:
                reverseBonus.unregisterTouch();
                reverseBonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case Constants.RUSH_HOUR:
                rushHourBonus.unregisterTouch();
                rushHourBonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            default:
                Log.e(TAG, "Error in detachSprite(). Invalid ID");
                break;
        }
    }
}
