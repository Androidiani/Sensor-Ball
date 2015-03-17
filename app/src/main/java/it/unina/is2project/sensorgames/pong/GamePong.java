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
    private static int CAMERA_WIDTH;
    private static int CAMERA_HEIGHT;
    private Camera camera;

    /*
       Scene
    */
    private Scene scene;
    private PhysicsHandler handler;
    private static int GAME_VELOCITY = 2;
    private static int BALL_SPEED = 350;
    private int previous_event = 0;
    private static final int NO_EVENT = 0;
    private static final int BOTTOM = 1;
    private static final int TOP = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;
    private static final int SIDE = 5;
    private static final int OVER = 6;
    private boolean game_over = false;

    /*
        Graphics
     */

    // Ball
    private BitmapTextureAtlas ballTexture;
    private ITextureRegion ballTextureRegion;
    private Sprite ballSprite;

    // Bar
    private BitmapTextureAtlas barTexture;
    private ITextureRegion barTextureRegion;
    private Sprite barSprite;

    // Life
    private BitmapTextureAtlas lifeTexture;
    private ITextureRegion lifeTextureRegion;
    private Sprite lifeSprite1;
    private Sprite lifeSprite2;
    private Sprite lifeSprite3;

    /*
        Sounds
     */
    private Sound touch;

    /*
        Fonts
     */
    private ITexture fontTexture;
    private Font font;
    private Text txtScore;
    private Text txtEvnt;

    /*
        Sensors
     */
    private AccelerationSensorOptions mAccelerationOptions;

    /*
        Game data
     */
    private int score = 0;
    private static final int MAX_LIFE = 3;
    private int life = MAX_LIFE-1;
    private boolean x2_ballspeed = false;
    private static final int X2_BALLSPEED = 7;
    private boolean x2_barspeed = false;
    private static final int X2_BARSPEED = 4;
    private boolean x3_barspeed = false;
    private static final int X3_BARSPEED = 11;
    private boolean x4_ballspeed = false;
    private static final int X4_BALLSPEED = 235;
    private boolean reduce_bar = false;
    private static final int REDUCE_BAR = 45;


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
        /** Making a new scene */
        scene = new Scene();

        /** Setting up the background color */
        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        /** Adding the ballSprite to the scene */
        ballSprite = new Sprite((CAMERA_WIDTH - ballTexture.getWidth())/2,
                (CAMERA_HEIGHT - ballTexture.getHeight())/2,
                ballTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(ballSprite);

        /** Adding the scoring text to the scene */
        txtScore = new Text(10,
                10,
                font,
                "",
                20,
                getVertexBufferObjectManager());

        scene.attachChild(txtScore);

        /** Adding the event text to the scene */
        txtEvnt = new Text(10,
                45,
                font,
                "",
                20,
                getVertexBufferObjectManager());

        scene.attachChild(txtEvnt);

        /** Adding the life sprites to the scene */
        lifeSprite1 = new Sprite(CAMERA_WIDTH - lifeTexture.getWidth(),
                0,
                lifeTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(lifeSprite1);

        lifeSprite2 = new Sprite(CAMERA_WIDTH - 2*lifeTexture.getWidth(),
                0,
                lifeTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(lifeSprite2);

        lifeSprite3 = new Sprite(CAMERA_WIDTH - 3*lifeTexture.getWidth(),
                0,
                lifeTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(lifeSprite3);


        /** Setting up physics
         * - A physics handler is linked to the ballSprite */
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        /** The ball has the initial speed
         * - vx = BALL_SPEED
         * - vy = - BALL_SPEED
         */
        handler.setVelocity(BALL_SPEED,-BALL_SPEED);

        /** The Update Handler is linked to the scene. It evalutates the condition of the scene
         *  every frame.
         */
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                /** Border variables */
                int rL = CAMERA_WIDTH - (int)ballSprite.getWidth()/2;
                int bL = CAMERA_HEIGHT - (int)ballSprite.getHeight()/2;

                /** Edge's condition
                 *  The direction of the ball changes depending on the
                 *  affected side
                 */
                if((ballSprite.getX() > rL - (int)ballSprite.getWidth()/2) && previous_event != RIGHT && !game_over){
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    Log.d("", "Right. V(X,Y): " + handler.getVelocityX() + "," + handler.getVelocityY());
                    previous_event = RIGHT;
                }
                if(ballSprite.getX() < 0 && previous_event != LEFT && !game_over){
                    handler.setVelocityX(-handler.getVelocityX());
                    touch.play();
                    Log.d("","Left. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = LEFT;
                }
                if((ballSprite.getY() > bL - (int)ballSprite.getHeight()/2) && previous_event != BOTTOM && !game_over){
                    /** If the previous_event is "SIDE" it will reduce the undeserved points. */
                    if(previous_event == SIDE)
                        remScore();
                    Log.d("","Bottom. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = BOTTOM;
                    /** If the game is not over it's restarted */
                    if(!game_over)
                        restart_game();
                }
                if(ballSprite.getY() < 0 && previous_event != TOP && !game_over){
                    handler.setVelocityY(-handler.getVelocityY());
                    touch.play();
                    Log.d("","Top. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = TOP;
                }

                /** When the barSprite and the ballSprite collides */
                if(ballSprite.collidesWith(barSprite)){
                    /** Condition variable who understand if the ball hit the bar side or front
                     *
                     * - ya: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     *
                     * - yb: is the relative position of the ball according
                     *      to the CAMERA_HEIGHT
                     */
                    float ya = ballSprite.getY() - ballSprite.getHeight()/2;
                    float yb = barSprite.getY() - barSprite.getHeight()/2;

                    /** The ball hit the bar's top surface */
                    if(ya <= yb && previous_event != OVER && previous_event != SIDE){
                        Log.d("","Top event");
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                        addScore();
                    }
                    /** The ball hit the bar's side surface */
                    else if(previous_event != SIDE && previous_event != OVER){
                        handler.setVelocityX(-handler.getVelocityX());
                        Log.d("","Side event");
                        previous_event = SIDE;
                        addScore();
                    }
                    touch.play();
                }

                /** The score text is updated to the current value */
                txtScore.setText("Score: " + score);

                /** Game events section */
                game_events();
            }
            @Override
            public void reset() {

            }
        });

        /** Adding the barSprite to the scene */
        barSprite = new Sprite((CAMERA_WIDTH - barTexture.getWidth())/2,
                (CAMERA_HEIGHT - 2*barTexture.getHeight()),
                barTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(barSprite);

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.enableAccelerationSensor(this);
        mAccelerationOptions = new AccelerationSensorOptions(SensorDelay.GAME);

        /** Return the completed scene */
        return scene;
    }

    public void addScore(){
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

    public void remScore(){
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

    private void restart_game(){
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

    private void game_events(){
        /** This procedure understand what modifier needs to associate with the game according to
         * the score.
         */

        /** Increasing x2 tha bar speed */
        if(score >= X2_BARSPEED && !x2_barspeed){
            GAME_VELOCITY *= 2;
            x2_barspeed = true;
            txtEvnt.setText("2X Bar Speed");
        }

        /** Increasing x2 the ball speed */
        if(score >= X2_BALLSPEED && !x2_ballspeed){
            handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
            x2_ballspeed = true;
            txtEvnt.setText("2X Ball Speed");
        }

        /** Increasing x3 the bar speed */
        if(score >= X3_BARSPEED && !x3_barspeed){
            GAME_VELOCITY *= 1.5f;
            x3_barspeed = true;
            txtEvnt.setText("3X Bar Speed");
        }

        /** Increasing x4 the ball speed */
        if(score >= X4_BALLSPEED && !x4_ballspeed){
            handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
            x4_ballspeed = true;
            txtEvnt.setText("4X Ball Speed");
        }

        /** Scale by 70% the bar dimensions */
        if(score >= REDUCE_BAR && !reduce_bar){
            barSprite.setScale(0.7f);
            reduce_bar = true;
            txtEvnt.setText("Bar reduced");
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
