package it.unina.is2project.sensorgames.game.entity;

import org.andengine.entity.scene.Scene;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class Bar extends GameObject implements IAccelerationListener {

    // Bar field
    private float barSpeed;
    private float barWidth;
    // Sensor
    private AccelerationSensorOptions accelerationSensorOptions;

    public Bar(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable) {
        super(simpleBaseGameActivity, idDrawable);
    }

    public float getBarSpeed() {
        return barSpeed;
    }

    public void setBarSpeed(float barSpeed) {
        this.barSpeed = barSpeed;
    }

    public float getBarWidth() {
        return barWidth;
    }

    public void setBarWidth(float barWidth) {
        this.barWidth = barWidth;
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // The bar is moving only on X
        float newXPosition = gSprite.getX() + pAccelerationData.getX() * this.barSpeed;
        // There's the edges' condition that do not hide the bar beyond the walls
        if (this.barSpeed > 0) {
            if (newXPosition < displaySize.x - gSprite.getWidth() / 2 || Math.signum(pAccelerationData.getX()) < 0)
                if (newXPosition > -gSprite.getWidth() / 2 || Math.signum(pAccelerationData.getX()) > 0)
                    gSprite.setX(newXPosition);
        } else {
            if (newXPosition < displaySize.x - gSprite.getWidth() / 2 || Math.signum(pAccelerationData.getX()) > 0)
                if (newXPosition > -gSprite.getWidth() / 2 || Math.signum(pAccelerationData.getX()) < 0)
                    gSprite.setX(newXPosition);
        }
    }

    @Override
    public void addToScene(Scene scene, float xRatio, float yRatio) {
        super.addToScene(scene, xRatio, yRatio);

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.simpleBaseGameActivity.getEngine().enableAccelerationSensor(context, this);
        this.accelerationSensorOptions = new AccelerationSensorOptions(SensorDelay.GAME);
    }
}