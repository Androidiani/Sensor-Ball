package it.unina.is2project.sensorgames.game.entity;

import org.andengine.entity.scene.Scene;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class Bar extends GameObject implements IAccelerationListener {

    private float speed;
    // Sensors
    private AccelerationSensorOptions accelerationSensorOptions;

    public Bar(SimpleBaseGameActivity simpleBaseGameActivity, int idDrawable) {
        super(simpleBaseGameActivity, idDrawable);
        this.accelerationSensorOptions = new AccelerationSensorOptions(SensorDelay.GAME);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {

    }

    @Override
    public void onAccelerationChanged(AccelerationData pAccelerationData) {
        // The bar is moving only on X
        float newXPosition = gSprite.getX() + pAccelerationData.getX() * this.speed;
        // There's the edges' condition that do not hide the bar beyond the walls
        if (this.speed > 0) {
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
    public void addToScene(Scene scene, float spriteRatio) {
        super.addToScene(scene, spriteRatio);

        /** Enable the Acceleration Sensor
         * - Option: SensorDelay.GAME */
        this.simpleBaseGameActivity.getEngine().enableAccelerationSensor(context, this);
        this.accelerationSensorOptions = new AccelerationSensorOptions(SensorDelay.GAME);
    }
}
