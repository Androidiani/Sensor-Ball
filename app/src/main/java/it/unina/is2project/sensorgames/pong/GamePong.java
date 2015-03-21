package it.unina.is2project.sensorgames.pong;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.io.IOException;

import it.unina.is2project.sensorgames.R;

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromAsset;
import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromResource;
import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.setAssetBasePath;

public abstract class GamePong extends SimpleBaseGameActivity implements IAccelerationListener {

    /*
     Camera
   */
    protected static int CAMERA_WIDTH;
    protected static int CAMERA_HEIGHT;
    protected Camera camera;

    /*
       Scene
    */
    protected Scene scene;
    protected PhysicsHandler handler;
    protected static int GAME_VELOCITY = 2;
    protected static int BALL_SPEED = 350;
    protected static final int NO_EVENT = 0;
    protected static final int BOTTOM = 1;
    protected static final int TOP = 2;
    protected static final int LEFT = 3;
    protected static final int RIGHT = 4;
    protected static final int SIDE = 5;
    protected static final int OVER = 6;
    protected int previous_event = 0;

    /*
        Graphics
     */

    // Ball
    protected BitmapTextureAtlas ballTexture;
    protected ITextureRegion ballTextureRegion;
    protected Sprite ballSprite;

    // Bar
    protected BitmapTextureAtlas barTexture;
    protected ITextureRegion barTextureRegion;
    protected Sprite barSprite;

    /*
        Sounds
     */
    protected Sound touch;

    /*
        Fonts
     */
    protected ITexture fontTexture;
    protected Font font;

    /*
        Sensors
     */
    protected AccelerationSensorOptions mAccelerationOptions;

    @Override
    protected void onCreateResources() {
        loadGraphics();
        loadSounds();
        loadFonts();
    }

    @Override
    protected Scene onCreateScene() {
        /** Create a new scene */
        scene = new Scene();

        /** Setting up the background color */
        scene.setBackground(new Background(0f, 0f, 0f));

        /** Adding the ballSprite to the scene */
        ballSprite = new Sprite((CAMERA_WIDTH - ballTexture.getWidth()) / 2, (CAMERA_HEIGHT - ballTexture.getHeight()) / 2, ballTextureRegion, getVertexBufferObjectManager());
        ballSprite.setWidth(CAMERA_WIDTH*0.1f);
        ballSprite.setHeight(CAMERA_WIDTH*0.1f);
        scene.attachChild(ballSprite);

        /** Adding the barSprite to the scene */
        barSprite = new Sprite((CAMERA_WIDTH - barTexture.getWidth()) / 2, (CAMERA_HEIGHT - 2 * barTexture.getHeight()), barTextureRegion, getVertexBufferObjectManager());
        barSprite.setWidth(CAMERA_WIDTH*0.3f);
        scene.attachChild(barSprite);

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.enableAccelerationSensor(this);
        mAccelerationOptions = new AccelerationSensorOptions(SensorDelay.GAME);

        /** Return the completed scene */
        return scene;
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        /** Understanding the device display's dimensions */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        CAMERA_WIDTH = size.x;
        CAMERA_HEIGHT = size.y;

        /** Setting up the andengine camera */
        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        /** Setting up the andengine options */
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);

        /** Enable the sound option */
        engineOptions.getAudioOptions().setNeedsSound(true);

        /** Return the engineOptions */
        return engineOptions;
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        /** The bar is moving only on X
         * There's the edges' condition that do not hide the bar beyond the walls.
         * New position variable
         */
        float new_position = barSprite.getX() + pAccelerationData.getX() * GAME_VELOCITY;

        /** Border variables */
        float rL = CAMERA_WIDTH - barSprite.getWidth() / 2;
        float lL = -barSprite.getWidth() / 2;

        if (!(new_position > rL || new_position < lL))
            barSprite.setX(new_position);
    }

    protected void loadGraphics() {
        /** White Ball texture loading */
        Drawable ballDraw = getResources().getDrawable(R.drawable.ball_white);
        ballTexture = new BitmapTextureAtlas(getTextureManager(), ballDraw.getIntrinsicWidth(), ballDraw.getIntrinsicHeight());
        ballTextureRegion = createFromResource(ballTexture, this,R.drawable.ball_white , 0, 0);
        ballTexture.load();

        /** Bar texture loading */
        Drawable barDraw = getResources().getDrawable(R.drawable.bar_white);
        barTexture = new BitmapTextureAtlas(getTextureManager(), barDraw.getIntrinsicWidth(), barDraw.getIntrinsicHeight());
        barTextureRegion = createFromResource(barTexture, this, R.drawable.bar_white, 0, 0);
        barTexture.load();
    }

    protected void loadFonts() {
        /** Setting the Asset Base Path for fonts */
        FontFactory.setAssetBasePath("font/");

        /** "secrcode.ttf" texture loading */
        fontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        font = FontFactory.createFromAsset(getFontManager(), fontTexture, getAssets(), "secrcode.ttf", 40, true, Color.WHITE);
        font.load();
    }

    protected void loadSounds() {
        /** Setting the Asset Base Path for sounds */
        SoundFactory.setAssetBasePath("mfx/");

        /** "paddlehit.ogg" sound loading */
        try {
            touch = SoundFactory.createSoundFromAsset(getEngine().getSoundManager(), this, "paddlehit.ogg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract public void settingPhysics();

    abstract public void restartOnBallLost();

    abstract public void addScore();

    abstract public void remScore();

}
