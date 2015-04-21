package it.unina.is2project.sensorgames.game.entity;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;

import java.util.Random;

public class GameObject {

    public final static int TOP = 0;
    public final static int MIDDLE = 1;
    public final static int BOTTOM = 2;

    protected Drawable gDraw;
    protected BitmapTextureAtlas gTexture;
    protected ITextureRegion gTextureRegion;
    protected Sprite gSprite;
    protected Point displaySize;
    protected SimpleBaseGameActivity simpleBaseGameActivity;
    protected Context context;
    protected Scene scene;

    public GameObject(SimpleBaseGameActivity simpleBaseGameActivity, final int idDrawable) {
        this.simpleBaseGameActivity = simpleBaseGameActivity;
        this.context = simpleBaseGameActivity.getApplicationContext();

        this.displaySize = new Point();
        simpleBaseGameActivity.getWindow().getWindowManager().getDefaultDisplay().getSize(this.displaySize);

        this.gDraw = this.context.getResources().getDrawable(idDrawable);
        this.gTexture = new BitmapTextureAtlas(simpleBaseGameActivity.getTextureManager(), this.gDraw.getIntrinsicWidth(), this.gDraw.getIntrinsicHeight());
        this.gTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(this.gTexture, this.context, idDrawable, 0, 0);
        this.gTexture.load();
    }

    public void addToScene(Scene scene, float spriteRatio) {
        this.scene = scene;
        this.gSprite = new Sprite(0, 0, this.gTextureRegion, this.simpleBaseGameActivity.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                onTouch();
                return true;
            }
        };
        this.gSprite.setWidth(this.displaySize.x * spriteRatio);
        attach();
    }

    public void onTouch() {
        Log.d("onTouch()", "Sprite touched");
    }

    public GameObject duplicate(GameObject gameObject) {
        gameObject.gDraw = this.gDraw;
        gameObject.gTexture = this.gTexture;
        gameObject.gTextureRegion = this.gTextureRegion;
        gameObject.displaySize = this.displaySize;
        gameObject.simpleBaseGameActivity = this.simpleBaseGameActivity;
        gameObject.context = this.context;
        gameObject.scene = this.scene;

        return gameObject;
    }

    /**
     * Se chiamato addToScene, aggiunge lo sprite alla scena.
     */
    public void attach() {
        this.scene.attachChild(gSprite);
    }

    /**
     * Rimuove lo sprite con operazione dei detach
     */
    public void detach() {
        this.gSprite.detachSelf();
    }

    public Sprite getSprite() {
        //Se gsprite è null vuol dire che non è stata chiamata addToScene
        return this.gSprite;
    }

    public boolean collidesWith(GameObject gameObject) {
        return (this.gSprite.collidesWith(gameObject.gSprite));
    }

    public void setPosition(float x, float y) {
        this.gSprite.setPosition(x, y);
    }

    public void setPosition(int position) {
        switch (position) {
            case TOP:
                //Posiziona lo sprite in alto con un margine proporzionale all'altezza dello sprite
                this.gSprite.setPosition((displaySize.x - gSprite.getWidth()) / 2, (displaySize.y - gSprite.getHeight()) / 3);
                break;
            case MIDDLE:
                //Posiziona lo sprite al centro dello schermo
                this.gSprite.setPosition((displaySize.x - gSprite.getWidth()) / 2, (displaySize.y - gSprite.getHeight()) / 2);
                break;
            case BOTTOM:
                //Posiziona lo sprite in basso con un margine proporzionale all'altezza dello sprite
                this.gSprite.setPosition((displaySize.x - gSprite.getWidth()) / 2, (displaySize.y - 2 * gSprite.getHeight()));
                break;
        }
    }

    public void setRandomPosition() {
        Random random = new Random();
        this.gSprite.setPosition(
                getObjectWidth() * 2 + random.nextInt(this.displaySize.x - (getObjectWidth() * 2)),
                getObjectHeight() * 4 + random.nextInt(this.displaySize.y - (getObjectHeight() * 4))
        );
    }

    public int getXCoordinate() {
        return (int) this.gSprite.getX();
    }

    public int getYCoordinate() {
        return (int) this.gSprite.getY();
    }

    public float getXCentreCoordinate() {
        float[] center_coords = this.gSprite.getSceneCenterCoordinates();
        return center_coords[0];
    }

    public int getObjectWidth() {
        return (int) this.gSprite.getWidth();
    }

    public int getObjectHeight() {
        return (int) this.gSprite.getHeight();
    }

    public void setObjectWidth(float width) {
        this.gSprite.setWidth(width);
    }

    public void setObjectHeight(float height) {
        this.gSprite.setHeight(height);
    }
}
