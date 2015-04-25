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

    protected SimpleBaseGameActivity simpleBaseGameActivity;
    protected Context context;
    protected Point displaySize;
    protected Drawable gDraw;
    protected BitmapTextureAtlas gTexture;
    protected ITextureRegion gTextureRegion;
    protected Sprite gSprite;
    protected Scene scene;

    public GameObject(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable) {
        this.simpleBaseGameActivity = simpleBaseGameActivity;
        this.context = simpleBaseGameActivity.getApplicationContext();
        this.displaySize = new Point();
        simpleBaseGameActivity.getWindow().getWindowManager().getDefaultDisplay().getSize(this.displaySize);
        this.gDraw = this.context.getResources().getDrawable(idDrawable);
        this.gTexture = new BitmapTextureAtlas(simpleBaseGameActivity.getTextureManager(), this.gDraw.getIntrinsicWidth(), this.gDraw.getIntrinsicHeight());
        this.gTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(this.gTexture, this.context, idDrawable, 0, 0);
        this.gTexture.load();
    }

    public GameObject(GameObject gameObject) {
        this.gDraw = gameObject.gDraw;
        this.gTexture = gameObject.gTexture;
        this.gTextureRegion = gameObject.gTextureRegion;
        this.displaySize = gameObject.displaySize;
        this.simpleBaseGameActivity = gameObject.simpleBaseGameActivity;
        this.context = gameObject.context;
        this.scene = gameObject.scene;
    }

    public void addToScene(Scene scene, float xRatio, float yRatio) {
        this.scene = scene;
        this.gSprite = new Sprite(0, 0, this.gTextureRegion, this.simpleBaseGameActivity.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                onTouch();
                return true;
            }
        };
        setObjectDimension(xRatio, yRatio);
        attach();
    }

    public void addToScene(Scene scene) {
        this.scene = scene;
        this.gSprite = new Sprite(0, 0, this.gTextureRegion, this.simpleBaseGameActivity.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                onTouch();
                return true;
            }
        };
    }

    public void onTouch() {
        Log.d("onTouch()", "Sprite touched");
    }

    public boolean checkOnTouch(float x, float y){
        return (x <= gSprite.getX() + gSprite.getWidth() &&
                x >= gSprite.getX() &&
                y >= gSprite.getY() &&
                y <= gSprite.getY() + gSprite.getHeight());
    }

    public void registerTouch() {
        this.scene.registerTouchArea(this.gSprite);
    }

    public void unregisterTouch() {
        this.scene.unregisterTouchArea(this.gSprite);
    }

    public void attach() {
        this.scene.attachChild(gSprite);
    }

    public void detach() {
        this.gSprite.detachSelf();
    }

    public Sprite getSprite() {
        return this.gSprite;
    }

    public Point getDisplaySize() {
        return displaySize;
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
                getObjectWidth() * 2 + random.nextInt(this.displaySize.x - (int)(getObjectWidth() * 4)),
                getObjectHeight() * 2 + random.nextInt(this.displaySize.y - (int)(getObjectHeight() * 4))
        );
    }

    public float getXCoordinate() {
        return this.gSprite.getX();
    }

    public float getYCoordinate() {
        return this.gSprite.getY();
    }

    public float getXCentreCoordinate() {
        float[] center_coords = this.gSprite.getSceneCenterCoordinates();
        return center_coords[0];
    }

    public float getObjectWidth() {
        return this.gSprite.getWidth();
    }

    public float getObjectHeight() {
        return this.gSprite.getHeight();
    }

    public void setObjectWidth(float width) {
        this.gSprite.setWidth(width);
    }

    public void setObjectHeight(float height) {
        this.gSprite.setHeight(height);
    }

    public void setObjectDimension(float xRatio, float yRatio) {
        this.gSprite.setWidth(this.displaySize.x * xRatio);
        this.gSprite.setHeight(this.displaySize.x * yRatio);
    }

    public void setObjectDimension(float size) {
        this.gSprite.setWidth(size);
        this.gSprite.setHeight(size);
    }
}
