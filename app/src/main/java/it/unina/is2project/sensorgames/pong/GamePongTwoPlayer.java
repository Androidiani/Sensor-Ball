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
    private boolean transferringBall = false;
    // Result Codes
    public static final int CONNECTION_DOWN = 203;
    // Service bluetooth
    private BluetoothService mBluetoothService = null;
    // Finite State Machine
    private FSMGame fsmGame = null;

    @Override
    protected Scene onCreateScene() {
        fsmGame = FSMGame.getFsmInstance(fsmHandler);
        // Retrieve intent message
        Intent i = getIntent();

        if(i.getIntExtra("ball", 0) == 1){
            haveBall = true;
        }else{
            haveBall = false;
        }

        super.onCreateScene();

        Log.d(TAG, "Ho la palla : " + haveBall);

        // Set result in failure case
        setResult(Activity.RESULT_CANCELED);

        /** Setting up the physics of the game */
        settingPhysics();

        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);

        game_over = false;
        previous_event = 0;

        if(i.getIntExtra("master", 0) == 1){
            isMaster = true;
            fsmGame.setState(FSMGame.STATE_IN_GAME_WAITING);
        }else{
            isMaster = false;
            fsmGame.setState(FSMGame.STATE_IN_GAME);
        }

        Log.d(TAG, "Sono master : " + isMaster);

        return scene;

    }

    @Override
    protected void attachBall() {
        Log.i(TAG, "Call drawBall() with haveBall = " + haveBall);
        if(haveBall) super.attachBall();
    }

    @Override
    protected void setBallVeloctity() {
        // do nothing
    }

    @Override
    protected boolean topCondition() {
        if (ballSprite.getY() < ballSprite.getWidth()/2 && previous_event != TOP && haveBall && !transferringBall){
            Log.d(TAG, "topCondition TRUE");
            return true;
        }
        else return false;
        //return super.topCondition() && haveBall;
    }

    @Override
    protected void collidesTop() {
        Log.d(TAG, "collidesTop");
        float xRatio = ballSprite.getX()/CAMERA_WIDTH;
        AppMessage messageCoords = new AppMessage(Constants.MSG_TYPE_COORDS,
                handler.getVelocityX(),
                handler.getVelocityY(),
                xRatio);
        sendBluetoothMessage(messageCoords);
        Log.d(TAG, "End Top. TransBall: " + transferringBall);
        Log.d(TAG, "End Top. HaveBall: " + haveBall);
        haveBall = false;
        transferringBall = true;
        previous_event = TOP;
    }

    @Override
    protected void bluetoothExtra() {
        if (ballSprite.getY() < -ballSprite.getWidth()/2){
            Log.d(TAG, "getY < getwidth/2");
            scene.detachChild(ballSprite);
            //ballSprite.detachSelf();
            transferringBall = false;
        }
        if (ballSprite.getY() > ballSprite.getWidth()/2){
            Log.d(TAG, "getY > getwidth/2");
            transferringBall = false;
        }
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
        //do nothing
    }

    @Override
    public void remScore() {
        //do nothing
    }

    @Override
    public void actionDownEvent() {
        //do nothing
    }

    @Override
    public void onBackPressed() {
        if(fsmGame.getState() == FSMGame.STATE_IN_GAME) {
            AppMessage messageFail = new AppMessage(Constants.MSG_TYPE_FAIL);
            sendBluetoothMessage(messageFail);
        }
        setResult(Activity.RESULT_CANCELED);
        fsmGame.setState(FSMGame.STATE_NOT_READY);
        super.onBackPressed();
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Handler Called");
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.i(TAG, "Message Write");
                    break;
                case Constants.MESSAGE_READ:
                    Log.i(TAG, "Message Read");
                    byte[] readBuf = (byte[]) msg.obj;
                    AppMessage recMsg = (AppMessage) Serializer.deserializeObject(readBuf);
                    if(recMsg != null) {
                        switch (recMsg.TYPE){
                            case Constants.MSG_TYPE_COORDS:
                                if(!haveBall){
                                    float xPos = (1-recMsg.OP4) * CAMERA_WIDTH;
                                    ballSprite.setPosition(xPos, -ballSprite.getWidth()/2 );
                                    handler.setVelocity(-recMsg.OP2,-recMsg.OP3);
                                    scene.attachChild(ballSprite);
                                    transferringBall = true;
                                    haveBall = true;
                                    Log.i(TAG, "x = " + recMsg.OP2 + " y = " + recMsg.OP3);
                                }
                                break;

                            case Constants.MSG_TYPE_SYNC:
                                //if(!synchronizedGame){
                                if(fsmGame.getState() == FSMGame.STATE_IN_GAME_WAITING){
                                    fsmGame.setState(FSMGame.STATE_IN_GAME);
                                }
                                break;
                            case Constants.MSG_TYPE_FAIL:
                                //if(synchronizedGame){
                                if(fsmGame.getState() == FSMGame.STATE_IN_GAME){
                                    fsmGame.setState(FSMGame.STATE_OPPONENT_LEFT);
                                }
                                break;
                            default:
                                Log.e(TAG, "Ricevuto messaggio non idoneo - Type is " + recMsg.TYPE);
                        }
                    }else{
                        Log.e(TAG, "Ricevuto messaggio nullo.");
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    private final Handler fsmHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case FSMGame.STATE_NOT_READY:
                            break;
                        case FSMGame.STATE_READY:
                            break;
                        case FSMGame.STATE_CONNECTED:
                            break;
                        case FSMGame.STATE_IN_GAME:
                            handler.setVelocity(BALL_SPEED, -BALL_SPEED);
                            GAME_VELOCITY = 2;
                            AppMessage messageSync = new AppMessage(Constants.MSG_TYPE_SYNC);
                            sendBluetoothMessage(messageSync);
                            break;
                        case FSMGame.STATE_IN_GAME_WAITING:
                            handler.setVelocity(0, 0);
                            GAME_VELOCITY = 0;
                            break;
                        case FSMGame.STATE_DISCONNECTED:
                            break;
                        case FSMGame.STATE_OPPONENT_LEFT:
                            handler.setVelocity(0, 0);
                            GAME_VELOCITY = 0;
                            Text textLeft = new Text(
                                    10,
                                    10,
                                    font,
                                    getApplicationContext().getString(R.string.text_opponent_left),
                                    30,
                                    getVertexBufferObjectManager());
                            //TODO Piazzare al centro la scritta.
                            //textLeft.setX(CAMERA_WIDTH-textLeft.getWidth()/2);
                            //textLeft.setY(CAMERA_HEIGHT-textLeft.getHeight()/2);
                            scene.attachChild(textLeft);
                            break;
                        default:
                    }
                default:
            }
        }
    };

    private void sendBluetoothMessage(AppMessage message){
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

}
