package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import it.unina.is2project.sensorgames.FSMGame;
import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.TwoPlayerActivity;
import it.unina.is2project.sensorgames.bluetooth.BluetoothService;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.bluetooth.Serializer;
import it.unina.is2project.sensorgames.bluetooth.messages.AppMessage;
import it.unina.is2project.sensorgames.game.entity.GameObject;
import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatTwoPlayerDAO;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatTwoPlayer;

public class GamePongTwoPlayer extends GamePong {

    private final String TAG = "2PlayersGame";

    // Indicates the holding's ball
    private boolean haveBall = false;
    // Indicates who starts game
    private boolean isMaster = false;
    // Indicates when ball is passing
    private boolean transferringBall;
    // Indicates who backs activity
    private boolean backPressed = false;
    // Service bluetooth
    private BluetoothService mBluetoothService = null;
    // Finite State Machine
    private FSMGame fsmGame = null;
    // Bonus manager
    private BonusManager bonusManager = null;
    // Text information
    private Text textInfo;
    private Text textPoint;
    // Points to reach
    private int points;
    // User nickname
    private String nickname;
    // Indicates the winner of the match
    private boolean winner = false;
    // Return intent extra
    public static String EXTRA_MASTER = "isMaster_boolean";
    public static String EXTRA_CONNECTION_STATE = "isConnected_boolean";
    public static String EXTRA_DEVICE = "deviceName_string";
    // Pause Utils
    private long tap;
    private boolean proximityRegion;
    private int PROXIMITY_ZONE;
    private int previous_bonus;
    private boolean resumeAllowed;
    private boolean receivedStop;
    private final long BONUS_REPEATING_TIME_MILLIS = 7000;
    private final long TIMEOUT_FOR_GAME_OVER = 120000;
    // Connections Utils
    private boolean isConnected;
    private String mConnectedDeviceName = "";
    // Score variables
    private int score;
    private int opponentScore;
    // Constant Utils
    private final int SPRITE_NONE = -1;

    // Speed X2 Bonus
    private GameObject speedX2Bonus;
    // Speed X2 Icon
    private GameObject speedX2BonusIcon;
    // Speed X3 Bonus
    private GameObject speedX3Bonus;
    // Speed X3 Icon
    private GameObject speedX3BonusIcon;
    // Speed X4 Bonus
    private GameObject speedX4Bonus;
    // Speed X4 Icon
    private GameObject speedX4BonusIcon;
    // Lock Field Bonus
    private GameObject lockFieldBonus;
    // Lock Field Icon
    private GameObject lockFieldBonusIcon;
    // Cut Bar 30 Bonus
    private GameObject cutBar30Bonus;
    // Cut Bar 30 Icon
    private GameObject cutBar30BonusIcon;
    // Cut Bar 50 Bonus
    private GameObject cutBar50Bonus;
    // Cut Bar 50 Icon
    private GameObject cutBar50BonusIcon;
    // Reverted Bar Bonus
    private GameObject reverseBonus;
    // Reverted Bar Icon
    private GameObject reverseBonusIcon;
    // Rush-Hour Bonus
    private GameObject rushHourBonus;
    // Rush-Hour Icon
    private GameObject rushHourBonusIcon;

    // Bonus Constants
    public static final int NO_BONUS = 1;
    public static final int SPEED_X2 = 2;
    public static final int SPEED_X3 = 3;
    public static final int SPEED_X4 = 4;
    public static final int LOCK_FIELD = 5;
    public static final int CUT_BAR_30 = 6;
    public static final int CUT_BAR_50 = 7;
    public static final int REVERTED_BAR = 8;
    public static final int RUSH_HOUR = 9;
    // Bonus Icon Constants
    private static final int SPEED_X2_ICON = 10;
    private static final int SPEED_X3_ICON = 11;
    private static final int SPEED_X4_ICON = 12;
    private static final int LOCK_FIELD_ICON = 13;
    private static final int CUT_BAR_30_ICON = 14;
    private static final int CUT_BAR_50_ICON = 15;
    private static final int REVERTED_BAR_ICON = 16;
    private static final int RUSH_HOUR_ICON = 17;
    // Bonus Utils
    private int activedBonusSprite = SPRITE_NONE;
    private float SPRITE_ICON_SIZE;
    private boolean deletedBonusSprite = true;
    private List<Integer> bonusStatusArray = new ArrayList<>();
    TimerTask taskBonus;
    Timer timerBonus;
    private long scheduleDelay;
    private long previousScheduleTime;
    private boolean locksField = false;
    private boolean rush_hour = false;
    private float mSPEED_X1;
    private float mSPEED_X2;
    private float mSPEED_X3;
    private float mSPEED_X4;
    TimerTask taskTimeout;
    Timer timerTimeout;


    @Override
    protected Scene onCreateScene() {
        Log.d("LifeCycle", "onCreateScene()");
        // Getting instance of fsm, service bluetooth and bonus manager
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
        bonusManager = BonusManager.getBonusInstance(bonusHandler);

        //Retrieve user nickname
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        nickname = sharedPreferences.getString(Constants.PREF_NICKNAME, getString(R.string.txt_no_name));

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
        mConnectedDeviceName = i.getStringExtra(TwoPlayerActivity.EXTRA_DEVICENAME);

        // Set result in case of failure
        setResult(Activity.RESULT_CANCELED);

        // Setting up the physics of the game
        settingPhysics();

        // Setting variables
        isConnected = true;
        proximityRegion = false;
        transferringBall = false;
        receivedStop = false;
        resumeAllowed = true;
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
        previous_event = -1;
        previous_bonus = NO_BONUS;

        // Attaching textInfo
        textInfo = new Text(10, 10, font, "", 30, getVertexBufferObjectManager());
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
            increasePartiteGiocate(nickname);
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
                    detachSprite(SPEED_X2);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx2Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX2, randNum);
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
                    detachSprite(SPEED_X3);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx3Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX3, randNum);
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
                    detachSprite(SPEED_X4);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage speedx4Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX4, randNum);
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
                    detachSprite(LOCK_FIELD);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage lockFieldMessage = new AppMessage(Constants.MSG_TYPE_BONUS_LOCKFIELD, randNum);
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
                    detachSprite(CUT_BAR_30);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage cutBar30Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUTBAR30, randNum);
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
                    detachSprite(CUT_BAR_50);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage cutBar50Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUTBAR50, randNum);
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
                    detachSprite(REVERTED_BAR);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage revertedBarMessage = new AppMessage(Constants.MSG_TYPE_BONUS_REVERTEDBAR, randNum);
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
                    detachSprite(RUSH_HOUR);
                    Random rand = new Random();
                    int randNum = rand.nextInt(5) + 1;
                    AppMessage rushHourMessage = new AppMessage(Constants.MSG_TYPE_BONUS_RUSHHOUR, randNum);
                    sendBluetoothMessage(rushHourMessage);
                }
            }
        };
        // Rush Hour Icon
        rushHourBonusIcon = new GameObject(this, R.drawable.rushhour_icon);
    }

    @Override
    protected void settingPhysics() {
        doPhysics();
    }

    @Override
    protected synchronized void onResume() {
        backPressed = false;
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
                if(timerTimeout != null)timerTimeout.cancel();
                if(taskTimeout != null)taskTimeout.cancel();
            }
            //TODO Contemplare il caso in cui ti trovi IN GAME WAITING
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
//            Log.d(TAG, "CollisionTop - S: X: " + ball.getXCoordinate() + " of " + CAMERA_WIDTH);
//            Log.d(TAG, "CollisionTop - S: XRatio: " + xRatio);
//            Log.d(TAG, "CollisionTop - S: Module " + myModule);
//            Log.d(TAG, "CollisionTop - S: VelX " + ball.getHandlerSpeedX());
//            Log.d(TAG, "CollisionTop - S: VelY " + ball.getHandlerSpeedY());
//            Log.d(TAG, "CollisionTop - S: Sign(Velx) " + Math.signum(ball.getHandlerSpeedX()));
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
        saveGame(nickname);
    }

    private void increasePartiteGiocate(String user) {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player currentPlayer = playerDAO.findByNome(user);
        long idPlayer;

        if (currentPlayer == null) {
            currentPlayer = new Player(user);
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
    protected void saveGame(String user) {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player currentPlayer = playerDAO.findByNome(user);
        long idPlayer;

//        if(currentPlayer == null){
//            currentPlayer = new Player(user);
//            idPlayer = playerDAO.insert(currentPlayer);
//        }else idPlayer = currentPlayer.getId();
        idPlayer = currentPlayer.getId();

        StatTwoPlayerDAO statTwoPlayerDAO = new StatTwoPlayerDAO(getApplicationContext());
        StatTwoPlayer currentPlayerStats = statTwoPlayerDAO.findById((int) idPlayer);

//        if(currentPlayerStats == null){
//            currentPlayerStats = new StatTwoPlayer((int)idPlayer, 0, 0);
//            statTwoPlayerDAO.insert(currentPlayerStats);
//        }


//        currentPlayerStats.increasePartiteGiocate();

        if (winner)
            currentPlayerStats.increasePartiteVinte();

        statTwoPlayerDAO.update(currentPlayerStats);

        playerDAO.close();
        statTwoPlayerDAO.close();
    }

    @Override
    public void addScore() {
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
                fsmGame.setState(FSMGame.STATE_IN_GAME);
                AppMessage resumeMessage = new AppMessage(Constants.MSG_TYPE_RESUME);
                sendBluetoothMessage(resumeMessage);
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
        setResult(Activity.RESULT_CANCELED, intent);
        if (timerBonus != null) timerBonus.cancel();
        super.onBackPressed();
    }

    //----------------------------------------------
    // MISCELLANEA
    //----------------------------------------------
    private synchronized void sendBluetoothMessage(AppMessage message) {
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

    private boolean checkTouchOnSprite(int bonusID, float x, float y) {
        boolean checkTouchSpriteStatus;
        switch (bonusID) {
            case SPEED_X2:
                checkTouchSpriteStatus = speedX2Bonus.checkOnTouch(x, y);
                break;
            case SPEED_X3:
                checkTouchSpriteStatus = speedX3Bonus.checkOnTouch(x, y);
                break;
            case SPEED_X4:
                checkTouchSpriteStatus = speedX4Bonus.checkOnTouch(x, y);
                break;
            case LOCK_FIELD:
                checkTouchSpriteStatus = lockFieldBonus.checkOnTouch(x, y);
                break;
            case CUT_BAR_30:
                checkTouchSpriteStatus = cutBar30Bonus.checkOnTouch(x, y);
                break;
            case CUT_BAR_50:
                checkTouchSpriteStatus = cutBar50Bonus.checkOnTouch(x, y);
                break;
            case REVERTED_BAR:
                checkTouchSpriteStatus = reverseBonus.checkOnTouch(x, y);
                break;
            case RUSH_HOUR:
                checkTouchSpriteStatus = rushHourBonus.checkOnTouch(x, y);
                break;
            default:
                checkTouchSpriteStatus = false;
                break;
        }
        return checkTouchSpriteStatus;
    }

    private void saveHandlerState() {
        Log.d(TAG, "Save Handler State");
        old_x_speed = ball.getHandlerSpeedX();
        old_y_speed = ball.getHandlerSpeedY();
        old_bar_speed = bar.getBarSpeed();
        if (rush_hour) {
            rushHour.pause();
        }
    }

    private void initializeSprite() {

        // SPEED X2 BONUS INITIALIZING
        speedX2Bonus.addToScene(scene);
        speedX2Bonus.getSprite().setScale(speedX2Bonus.getSprite().getScaleX() / 2);
        speedX2Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X2 ICON INITIALIZING
        speedX2BonusIcon.addToScene(scene);
        speedX2BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX2BonusIcon.setPosition(CAMERA_WIDTH - speedX2BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX2BonusIcon.getObjectHeight() / 2);

        // SPEED X3 BONUS INITIALIZING
        speedX3Bonus.addToScene(scene);
        speedX3Bonus.getSprite().setScale(speedX3Bonus.getSprite().getScaleX() / 2);
        speedX3Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X3 ICON INITIALIZING
        speedX3BonusIcon.addToScene(scene);
        speedX3BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX3BonusIcon.setPosition(speedX2BonusIcon.getXCoordinate() - speedX2BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX3BonusIcon.getObjectHeight() / 2);

        // SPEED X4 BONUS INITIALIZING
        speedX4Bonus.addToScene(scene);
        speedX4Bonus.getSprite().setScale(speedX4Bonus.getSprite().getScaleX() / 2);
        speedX4Bonus.setPosition(GameObject.MIDDLE);

        // SPEED X4 ICON INITIALIZING
        speedX4BonusIcon.addToScene(scene);
        speedX4BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        speedX4BonusIcon.setPosition(speedX3BonusIcon.getXCoordinate() - speedX3BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - speedX4BonusIcon.getObjectHeight() / 2);

        // LOCK FIELD INITIALIZING
        lockFieldBonus.addToScene(scene);
        lockFieldBonus.getSprite().setScale(lockFieldBonus.getSprite().getScaleX() / 2);
        lockFieldBonus.setPosition(GameObject.MIDDLE);

        // LOCK FIELD ICON INITIALIZING
        lockFieldBonusIcon.addToScene(scene);
        lockFieldBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        lockFieldBonusIcon.setPosition(speedX4BonusIcon.getXCoordinate() - speedX4BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - lockFieldBonusIcon.getObjectHeight() / 2);

        // CUT BAR 30 INITIALIZING
        cutBar30Bonus.addToScene(scene);
        cutBar30Bonus.getSprite().setScale(cutBar30Bonus.getSprite().getScaleX() / 2);
        cutBar30Bonus.setPosition(GameObject.MIDDLE);

        // CUT BAR 30 ICON INITIALIZING

        cutBar30BonusIcon.addToScene(scene);
        cutBar30BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        cutBar30BonusIcon.setPosition(lockFieldBonusIcon.getXCoordinate() - lockFieldBonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - cutBar30BonusIcon.getObjectHeight() / 2);

        // CUT BAR 50 INITIALIZING
        cutBar50Bonus.addToScene(scene);
        cutBar50Bonus.getSprite().setScale(cutBar50Bonus.getSprite().getScaleX() / 2);
        cutBar50Bonus.setPosition(GameObject.MIDDLE);

        // CUT BAR 50 ICON INITIALIZING
        cutBar50BonusIcon.addToScene(scene);
        cutBar50BonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        cutBar50BonusIcon.setPosition(cutBar30BonusIcon.getXCoordinate() - cutBar30BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - cutBar50BonusIcon.getObjectHeight() / 2);

        // REVERTED BAR INITIALIZING
        reverseBonus.addToScene(scene);
        reverseBonus.getSprite().setScale(reverseBonus.getSprite().getScaleX() / 2);
        reverseBonus.setPosition(GameObject.MIDDLE);

        // REVERTED BAR ICON INITIALIZING
        reverseBonusIcon.addToScene(scene);
        reverseBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        reverseBonusIcon.setPosition(cutBar50BonusIcon.getXCoordinate() - cutBar50BonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - reverseBonusIcon.getObjectHeight() / 2);

        // RUSH-HOUR INITIALIZING

        rushHourBonus.addToScene(scene);
        rushHourBonus.getSprite().setScale(rushHourBonus.getSprite().getScaleX() / 2);
        rushHourBonus.setPosition(GameObject.MIDDLE);

        // RUSH HOUR ICON INITIALIZING
        rushHourBonusIcon.addToScene(scene);
        rushHourBonusIcon.setObjectDimension(SPRITE_ICON_SIZE);
        rushHourBonusIcon.setPosition(reverseBonusIcon.getXCoordinate() - reverseBonusIcon.getObjectWidth(), textPoint.getY() + textPoint.getHeight() / 2 - rushHourBonusIcon.getObjectHeight() / 2);
    }


    //----------------------------------------------
    // HANDLERS
    //----------------------------------------------
    @SuppressWarnings("all")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "mHandler Called");
            synchronized (this) {
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
//                                        Log.d(TAG, "Received : COS_X " + recMsg.OP2);
//                                        Log.d(TAG, "Received : SIN_X " + recMsg.OP3);
                                        float offset = 0;
                                        if (recMsg.OP1 == 0) {
                                            offset = ball.getObjectWidth();
                                        } else if (recMsg.OP1 > 0) {
                                            offset = 2f * ball.getObjectWidth();
                                        }
                                        float xPos = ((1 - recMsg.OP4) * CAMERA_WIDTH) - offset;
                                        float velX = -recMsg.OP1 * myModule * recMsg.OP2;
                                        float velY = myModule * recMsg.OP3;
                                        COS_X = recMsg.OP2;
                                        SIN_X = recMsg.OP3;
//                                        Log.d(TAG, "Received : Module " + myModule);
//                                        Log.d(TAG, "Received : VelX " + velX);
//                                        Log.d(TAG, "Received : VelY " + velY);
//                                        Log.d(TAG, "Received : X: " + xPos + " of " + CAMERA_WIDTH);
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
                                        increasePartiteGiocate(nickname);
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
                                            fsmGame.getState() == FSMGame.STATE_GAME_EXIT_PAUSE ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_PAUSE_STOP) {
                                        fsmGame.setState(FSMGame.STATE_IN_GAME);
                                        if(taskTimeout != null)taskTimeout.cancel();
                                        if(timerTimeout != null)timerTimeout.cancel();
                                    } else if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSED) {
                                        AppMessage resumeNotReadyMessage = new AppMessage(Constants.MSG_TYPE_RESUME_NOREADY);
                                        sendBluetoothMessage(resumeNotReadyMessage);
                                    }
                                    break;
                                //------------------------RESUME NOREADY------------------------
                                case Constants.MSG_TYPE_RESUME_NOREADY:
                                    Log.d(TAG, "Received : MSG_TYPE_RESUME_NOREADY");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        fsmGame.setState(FSMGame.STATE_GAME_EXIT_PAUSE);
                                    }
                                    break;
                                //------------------------INTEGER------------------------
                                case Constants.MSG_TYPE_FIRST_START:
                                    Log.d(TAG, "Received : MSG_TYPE_FIRST_START");
                                    //TODO
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
                                        AppMessage notReadyMessage = new AppMessage(Constants.MSG_TYPE_NOREADY);
                                        sendBluetoothMessage(notReadyMessage);
                                    }
                                    break;
                                //------------------------STOP REQUEST------------------------
                                case Constants.MSG_TYPE_STOP_REQUEST:
                                    Log.d(TAG, "Received : MSG_TYPE_STOP_REQUEST");
                                    // TODO da valutare
//                                    if(!haveBall && ball.getYCoordinate() < ball.getObjectHeight()){
                                    if (!haveBall && ball.getYCoordinate() < 0) {
                                        ball.detach();
                                    }
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        saveHandlerState();
                                    }
                                    receivedStop = true;
                                    if (fsmGame.getState() != FSMGame.STATE_GAME_PAUSE_STOP) {
                                        taskTimeout = new TimerGameTimeoutTask();
                                        timerTimeout = new Timer();
                                        timerTimeout.schedule(taskTimeout, TIMEOUT_FOR_GAME_OVER);
                                    }
                                    fsmGame.setState(FSMGame.STATE_GAME_PAUSE_STOP);
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
                                case Constants.MSG_TYPE_NOREADY:
                                    Log.d(TAG, "Received : MSG_TYPE_NOREADY");
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
                                case Constants.MSG_TYPE_BONUS_SPEEDX2:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEEDX2");
                                    bonusManager.addBonus(SPEED_X2, recMsg.OP1);
                                    break;
                                //------------------------BONUS SPEED X3------------------------
                                case Constants.MSG_TYPE_BONUS_SPEEDX3:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEEDX3");
                                    bonusManager.addBonus(SPEED_X3, recMsg.OP1);
                                    break;
                                //------------------------BONUS SPEED X4------------------------
                                case Constants.MSG_TYPE_BONUS_SPEEDX4:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_SPEEDX4");
                                    bonusManager.addBonus(SPEED_X4, recMsg.OP1);
                                    break;
                                //------------------------BONUS LOCK_FIELD------------------------
                                case Constants.MSG_TYPE_BONUS_LOCKFIELD:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_LOCKFIELD");
                                    bonusManager.addBonus(LOCK_FIELD, recMsg.OP1);
                                    break;
                                //------------------------BONUS CUT_BAR_30------------------------
                                case Constants.MSG_TYPE_BONUS_CUTBAR30:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_CUTBAR30");
                                    bonusManager.addBonus(CUT_BAR_30, recMsg.OP1);
                                    break;
                                //------------------------BONUS CUT_BAR_50------------------------
                                case Constants.MSG_TYPE_BONUS_CUTBAR50:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_CUTBAR50");
                                    bonusManager.addBonus(CUT_BAR_50, recMsg.OP1);
                                    break;
                                //------------------------BONUS REVERTED_BAR------------------------
                                case Constants.MSG_TYPE_BONUS_REVERTEDBAR:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_REVERTEDBAR");
                                    bonusManager.addBonus(REVERTED_BAR, recMsg.OP1);
                                    break;
                                //------------------------BONUS RUSH-HOUR------------------------
                                case Constants.MSG_TYPE_BONUS_RUSHHOUR:
                                    Log.d(TAG, "Received : MSG_TYPE_BONUS_RUSHHOUR");
                                    bonusManager.addBonus(RUSH_HOUR, recMsg.OP1);
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
        }
    };

    @SuppressWarnings("all")
    private final Handler fsmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case FSMGame.STATE_NOT_READY:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                break;
                            case FSMGame.STATE_CONNECTED:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                break;
                            case FSMGame.STATE_IN_GAME:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                ball.setHandlerSpeed(old_x_speed, old_y_speed);
                                bar.setBarSpeed(old_bar_speed);
                                textInfo.setText(" ");
                                if (rush_hour) {
                                    rushHour.restartAfterPause();
                                }
                                if (receivedStop && haveBall && Math.signum(ball.getHandlerSpeedY()) > 0) {
                                    ball.setHandlerSpeedX(-ball.getHandlerSpeedX());
                                    ball.setHandlerSpeedY(-ball.getHandlerSpeedY());
                                    previous_event = -1;
                                }
                                receivedStop = false;
                                resumeAllowed = false;
                                taskBonus = new TimerBonusTask();
                                timerBonus = new Timer();
//                                Log.d(TAG, "Try To Schedule Using : " + scheduleDelay);
                                scheduleDelay = scheduleDelay < 0 ? 0 : scheduleDelay;
                                timerBonus.schedule(taskBonus, scheduleDelay, BONUS_REPEATING_TIME_MILLIS);
                                break;
                            case FSMGame.STATE_IN_GAME_WAITING:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                textInfo.setText(getResources().getString(R.string.text_waiting));
                                break;
                            case FSMGame.STATE_GAME_PAUSED:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_pause));
                                saveHandlerState();
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                if (taskBonus.scheduledExecutionTime() != 0) {
                                    previousScheduleTime = taskBonus.scheduledExecutionTime();
                                }
                                scheduleDelay = BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                                Log.d(TAG, "Schedule - Current: " + System.currentTimeMillis() + " Task: " + previousScheduleTime);
                                Log.d(TAG, "ScheduleDelay PAUSED : " + scheduleDelay);
                                if (timerBonus != null) timerBonus.cancel();
                                break;
                            case FSMGame.STATE_GAME_PAUSE_STOP:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.pause_stop));
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                if (taskBonus != null) {
                                    if (taskBonus.scheduledExecutionTime() != 0) {
                                        previousScheduleTime = taskBonus.scheduledExecutionTime();
                                    }
                                    scheduleDelay = BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                                    Log.d(TAG, "Schedule - Current: " + System.currentTimeMillis() + " Task: " + previousScheduleTime);
                                    Log.d(TAG, "ScheduleDelay PAUSED : " + scheduleDelay);
                                    taskBonus = null;
                                }
                                if (timerBonus != null) timerBonus.cancel();
                                break;
                            case FSMGame.STATE_GAME_EXIT_PAUSE:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_exit_pause));
                                saveHandlerState();
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                break;
                            case FSMGame.STATE_GAME_OPPONENT_PAUSED:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_opponent_pause));
                                saveHandlerState();
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                if (taskBonus.scheduledExecutionTime() != 0) {
                                    previousScheduleTime = taskBonus.scheduledExecutionTime();
                                }
                                scheduleDelay = BONUS_REPEATING_TIME_MILLIS - (System.currentTimeMillis() - previousScheduleTime);
                                Log.d(TAG, "Schedule - Current: " + System.currentTimeMillis() + " Task: " + previousScheduleTime);
                                Log.d(TAG, "ScheduleDelay PAUSED : " + scheduleDelay);
                                if (timerBonus != null) timerBonus.cancel();
                                break;
                            case FSMGame.STATE_OPPONENT_NOT_READY:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_opponent_not_ready));
                                break;
                            case FSMGame.STATE_DISCONNECTED:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_disconnected));
                                isConnected = false;
                                mConnectedDeviceName = "";
                                ball.setHandlerSpeed(0f, 0f);
                                bar.setBarSpeed(0f);
                                isMaster = false;
                                break;
                            case FSMGame.STATE_GAME_SUSPENDED:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_game_suspended));
                                break;
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
                            case FSMGame.STATE_GAME_WINNER:
                                Log.d(TAG, "State Change From " + FSMGame.toStringDebug(msg.arg2) + " To " + fsmGame.toString());
                                textInfo.setText(getResources().getString(R.string.text_you_win));
                                winner = true;
                                gameOver();
                                break;
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
        }
    };

    @SuppressWarnings("all")
    private final Handler bonusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BonusManager.BONUS_CREATED:
                    switch (msg.arg1) {
                        case SPEED_X2:
                            Log.d(TAG, "Bonus Created : SPEED_X2");
                            setVelocityBonus(SPEED_X2);
                            safeAttachSpriteIcon(SPEED_X2_ICON);
                            break;
                        case SPEED_X3:
                            Log.d(TAG, "Bonus Created : SPEED_X3");
                            setVelocityBonus(SPEED_X3);
                            safeAttachSpriteIcon(SPEED_X3_ICON);
                            break;
                        case SPEED_X4:
                            Log.d(TAG, "Bonus Created : SPEED_X4");
                            setVelocityBonus(SPEED_X4);
                            safeAttachSpriteIcon(SPEED_X4_ICON);
                            break;
                        case LOCK_FIELD:
                            Log.d(TAG, "Bonus Created : LOCK_FIELD");
                            safeAttachSpriteIcon(LOCK_FIELD_ICON);
                            synchronized (this) {
                                locksField = true;
                            }
                            break;
                        case CUT_BAR_30:
                            Log.d(TAG, "Bonus Created : CUT_BAR_30");
                            bar.setObjectWidth(0.7f * bar.getBarWidth());
                            safeAttachSpriteIcon(CUT_BAR_30_ICON);
                            break;
                        case CUT_BAR_50:
                            Log.d(TAG, "Bonus Created : CUT_BAR_50");
                            bar.setObjectWidth(0.5f * bar.getBarWidth());
                            safeAttachSpriteIcon(CUT_BAR_50_ICON);
                            break;
                        case REVERTED_BAR:
                            Log.d(TAG, "Bonus Created : REVERTED_BAR");
                            safeAttachSpriteIcon(REVERTED_BAR_ICON);
                            if (Math.signum(bar.getBarSpeed()) > 0)
                                bar.setBarSpeed(-bar.getBarSpeed());
                            break;
                        case RUSH_HOUR:
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
                        case SPEED_X2:
                            Log.d(TAG, "Bonus Expired : SPEED_X2");
                            setVelocityBonus(NO_BONUS);
                            speedX2BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X2_ICON));
                            break;
                        case SPEED_X3:
                            Log.d(TAG, "Bonus Expired : SPEED_X3");
                            setVelocityBonus(NO_BONUS);
                            speedX3BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X3_ICON));
                            break;
                        case SPEED_X4:
                            Log.d(TAG, "Bonus Expired : SPEED_X4");
                            setVelocityBonus(NO_BONUS);
                            speedX4BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(SPEED_X4_ICON));
                            break;
                        case LOCK_FIELD:
                            Log.d(TAG, "Bonus Expired : LOCK_FIELD");
                            synchronized (this) {
                                locksField = false;
                            }
                            lockFieldBonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(LOCK_FIELD_ICON));
                            break;
                        case CUT_BAR_30:
                            Log.d(TAG, "Bonus Expired : CUT_BAR_30");
                            bar.setObjectWidth(bar.getBarWidth());
                            cutBar30BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(CUT_BAR_30_ICON));
                            break;
                        case CUT_BAR_50:
                            Log.d(TAG, "Bonus Expired : CUT_BAR_50");
                            bar.setObjectWidth(bar.getBarWidth());
                            cutBar50BonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(CUT_BAR_50_ICON));
                            break;
                        case REVERTED_BAR:
                            Log.d(TAG, "Bonus Expired : REVERTED_BAR");
                            if (Math.signum(bar.getBarSpeed()) < 0)
                                bar.setBarSpeed(-bar.getBarSpeed());
                            reverseBonusIcon.detachOnUIThread();
                            bonusStatusArray.remove(Integer.valueOf(REVERTED_BAR_ICON));
                            break;
                        case RUSH_HOUR:
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
//        Log.d(TAG, "BonusVelocity - Previous Module: " + myModule);
//        Log.d(TAG, "BonusVelocity - Previous Velx: " + ball.getHandlerSpeedX());
//        Log.d(TAG, "BonusVelocity - Previous Vely: " + ball.getHandlerSpeedY());

        switch (nextBonus) {
            case NO_BONUS:
                myModule = mSPEED_X1;
                break;
            case SPEED_X2:
                myModule = mSPEED_X2;
                break;
            case SPEED_X3:
                myModule = mSPEED_X3;
                break;
            case SPEED_X4:
                myModule = mSPEED_X4;
                break;
            default:
                break;
        }
        if (haveBall) {
            ball.setHandlerSpeedX(Math.signum(ball.getHandlerSpeedX()) * myModule * COS_X);
            ball.setHandlerSpeedY(Math.signum(ball.getHandlerSpeedY()) * myModule * SIN_X);
        }
//        Log.d(TAG, "BonusVelocity - New Module: " + myModule);
//        Log.d(TAG, "BonusVelocity - New Velx: " + ball.getHandlerSpeedX());
//        Log.d(TAG, "BonusVelocity - New Vely: " + ball.getHandlerSpeedY());
    }

    //----------------------------------------------
    // THREADS
    //----------------------------------------------

    private class TimerBonusTask extends TimerTask {

        private int bonusChoice = NO_BONUS;

        @Override
        public void run() {
            Random rand = new Random();
            do {
                bonusChoice = rand.nextInt((RUSH_HOUR - SPEED_X2) + 1) + SPEED_X2;
            } while (bonusChoice == previous_bonus);
            previous_bonus = bonusChoice;
//            bonusChoice = RUSH_HOUR;
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
                Log.d(TAG, "Sprite - TimerCancel()");
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
                Log.d(TAG, "Game Timeout : Send Message");
                AppMessage lostYourGameMessage = new AppMessage(Constants.MSG_TYPE_GAME_TIMEOUT);
                sendBluetoothMessage(lostYourGameMessage);
                fsmGame.setState(FSMGame.STATE_GAME_WINNER);
            }
        }
    }

    //----------------------------------------------
    // THREADS UTILITY
    //----------------------------------------------

    private void attachSprite(int bonusID) {
        switch (bonusID) {
            case SPEED_X2:
//                Log.d(TAG, "Sprite - Attaching SPEED_X2");
                speedX2Bonus.registerTouch();
                speedX2Bonus.attach();
                activedBonusSprite = SPEED_X2;
                deletedBonusSprite = false;
                break;

            case SPEED_X3:
//                Log.d(TAG, "Sprite - Attaching SPEED_X3");
                speedX3Bonus.registerTouch();
                speedX3Bonus.attach();
                activedBonusSprite = SPEED_X3;
                deletedBonusSprite = false;
                break;

            case SPEED_X4:
//                Log.d(TAG, "Sprite - Attaching SPEED_X4");
                speedX4Bonus.registerTouch();
                speedX4Bonus.attach();
                activedBonusSprite = SPEED_X4;
                deletedBonusSprite = false;
                break;

            case LOCK_FIELD:
//                Log.d(TAG, "Sprite - Attaching LOCK_FIELD");
                lockFieldBonus.registerTouch();
                lockFieldBonus.attach();
                activedBonusSprite = LOCK_FIELD;
                deletedBonusSprite = false;
                break;

            case CUT_BAR_30:
//                Log.d(TAG, "Sprite - Attaching CUT_BAR_30");
                cutBar30Bonus.registerTouch();
                cutBar30Bonus.attach();
                activedBonusSprite = CUT_BAR_30;
                deletedBonusSprite = false;
                break;

            case CUT_BAR_50:
//                Log.d(TAG, "Sprite - Attaching CUT_BAR_50");
                cutBar50Bonus.registerTouch();
                cutBar50Bonus.attach();
                activedBonusSprite = CUT_BAR_50;
                deletedBonusSprite = false;
                break;

            case REVERTED_BAR:
//                Log.d(TAG, "Sprite - Attaching REVERTED_BAR");
                reverseBonus.registerTouch();
                reverseBonus.attach();
                activedBonusSprite = REVERTED_BAR;
                deletedBonusSprite = false;
                break;

            case RUSH_HOUR:
//                Log.d(TAG, "Sprite - Attaching RUSH_HOUR");
                rushHourBonus.registerTouch();
                rushHourBonus.attach();
                activedBonusSprite = RUSH_HOUR;
                deletedBonusSprite = false;
                break;

            default:
                Log.e(TAG, "Error in attachSprite(). Invalid ID");
                break;
        }
    }

    private void detachSprite(int bonusID) {
        switch (bonusID) {
            case SPEED_X2:
//                Log.d(TAG, "Sprite - Detaching SPEED_X2");
                speedX2Bonus.unregisterTouch();
                speedX2Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case SPEED_X3:
//                Log.d(TAG, "Sprite - Detaching SPEED_X3");
                speedX3Bonus.unregisterTouch();
                speedX3Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case SPEED_X4:
//                Log.d(TAG, "Sprite - Detaching SPEED_X4");
                speedX4Bonus.unregisterTouch();
                speedX4Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case LOCK_FIELD:
//                Log.d(TAG, "Sprite - Detaching LOCK_FIELD");
                lockFieldBonus.unregisterTouch();
                lockFieldBonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case CUT_BAR_30:
//                Log.d(TAG, "Sprite - Detaching CUT_BAR_30");
                cutBar30Bonus.unregisterTouch();
                cutBar30Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case CUT_BAR_50:
//                Log.d(TAG, "Sprite - Detaching CUT_BAR_50");
                cutBar50Bonus.unregisterTouch();
                cutBar50Bonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case REVERTED_BAR:
//                Log.d(TAG, "Sprite - Detaching REVERTED_BAR");
                reverseBonus.unregisterTouch();
                reverseBonus.detachOnUIThread();
                activedBonusSprite = SPRITE_NONE;
                deletedBonusSprite = true;
                break;

            case RUSH_HOUR:
//                Log.d(TAG, "Sprite - Detaching RUSH_HOUR");
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
