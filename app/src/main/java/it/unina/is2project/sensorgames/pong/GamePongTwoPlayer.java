package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

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


    @Override
    protected Scene onCreateScene() {
        // Getting instance of fsm and service bluetooth
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);

        // Retrieve intent message
        Intent i = getIntent();

        if (i.getIntExtra("ball", 0) == 1) {
            haveBall = true;
        } else {
            haveBall = false;
        }

        super.onCreateScene();

        Log.d(TAG, "Ho la palla : " + haveBall);

        // Set result in failure case
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
//        Log.d(TAG, "End Top. TransBall: " + transferringBall);
//        Log.d(TAG, "End Top. HaveBall: " + haveBall);
        haveBall = false;
        transferringBall = true;
        previous_event = TOP;
    }

    @Override
    protected void bluetoothExtra() {
        // Setting proximityRegion ON
        if(!proximityRegion && ballSprite.getY() <= PROXIMITY_ZONE){
            Log.d("Proximity", "Set Proximity To TRUE");
            proximityRegion = true;
        }

        // Al rientro della pallina, proximity regione in ricezione (la metà di quella di invio, più critica)
        if(proximityRegion && ballSprite.getY() > PROXIMITY_ZONE/2){
            Log.d("Proximity", "Set Proximity To FALSE");
            proximityRegion = false;
        }

        // Quando la palla ESCE COMPLETAMENTE dal device
        if (proximityRegion && ballSprite.getY() < -ballSprite.getHeight()){
            scene.detachChild(ballSprite);
            //ballSprite.detachSelf();
            transferringBall = false;
            Log.d("Proximity", "Set Proximity To FALSE");
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
    public void addScore() {
        score++;
        textPoint.setText(getResources().getString(R.string.sts_score) + " " + score);
    }

    @Override
    public void actionDownEvent() {
        Log.d("Proximity", "Proximity:" + proximityRegion);
        if (fsmGame.getState() == FSMGame.STATE_IN_GAME && !proximityRegion) {
            if(haveBall) {
                tap = System.currentTimeMillis();
                fsmGame.setState(FSMGame.STATE_GAME_PAUSED);
                AppMessage pauseMessage = new AppMessage(Constants.MSG_TYPE_PAUSE);
                sendBluetoothMessage(pauseMessage);
            }else{
                textInfo.setText(getResources().getString(R.string.text_pause_not_allowed));
            }
        }

        if (fsmGame.getState() == FSMGame.STATE_GAME_PAUSED && (System.currentTimeMillis() - tap > 500)) {
            fsmGame.setState(FSMGame.STATE_IN_GAME);
            AppMessage resumeMessage = new AppMessage(Constants.MSG_TYPE_RESUME);
            sendBluetoothMessage(resumeMessage);
        }
    }

    @Override
    protected void saveGame(String s) {

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
        super.onBackPressed();
    }

    //----------------------------------------------
    // MISCELLANEA
    //----------------------------------------------
    private synchronized void sendBluetoothMessage(AppMessage message) {
        Log.d("SendReceived", "Send Message Type: " + message.TYPE);
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

    //----------------------------------------------
    // HANDLERS
    //----------------------------------------------
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
                                case Constants.MSG_TYPE_SYNC:
                                    Log.d("SendReceived", "MSG_TYPE_SYNC");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                        fsmGame.setState(FSMGame.STATE_IN_GAME);
                                    }
                                    break;
                                case Constants.MSG_TYPE_FAIL:
                                    Log.d("SendReceived", "MSG_TYPE_FAIL");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_PAUSED ||
                                            fsmGame.getState() == FSMGame.STATE_GAME_OPPONENT_PAUSED) {
                                        fsmGame.setState(FSMGame.STATE_OPPONENT_LEFT);
                                    }
                                    break;
                                case Constants.MSG_TYPE_PAUSE:
                                    Log.d("SendReceived", "MSG_TYPE_PAUSE");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        fsmGame.setState(FSMGame.STATE_GAME_OPPONENT_PAUSED);
                                    }
                                    break;
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
                                case Constants.MSG_TYPE_RESUME_NOREADY:
                                    Log.d("SendReceived", "MSG_TYPE_RESUME_NOREADY");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME) {
                                        fsmGame.setState(FSMGame.STATE_GAME_EXIT_PAUSE);
                                    }
                                    break;
                                case Constants.MSG_TYPE_INTEGER:
                                    Log.d("SendReceived", "MSG_TYPE_INTEGER");
                                    //TODO
                                    break;
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
                                case Constants.MSG_TYPE_NOREADY:
                                    Log.d("SendReceived", "MSG_TYPE_NOREADY");
                                    if (fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING) {
                                        fsmGame.setState(FSMGame.STATE_OPPONENT_NOT_READY);
                                    }
                                    break;
                                case Constants.MSG_TYPE_POINT_UP:
                                    Log.d("SendReceived", "MSG_TYPE_POINT_UP");
                                    addScore();
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

    private final Handler bonusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bonus.BONUS_CREATED:
                    //TODO
                    break;
                case Bonus.BONUS_EXPIRED:
                    //TODO
                    break;
                default:
                    break;
            }
        }
    };

}
