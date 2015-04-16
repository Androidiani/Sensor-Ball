package it.unina.is2project.sensorgames.game.entity;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;

public class GameObject {

    protected Drawable gDraw;
    protected BitmapTextureAtlas gTexture;
    protected ITextureRegion gTextureRegion;
    protected Sprite gSprite;
    protected Point displaySize;
    protected SimpleBaseGameActivity simpleBaseGameActivity;

    protected Context context;

    public GameObject(SimpleBaseGameActivity simpleBaseGameActivity, final int idDrawable) {
        this.simpleBaseGameActivity = simpleBaseGameActivity;
        this.context = simpleBaseGameActivity.getApplicationContext();

        this.displaySize = new Point();
        simpleBaseGameActivity.getWindow().getWindowManager().getDefaultDisplay().getSize(this.displaySize);

        this.gDraw = context.getResources().getDrawable(idDrawable);
        this.gTexture = new BitmapTextureAtlas(simpleBaseGameActivity.getTextureManager(), this.gDraw.getIntrinsicWidth(), this.gDraw.getIntrinsicHeight());
        this.gTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(this.gTexture, context, idDrawable, 0, 0);
        this.gTexture.load();
    }

    public void addToScene(Scene scene, float spriteRatio) {
        this.gSprite = new Sprite(0, 0, gTextureRegion, simpleBaseGameActivity.getVertexBufferObjectManager());
        this.gSprite.setWidth(displaySize.x * spriteRatio);
        scene.attachChild(gSprite);
    }

    public void centre() {
        //Posiziona lo sprite al centro dello schermo
        this.gSprite.setPosition((displaySize.x - gTexture.getWidth()) / 2, (displaySize.y - gTexture.getHeight()) / 2);
    }

    public void top() {
        //Posiziona lo sprite in alto con un margine proporzionale all'altezza dello sprite
        this.gSprite.setPosition((displaySize.x - gTexture.getWidth()) / 2, (displaySize.y - gTexture.getHeight()) / 3);
    }

    public void bottom() {
        //Posiziona lo sprite in basso con un margine proporzionale all'altezza dello sprite
        this.gSprite.setPosition((displaySize.x - gTexture.getWidth()) / 2, (displaySize.y - 2 * gTexture.getHeight()));
    }

    public void setPosition(float x, float y) {
        this.gSprite.setPosition(x, y);
    }

    public Sprite getSprite() {
        //Segsprite è null vuoldire che non è stata chiamata addToScene
        return this.gSprite;
    }
}
