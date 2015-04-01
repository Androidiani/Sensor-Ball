package it.unina.is2project.sensorgames.pong;

import android.test.ActivityInstrumentationTestCase2;

import com.genymotion.api.GenymotionManager;

public class GamePongOnePlayerTest extends ActivityInstrumentationTestCase2<GamePongOnePlayer> {

    private GamePongOnePlayer mGamePongOnePlayer;
    private GenymotionManager mGenymotionManager;

    public GamePongOnePlayerTest() {
        super(GamePongOnePlayer.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGamePongOnePlayer = new GamePongOnePlayer();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCall() throws Exception{
        if (!GenymotionManager.isGenymotionDevice()) {
            // Avoid test on non Genymotion devices.
            return;
        }
        mGenymotionManager = GenymotionManager.getGenymotionManager(getInstrumentation().getTargetContext());
        // Send a CALL by 3334445526
        mGenymotionManager.getRadio().call("3334445526");
        assertEquals(mGamePongOnePlayer.pause, true);
    }

    public void testSMS() throws Exception{
        if (!GenymotionManager.isGenymotionDevice()) {
            // Avoid test on non Genymotion devices.
            return;
        }
        mGenymotionManager = GenymotionManager.getGenymotionManager(getInstrumentation().getTargetContext());
        // Send SMS by 3334445526
        mGenymotionManager.getRadio().sendSms("3334445526","Hello");
        assertEquals(mGamePongOnePlayer.pause, true);
    }
}
