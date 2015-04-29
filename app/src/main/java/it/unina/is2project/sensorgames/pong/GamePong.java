package it.unina.is2project.sensorgames.pong;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.io.IOException;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.bluetooth.Constants;
import it.unina.is2project.sensorgames.game.entity.Ball;
import it.unina.is2project.sensorgames.game.entity.Bar;

public abstract class GamePong extends SimpleBaseGameActivity {

    //===========================================
    // CAMERA
    //===========================================
    protected Camera camera;
    protected int CAMERA_WIDTH;
    protected int CAMERA_HEIGHT;
    protected float DEVICE_RATIO;

    //===========================================
    // GRAPHICS
    //===========================================
    // Game Theme
    protected int theme;
    protected int theme_ball;
    protected int theme_bar;
    protected static final int CLASSIC = 0;
    protected static final int GOLD = 1;
    protected static final int BLUE = 2;
    // Ball
    protected Ball ball;
    protected float BALL_SPEED;
    // Bar
    protected Bar bar;
    protected float BAR_SPEED;
    // First Enemy
    protected FirstEnemyBonus firstEnemy;
    // Rush Hour
    protected RushHourBonus rushHour;

    //===========================================
    // SOUNDS
    //===========================================
    protected Sound touch;

    //===========================================
    // FONTS
    //===========================================
    protected ITexture fontTexture;
    protected Font font;

    //===========================================
    // Scene
    //===========================================
    protected Scene scene;
    // Collision events
    protected int previous_event;
    protected static final int NO_COLL = -1;
    protected static final int BOTTOM = 1;
    protected static final int TOP = 2;
    protected static final int LEFT = 3;
    protected static final int RIGHT = 4;
    protected static final int OVER = 5;
    protected static final int SIDE = 6;
    // Bounce bar angles
    protected float COS_20 = 0.93969262078590838405410927732473f;
    protected float SIN_20 = 0.34202014332566873304409961468226f;
    protected float COS_30 = 0.86602540378443864676372317075294f;
    protected float SIN_30 = 0.5f;
    protected float COS_40 = 0.76604444311897803520239265055542f;
    protected float SIN_40 = 0.64278760968653932632264340990726f;
    protected float COS_45 = 0.70710678118654752440084436210485f;
    protected float SIN_45 = 0.70710678118654752440084436210485f;
    protected float COS_50 = 0.64278760968653932632264340990726f;
    protected float SIN_50 = 0.76604444311897803520239265055542f;
    protected float COS_60 = 0.5f;
    protected float SIN_60 = 0.86602540378443864676372317075294f;
    protected float COS_70 = 0.34202014332566873304409961468226f;
    protected float SIN_70 = 0.93969262078590838405410927732473f;
    protected float COS_80 = 0.17364817766693034885171662676931f;
    protected float SIN_80 = 0.98480775301220805936674302458952f;
    protected float COS_90 = 0;
    protected float SIN_90 = 1;
    // Selected angles and module
    protected float SIN_X;
    protected float COS_X;
    protected float myModule;

    //===========================================
    // GAME DATA
    //===========================================
    protected String nickname;    // User nickname

    //===========================================
    // PAUSE UTILS
    //===========================================
    protected Text textPause;
    protected Text textPause_util;
    protected static final int PAUSE = -1;
    protected boolean pause = false;
    protected float old_x_speed;
    protected float old_y_speed;
    protected float old_bar_speed;
    protected long firstTap;

    //===========================================
    // ANIMATION UTILS
    //===========================================
    protected boolean animActive = false;

    @Override
    public EngineOptions onCreateEngineOptions() {
        // Understanding the device's display dimensions
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        CAMERA_WIDTH = displaySize.x;
        CAMERA_HEIGHT = displaySize.y;
        DEVICE_RATIO = (float) CAMERA_WIDTH / 480;
        Log.d("Camera", "Camera Width = " + CAMERA_WIDTH + ", Camera Height = " + CAMERA_HEIGHT + ", Device Ratio = " + DEVICE_RATIO);

        // Setting up the andEngine camera
        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        // Setting up the andEngine options
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);

        // Enable the sound option
        engineOptions.getAudioOptions().setNeedsSound(true);

        return engineOptions;
    }

    @Override
    protected void onCreateResources() {
        // Retrieve shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        theme = Integer.parseInt(sharedPreferences.getString("prefGameTheme", "0"));
        nickname = sharedPreferences.getString(Constants.PREF_NICKNAME, getString(R.string.txt_no_name));

        loadGraphics();
        loadSounds();
        loadFonts();
    }

    @Override
    protected Scene onCreateScene() {
        // Create a new scene
        scene = new Scene() {
            @Override
            public boolean onSceneTouchEvent(TouchEvent pSceneTouchEvent) {
                if (pSceneTouchEvent.isActionDown()) {
                    actionDownEvent(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                }
                return super.onSceneTouchEvent(pSceneTouchEvent);
            }
        };

        // Setting background
        setBackground();

        // Adding the textPause to the scene
        textPause_util = new Text(0, 0, font, "Pause", 20, getVertexBufferObjectManager());
        textPause = new Text((CAMERA_WIDTH - textPause_util.getWidth()) / 2, (CAMERA_HEIGHT - textPause_util.getHeight()) / 2, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textPause);

        // Adding the ball to the scene
        ball.addToScene(scene, 0.1f, 0.1f);
        ball.setPosition(Ball.TOP);

        // Adding the bar to the scene
        bar.addToScene(scene, 0.3f, 0.05f);
        bar.setPosition(Bar.BOTTOM);

        // Setting game velocity
        BAR_SPEED = 2 * DEVICE_RATIO;
        BALL_SPEED = 350 * DEVICE_RATIO;

        // Setting Bar speed
        bar.setBarSpeed(BAR_SPEED);

        // Setting Ball speed
        ball.setBallSpeed(BALL_SPEED);

        this.mEngine.registerUpdateHandler(new FPSLogger());

        return scene;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setBackground() {
        switch (theme) {
            case CLASSIC:
                scene.setBackground(new Background(0f, 0f, 0f));
                break;
            case GOLD:
                scene.setBackground(new Background(0.678f, 0.082f, 0.082f));
                break;
            case BLUE:
                scene.setBackground(new Background(0.082f, 0.2f, 0.678f));
                break;
        }
    }

    protected void loadGraphics() {
        switch (theme) {
            case CLASSIC:
                theme_ball = R.drawable.ball_white;
                theme_bar = R.drawable.bar_white;
                break;
            case GOLD:
                theme_ball = R.drawable.ball_gold;
                theme_bar = R.drawable.bar_gold;
                break;
            case BLUE:
                theme_ball = R.drawable.ball_blue;
                theme_bar = R.drawable.bar_blue;
                break;
        }
        // Ball and Bar texture loading
        ball = new Ball(this, theme_ball);
        bar = new Bar(this, theme_bar);
        // Rush Hour
        rushHour = new RushHourBonus(ball);

        loadAdditionalGraphics();
    }

    protected void loadAdditionalGraphics() {
        // First Enemy
        firstEnemy = new FirstEnemyBonus(this, theme_bar, ball);
    }

    protected void loadFonts() {
        // Setting the Asset Base Path for fonts
        FontFactory.setAssetBasePath("font/");
        // "secrcode.ttf" texture loading
        int fontSize = (int) getResources().getDimension(R.dimen.text_font);
        fontTexture = new BitmapTextureAtlas(getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        font = FontFactory.createFromAsset(getFontManager(), fontTexture, getAssets(), "secrcode.ttf", fontSize, true, Color.WHITE);
        font.load();
    }

    protected void loadSounds() {
        // Setting the Asset Base Path for sounds
        SoundFactory.setAssetBasePath("mfx/");
        // "paddlehit.ogg" sound loading
        try {
            touch = SoundFactory.createSoundFromAsset(getEngine().getSoundManager(), this, "paddlehit.ogg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void settingPhysics() {
        animActive = true;
        CountdownTimerScene countdownTimerScene = new CountdownTimerScene(3);
        scene.registerUpdateHandler(countdownTimerScene);
    }

    private class CountdownTimerScene implements IUpdateHandler{

        private float seconds;

        public CountdownTimerScene(int toSecond){
            this.seconds = toSecond;
        }

        @Override
        public void onUpdate(float pSecondsElapsed) {
            seconds -= pSecondsElapsed;
            textPause.setText("  " + (int)Math.ceil(seconds) + "  ");
            if(seconds < 0){
                animActive = false;
                textPause.setText("");
                scene.unregisterUpdateHandler(this);
                doPhysics();
            }
        }

        @Override
        public void reset() {
            seconds = 0;
        }
    }

    protected void doPhysics() {
        // Setting initial ball velocity
        setBallVelocity();

        // The Update Handler is linked to the scene. It evalutates the condition of the scene every frame
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                // Collision Detection
                if (!pause) {
                    collides(condition());
                    // Extra action relative to 2Player Game
                    bluetoothExtra();
                    // Game events collision
                    gameEventsCollisionLogic();
                }
            }

            @Override
            public void reset() {
            }
        });
    }

    protected void setBallVelocity() {
        ball.setHandlerSpeed(0, -ball.getBallSpeed());
    }

    protected int condition() {
        if ((ball.getXCoordinate() < 0) && (previous_event != LEFT)) {
            return LEFT;
        } else if ((ball.getXCoordinate() > ball.getDisplaySize().x - ball.getObjectWidth()) && (previous_event != RIGHT)) {
            return RIGHT;
        } else if (topCondition()) {
            return TOP;
        } else if ((ball.getYCoordinate() > ball.getDisplaySize().y) && (previous_event != BOTTOM)) {
            return BOTTOM;
        }
        if (ball.collidesWith(bar)) {
            float ballY = ball.getYCoordinate() + ball.getObjectHeight();
            float barY = bar.getYCoordinate() + bar.getObjectHeight();
            float ballX = ball.getXCentreCoordinate();
            float barX = bar.getXCentreCoordinate();
            float leftBar = -(bar.getObjectWidth()) / 2;
            float rightBar = (bar.getObjectWidth()) / 2;
            Log.d("Collision", "ballY = " + ballY);
            Log.d("Collision", "barY = " + barY);
            Log.d("Collision", "ballX - barX = " + (ballX - barX));
            Log.d("Collision", "LEFT BAR = " + leftBar);
            Log.d("Collision", "RIGHT BAR = " + rightBar);
            if ((ballY > barY) && ((ballX - barX < leftBar) || (ballX - barX > rightBar))
                    && (previous_event != OVER) && (previous_event != SIDE)) {
                Log.d("Collision", "SIDE. Because ballY > barY : " + ballY + " > " + barY +
                        " && (ballX - barX < LEFT BAR : " + (ballX - barX) + " < " + leftBar +
                        " || ballX - barX > RIGHT BAR : " + (ballX - barX)  + " > " + rightBar + ")");
                return SIDE;
            } else if ((previous_event != SIDE) && (previous_event != OVER)) {
                return OVER;
            }
        }
        return NO_COLL;
    }

    protected boolean topCondition() {
        return (ball.getYCoordinate() < 0) && (previous_event != TOP);
    }

    protected void collides(int collision_event) {
        switch (collision_event) {
            case RIGHT:
            case LEFT:
            case SIDE:
                Log.d("Collision", "RIGHT - LEFT - SIDE. V(X,Y): " + ball.getHandlerSpeedX() + "," + ball.getHandlerSpeedY());
                previous_event = collision_event;
                ball.setHandlerSpeedX(-ball.getHandlerSpeedX());
                touch.play();
                break;
            case TOP:
                collidesTop();
                break;
            case BOTTOM:
                collidesBottom();
                break;
            case OVER:
                collidesOverBar();
                break;
        }
    }

    protected void collidesTop() {
        Log.d("Collision", "TOP EDGE. V(X,Y): " + ball.getHandlerSpeedX() + "," + ball.getHandlerSpeedY());
        previous_event = TOP;
        ball.setHandlerSpeedY(-ball.getHandlerSpeedY());
        touch.play();
    }

    protected void collidesBottom() {
        Log.d("Collision", "BOTTOM EDGE. V(X,Y): " + ball.getHandlerSpeedX() + "," + ball.getHandlerSpeedY());
        previous_event = BOTTOM;
        ball.onBallLost();
        ball.setHandlerSpeedY(-ball.getHandlerSpeedY());
    }

    protected void collidesOverBar() {
        Log.d("Collision", "OVER BAR. V(X,Y): " + ball.getHandlerSpeedX() + "," + ball.getHandlerSpeedY());
        previous_event = OVER;

        float ballX = ball.getXCentreCoordinate();
        float barX = bar.getXCentreCoordinate();
        myModule = (float) Math.sqrt(Math.pow(ball.getHandlerSpeedX(), 2) + Math.pow(ball.getHandlerSpeedY(), 2));

        if (ballX - barX <= (-(13 * bar.getObjectWidth()) / 30)) {
            Log.d("Collision", "20° LEFT");
            SIN_X = SIN_20;
            COS_X = COS_20;
            ball.setHandlerSpeed(-myModule * COS_20, -myModule * SIN_20);
        } else if ((ballX - barX > (-(13 * bar.getObjectWidth()) / 30)) && (ballX - barX <= (-(11 * bar.getObjectWidth()) / 30))) {
            Log.d("Collision", "30° LEFT");
            SIN_X = SIN_30;
            COS_X = COS_30;
            ball.setHandlerSpeed(-myModule * COS_30, -myModule * SIN_30);
        } else if ((ballX - barX > (-(11 * bar.getObjectWidth()) / 30)) && (ballX - barX <= (-(3 * bar.getObjectWidth()) / 10))) {
            Log.d("Collision", "40° LEFT");
            SIN_X = SIN_40;
            COS_X = COS_40;
            ball.setHandlerSpeed(-myModule * COS_40, -myModule * SIN_40);
        } else if ((ballX - barX > (-(3 * bar.getObjectWidth()) / 10)) && (ballX - barX <= (-(7 * bar.getObjectWidth()) / 30))) {
            Log.d("Collision", "50° LEFT");
            SIN_X = SIN_50;
            COS_X = COS_50;
            ball.setHandlerSpeed(-myModule * COS_50, -myModule * SIN_50);
        } else if ((ballX - barX > (-(7 * bar.getObjectWidth()) / 30)) && (ballX - barX <= (-bar.getObjectWidth() / 6))) {
            Log.d("Collision", "60° LEFT");
            SIN_X = SIN_60;
            COS_X = COS_60;
            ball.setHandlerSpeed(-myModule * COS_60, -myModule * SIN_60);
        } else if ((ballX - barX > (-bar.getObjectWidth() / 6)) && (ballX - barX <= (-bar.getObjectWidth() / 10))) {
            Log.d("Collision", "70° LEFT");
            SIN_X = SIN_70;
            COS_X = COS_70;
            ball.setHandlerSpeed(-myModule * COS_70, -myModule * SIN_70);
        } else if ((ballX - barX > (-bar.getObjectWidth() / 10)) && (ballX - barX <= (-bar.getObjectWidth() / 30))) {
            Log.d("Collision", "80° LEFT");
            SIN_X = SIN_80;
            COS_X = COS_80;
            ball.setHandlerSpeed(-myModule * COS_80, -myModule * SIN_80);
        } else if ((ballX - barX > (-bar.getObjectWidth() / 30)) && (ballX - barX < (bar.getObjectWidth() / 30))) {
            Log.d("Collision", "90°");
            SIN_X = SIN_90;
            COS_X = COS_90;
            ball.setHandlerSpeed(myModule * COS_90, -myModule * SIN_90);
        } else if ((ballX - barX >= (bar.getObjectWidth() / 30)) && (ballX - barX < (bar.getObjectWidth() / 10))) {
            Log.d("Collision", "80° RIGHT");
            SIN_X = SIN_80;
            COS_X = COS_80;
            ball.setHandlerSpeed(myModule * COS_80, -myModule * SIN_80);
        } else if ((ballX - barX >= (bar.getObjectWidth() / 10)) && (ballX - barX < (bar.getObjectWidth() / 6))) {
            Log.d("Collision", "70° RIGHT");
            SIN_X = SIN_70;
            COS_X = COS_70;
            ball.setHandlerSpeed(myModule * COS_70, -myModule * SIN_70);
        } else if ((ballX - barX >= (bar.getObjectWidth() / 6)) && (ballX - barX < ((7 * bar.getObjectWidth()) / 30))) {
            Log.d("Collision", "60° RIGHT");
            SIN_X = SIN_60;
            COS_X = COS_60;
            ball.setHandlerSpeed(myModule * COS_60, -myModule * SIN_60);
        } else if ((ballX - barX >= ((7 * bar.getObjectWidth()) / 30)) && (ballX - barX < ((3 * bar.getObjectWidth()) / 10))) {
            Log.d("Collision", "50° RIGHT");
            SIN_X = SIN_50;
            COS_X = COS_50;
            ball.setHandlerSpeed(myModule * COS_50, -myModule * SIN_50);
        } else if ((ballX - barX >= ((3 * bar.getObjectWidth()) / 10)) && (ballX - barX < ((11 * bar.getObjectWidth()) / 30))) {
            Log.d("Collision", "40° RIGHT");
            SIN_X = SIN_40;
            COS_X = COS_40;
            ball.setHandlerSpeed(myModule * COS_40, -myModule * SIN_40);
        } else if ((ballX - barX >= ((11 * bar.getObjectWidth()) / 30)) && (ballX - barX < ((13 * bar.getObjectWidth()) / 30))) {
            Log.d("Collision", "30° RIGHT");
            SIN_X = SIN_30;
            COS_X = COS_30;
            ball.setHandlerSpeed(myModule * COS_30, -myModule * SIN_30);
        } else if (ballX - barX >= ((13 * bar.getObjectWidth()) / 30)) {
            Log.d("Collision", "20° RIGHT");
            SIN_X = SIN_20;
            COS_X = COS_20;
            ball.setHandlerSpeed(myModule * COS_20, -myModule * SIN_20);
        }

        touch.play();
    }

    protected void clearGame() {
        BAR_SPEED = 2 * DEVICE_RATIO;
        BALL_SPEED = 350 * DEVICE_RATIO;
        previous_event = NO_COLL;
        pause = false;
    }

    protected void actionDownEvent(float x, float y) {
        if (!pause && !animActive) {
            pauseGame();
        }
        if (pause && (System.currentTimeMillis() - firstTap > 500)) {
            restartGameAfterPause();
        }
    }

    protected void pauseGame() {
        Log.d("Pause", "Game Paused");
        textPause.setText(getResources().getString(R.string.text_pause));
        // Saving Game Speed
        old_x_speed = ball.getHandlerSpeedX();
        old_y_speed = ball.getHandlerSpeedY();
        old_bar_speed = bar.getBarSpeed();
        // Stop the Game
        ball.setHandlerSpeed(0f, 0f);
        bar.setBarSpeed(0f);
        // Setting pause utils
        firstTap = System.currentTimeMillis();
        previous_event = PAUSE;
        touch.stop();
        pause = true;
        // Stopping Rush Hour Balls
        rushHour.pause();
    }

    protected void restartGameAfterPause() {
        Log.d("Pause", "Game Restarted");
        textPause.setText("");
        // Setting old game speed
        ball.setHandlerSpeed(old_x_speed, old_y_speed);
        bar.setBarSpeed(old_bar_speed);
        // Setting pause utils
        pause = false;
        // Setting old Rush Hour ball speed
        rushHour.restartAfterPause();
    }

    protected abstract void bluetoothExtra();

    protected abstract void gameEventsCollisionLogic();

    protected abstract void gameOver();

    protected abstract void saveGame(String s);

    protected abstract void addScore();
}