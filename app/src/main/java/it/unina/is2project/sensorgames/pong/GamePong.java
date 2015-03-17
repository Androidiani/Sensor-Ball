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
        setAssetBasePath("gfx/");

        // Ball
        ballTexture = new BitmapTextureAtlas(getTextureManager(),
                60,
                60);

        ballTextureRegion = createFromAsset(ballTexture,
                this,
                "ball.png",
                0,
                0);

        ballTexture.load();

        // Bar
        barTexture = new BitmapTextureAtlas(getTextureManager(),
                260,
                90);

        barTextureRegion = createFromAsset(barTexture,
                this,
                "bar.png",
                0,
                0);

        barTexture.load();

        // Life
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
        FontFactory.setAssetBasePath("font/");

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
        scene = new Scene();

        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        ballSprite = new Sprite((CAMERA_WIDTH - ballTexture.getWidth())/2,
                (CAMERA_HEIGHT - ballTexture.getHeight())/2,
                ballTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(ballSprite);

        // Score text
        txtScore = new Text(10,
                10,
                font,
                "",
                20,
                getVertexBufferObjectManager());
        scene.attachChild(txtScore);

        // Game events text
        txtEvnt = new Text(10,
                45,
                font,
                "",
                20,
                getVertexBufferObjectManager());
        scene.attachChild(txtEvnt);

        // Lifes
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


        // Physics settings
        handler = new PhysicsHandler(ballSprite);
        ballSprite.registerUpdateHandler(handler);

        // First time set Ball Velocity
        handler.setVelocity(BALL_SPEED,-BALL_SPEED);

        // Update Handler - La scena viene aggiornata periodicamente (può
        // provocare delay) e si valuta la velocità e la posizione della
        // palla per stabilire gli eventi da gestire

        // previous_event: utile ad evitare che la palla rimanga ancorata
        // ai lati del campo
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                // variabili per i bordi (associati alla sfera)
                int rL = CAMERA_WIDTH - (int)ballSprite.getWidth()/2;
                int bL = CAMERA_HEIGHT - (int)ballSprite.getHeight()/2;

                //edge's condition (rimbalzo sui muri)

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
                    if(previous_event == SIDE)
                        remScore();
                    //handler.setVelocityY(-handler.getVelocityY());
                    //touch.play();
                    Log.d("","Bottom. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = BOTTOM;
                    if(!game_over)
                        restart_game();
                }
                if(ballSprite.getY() < 0 && previous_event != TOP && !game_over){
                    handler.setVelocityY(-handler.getVelocityY());
                    touch.play();
                    Log.d("","Top. V(X,Y): " + handler.getVelocityX() + ","  + handler.getVelocityY());
                    previous_event = TOP;
                }

                if(ballSprite.collidesWith(barSprite)){
                    float ya = ballSprite.getY() - ballSprite.getHeight()/2;
                    float yb = barSprite.getY() - barSprite.getHeight()/2;

                    if(ya <= yb && previous_event != OVER && previous_event != SIDE){
                        Log.d("","Evento sopra");
                        handler.setVelocityY(-handler.getVelocityY());
                        previous_event = OVER;
                        addScore();
                    }
                    else if(previous_event != SIDE && previous_event != OVER){
                        handler.setVelocityX(-handler.getVelocityX());
                        Log.d("","Evento lato");
                        previous_event = SIDE;
                        addScore();
                    }
                    touch.play();
                    Log.d("","ya = " + ya + " yb = " + yb);
                }

                txtScore.setText("Score: " + score);

                // Condizioni di confine per la barra

                // Confine destro
                if( barSprite.getX() > CAMERA_WIDTH - barSprite.getWidth())
                    barSprite.setX(CAMERA_WIDTH - barSprite.getWidth() - 15);

                // Confine sinistro
                if ( barSprite.getX() < 15)
                    barSprite.setX(15);

                // Game events

                // 2x Bar Speed
                if(score >= X2_BARSPEED && !x2_barspeed){
                    GAME_VELOCITY *= 2;
                    x2_barspeed = true;
                    txtEvnt.setText("2X Bar Speed");
                }

                // 2x Ball Speed
                if(score >= X2_BALLSPEED && !x2_ballspeed){
                    handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
                    x2_ballspeed = true;
                    txtEvnt.setText("2X Ball Speed");
                }

                // 3x Bar Speed
                if(score >= X3_BARSPEED && !x3_barspeed){
                    GAME_VELOCITY *= 1.5f;
                    x3_barspeed = true;
                    txtEvnt.setText("3X Bar Speed");
                }

                // 4x Ball Speed
                if(score >= X4_BALLSPEED && !x4_ballspeed){
                    handler.setVelocity(handler.getVelocityX()*2,handler.getVelocityY()*2);
                    x4_ballspeed = true;
                    txtEvnt.setText("4X Ball Speed");
                }

                // reduce bar
                if(score >= REDUCE_BAR && !reduce_bar){
                    barSprite.setScale(0.7f);
                    reduce_bar = true;
                    txtEvnt.setText("Bar reduced");
                }
            }
            @Override
            public void reset() {

            }
        });

        barSprite = new Sprite((CAMERA_WIDTH - barTexture.getWidth())/2,
                (CAMERA_HEIGHT - 2*barTexture.getHeight()),
                barTextureRegion,
                getVertexBufferObjectManager());

        scene.attachChild(barSprite);

        //enabling sensor
        this.enableAccelerationSensor(this);
        mAccelerationOptions = new AccelerationSensorOptions(SensorDelay.GAME);

        return scene;
    }

    public void addScore(){
        /**
         * Aumenta il punteggio in base allo score ottenuto
         *
         * Colpire il lato della barra e rimanere in gioco,
         * incrementa maggiormente lo score
         */

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
        /**
         * Diminuisce il punteggio nel caso in cui il
         * giocatore colpisce il bordo della barra e
         * successivamente perde la partita
         */
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
        // Toglie una vita
        scene.detachChild(ballSprite);
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
        life--;
        if(life < 0){
            game_over = true;
            txtEvnt.setText("Game over");
        }
        else{
            // Riposiziona la palla
            ballSprite.setPosition((CAMERA_WIDTH - ballSprite.getWidth())/2,
                    (CAMERA_HEIGHT - ballSprite.getHeight())/2);

            // La indirizza verso l'alto
            handler.setVelocityY(-handler.getVelocityY());
            scene.attachChild(ballSprite);
        }
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        CAMERA_WIDTH = size.x;
        CAMERA_HEIGHT = size.y;
        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        EngineOptions engineOptions = new EngineOptions(true,
                ScreenOrientation.PORTRAIT_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                camera);

        engineOptions.getAudioOptions().setNeedsSound(true);

        return engineOptions;
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {

    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // Il movimento della barra è gestito mediante l'accelerometro
        barSprite.setX(barSprite.getX()+pAccelerationData.getX()*GAME_VELOCITY);
    }
}
