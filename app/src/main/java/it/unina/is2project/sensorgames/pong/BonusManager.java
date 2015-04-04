package it.unina.is2project.sensorgames.pong;

import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class BonusManager {

    private final String TAG = "BonusManager";

    private static BonusManager bonusManagerInstance;
    private Handler handler;
    private Map<Integer, Integer> bonusMap = new HashMap<>();

    private final int DEFAULT_BONUS_COUNT = 3;

    public static final int BONUS_EXPIRED = 725;
    public static final int BONUS_CREATED = 825;

    private BonusManager(Handler handler) {
        this.handler = handler;
    }

    public static BonusManager getBonusInstance(Handler h){
        if(bonusManagerInstance == null){
            bonusManagerInstance = new BonusManager(h);
        }
        bonusManagerInstance.setHandler(h);
        return bonusManagerInstance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void addBonus(int bonusID, int reachCount){
        if (reachCount <= 0) reachCount = DEFAULT_BONUS_COUNT;
        int previousBonus = GamePongTwoPlayer.NOBONUS;
        if(bonusID == GamePongTwoPlayer.SPEEDX2 ||
                bonusID == GamePongTwoPlayer.SPEEDX3 ||
                bonusID == GamePongTwoPlayer.SPEEDX4){
            if(bonusMap.remove(GamePongTwoPlayer.SPEEDX2) != null){
                previousBonus = GamePongTwoPlayer.SPEEDX2;
            }
            if(bonusMap.remove(GamePongTwoPlayer.SPEEDX3) != null){
                previousBonus = GamePongTwoPlayer.SPEEDX3;
            }
            if(bonusMap.remove(GamePongTwoPlayer.SPEEDX4) != null){
                previousBonus = GamePongTwoPlayer.SPEEDX4;
            }
        }

        // If the map previously contained a mapping for the key, the old value is replaced.
        bonusMap.put(bonusID, reachCount);
        Log.d(TAG, "Added Bonus With ID: " + bonusID + " and reachCount " + reachCount);

        if(previousBonus != GamePongTwoPlayer.NOBONUS)
            Log.d(TAG, "Exist Previous bonus ID: " + previousBonus);

        handler.obtainMessage(BONUS_CREATED, bonusID, previousBonus, -1).sendToTarget();
    }

    public void deleteBonus(int bonusID){
        bonusMap.remove(bonusID);
    }

    public void decrementCount(){
        Iterator it = bonusMap.entrySet().iterator();
        Integer value;
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            value = (Integer)pair.getValue();
            value--;
            Log.d("BonusManager", "Remain " + value + " of " + pair.getValue() + " ID: " + pair.getKey());
            if (value == 0){
//                bonusMap.remove(pair.getKey());
                it.remove();
                Log.d("BonusManager", "Removed bonus ID: " + pair.getKey());
                handler.obtainMessage(BONUS_EXPIRED, (Integer)pair.getKey(),-1).sendToTarget();
            }else{
                bonusMap.put((Integer)pair.getKey(), value);
            }
        }
    }

    public void clearAll(){
        bonusMap.clear();
    }

    public boolean alreadyExist(int bonusID){
        return bonusMap.containsKey(bonusID);
    }
}
