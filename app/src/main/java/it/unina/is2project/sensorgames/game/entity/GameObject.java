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

        this.gDraw = context.getResources().getDrawable(idDrawable);
        this.gTexture = new BitmapTextureAtlas(simpleBaseGameActivity.getTextureManager(), this.gDraw.getIntrinsicWidth(), this.gDraw.getIntrinsicHeight());
        this.gTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(this.gTexture, context, idDrawable, 0, 0);
        this.gTexture.load();
    }

    public ITextureRegion getTextureRegion() {
        return gTextureRegion;
    }

    public void setTextureRegion(ITextureRegion gTextureRegion) {
        this.gTextureRegion = gTextureRegion;
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

    public Sprite getSprite() {
        //Segsprite è null vuol dire che non è stata chiamata addToScene
        return this.gSprite;
    }

    public void addToScene(Scene scene, float spriteRatio) {
        this.scene = scene;
        this.gSprite = new Sprite(0, 0, gTextureRegion, simpleBaseGameActivity.getVertexBufferObjectManager());
        this.gSprite.setWidth(displaySize.x * spriteRatio);
        attach();
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
        gSprite.detachSelf();
    }
}
