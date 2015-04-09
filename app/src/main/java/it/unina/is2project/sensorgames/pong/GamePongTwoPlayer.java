package it.unina.is2project.sensorgames.pong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

import java.util.ArrayList;
import java.util.List;
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
    // Speed X2 Icon
    private BitmapTextureAtlas speedIconTexture_X2;
    private ITextureRegion speedIconTextureRegion_X2;
    private Sprite speedIconSprite_X2;
    // Speed X3 Bonus
    private BitmapTextureAtlas speedTexture_X3;
    private ITextureRegion speedTextureRegion_X3;
    private Sprite speedSprite_X3;
    // Speed X3 Icon
    private BitmapTextureAtlas speedIconTexture_X3;
    private ITextureRegion speedIconTextureRegion_X3;
    private Sprite speedIconSprite_X3;
    // Speed X4 Bonus
    private BitmapTextureAtlas speedTexture_X4;
    private ITextureRegion speedTextureRegion_X4;
    private Sprite speedSprite_X4;
    // Speed X4 Icon
    private BitmapTextureAtlas speedIconTexture_X4;
    private ITextureRegion speedIconTextureRegion_X4;
    private Sprite speedIconSprite_X4;
    // Lock Field Bonus
    private BitmapTextureAtlas lockFieldTexture;
    private ITextureRegion lockFieldTextureRegion;
    private Sprite lockFieldSprite;
    // Cut Bar 30 Bonus
    private BitmapTextureAtlas cutBar30Texture;
    private ITextureRegion cutBar30TextureRegion;
    private Sprite cutBar30Sprite;
    // Cut Bar 50 Bonus
    private BitmapTextureAtlas cutBar50Texture;
    private ITextureRegion cutBar50TextureRegion;
    private Sprite cutBar50Sprite;
    // Reverted Bar Bonus
    private BitmapTextureAtlas revertedBarTexture;
    private ITextureRegion revertedBarTextureRegion;
    private Sprite revertedBarSprite;
    // Rush-Hour Bonus
    private BitmapTextureAtlas rushHourTexture;
    private ITextureRegion rushHourTextureRegion;
    private Sprite rushHourSprite;
    private List<Sprite> rushHour = new ArrayList<>();
    private List<PhysicsHandler> rushHourHandlers = new ArrayList<>();
    private boolean rush_hour = false;
    private final int RUSH_HOUR_MIN_NUM = 15;
    private final int RUSH_HOUR_MAX_NUM = 30;
    // Bonus Constants
    public final static int NOBONUS = 1; // Necessarily with value 1 -> If change see setVelocityFromPrevious
    public final static int SPEEDX2 = 2; // Necessarily with value 2 -> If change see setVelocityFromPrevious
    public final static int SPEEDX3 = 3; // Necessarily with value 3 -> If change see setVelocityFromPrevious
    public final static int SPEEDX4 = 4; // Necessarily with value 4 -> If change see setVelocityFromPrevious
    public final static int LOCKFIELD = 5;
    public final static int CUTBAR30 = 6;
    public final static int CUTBAR50 = 7;
    public final static int REVERTEDBAR = 8;
    public final static int RUSHHOUR = 9;
    // Bonus Utils
    private int activedSprite = SPRITE_NONE;
    private boolean deletedSprite = true;
    TimerTask task;
    Timer timer;
    private boolean locksField = false;
    private boolean speedx2_bonus = false;
    private boolean speedx3_bonus = false;
    private boolean speedx4_bonus = false;
    private float BARWIDTH;


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

        // Attaching textInfo
        textInfo = new Text(10, 10, font, "", 30, getVertexBufferObjectManager());
        scene.attachChild(textInfo);

        // Attachning textPoint
        textPoint = new Text(10, CAMERA_HEIGHT-textInfo.getHeight(), font, getResources().getString(R.string.sts_score) + " " + score, 30, getVertexBufferObjectManager());
        scene.attachChild(textPoint);

        // Traslating bar
        barSprite.setY(CAMERA_HEIGHT-textPoint.getHeight()-barSprite.getHeight());
        BARWIDTH = barSprite.getWidth();

        initializeSprite();

        return scene;
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();
        // Speed X2 Bonus
        Drawable speedDrawable_X2 = getResources().getDrawable(R.drawable.speedx2);
        speedTexture_X2 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X2.getIntrinsicWidth(), speedDrawable_X2.getIntrinsicHeight());
        speedTextureRegion_X2 = createFromResource(speedTexture_X2, this, R.drawable.speedx2, 0, 0);
        speedTexture_X2.load();
        // Speed X2 Icon
        Drawable speedIconDrawable_X2 = getResources().getDrawable(R.drawable.speedx2_icon);
        speedIconTexture_X2 = new BitmapTextureAtlas(getTextureManager(), speedIconDrawable_X2.getIntrinsicWidth(), speedIconDrawable_X2.getIntrinsicHeight());
        speedIconTextureRegion_X2 = createFromResource(speedIconTexture_X2, this, R.drawable.speedx2_icon, 0, 0);
        speedIconTexture_X2.load();
        // Speed X3 Bonus
        Drawable speedDrawable_X3 = getResources().getDrawable(R.drawable.speedx3);
        speedTexture_X3 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X3.getIntrinsicWidth(), speedDrawable_X3.getIntrinsicHeight());
        speedTextureRegion_X3 = createFromResource(speedTexture_X3, this, R.drawable.speedx3, 0, 0);
        speedTexture_X3.load();
        // Speed X3 Icon
        Drawable speedIconDrawable_X3 = getResources().getDrawable(R.drawable.speedx3_icon);
        speedIconTexture_X3 = new BitmapTextureAtlas(getTextureManager(), speedIconDrawable_X3.getIntrinsicWidth(), speedIconDrawable_X3.getIntrinsicHeight());
        speedIconTextureRegion_X3 = createFromResource(speedIconTexture_X3, this, R.drawable.speedx3_icon, 0, 0);
        speedIconTexture_X3.load();
        // Speed X4 Bonus
        Drawable speedDrawable_X4 = getResources().getDrawable(R.drawable.speedx4);
        speedTexture_X4 = new BitmapTextureAtlas(getTextureManager(), speedDrawable_X4.getIntrinsicWidth(), speedDrawable_X4.getIntrinsicHeight());
        speedTextureRegion_X4 = createFromResource(speedTexture_X4, this, R.drawable.speedx4, 0, 0);
        speedTexture_X4.load();
        // Speed X4 Icon
        Drawable speedIconDrawable_X4 = getResources().getDrawable(R.drawable.speedx4_icon);
        speedIconTexture_X4 = new BitmapTextureAtlas(getTextureManager(), speedIconDrawable_X4.getIntrinsicWidth(), speedIconDrawable_X4.getIntrinsicHeight());
        speedIconTextureRegion_X4 = createFromResource(speedIconTexture_X4, this, R.drawable.speedx4_icon, 0, 0);
        speedIconTexture_X4.load();
        // Lock Screen Bonus
        Drawable lockFieldDrawable = getResources().getDrawable(R.drawable.firstenemy);
        lockFieldTexture = new BitmapTextureAtlas(getTextureManager(), lockFieldDrawable.getIntrinsicWidth(), lockFieldDrawable.getIntrinsicHeight());
        lockFieldTextureRegion = createFromResource(lockFieldTexture, this, R.drawable.firstenemy, 0, 0);
        lockFieldTexture.load();
        // Cut Bar 30% Bonus
        Drawable cutBar30Drawable = getResources().getDrawable(R.drawable.reduce30);
        cutBar30Texture = new BitmapTextureAtlas(getTextureManager(), cutBar30Drawable.getIntrinsicWidth(), cutBar30Drawable.getIntrinsicHeight());
        cutBar30TextureRegion = createFromResource(cutBar30Texture, this, R.drawable.reduce30, 0, 0);
        cutBar30Texture.load();
        // Cut Bar 50% Bonus
        Drawable cutBar50Drawable = getResources().getDrawable(R.drawable.reduce50);
        cutBar50Texture = new BitmapTextureAtlas(getTextureManager(), cutBar50Drawable.getIntrinsicWidth(), cutBar50Drawable.getIntrinsicHeight());
        cutBar50TextureRegion = createFromResource(cutBar50Texture, this, R.drawable.reduce50, 0, 0);
        cutBar50Texture.load();
        // Reverted Bar Bonus
        Drawable revertedBarDrawable = getResources().getDrawable(R.drawable.revert);
        revertedBarTexture = new BitmapTextureAtlas(getTextureManager(), revertedBarDrawable.getIntrinsicWidth(), revertedBarDrawable.getIntrinsicHeight());
        revertedBarTextureRegion = createFromResource(revertedBarTexture, this, R.drawable.revert, 0, 0);
        revertedBarTexture.load();
        // Rush Hour Bonus
        Drawable rushHourDrawable = getResources().getDrawable(R.drawable.rush_hour);
        rushHourTexture = new BitmapTextureAtlas(getTextureManager(), rushHourDrawable.getIntrinsicWidth(), rushHourDrawable.getIntrinsicHeight());
        rushHourTextureRegion = createFromResource(rushHourTexture, this, R.drawable.rush_hour, 0, 0);
        rushHourTexture.load();
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
        if(!locksField) {
            float xRatio = ballSprite.getX() / CAMERA_WIDTH;
            AppMessage messageCoords = new AppMessage(Constants.MSG_TYPE_COORDS,
                    Math.signum(handler.getVelocityX()),
                    COS_X,
                    SIN_X,
                    xRatio);
            sendBluetoothMessage(messageCoords);
//            Log.d("MESSAGECOORDSsen", "Module " + myModule);
//            Log.d("MESSAGECOORDSsen", "VelX " + handler.getVelocityX());
//            Log.d("MESSAGECOORDSsen", "VelY " + handler.getVelocityY());
//            Log.d("MESSAGECOORDSsen", "Sign(Velx) " + Math.signum(handler.getVelocityX()));
            haveBall = false;
            transferringBall = true;
            previous_event = TOP;
        }else{
            super.collidesTop();
        }
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
        if(rush_hour){
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
        if(timer != null) timer.cancel();
        super.onBackPressed();
    }

    //----------------------------------------------
    // MISCELLANEA
    //----------------------------------------------
    private void sendBluetoothMessage(AppMessage message) {
        Log.d("SendReceived", "Send Message Type: " + message.TYPE);
        if (mBluetoothService.getState() != mBluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_notConnected), Toast.LENGTH_SHORT).show();
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
            case LOCKFIELD:
                if(x <= lockFieldSprite.getX() + lockFieldSprite.getWidth() &&
                        x >= lockFieldSprite.getX() &&
                        y >= lockFieldSprite.getY() &&
                        y <= lockFieldSprite.getY() + lockFieldSprite.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case CUTBAR30:
                if(x <= cutBar30Sprite.getX() + cutBar30Sprite.getWidth() &&
                        x >= cutBar30Sprite.getX() &&
                        y >= cutBar30Sprite.getY() &&
                        y <= cutBar30Sprite.getY() + cutBar30Sprite.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case CUTBAR50:
                if(x <= cutBar50Sprite.getX() + cutBar50Sprite.getWidth() &&
                        x >= cutBar50Sprite.getX() &&
                        y >= cutBar50Sprite.getY() &&
                        y <= cutBar50Sprite.getY() + cutBar50Sprite.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case REVERTEDBAR:
                if(x <= revertedBarSprite.getX() + revertedBarSprite.getWidth() &&
                        x >= revertedBarSprite.getX() &&
                        y >= revertedBarSprite.getY() &&
                        y <= revertedBarSprite.getY() + revertedBarSprite.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            case RUSHHOUR:
                if(x <= rushHourSprite.getX() + rushHourSprite.getWidth() &&
                        x >= rushHourSprite.getX() &&
                        y >= rushHourSprite.getY() &&
                        y <= rushHourSprite.getY() + rushHourSprite.getHeight())
                    checkTouchSpriteStatus = true;
                break;
            default:
                checkTouchSpriteStatus = false;
                break;
        }
        return checkTouchSpriteStatus;
    }

    private void initializeSprite(){

        // SPEED X2 BONUS INITIALIZING

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

        // SPEED X2 ICON INITIALIZING

        speedIconSprite_X2 = new Sprite(0, 0, speedIconTextureRegion_X2, getVertexBufferObjectManager());
        speedIconSprite_X2.setX(CAMERA_WIDTH/2);
        speedIconSprite_X2.setY(textPoint.getY());

        // SPEED X3 BONUS INITIALIZING

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

        // SPEED X3 ICON INITIALIZING

        speedIconSprite_X3 = new Sprite(0, 0, speedIconTextureRegion_X3, getVertexBufferObjectManager());
        speedIconSprite_X3.setX(speedIconSprite_X2.getX() + speedIconSprite_X2.getWidth());
        speedIconSprite_X3.setY(textPoint.getY());
        
        // SPEED X4 BONUS INITIALIZING

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

        // SPEED X4 ICON INITIALIZING

        speedIconSprite_X4 = new Sprite(0, 0, speedIconTextureRegion_X4, getVertexBufferObjectManager());
        speedIconSprite_X4.setX(speedIconSprite_X3.getX() + speedIconSprite_X3.getWidth());
        speedIconSprite_X4.setY(textPoint.getY());

        // LOCK FIELD INITIALIZING

        lockFieldSprite = new Sprite(0, 0, lockFieldTextureRegion, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite LOCKFIELD Touched");
                detachSprite(LOCKFIELD);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage lockFieldMessage = new AppMessage(Constants.MSG_TYPE_BONUS_LOCKFIELD, randNum);
                sendBluetoothMessage(lockFieldMessage);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        lockFieldSprite.setScale(lockFieldSprite.getScaleX()/2);
        lockFieldSprite.setX((CAMERA_WIDTH / 2) - (lockFieldSprite.getWidth() / 2));
        lockFieldSprite.setY((CAMERA_HEIGHT / 2) - (lockFieldSprite.getHeight() / 2));

        // CUT BAR 30 INITIALIZING

        cutBar30Sprite = new Sprite(0, 0, cutBar30TextureRegion, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite CUTBAR30 Touched");
                detachSprite(CUTBAR30);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage cutBar30Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUTBAR30, randNum);
                sendBluetoothMessage(cutBar30Message);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        cutBar30Sprite.setScale(cutBar30Sprite.getScaleX()/2);
        cutBar30Sprite.setX((CAMERA_WIDTH / 2) - (cutBar30Sprite.getWidth() / 2));
        cutBar30Sprite.setY((CAMERA_HEIGHT / 2) - (cutBar30Sprite.getHeight() / 2));

        // CUT BAR 50 INITIALIZING

        cutBar50Sprite = new Sprite(0, 0, cutBar50TextureRegion, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite CUTBAR50 Touched");
                detachSprite(CUTBAR50);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage cutBar50Message = new AppMessage(Constants.MSG_TYPE_BONUS_CUTBAR50, randNum);
                sendBluetoothMessage(cutBar50Message);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        cutBar50Sprite.setScale(cutBar50Sprite.getScaleX()/2);
        cutBar50Sprite.setX((CAMERA_WIDTH / 2) - (cutBar50Sprite.getWidth() / 2));
        cutBar50Sprite.setY((CAMERA_HEIGHT / 2) - (cutBar50Sprite.getHeight() / 2));

        // REVERTED BAR INITIALIZING

        revertedBarSprite = new Sprite(0, 0, revertedBarTextureRegion, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite REVERTEDBAR Touched");
                detachSprite(REVERTEDBAR);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage revertedBarMessage = new AppMessage(Constants.MSG_TYPE_BONUS_REVERTEDBAR, randNum);
                sendBluetoothMessage(revertedBarMessage);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        revertedBarSprite.setScale(revertedBarSprite.getScaleX()/2);
        revertedBarSprite.setX((CAMERA_WIDTH / 2) - (revertedBarSprite.getWidth() / 2));
        revertedBarSprite.setY((CAMERA_HEIGHT / 2) - (revertedBarSprite.getHeight() / 2));
        
        // RUSH-HOUR INITIALIZING

        rushHourSprite = new Sprite(0, 0, rushHourTextureRegion, getVertexBufferObjectManager()){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Log.d("Sprite", "Sprite RUSHHOUR Touched");
                detachSprite(RUSHHOUR);
                Random rand = new Random();
                int randNum = rand.nextInt(5) + 1;
                AppMessage rushHourMessage = new AppMessage(Constants.MSG_TYPE_BONUS_RUSHHOUR, randNum);
                sendBluetoothMessage(rushHourMessage);
                return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        rushHourSprite.setScale(rushHourSprite.getScaleX()/2);
        rushHourSprite.setX((CAMERA_WIDTH / 2) - (rushHourSprite.getWidth() / 2));
        rushHourSprite.setY((CAMERA_HEIGHT / 2) - (rushHourSprite.getHeight() / 2));
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
//                                        Log.d("MESSAGECOORDSrec", "COS_X " + recMsg.OP2);
//                                        Log.d("MESSAGECOORDSrec", "SIN_X " + recMsg.OP3);
                                        float xPos = (1 - recMsg.OP4) * CAMERA_WIDTH;
                                        float velX = -recMsg.OP1*myModule*recMsg.OP2;
                                        float velY = myModule*recMsg.OP3;
                                        COS_X = recMsg.OP2;
                                        SIN_X = recMsg.OP3;
//                                        Log.d("MESSAGECOORDSrec", "Module " + myModule);
//                                        Log.d("MESSAGECOORDSrec", "VelX " + velX);
//                                        Log.d("MESSAGECOORDSrec", "VelY " + velY);
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
                                //------------------------BONUS LOCKFIELD------------------------
                                case Constants.MSG_TYPE_BONUS_LOCKFIELD:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_LOCKFIELD");
                                    bonusManager.addBonus(LOCKFIELD, recMsg.OP1);
                                    break;
                                //------------------------BONUS CUTBAR30------------------------
                                case Constants.MSG_TYPE_BONUS_CUTBAR30:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_CUTBAR30");
                                    bonusManager.addBonus(CUTBAR30, recMsg.OP1);
                                    break;
                                //------------------------BONUS CUTBAR50------------------------
                                case Constants.MSG_TYPE_BONUS_CUTBAR50:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_CUTBAR50");
                                    bonusManager.addBonus(CUTBAR50, recMsg.OP1);
                                    break;
                                //------------------------BONUS REVERTEDBAR------------------------
                                case Constants.MSG_TYPE_BONUS_REVERTEDBAR:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_REVERTEDBAR");
                                    bonusManager.addBonus(REVERTEDBAR, recMsg.OP1);
                                    break;
                                //------------------------BONUS RUSH-HOUR------------------------
                                case Constants.MSG_TYPE_BONUS_RUSHHOUR:
                                    Log.d("SendReceived", "MSG_TYPE_BONUS_RUSHHOUR");
                                    bonusManager.addBonus(RUSHHOUR, recMsg.OP1);
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
                                textInfo.setText(getResources().getString(R.string.text_waiting));
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
                                textInfo.setText(getResources().getString(R.string.text_disconnected));
                                break;
                            case FSMGame.STATE_OPPONENT_LEFT:
                                handler.setVelocity(0, 0);
                                GAME_VELOCITY = 0;
                                if(rush_hour){
                                    clearRushHour();
                                }
                                textInfo.setText(getResources().getString(R.string.text_opponent_left));
                                break;
                            default:
                                Log.e("FSMGame", "Invalid State : " + msg.arg1);
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
                            Log.d("BONUSCREATED", "SPEEDX2");
                            setVelocityFromPrevious(msg.arg2, SPEEDX2);
                            removePreviuousSpeedSpriteIcons();
                            speedx2_bonus = true;
                            scene.attachChild(speedIconSprite_X2);
                            break;
                        case SPEEDX3:
                            Log.d("BONUSCREATED", "SPEEDX3");
                            setVelocityFromPrevious(msg.arg2, SPEEDX3);
                            removePreviuousSpeedSpriteIcons();
                            speedx3_bonus = true;
                            scene.attachChild(speedIconSprite_X3);
                            break;
                        case SPEEDX4:
                            Log.d("BONUSCREATED", "SPEEDX4");
                            setVelocityFromPrevious(msg.arg2, SPEEDX4);
                            removePreviuousSpeedSpriteIcons();
                            speedx4_bonus = true;
                            scene.attachChild(speedIconSprite_X4);
                            break;
                        case LOCKFIELD:
                            Log.d("BONUSCREATED", "LOCKFIELD");
                            locksField = true;
                            break;
                        case CUTBAR30:
                            Log.d("BONUSCREATED", "CUTBAR30");
                            barSprite.setWidth(BARWIDTH*0.7f);
                            break;
                        case CUTBAR50:
                            Log.d("BONUSCREATED", "CUTBAR50");
                            barSprite.setWidth(BARWIDTH*0.5f);
                            break;
                        case REVERTEDBAR:
                            Log.d("BONUSCREATED", "REVERTEDBAR");
                            if(Math.signum(GAME_VELOCITY) > 0)
                                GAME_VELOCITY = -GAME_VELOCITY;
                            break;
                        case RUSHHOUR:
                            Log.d("BONUSCREATED", "RUSHHOUR");
                            if(!rush_hour) {
                                rush_hour = true;
                                rushHourLogic();
                            }
                            break;

                        default:
                            Log.e("Bonus", "Error - Invalid Bonus ID Created");
                            break;
                    }
                    break;
                case BonusManager.BONUS_EXPIRED:
                    switch (msg.arg1){
                        case SPEEDX2:
                            Log.d("BONUSEXPIRED", "SPEEDX2");
                            setVelocityFromPrevious(SPEEDX2, NOBONUS);
                            speedx2_bonus = false;
                            scene.detachChild(speedIconSprite_X2);
                            break;
                        case SPEEDX3:
                            Log.d("BONUSEXPIRED", "SPEEDX3");
                            setVelocityFromPrevious(SPEEDX3, NOBONUS);
                            speedx3_bonus = false;
                            scene.detachChild(speedIconSprite_X3);
                            break;
                        case SPEEDX4:
                            Log.d("BONUSEXPIRED", "SPEEDX4");
                            setVelocityFromPrevious(SPEEDX4, NOBONUS);
                            speedx4_bonus = false;
                            scene.detachChild(speedIconSprite_X4);
                            break;
                        case LOCKFIELD:
                            Log.d("BONUSEXPIRED", "LOCKFIELD");
                            locksField = false;
                            break;
                        case CUTBAR30:
                            Log.d("BONUSEXPIRED", "CUTBAR30");
                            barSprite.setWidth(BARWIDTH);
                            break;
                        case CUTBAR50:
                            Log.d("BONUSEXPIRED", "CUTBAR50");
                            barSprite.setWidth(BARWIDTH);
                            break;
                        case REVERTEDBAR:
                            Log.d("BONUSEXPIRED", "REVERTEDBAR");
                            if(Math.signum(GAME_VELOCITY) < 0)
                                GAME_VELOCITY = -GAME_VELOCITY;
                            break;
                        case RUSHHOUR:
                            Log.d("BONUSEXPIRED", "RUSHHOUR");
                            clearRushHour();
                            rush_hour = false;
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

    private void removePreviuousSpeedSpriteIcons() {
        if(speedx2_bonus){
            scene.detachChild(speedIconSprite_X2);
            speedx2_bonus = false;
        }
        if(speedx3_bonus){
            scene.detachChild(speedIconSprite_X3);
            speedx3_bonus = false;
        }
        if(speedx4_bonus){
            scene.detachChild(speedIconSprite_X4);
            speedx4_bonus = false;
        }
    }

    private void rushHourLogic() {
        Random random = new Random();
        int RUSH_HOUR_NUM = RUSH_HOUR_MIN_NUM + random.nextInt(RUSH_HOUR_MAX_NUM - RUSH_HOUR_MIN_NUM + 1);
        for (int i = 0; i < RUSH_HOUR_NUM; i++) {
            Sprite rush = new Sprite(0, 0, ballTextureRegion, getVertexBufferObjectManager());
            rushHour.add(rush);
            rush.setWidth(CAMERA_WIDTH * 0.1f);
            rush.setHeight(CAMERA_WIDTH * 0.1f);
            rush.setPosition((int) rush.getWidth() + random.nextInt(CAMERA_WIDTH - (int) rush.getWidth() * 2), (int) rush.getHeight() + random.nextInt(CAMERA_HEIGHT - (int) rush.getHeight() * 2));

            PhysicsHandler physicsHandler = new PhysicsHandler(rushHour.get(i));
            physicsHandler.setVelocity(BALL_SPEED * (random.nextFloat() - random.nextFloat()), BALL_SPEED * (random.nextFloat() - random.nextFloat()));
            rushHourHandlers.add(physicsHandler);

            rushHour.get(i).registerUpdateHandler(rushHourHandlers.get(i));

            scene.attachChild(rushHour.get(i));
        }
    }

    private void clearRushHour() {
        while (rushHour.size() > 0){
            rushHour.get(0).detachSelf();
        }
        rushHour.clear();
        rushHourHandlers.clear();
    }

    private void setVelocityFromPrevious(int previousBonus, int nextBonus){
//        Log.d("BonusVelocity", "Previous Module: " + myModule);
//        Log.d("BonusVelocity", "Previous Velx: " + handler.getVelocityX());
//        Log.d("BonusVelocity", "Previous Vely: " + handler.getVelocityY());

        myModule = (myModule/previousBonus)*nextBonus;
        if(haveBall) {
            handler.setVelocityX(Math.signum(handler.getVelocityX()) * myModule * COS_X);
            handler.setVelocityY(Math.signum(handler.getVelocityY()) * myModule * SIN_X);
        }
//        Log.d("BonusVelocity", "New Module: " + myModule);
//        Log.d("BonusVelocity", "New Velx: " + handler.getVelocityX());
//        Log.d("BonusVelocity", "New Vely: " + handler.getVelocityY());
    }

    //----------------------------------------------
    // THREADS
    //----------------------------------------------

    private class TimerBonusTask extends TimerTask{
        @Override
        public void run() {
            Random rand = new Random();
            int bonusChoice = rand.nextInt(( RUSHHOUR - SPEEDX2) + 1) + SPEEDX2;
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

            case LOCKFIELD:
                Log.d("AttachSprite", "Attaching LOCKFIELD");
                scene.registerTouchArea(lockFieldSprite);
                scene.attachChild(lockFieldSprite);
                activedSprite = LOCKFIELD;
                deletedSprite = false;
                break;

            case CUTBAR30:
                Log.d("AttachSprite", "Attaching CUTBAR30");
                scene.registerTouchArea(cutBar30Sprite);
                scene.attachChild(cutBar30Sprite);
                activedSprite = CUTBAR30;
                deletedSprite = false;
                break;

            case CUTBAR50:
                Log.d("AttachSprite", "Attaching CUTBAR50");
                scene.registerTouchArea(cutBar50Sprite);
                scene.attachChild(cutBar50Sprite);
                activedSprite = CUTBAR50;
                deletedSprite = false;
                break;

            case REVERTEDBAR:
                Log.d("AttachSprite", "Attaching REVERTEDBAR");
                scene.registerTouchArea(revertedBarSprite);
                scene.attachChild(revertedBarSprite);
                activedSprite = REVERTEDBAR;
                deletedSprite = false;
                break;

            case RUSHHOUR:
                Log.d("AttachSprite", "Attaching RUSHHOUR");
                scene.registerTouchArea(rushHourSprite);
                scene.attachChild(rushHourSprite);
                activedSprite = RUSHHOUR;
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

            case LOCKFIELD:
                Log.d("DetachSprite", "Deattaching LOCKFIELD");
                scene.unregisterTouchArea(lockFieldSprite);
                scene.detachChild(lockFieldSprite);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;
            
            case CUTBAR30:
                Log.d("DetachSprite", "Deattaching CUTBAR30");
                scene.unregisterTouchArea(cutBar30Sprite);
                scene.detachChild(cutBar30Sprite);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            case CUTBAR50:
                Log.d("DetachSprite", "Deattaching CUTBAR50");
                scene.unregisterTouchArea(cutBar50Sprite);
                scene.detachChild(cutBar50Sprite);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            case REVERTEDBAR:
                Log.d("DetachSprite", "Deattaching REVERTEDBAR");
                scene.unregisterTouchArea(revertedBarSprite);
                scene.detachChild(revertedBarSprite);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            case RUSHHOUR:
                Log.d("DetachSprite", "Deattaching RUSHHOUR");
                scene.unregisterTouchArea(rushHourSprite);
                scene.detachChild(rushHourSprite);
                activedSprite = SPRITE_NONE;
                deletedSprite = true;
                break;

            default:
                Log.e(TAG, "Error in detachSprite(). Invalid ID");
                break;
        }
    }

}
