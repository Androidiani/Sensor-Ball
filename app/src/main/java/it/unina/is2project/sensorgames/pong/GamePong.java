package it.unina.is2project.sensorgames.pong;

import android.graphics.Color;
import android.graphics.Point;
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

import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.*;


public class GamePong extends SimpleBaseGameActivity implements IAccelerationListener {

    /*
      Camera
    */
    protected static int CAMERA_WIDTH;
    protected static int CAMERA_HEIGHT;
    private Camera camera;

    /*
       Scene
    */
    protected Scene scene;
    protected PhysicsHandler handler;
    protected static int GAME_VELOCITY = 2;
    protected static int BALL_SPEED = 350;
    protected int previous_event = 0;
    protected static final int NO_EVENT = 0;
    protected static final int BOTTOM = 1;
    protected static final int TOP = 2;
    protected static final int LEFT = 3;
    protected static final int RIGHT = 4;
    protected static final int SIDE = 5;
    protected static final int OVER = 6;
    protected boolean game_over = false;

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

    // Life
    protected BitmapTextureAtlas lifeTexture;
    protected ITextureRegion lifeTextureRegion;
    protected Sprite lifeSprite1;
    protected Sprite lifeSprite2;
    protected Sprite lifeSprite3;

    /*
        Sounds
     */
    protected Sound touch;

    /*
        Fonts
     */
    private ITexture fontTexture;
    protected Font font;
    protected Text txtScore;
    protected Text txtEvnt;

    /*
        Sensors
     */
    protected AccelerationSensorOptions mAccelerationOptions;

    /*
        Game data
     */
    protected int score = 0;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE-1;


    @Override
    protected void onCreateResources() {
        loadGraphics();
        loadSouns();
        loadFonts();
    }


    private void loadGraphics() {
        /** Setting the Asset base path for graphics */
        setAssetBasePath("gfx/");

        /** Ball texture loading */
        ballTexture = new BitmapTextureAtlas(getTextureManager(),
                60,
                60);

        ballTextureRegion = createFromAsset(ballTexture,
                this,
                "ball.png",
                0,
                0);

        ballTexture.load();

        /** Bar texture loading */
        barTexture = new BitmapTextureAtlas(getTextureManager(),
                260,
                90);

        barTextureRegion = createFromAsset(barTexture,
                this,
                "bar.png",
                0,
                0);

        barTexture.load();

        /** Life texture loading */
        lifeTexture = new BitmapTextureAtlas(getTextureManager(),
                48,
                48);

        lifeTextureRegion = createFromAsset(lifeTexture,
                this,
                "life.png",
                0,
                0);

        lifeTexture.load();
    }

    private void loadFonts(){
        /** Setting the Asset Base Path for fonts */
        FontFactory.setAssetBasePath("font/");

        /** "secrcode.ttf" texture loading */
        fontTexture = new BitmapTextureAtlas(getTextureManager(),
                256,
                256,
                TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        font = FontFactory.createFromAsset(getFontManager(),
                fontTexture,
                getAssets(),
                "secrcode.ttf",
                40, true, Color.BLACK);

        font.load();
    }

    private void loadSouns(){
        /** "paddlehit.ogg" sound loading */
        try
        {
            touch = SoundFactory.createSoundFromAsset(getEngine().getSoundManager(), this, "mfx/paddlehit.ogg");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    protected Scene onCreateScene() {
        return null;
    }

    protected void addScore(){
        /** This procedure increase the score according to the current score. */

        if(score >= 0 && score <= 9){
            if(previous_event == SIDE)
                score += 3;
            else score++;
        }

        if(score >=10 && score <= 30){
            if(previous_event == SIDE)
                score += 9;
            else score += 3;
        }

        if(score >= 31){
            if(previous_event == SIDE)
                score += 27;
            else score += 9;
        }
    }

    protected void remScore(){
        /** If the previous_event is "SIDE" it will reduce the undeserved points. */

        if(previous_event == SIDE){
            if(score >= 0 && score <= 9)
                score -= 3;

            if(score >=10 && score <= 30)
                score -= 9;

            if(score >= 31)
                score -= 27;
        }
    }

    protected void restart_game(){
        /** This procedure restart the game if it's not over.
         *
         *  The ballSprite is detached.
         */
        scene.detachChild(ballSprite);

        /** The lifeSprite is detached */
        switch(life){
            case 2:{
                scene.detachChild(lifeSprite3);
                break;
            }
            case 1:{
                scene.detachChild(lifeSprite2);
                break;
            }
            case 0:{
                scene.detachChild(lifeSprite1);
                break;
            }
        }

        /** Life count is decremented */
        life--;

        /** If the life count is less equal than 0, the game is over */
        if(life < 0){
            game_over = true;
            txtEvnt.setText("Game over");
        }
        /** Else you can replace the ball */
        else{
            ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth())/2,
                    (CAMERA_HEIGHT - ballSprite.getHeight())/2);

            /** Set the direction upward */
            handler.setVelocityY(-handler.getVelocityY());
            scene.attachChild(ballSprite);
        }
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
        EngineOptions engineOptions = new EngineOptions(true,
                ScreenOrientation.PORTRAIT_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                camera);

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
         *
         * There's the edges' condition that do not hide the bar beyond the walls.
         *
         * New position variable
         */
        float new_position = barSprite.getX()+pAccelerationData.getX()*GAME_VELOCITY;

        /** Border variables */
        float rL = CAMERA_WIDTH - barSprite.getWidth()/2;
        float lL = -barSprite.getWidth()/2;

        if ( !(new_position > rL || new_position < lL) )
            barSprite.setX(new_position);
    }
}
