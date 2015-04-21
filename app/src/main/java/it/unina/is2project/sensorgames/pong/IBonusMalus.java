package it.unina.is2project.sensorgames.pong;

import org.andengine.entity.scene.Scene;

public interface IBonusMalus {

    public void addToScene(Scene scene);

    public void collision();

    public void clear();
}
