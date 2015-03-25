package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

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
    // Indicates when we starts to play
    private boolean synchronizedGame = false;
    // Indicates when ball is passing
    private boolean transferringBall = false;

    // Result Codes
    public static final int CONNECTION_DOWN = 203;

    // Service bluetooth
    private BluetoothService mBluetoothService = null;

    @Override
    protected Scene onCreateScene() {
        // Retrieve intent message
        Intent i = getIntent();
        if(i.getIntExtra("ball", 0) == 1){
            haveBall = true;
        }else{
            haveBall = false;
        }

        if(i.getIntExtra("master", 0) == 1){
            isMaster = true;
            synchronizedGame = false;
        }else{
            isMaster = false;
            synchronizedGame = true;
        }

        Log.d(TAG, "Sono master : " + isMaster);
        Log.d(TAG, "Ho la palla : " + haveBall);
        Log.d(TAG, "Sono pronto : " + synchronizedGame);

        // Set result in failure case
        setResult(Activity.RESULT_CANCELED);

        super.onCreateScene();

        mBluetoothService = BluetoothService.getBluetoothService(getApplicationContext(), mHandler);

        settingPhysics();

        return scene;

    }


    @Override
    public void addScore() {

    }

    @Override
    public void remScore() {

    }

    @Override
    public void actionDownEvent() {

    }

    @Override
    protected void gameOver() {

    }

    @Override
    protected void attachBall() {
        Log.i(TAG, "Call drawBall() with haveBall = " + haveBall);
        if(haveBall) super.attachBall();
    }

    @Override
    protected void gameLevels() {

    }

    @Override
    protected void gameEvents() {

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
                                if(!synchronizedGame){
                                    handler.setVelocity(BALL_SPEED, -BALL_SPEED);
                                    GAME_VELOCITY = 2;
                                    synchronizedGame = true;
                                }
                                break;
                            case Constants.MSG_TYPE_FAIL:
                                if(synchronizedGame){
                                    handler.setVelocity(0, 0);
                                    GAME_VELOCITY = 0;
                                    synchronizedGame = false;
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

    private void sendMessage(AppMessage message){
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(message);
        mBluetoothService.write(send);
    }

    @Override
    public void onBackPressed() {
        if(synchronizedGame) {
            AppMessage messageFail = new AppMessage(Constants.MSG_TYPE_FAIL);
            sendMessage(messageFail);
            synchronizedGame = false;
        }
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void setBallVeloctity() {
        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        if(!synchronizedGame){
            handler.setVelocity(0, 0);
            GAME_VELOCITY = 0;
        }else {
            handler.setVelocity(BALL_SPEED, -BALL_SPEED);
            AppMessage messageSync = new AppMessage(Constants.MSG_TYPE_SYNC);
            sendMessage(messageSync);
        }
    }

    @Override
    protected boolean topCondition() {
        if (ballSprite.getY() < 0 && previous_event != TOP && haveBall){
            return true;
        }
        else return false;
        //return super.topCondition() && haveBall;
    }

    @Override
    protected void collidesTop() {
        Log.d(TAG, "Top. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = TOP;
        if(!transferringBall) {
            float xRatio = ballSprite.getX()/CAMERA_WIDTH;
            AppMessage messageCoords = new AppMessage(Constants.MSG_TYPE_COORDS,
                    handler.getVelocityX(),
                    handler.getVelocityY(),
                    xRatio);
            sendMessage(messageCoords);
            //scene.detachChild(ballSprite);
            haveBall = false;
            transferringBall = true;
        }
        if (ballSprite.getY() < -ballSprite.getWidth()){
            scene.detachChild(ballSprite);
            transferringBall = false;
        }
    }
}
