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
import it.unina.is2project.sensorgames.bluetooth.messages.CoordsMessage;

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
    public void settingPhysics() {
        /** Setting up physics
         * - A physics handler is linked to the ballSprite
         */
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        if(!synchronizedGame){
            handler.setVelocity(0, 0);
            GAME_VELOCITY = 0;
        }else {
            handler.setVelocity(BALL_SPEED, -BALL_SPEED);
            CoordsMessage cm = new CoordsMessage(Constants.MSG_TYPE_SYNC, 0, 0, 0);
            sendMessage(cm);
        }


        /** The Update Handler is linked to the scene. It evalutates the condition of the scene
         *  every frame.
         */
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                /** Border variables */
                int rL = CAMERA_WIDTH - (int) ballSprite.getWidth() / 2;
                int bL = CAMERA_HEIGHT - (int) ballSprite.getHeight() / 2;

                /** Edge's condition
                 *  The direction of the ball changes depending on the
                 *  affected side
                 */
                if ((ballSprite.getX() > rL - (int) ballSprite.getWidth() / 2) && previous_event != RIGHT) {
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    previous_event = RIGHT;
                }
                if (ballSprite.getX() < 0 && previous_event != LEFT) {
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    previous_event = LEFT;
                }
                //if (ballSprite.getY() < 0 && previous_event != TOP && haveBall) {
                if (ballSprite.getY() < ballSprite.getWidth()/2 && previous_event != TOP && haveBall && !transferringBall) {
                    //handler.setVelocityY(-handler.getVelocityY());
                    //touch.play();
                    float xRatio = ballSprite.getX()/CAMERA_WIDTH;
                    CoordsMessage cm = new CoordsMessage(Constants.MSG_TYPE_DATA,
                            handler.getVelocityX(),
                            handler.getVelocityY(),
                            xRatio);
                    sendMessage(cm);
                    //scene.detachChild(ballSprite);
                    haveBall = false;
                    transferringBall = true;
                    previous_event = TOP;
                }
                if (ballSprite.getY() < -ballSprite.getWidth()/2){
                    scene.detachChild(ballSprite);
                    transferringBall = false;
                }
                if (ballSprite.getY() > ballSprite.getWidth()/2){
                    transferringBall = false;
                }

                if ((ballSprite.getY() > bL - (int) ballSprite.getHeight() / 2) && previous_event != BOTTOM) {
                    previous_event = BOTTOM;
                    restartOnBallLost();
                }

                /** When the barSprite and the ballSprite collides */
                if (ballSprite.collidesWith(barSprite)) {
                    /** Condition variable who understand if the ball hit the bar side or front
                     *
                     * - ya: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     * - yb: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     */
                    float ya = ballSprite.getY() - ballSprite.getHeight() / 2;
                    float yb = barSprite.getY() - barSprite.getHeight() / 2;

                    /** The ball hit the bar's top surface */
                    if (ya <= yb && previous_event != OVER && previous_event != SIDE) {
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                    }
                    /** The ball hit the bar's side surface */
                    if (previous_event != SIDE && previous_event != OVER) {
                        handler.setVelocityX(-handler.getVelocityX());
                        previous_event = SIDE;
                    }
                    touch.play();
                }
            }

            @Override
            public void reset() {
            }
        });
    }

    @Override
    public void restartOnBallLost() {
        /**  The ballSprite is detached */
        scene.detachChild(ballSprite);

        /** Setting the position on centre of screen */
        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth())/2, (CAMERA_HEIGHT - ballSprite.getHeight())/2);

        /** Set the direction upward */
        handler.setVelocityY(-handler.getVelocityY());

        /** The ballSprite is attached */
        scene.attachChild(ballSprite);
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
    protected void attachBall() {
        Log.i(TAG, "Call drawBall() with haveBall = " + haveBall);
        if(haveBall) super.attachBall();

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
                    CoordsMessage recMsg = (CoordsMessage) Serializer.deserializeObject(readBuf);
                    if(recMsg != null) {
                        switch (recMsg.TYPE){
                            case Constants.MSG_TYPE_DATA:
                                if(!haveBall){
                                    float xPos = (1-recMsg.X_RATIO) * CAMERA_WIDTH;
                                    ballSprite.setPosition(xPos, -ballSprite.getWidth()/2 );
                                    handler.setVelocity(-recMsg.VELOCITY_X,-recMsg.VELOCITY_Y);
                                    scene.attachChild(ballSprite);
                                    transferringBall = true;
                                    haveBall = true;
                                    Log.i(TAG, "x = " + recMsg.VELOCITY_X + " y = " + recMsg.VELOCITY_Y);
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

    private void sendMessage(CoordsMessage fm){
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = Serializer.serializeObject(fm);
        mBluetoothService.write(send);
    }

    @Override
    public void onBackPressed() {
        if(synchronizedGame) {
            CoordsMessage cm = new CoordsMessage(Constants.MSG_TYPE_FAIL, 0, 0, 0);
            sendMessage(cm);
            synchronizedGame = false;
        }
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
