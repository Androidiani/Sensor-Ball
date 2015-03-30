package it.unina.is2project.sensorgames.pong;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;


public class GamePongOnePlayerTest extends AndroidTestCase {
    private GamePongOnePlayer gamePongOnePlayer;

    public void setUp() throws Exception {
        Log.d("GamePongOnePlayerTest", "setup()");
        super.setUp();

        gamePongOnePlayer = new GamePongOnePlayer();
    }

    public void tearDown() throws Exception {
        Log.d("GamePongOnePlayerTest", "tearDown()");
        super.tearDown();
    }

    public void testClose() throws Exception {

    }

}
