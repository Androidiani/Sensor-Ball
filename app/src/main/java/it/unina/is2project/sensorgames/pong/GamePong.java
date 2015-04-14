package it.unina.is2project.sensorgames.pong;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.io.IOException;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;

public abstract class GamePong extends SimpleBaseGameActivity implements IAccelerationListener {

    /**
     * Camera
     */
    protected Camera camera;
    protected int CAMERA_WIDTH;
    protected int CAMERA_HEIGHT;
    protected float DEVICE_RATIO;
    protected float density;

    /**
     * Graphics
     */

    // Ball
    protected BitmapTextureAtlas ballTexture;
    protected ITextureRegion ballTextureRegion;
    protected Sprite ballSprite;

    // Bar
    protected BitmapTextureAtlas barTexture;
    protected ITextureRegion barTextureRegion;
    protected Sprite barSprite;

    /**
     * Sounds
     */
    protected Sound touch;

    /**
     * Fonts
     */
    protected ITexture fontTexture;
    protected Font font;

    /**
     * Sensors
     */
    protected AccelerationSensorOptions mAccelerationOptions;

    /**
     * Scene
     */
    protected Scene scene;
    protected PhysicsHandler handler;
    protected float BAR_SPEED;
    protected float BALL_SPEED;

    /**
     * Collision Events
     */
    protected int previous_event = 0;
    protected static final int NO_COLL = 0;
    protected static final int BOTTOM = 1;
    protected static final int TOP = 2;
    protected static final int LEFT = 3;
    protected static final int RIGHT = 4;
    protected static final int OVER = 5;
    protected static final int SIDE = 6;

    /**
     * Bounce bar angles
     */
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

    /**
     * Selected angles and module
     */
    protected float SIN_X;
    protected float COS_X;
    protected float myModule;

    /**
     * Pause Utils
     */
    protected Text textPause;
    protected Text textPause_util;
    protected static final int PAUSE = -1;
    protected boolean pause = false;
    protected float old_x_speed;
    protected float old_y_speed;
    protected float old_bar_speed;
    protected long firstTap;

    /**
     * Animation Utils
     */
    protected long ms = 0;
    protected long animTime = 3000;
    protected boolean animActive = false;

    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        // Understanding the device display's density
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        density = displayMetrics.density;
        // Understanding the device display's dimensions
        Point size = new Point();
        display.getSize(size);
        CAMERA_WIDTH = size.x;
        CAMERA_HEIGHT = size.y;
        DEVICE_RATIO = CAMERA_WIDTH / 480;
        Log.d("Camera", "Density = " + density + ", Camera Width = " + CAMERA_WIDTH + ", Camera Height = " + CAMERA_HEIGHT + ", Device Ratio = " + DEVICE_RATIO);
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

        // Setting up the background color
        scene.setBackground(new Background(0f, 0f, 0f));

        // Adding the textPause to the scene
        textPause_util = new Text(0, 0, font, "Pause", 20, getVertexBufferObjectManager());
        textPause = new Text((CAMERA_WIDTH - textPause_util.getWidth()) / 2, (CAMERA_HEIGHT - textPause_util.getHeight()) / 2, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textPause);

        // Adding the ballSprite to the scene
        ballSprite = new Sprite(0, 0, ballTextureRegion, getVertexBufferObjectManager());
        ballSprite.setWidth(CAMERA_WIDTH * 0.1f);
        ballSprite.setHeight(CAMERA_WIDTH * 0.1f);
        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth()) / 2, (CAMERA_HEIGHT - ballSprite.getHeight()) / 3);
        attachBall();

        // Adding the barSprite to the scene
        barSprite = new Sprite(0, 0, barTextureRegion, getVertexBufferObjectManager());
        barSprite.setWidth(CAMERA_WIDTH * 0.3f);
        barSprite.setPosition((CAMERA_WIDTH - barSprite.getWidth()) / 2, (CAMERA_HEIGHT - 2 * barSprite.getHeight()));
        scene.attachChild(barSprite);

        //Set game velocity
        BAR_SPEED = 2 * DEVICE_RATIO;
        BALL_SPEED = 350 * DEVICE_RATIO;

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.enableAccelerationSensor(this);
        mAccelerationOptions = new AccelerationSensorOptions(SensorDelay.GAME);

        this.mEngine.registerUpdateHandler(new FPSLogger());

        return scene;
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // The bar is moving only on X
        float new_position = barSprite.getX() + pAccelerationData.getX() * BAR_SPEED;
        // There's the edges' condition that do not hide the bar beyond the walls
        if (new_position < CAMERA_WIDTH - barSprite.getWidth() / 2 && new_position > -barSprite.getWidth() / 2)
            barSprite.setX(new_position);
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseGame();
    }

    protected void loadGraphics() {
        // White Ball texture loading
        Drawable ballDraw = getResources().getDrawable(R.drawable.ball_white);
        ballTexture = new BitmapTextureAtlas(getTextureManager(), ballDraw.getIntrinsicWidth(), ballDraw.getIntrinsicHeight());
        ballTextureRegion = createFromResource(ballTexture, this, R.drawable.ball_white, 0, 0);
        ballTexture.load();

        // White Bar texture loading
        Drawable barDraw = getResources().getDrawable(R.drawable.bar_white);
        barTexture = new BitmapTextureAtlas(getTextureManager(), barDraw.getIntrinsicWidth(), barDraw.getIntrinsicHeight());
        barTextureRegion = createFromResource(barTexture, this, R.drawable.bar_white, 0, 0);
        barTexture.load();
    }

    protected void loadFonts() {
        // Setting the Asset Base Path for fonts
        FontFactory.setAssetBasePath("font/");
        // "secrcode.ttf" texture loading
        int fontSize = (int) (30 * density);
        fontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
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
        Thread physicsThread = new Thread() {
            public void run() {
                try {
                    animActive = true;
                    while (ms < animTime) {
                        ms = ms + 100;
                        if (ms < 1000) {
                            Log.d("Animation", "3 - " + ms);
                            textPause.setText("  3  ");
                        }
                        if (ms > 1000 && ms < 2000) {
                            Log.d("Animation", "2 - " + ms);
                            textPause.setText("  2  ");
                        }
                        if (ms > 2000 && ms < 3000) {
                            Log.d("Animation", "1 - " + ms);
                            textPause.setText("  1  ");
                        }
                        sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    animActive = false;
                    textPause.setText("");
                    doPhysics();
                }
            }
        };
        physicsThread.start();
    }

    protected void doPhysics() {
        // A physics handler is linked to the ballSprite
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        // Setting the initial ball velocity
        setBallVeloctity();

        // The Update Handler is linked to the scene. It evalutates the condition of the scene every frame
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                // Edge collision
                if (!pause) {
                    if (leftCondition()) {
                        collidesLeft();
                    }
                    if (rightCondition()) {
                        collidesRight();
                    }
                    if (topCondition()) {
                        collidesTop();
                    }

                    // Extra action relative to the TOP side, needed for two player game
                    bluetoothExtra();

                    if (bottomCondition()) {
                        collidesBottom();
                    }

                    // Bar and Ball collision
                    if (ballSprite.collidesWith(barSprite)) {
                        if (overBarCondition()) {
                            collidesOverBar();
                        }
                        if (sideBarCondition()) {
                            collidesSideBar();
                        }
                    }

                    // Game events collision
                    gameEventsCollisionLogic();
                }
            }

            @Override
            public void reset() {
            }
        });
    }

    protected void attachBall() {
        scene.attachChild(ballSprite);
    }

    protected void setBallVeloctity() {
        handler.setVelocity(0, -BALL_SPEED);
    }

    protected boolean leftCondition() {
        return (ballSprite.getX() < 0) && (previous_event != LEFT);
    }

    protected boolean rightCondition() {
        return (ballSprite.getX() > CAMERA_WIDTH - (int) ballSprite.getWidth()) && (previous_event != RIGHT);
    }

    protected boolean topCondition() {
        return (ballSprite.getY() < 0) && (previous_event != TOP);
    }

    protected boolean bottomCondition() {
        return (ballSprite.getY() > CAMERA_HEIGHT) && (previous_event != BOTTOM);
    }

    protected void collidesLeft() {
        Log.d("CollisionEdge", "LEFT EDGE. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = LEFT;
        handler.setVelocityX(-handler.getVelocityX());
        touch.play();
    }

    protected void collidesRight() {
        Log.d("CollisionEdge", "RIGHT EDGE. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = RIGHT;
        handler.setVelocityX(-handler.getVelocityX());
        touch.play();
    }

    protected void collidesTop() {
        Log.d("CollisionEdge", "TOP EDGE. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = TOP;
        handler.setVelocityY(-handler.getVelocityY());
        touch.play();
    }

    protected void collidesBottom() {
        Log.d("CollisionEdge", "BOTTOM EDGE. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = BOTTOM;
        barSprite.detachSelf();
        ballSprite.detachSelf();
        barSprite.setPosition((CAMERA_WIDTH - barSprite.getWidth()) / 2, (CAMERA_HEIGHT - 2 * barSprite.getHeight()));
        ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth()) / 2, (CAMERA_HEIGHT - ballSprite.getHeight()) / 2);
        handler.setVelocityY(-handler.getVelocityY());
        scene.attachChild(barSprite);
        attachBall();
    }

    protected boolean overBarCondition() {
        return (ballSprite.getY() + ballSprite.getHeight() < barSprite.getY() + barSprite.getHeight()) && (previous_event != OVER) && (previous_event != SIDE);
    }

    protected boolean sideBarCondition() {
        return (previous_event != SIDE) && (previous_event != OVER);
    }

    protected void collidesOverBar() {
        Log.d("CollisionBar", "OVER BAR. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = OVER;

        // Necessarily in that order because getSceneCenterCoordinates return a shared float
        float[] bar_center_coords = barSprite.getSceneCenterCoordinates();
        float barX = bar_center_coords[0];
        float[] ball_center_coords = ballSprite.getSceneCenterCoordinates();
        float ballX = ball_center_coords[0];
        //myModule = (float) Math.sqrt(Math.pow(handler.getVelocityX(), 2) + Math.pow(handler.getVelocityY(), 2));

        if (ballX - barX <= (-(13 * barSprite.getWidth()) / 30)) {
            Log.d("CollisionBar", "20° LEFT");
            SIN_X = SIN_20;
            COS_X = COS_20;
            handler.setVelocity(-myModule * COS_20, -myModule * SIN_20);
        }
        if ((ballX - barX > (-(13 * barSprite.getWidth()) / 30)) && (ballX - barX <= (-(11 * barSprite.getWidth()) / 30))) {
            Log.d("CollisionBar", "30° LEFT");
            SIN_X = SIN_30;
            COS_X = COS_30;
            handler.setVelocity(-myModule * COS_30, -myModule * SIN_30);
        }
        if ((ballX - barX > (-(11 * barSprite.getWidth()) / 30)) && (ballX - barX <= (-(3 * barSprite.getWidth()) / 10))) {
            Log.d("CollisionBar", "40° LEFT");
            SIN_X = SIN_40;
            COS_X = COS_40;
            handler.setVelocity(-myModule * COS_40, -myModule * SIN_40);
        }
        if ((ballX - barX > (-(3 * barSprite.getWidth()) / 10)) && (ballX - barX <= (-(7 * barSprite.getWidth()) / 30))) {
            Log.d("CollisionBar", "50° LEFT");
            SIN_X = SIN_50;
            COS_X = COS_50;
            handler.setVelocity(-myModule * COS_50, -myModule * SIN_50);
        }
        if ((ballX - barX > (-(7 * barSprite.getWidth()) / 30)) && (ballX - barX <= (-barSprite.getWidth() / 6))) {
            Log.d("CollisionBar", "60° LEFT");
            SIN_X = SIN_60;
            COS_X = COS_60;
            handler.setVelocity(-myModule * COS_60, -myModule * SIN_60);
        }
        if ((ballX - barX > (-barSprite.getWidth() / 6)) && (ballX - barX <= (-barSprite.getWidth() / 10))) {
            Log.d("CollisionBar", "70° LEFT");
            SIN_X = SIN_70;
            COS_X = COS_70;
            handler.setVelocity(-myModule * COS_70, -myModule * SIN_70);
        }
        if ((ballX - barX > (-barSprite.getWidth() / 10)) && (ballX - barX <= (-barSprite.getWidth() / 30))) {
            Log.d("CollisionBar", "80° LEFT");
            SIN_X = SIN_80;
            COS_X = COS_80;
            handler.setVelocity(-myModule * COS_80, -myModule * SIN_80);
        }
        if ((ballX - barX > (-barSprite.getWidth() / 30)) && (ballX - barX < (barSprite.getWidth() / 30))) {
            Log.d("CollisionBar", "90°");
            SIN_X = SIN_90;
            COS_X = COS_90;
            handler.setVelocity(myModule * COS_90, -myModule * SIN_90);
        }
        if ((ballX - barX >= (barSprite.getWidth() / 30)) && (ballX - barX < (barSprite.getWidth() / 10))) {
            Log.d("CollisionBar", "80° RIGHT");
            SIN_X = SIN_80;
            COS_X = COS_80;
            handler.setVelocity(myModule * COS_80, -myModule * SIN_80);
        }
        if ((ballX - barX >= (barSprite.getWidth() / 10)) && (ballX - barX < (barSprite.getWidth() / 6))) {
            Log.d("CollisionBar", "70° RIGHT");
            SIN_X = SIN_70;
            COS_X = COS_70;
            handler.setVelocity(myModule * COS_70, -myModule * SIN_70);
        }
        if ((ballX - barX >= (barSprite.getWidth() / 6)) && (ballX - barX < ((7 * barSprite.getWidth()) / 30))) {
            Log.d("CollisionBar", "60° RIGHT");
            SIN_X = SIN_60;
            COS_X = COS_60;
            handler.setVelocity(myModule * COS_60, -myModule * SIN_60);
        }
        if ((ballX - barX >= ((7 * barSprite.getWidth()) / 30)) && (ballX - barX < ((3 * barSprite.getWidth()) / 10))) {
            Log.d("CollisionBar", "50° RIGHT");
            SIN_X = SIN_50;
            COS_X = COS_50;
            handler.setVelocity(myModule * COS_50, -myModule * SIN_50);
        }
        if ((ballX - barX >= ((3 * barSprite.getWidth()) / 10)) && (ballX - barX < ((11 * barSprite.getWidth()) / 30))) {
            Log.d("CollisionBar", "40° RIGHT");
            SIN_X = SIN_40;
            COS_X = COS_40;
            handler.setVelocity(myModule * COS_40, -myModule * SIN_40);
        }
        if ((ballX - barX >= ((11 * barSprite.getWidth()) / 30)) && (ballX - barX < ((13 * barSprite.getWidth()) / 30))) {
            Log.d("CollisionBar", "30° RIGHT");
            SIN_X = SIN_30;
            COS_X = COS_30;
            handler.setVelocity(myModule * COS_30, -myModule * SIN_30);
        }
        if (ballX - barX >= ((13 * barSprite.getWidth()) / 30)) {
            Log.d("CollisionBar", "20° RIGHT");
            SIN_X = SIN_20;
            COS_X = COS_20;
            handler.setVelocity(myModule * COS_20, -myModule * SIN_20);
        }

        touch.play();
    }

    protected void collidesSideBar() {
        Log.d("CollisionBar", "SIDE BAR. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
        previous_event = SIDE;
        handler.setVelocityX(-handler.getVelocityX());
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
        // Saving Game Data
        old_x_speed = handler.getVelocityX();
        old_y_speed = handler.getVelocityY();
        old_bar_speed = BAR_SPEED;
        // Stop the Game
        handler.setVelocity(0);
        BAR_SPEED = 0;
        firstTap = System.currentTimeMillis();
        previous_event = PAUSE;
        touch.stop();
        pause = true;
    }

    protected void restartGameAfterPause() {
        Log.d("Pause", "Game Restarted");
        textPause.setText("");
        // Setting the old game velocity
        handler.setVelocity(old_x_speed, old_y_speed);
        BAR_SPEED = old_bar_speed;
        pause = false;
    }

    /**
     * Get the directions of the ball
     */
    protected Point getDirections() {
        Point mPoint = new Point();
        int x = handler.getVelocityX() > 0 ? 1 : -1;
        int y = handler.getVelocityY() > 0 ? 1 : -1;
        x = handler.getVelocityX() == 0 ? 0 : x;
        y = handler.getVelocityY() == 0 ? 0 : y;
        mPoint.set(x, y);
        return mPoint;
    }

    protected abstract void bluetoothExtra();

    protected abstract void gameEventsCollisionLogic();

    protected abstract void addScore();

    protected abstract void gameOver();

    protected abstract void saveGame(String s);

}
