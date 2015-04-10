package it.unina.is2project.sensorgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class BallView extends View {

    public float x;
    public float y;
    private final int r;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Build new ball object
     */
    public BallView(Context context, float x, float y, int r, int color) {
        super(context);
        //color hex is [transparency][red][green][blue]
        mPaint.setColor(color);
        this.x = x;
        this.y = y;
        this.r = r;
    }

    /**
     * Draw the circle
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, r, mPaint);
    }
}
