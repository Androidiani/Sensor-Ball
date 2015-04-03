package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import it.unina.is2project.sensorgames.FSMGame;
import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.bluetooth.BluetoothService;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.bluetooth.Serializer;
import it.unina.is2project.sensorgames.bluetooth.messages.AppMessage;

public class GamePongTwoPlayer extends GamePong {

    private final String TAG = "2PlayersGame";

    // Indicates the holding's ball
    private boolean haveBall = false;
    // Indicates who starts game
    private boolean isMaster = false;
    // Indicates when ball is passing
    private boolean transferringBall;
    // Service bluetooth
    private BluetoothService mBluetoothService = null;
    // Finite State Machine
    private FSMGame fsmGame = null;
    // Bonus manager
    private BonusManager bonusManager = null;
    // Text information
    private Text textInfo;
    private Text textPoint;
    // Return intent extra
    public static String EXTRA_MASTER = "isMaster_boolean";
    public static String EXTRA_CONNECTION_STATE = "isConnected_boolean";
    // Pause Utils
    private float old_x_speed;
    private float old_y_speed;
    private int old_game_speed;
    private long tap;
    private boolean proximityRegion;
    private int PROXIMITY_ZONE;
    // Connections Utils
    private boolean isConnected;
    // Score variables
    private int score;
    // Constant Utils
    private final int SPRITE_NONE = -1;
    // Speed X2 Bonus
    private BitmapTextureAtlas speedTexture_X2;
    private ITextureRegion speedTextureRegion_X2;
    private Sprite speedSprite_X2;
    // Speed X3 Bonus
    private BitmapTextureAtlas speedTexture_X3;
    private ITextureRegion speedTextureRegion_X3;
    private Sprite speedSprite_X3;
    // Speed X4 Bonus
    private BitmapTextureAtlas speedTexture_X4;
    private ITextureRegion speedTextureRegion_X4;
    private Sprite speedSprite_X4;
    // Bonus Constants
    public final static int NOBONUS = 1;
    public final static int SPEEDX2 = 2;
    public final static int SPEEDX3 = 3;
    public final static int SPEEDX4 = 4;
    // Bonus Utils
    private int activedSprite = SPRITE_NONE;
    private boolean deletedSprite = true;
    TimerTask task;
    Timer timer;


    @Override
    protected Scene onCreateScene() {
        // Getting instance of fsm, service bluetooth and bonus manager
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);
        bonusManager = BonusManager.getBonusInstance(bonusHandler);

        // Retrieve intent message
        Intent i = getIntent();

        if (i.getIntExtra("ball", 0) == 1) {
            haveBall = true;
        } else {
            haveBall = false;
        }

        super.onCreateScene();

        initializeSprite();

        Log.d(TAG, "Ho la palla : " + haveBall);

        // Set result in case of failure
        setResult(Activity.RESULT_CANCELED);

        // Setting up the physics of the game
        settingPhysics();

        // Setting variables
        isConnected = true;
        proximityRegion = false;
        transferringBall = false;
        game_over = false;
        score = 0;
        myModule = (float) Math.sqrt(Math.pow(BALL_SPEED, 2) + Math.pow(BALL_SPEED, 2));
        COS_X = COS_45;
        SIN_X = SIN_45;
        PROXIMITY_ZONE = CAMERA_HEIGHT / 8;
        previous_event = -1;

        if (i.getIntExtra("master", 0) == 1) {
            AppMessage alertMessage = new AppMessage(Constants.MSG_TYPE_ALERT);
            sendBluetoothMessage(alertMessage);
            isMaster = true;
            fsmGame.setState(FSMGame.STATE_IN_GAME_WAITING);
        } else {
            AppMessage messageSync = new AppMessage(Constants.MSG_TYPE_SYNC);
            sendBluetoothMessage(messageSync);
            isMaster = false;
            fsmGame.setState(FSMGame.STATE_IN_GAME);
        }

        Log.d(TAG, "Sono master : " + isMaster);

        // Attachning textPoint
        textPoint = new Text(10, barSprite.getY() + barSprite.getHeight(), font, getResources().getString(R.string.sts_score) + " " + score, 30, getVertexBufferObjectManager());
        scene.attachChild(textPoint);

        // Attaching textInfo
        textInfo = new Text(10, 10, font, "", 30, getVertexBufferObjectManager());
        scene.attachChild(textInfo);

        return scene;
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();
        // Speed X2
        Drawable speedDrawable_X2 = getResources().getDrawable(R.drawable.speedx2);
        speedTexture_X2 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X2.getIntrinsicWidth(), speedDrawable_X2.getIntrinsicHeight());
        speedTextureRegion_X2 = createFromResource(speedTexture_X2, this, R.drawable.speedx2, 0, 0);
        speedTexture_X2.load();
        // Speed X3
        Drawable speedDrawable_X3 = getResources().getDrawable(R.drawable.speedx3);
        speedTexture_X3 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X3.getIntrinsicWidth(), speedDrawable_X3.getIntrinsicHeight());
        speedTextureRegion_X3 = createFromResource(speedTexture_X3, this, R.drawable.speedx3, 0, 0);
        speedTexture_X3.load();
        // Speed X4
        Drawable speedDrawable_X4 = getResources().getDrawable(R.drawable.speedx4);
        speedTexture_X4 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X4.getIntrinsicWidth(), speedDrawable_X4.getIntrinsicHeight());
        speedTextureRegion_X4 = createFromResource(speedTexture_X4, this, R.drawable.speedx4, 0, 0);
        speedTexture_X4.load();
    }

    @Override
    protected void attachBall() {
        Log.i(TAG, "Call drawBall() with haveBall = " + haveBall);
        if (haveBall) super.attachBall();
    }

    @Override
    protected void setBallVeloctity() {
        old_game_speed = 2;
        old_x_speed = BALL_SPEED;
        old_y_speed = -BALL_SPEED;
    }

    @Override
    protected boolean topCondition() {
        if (!transferringBall && ballSprite.getY() < 0 && previous_event != TOP && haveBall) {
            Log.d(TAG, "topCondition TRUE");
            return true;
        } else return false;
    }

    @Override
    protected void collidesTop() {
        Log.d(TAG, "collidesTop");
        float xRatio = ballSprite.getX() / CAMERA_WIDTH;
        AppMessage messageCoords = new AppMessage(Constants.MSG_TYPE_COORDS,
                Math.signum(handler.getVelocityX()),
                COS_X,
                SIN_X,
                xRatio);
        sendBluetoothMessage(messageCoords);
        Log.d("MESSAGECOORDSsen", "Module " + myModule);
        Log.d("MESSAGECOORDSsen", "VelX " + handler.getVelocityX());
        Log.d("MESSAGECOORDSsen", "VelY " + handler.getVelocityY());
        Log.d("MESSAGECOORDSsen", "Sign(Velx) " + Math.signum(handler.getVelocityX()));
        haveBall = false;
        transferringBall = true;
        previous_event = TOP;
    }

    @Override
    protected void bluetoothExtra() {
        // Setting proximityRegion ON
        if(!proximityRegion && ballSprite.getY() <= PROXIMITY_ZONE){
//            Log.d("Proximity", "Set Proximity To TRUE");
            proximityRegion = true;
        }

        // Al rientro della pallina, proximity regione in ricezione (la metà di quella di invio, più critica)
        if(proximityRegion && ballSprite.getY() > PROXIMITY_ZONE/2){
//            Log.d("Proximity", "Set Proximity To FALSE");
            proximityRegion = false;
        }

        // Quando la palla ESCE COMPLETAMENTE dal device
        if (proximityRegion && ballSprite.getY() < -ballSprite.getHeight()){
            scene.detachChild(ballSprite);
            //ballSprite.detachSelf();
            transferringBall = false;
//            Log.d("Proximity", "Set Proximity To FALSE");
            proximityRegion = false;
        }
        // Quando la palla ENTRA COMPLETAMENTE nel device
        if (transferringBall && ballSprite.getY() > 0) {
            transferringBall = false;
        }
    }

    @Override
    protected void collidesBottom() {
        super.collidesBottom();
        AppMessage pointToEnemyMessage = new AppMessage(Constants.MSG_TYPE_POINT_UP);
        sendBluetoothMessage(pointToEnemyMessage);
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();
        bonusManager.decrementCount();
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

    }

    @Override
    public void addScore() {
        score++;
        textPoint.setText(getResources().getString(R.string.sts_score) + " " + score);
    }

    @Override
    public void actionDownEvent(float x, float y) {
        if(!checkTouchOnSprite(activedSprite, x, y)) {
            Log.d("Proximity", "Proximity:" + proximityRegion);
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
        }
    }

    @Override
    public void onBackPressed() {
        if (fsmGame.getState() == FSMGame.STATE_IN_GAME ||
                fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
            AppMessage messageFail = new AppMessage(Constants.MSG_TYPE_FAIL);
            sendBluetoothMessage(messageFail);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONNECTION_STATE, isConnected);
        intent.putExtra(EXTRA_MASTER, isMaster);
        setResult(Activity.RESULT_CANCELED, intent);
        timer.cancel();
        super.onBackPressed();
    }

    //----------------------------------------------
    // MISCELLANEA
    //----------------------------------------------
    private void sendBluetoothMessage(AppMessage message) {
        Log.d("SendReceived", "Send Message Type: " + message.TYPE);
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

    private boolean checkTouchOnSprite(int bonusID, float x, float y){
        boolean checkTouchSpriteStatus = false;
        switch (bonusID){
            case SPEEDX2:
                if(x <= speedSprite_X2.getX() + speedSprite_X2.getWidth() &&
                        x >= speedSprite_X2.getX() &&
                        y >= speedSprite_X2.getY() &&
                        y <= speedSprite_X2.getY() + speedSprite_X2.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case SPEEDX3:
                if(x <= speedSprite_X3.getX() + speedSprite_X3.getWidth() &&
                        x >= speedSprite_X3.getX() &&
                        y >= speedSprite_X3.getY() &&
                        y <= speedSprite_X3.getY() + speedSprite_X3.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case SPEEDX4:
                if(x <= speedSprite_X4.getX() + speedSprite_X4.getWidth() &&
                        x >= speedSprite_X4.getX() &&
                        y >= speedSprite_X4.getY() &&
                        y <= speedSprite_X4.getY() + speedSprite_X4.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            default:
                checkTouchSpriteStatus = false;
                break;
        }
        return checkTouchSpriteStatus;
    }

    private void initializeSprite(){

        // SPEED X2 INITIALIZING

        speedSprite_X2 = new Sprite(0, 0, speedTextureRegion_X2, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite SPEEDX2 Touched");
                detachSprite(SPEEDX2);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage speedx2Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX2, randNum);
                sendBluetoothMessage(speedx2Message);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };

        speedSprite_X2.setScale(speedSprite_X2.getScaleX()/2);
        speedSprite_X2.setX((CAMERA_WIDTH/2) - (speedSprite_X2.getWidth()/2));
        speedSprite_X2.setY((CAMERA_HEIGHT/2) - (speedSprite_X2.getHeight()/2));
        
        // SPEED X3 INITIALIZING

        speedSprite_X3 = new Sprite(0, 0, speedTextureRegion_X3, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite SPEEDX3 Touched");
                detachSprite(SPEEDX3);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage speedx3Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX3, randNum);
                sendBluetoothMessage(speedx3Message);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };

        speedSprite_X3.setScale(speedSprite_X3.getScaleX()/2);
        speedSprite_X3.setX((CAMERA_WIDTH/2) - (speedSprite_X3.getWidth()/2));
        speedSprite_X3.setY((CAMERA_HEIGHT/2) - (speedSprite_X3.getHeight()/2));
        
        // SPEED X4 INITIALIZING

        speedSprite_X4 = new Sprite(0, 0, speedTextureRegion_X4, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite SPEEDX4 Touched");
                detachSprite(SPEEDX4);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage speedx4Message = new AppMessage(Constants.MSG_TYPE_BONUS_SPEEDX4, randNum);
                sendBluetoothMessage(speedx4Message);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };

        speedSprite_X4.setScale(speedSprite_X4.getScaleX()/2);
        speedSprite_X4.setX((CAMERA_WIDTH/2) - (speedSprite_X4.getWidth()/2));
        speedSprite_X4.setY((CAMERA_HEIGHT/2) - (speedSprite_X4.getHeight()/2));
    }

    //----------------------------------------------
    // HANDLERS
    //----------------------------------------------
    @SuppressWarnings("all")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Handler Called");
            synchronized (this) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                break;
                            case BluetoothService.STATE_LISTEN:
                                break;
                            case BluetoothService.STATE_NONE:
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
                                    Log.d("SendReceived", "MSG_TYPE_COORDS");
                                    if (!haveBall) {
                                        Log.d("MESSAGECOORDSrec", "COS_X " + recMsg.OP2);
                                        Log.d("MESSAGECOORDSrec", "SIN_X " + recMsg.OP3);
                                        float xPos = (1 - recMsg.OP4) * CAMERA_WIDTH;
                                        float velX = -recMsg.OP1*myModule*recMsg.OP2;
                                        float velY = myModule*recMsg.OP3;
                                        COS_X = recMsg.OP2;
                                        SIN_X = recMsg.OP3;
                                        Log.d("MESSAGECOORDSrec", "Module " + myModule);
                                        Log.d("MESSAGECOORDSrec", "VelX " + velX);
                                        Log.d("MESSAGECOORDSrec", "VelY " + velY);
                                        ballSprite.setPosition(xPos, -ballSprite.getHeight());
                                        scene.attachChild(ballSprite);
                                        handler.setVelocity(velX, velY);
                                        previous_event = TOP;
                                        transferringBall = true;
                                        haveBall = true;
                                        proximityRegion = true;
                                        textInfo.setText("");
                                    }
                                    break;
                                //------------------------SYNC------------------------
                                case Constants.MSG_TYPE_SYNC:
                                    Log.d("SendReceived", "MSG_TYPE_SYNC");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                        fsmGame.setState(FSMGame.STATE_IN_GAME);
                                    }
                                    break;
                                //------------------------FAIL------------------------
                                case Constants.MSG_TYPE_FAIL:
                                    Log.d("SendReceived", "MSG_TYPE_FAIL");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED) {
                                        fsmGame.setState(FSMGame.STATE_OPPONENT_LEFT);
                                    }
                                    break;
                                //------------------------PAUSE------------------------
                                case Constants.MSG_TYPE_PAUSE:
                                    Log.d("SendReceived", "MSG_TYPE_PAUSE");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        fsmGame.setState(FSMGame.STATE_GAME_OPPONENT_PAUSED);
                                    }
                                    break;
                                //------------------------RESUME------------------------
                                case Constants.MSG_TYPE_RESUME:
                                    Log.d("SendReceived", "MSG_TYPE_RESUME");
                                    if (fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_EXIT_PAUSE) {
                                        fsmGame.setState(FSMGame.STATE_IN_GAME);
                                    } else if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSED) {
                                        AppMessage resumeNotReadyMessage = new AppMessage(Constants.MSG_TYPE_RESUME_NOREADY);
                                        sendBluetoothMessage(resumeNotReadyMessage);
                                    }
                                    break;
                                //------------------------RESUME NOREADY------------------------
                                case Constants.MSG_TYPE_RESUME_NOREADY:
                                    Log.d("SendReceived", "MSG_TYPE_RESUME_NOREADY");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        fsmGame.setState(FSMGame.STATE_GAME_EXIT_PAUSE);
                                    }
                                    break;
                                //------------------------INTEGER------------------------
                                case Constants.MSG_TYPE_INTEGER:
                                    Log.d("SendReceived", "MSG_TYPE_INTEGER");
                                    //TODO
                                    break;
                                //------------------------ALERT------------------------
                                case Constants.MSG_TYPE_ALERT:
                                    Log.d("SendReceived", "MSG_TYPE_ALERT");
                                    if (fsmGame.getState() == FSMGame.STATE_DISCONNECTED ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED ||
                                            fsmGame.getState() == FSMGame.STATE_OPPONENT_LEFT) {
                                        AppMessage notReadyMessage = new AppMessage(Constants.MSG_TYPE_NOREADY);
                                        sendBluetoothMessage(notReadyMessage);
                                    }
                                    break;
                                //------------------------NOREADY------------------------
                                case Constants.MSG_TYPE_NOREADY:
                                    Log.d("SendReceived", "MSG_TYPE_NOREADY");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                        fsmGame.setState(FSMGame.STATE_OPPONENT_NOT_READY);
                                    }
                                    break;
                                //------------------------POINT UP------------------------
                                case Constants.MSG_TYPE_POINT_UP:
                                    Log.d("SendReceived", "MSG_TYPE_POINT_UP");
                                    addScore();
                                    break;
                                //------------------------BONUS SPEED X2------------------------
                                case Constants.MSG_TYPE_BONUS_SPEEDX2:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_SPEEDX2");
                                    bonusManager.addBonus(SPEEDX2, recMsg.OP1);
                                    break;
                                //------------------------BONUS SPEED X3------------------------
                                case Constants.MSG_TYPE_BONUS_SPEEDX3:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_SPEEDX3");
                                    bonusManager.addBonus(SPEEDX3, recMsg.OP1);
                                    break;
                                //------------------------BONUS SPEED X4------------------------
                                case Constants.MSG_TYPE_BONUS_SPEEDX4:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_SPEEDX4");
                                    bonusManager.addBonus(SPEEDX4, recMsg.OP1);
                                    break;
                                default:
                                    Log.e("SendReceived", "Ricevuto messaggio non idoneo - Type is " + recMsg.TYPE);
                            }
                            Log.d("SendReceived", Integer.toString(recMsg.TYPE));
                        } else {
                            Log.e("SendReceived", "Ricevuto messaggio nullo.");
                        }
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
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
                                break;
                            case FSMGame.STATE_CONNECTED:
                                break;
                            case FSMGame.STATE_IN_GAME:
                                handler.setVelocity(old_x_speed, old_y_speed);
                                GAME_VELOCITY = old_game_speed;
                                textInfo.setText("");
                                task = new TimerBonusTask();
                                timer = new Timer();
                                timer.schedule(task, 2000, 10000);
                                break;
                            case FSMGame.STATE_IN_GAME_WAITING:
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                textInfo.setText(getApplicationContext().getString(R.string.text_waiting));
                                break;
                            case FSMGame.STATE_GAME_PAUSED:
                                textInfo.setText(getResources().getString(R.string.text_pause));
                                old_x_speed = handler.getVelocityX();
                                old_y_speed = handler.getVelocityY();
                                old_game_speed = GAME_VELOCITY;
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                timer.cancel();
                                break;
                            case FSMGame.STATE_GAME_EXIT_PAUSE:
                                textInfo.setText(getResources().getString(R.string.text_exit_pause));
                                old_x_speed = handler.getVelocityX();
                                old_y_speed = handler.getVelocityY();
                                old_game_speed = GAME_VELOCITY;
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                break;
                            case FSMGame.STATE_GAME_OPPONENT_PAUSED:
                                textInfo.setText(getResources().getString(R.string.text_opponent_pause));
                                old_x_speed = handler.getVelocityX();
                                old_y_speed = handler.getVelocityY();
                                old_game_speed = GAME_VELOCITY;
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                timer.cancel();
                                break;
                            case FSMGame.STATE_OPPONENT_NOT_READY:
                                textInfo.setText(getResources().getString(R.string.text_opponent_not_ready));
                                break;
                            case FSMGame.STATE_DISCONNECTED:
                                isConnected = false;
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                textInfo.setText(getApplicationContext().getString(R.string.text_disconnected));
                                break;
                            case FSMGame.STATE_OPPONENT_LEFT:
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                textInfo.setText(getApplicationContext().getString(R.string.text_opponent_left));
                                break;
                            default:
                        }
                    default:
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
                    switch (msg.arg1){
                        case SPEEDX2:
                            setVelocityFromPrevious(msg.arg2, SPEEDX2);
                            break;
                        case SPEEDX3:
                            setVelocityFromPrevious(msg.arg2, SPEEDX3);
                            break;
                        case SPEEDX4:
                            setVelocityFromPrevious(msg.arg2, SPEEDX4);
                            break;
                        default:
                            Log.e("Bonus", "Error - Invalid Bonus ID Created");
                            break;
                    }
                    break;
                case BonusManager.BONUS_EXPIRED:
                    switch (msg.arg1){
                        case SPEEDX2:
                            setVelocityFromPrevious(SPEEDX2, NOBONUS);
                            break;
                        case SPEEDX3:
                            setVelocityFromPrevious(SPEEDX3, NOBONUS);
                            break;
                        case SPEEDX4:
                            setVelocityFromPrevious(SPEEDX4, NOBONUS);
                            break;
                        default:
                            Log.e("Bonus", "Error - Invalid Bonus ID Expired");
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void setVelocityFromPrevious(int previousBonus, int nextBonus){
        myModule = (myModule/previousBonus)*nextBonus;
        if(haveBall) {
            handler.setVelocityX(Math.signum(handler.getVelocityX()) * myModule * COS_X);
            handler.setVelocityY(Math.signum(handler.getVelocityY()) * myModule * COS_X);
        }
        Log.d("BonusVelocity", "New Module: " + myModule);
        Log.d("BonusVelocity", "New Velx: " + handler.getVelocityX());
        Log.d("BonusVelocity", "New Vely: " + handler.getVelocityY());
    }

    //----------------------------------------------
    // THREADS
    //----------------------------------------------

    private class TimerBonusTask extends TimerTask{
        @Override
        public void run() {
            Random rand = new Random();
            int bonusChoice = rand.nextInt(( SPEEDX4 - SPEEDX2) + 1) + SPEEDX2;
            if(deletedSprite){
                Log.d("Sprite", "First One Is None");
                attachSprite(bonusChoice);
            }else{
                Log.d("Sprite", "Actived Is " + activedSprite);
                detachSprite(activedSprite);
                attachSprite(bonusChoice);
            }
        }

        @Override
        public boolean cancel() {
            if(activedSprite != SPRITE_NONE){
                detachSprite(activedSprite);
                activedSprite = SPRITE_NONE;
            }
            return super.cancel();
        }
    }

    //----------------------------------------------
    // THREADS UTILITY
    //----------------------------------------------

    private void attachSprite(int bonusID){
        switch (bonusID){
            case SPEEDX2:
                Log.d("AttachSprite", "Attaching SPEEDX2");
                scene.registerTouchArea(speedSprite_X2);
                scene.attachChild(speedSprite_X2);
                activedSprite = SPEEDX2;
                deletedSprite = false;
                break;

            case SPEEDX3:
                Log.d("AttachSprite", "Attaching SPEEDX3");
                scene.registerTouchArea(speedSprite_X3);
                scene.attachChild(speedSprite_X3);
                activedSprite = SPEEDX3;
                deletedSprite = false;
                break;

            case SPEEDX4:
                Log.d("AttachSprite", "Attaching SPEEDX4");
                scene.registerTouchArea(speedSprite_X4);
                scene.attachChild(speedSprite_X4);
                activedSprite = SPEEDX4;
                deletedSprite = false;
                break;

            default:
                Log.e(TAG, "Error in attachSprite(). Invalid ID");
                break;
        }
    }

    private void detachSprite(int bonusID){
        switch (bonusID){
            case SPEEDX2:
                Log.d("DetachSprite", "Deattaching SPEEDX2");
                scene.unregisterTouchArea(speedSprite_X2);
                scene.detachChild(speedSprite_X2);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            case SPEEDX3:
                Log.d("DetachSprite", "Deattaching SPEEDX3");
                scene.unregisterTouchArea(speedSprite_X3);
                scene.detachChild(speedSprite_X3);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            case SPEEDX4:
                Log.d("DetachSprite", "Deattaching SPEEDX4");
                scene.unregisterTouchArea(speedSprite_X4);
                scene.detachChild(speedSprite_X4);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            default:
                Log.e(TAG, "Error in detachSprite(). Invalid ID");
                break;
        }
    }

}
