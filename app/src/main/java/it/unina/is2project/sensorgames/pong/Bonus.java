package it.unina.is2project.sensorgames.pong;

import android.os.Handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Bonus {

    private static Bonus bonusInstance;
    private Handler handler;
    private Map<Integer, Integer> bonusMap = new HashMap<>();

    public static final int DEFAULT_BONUS_COUNT = 3;

    public static final int BONUS_EXPIRED = 725;
    public static final int BONUS_CREATED = 825;

    private Bonus(Handler handler) {
        this.handler = handler;
    }

    public static Bonus getBonusInstance(Handler h){
        if(bonusInstance == null){
            bonusInstance = new Bonus(h);
        }
        bonusInstance.setHandler(h);
        return bonusInstance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    void addBonus(int bonusID, int reachCount){
        if (reachCount <= 0) reachCount = DEFAULT_BONUS_COUNT;
        bonusMap.put(bonusID, reachCount);
        handler.obtainMessage(BONUS_CREATED, bonusID, -1).sendToTarget();
    }

    void decrementCount(){
        Iterator it = bonusMap.entrySet().iterator();
        Integer value;
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            value = (Integer)pair.getValue();
            value--;
            if (value == 0){
                bonusMap.remove(pair.getKey());
                handler.obtainMessage(BONUS_EXPIRED, (Integer)pair.getKey(),-1).sendToTarget();
            }else{
                bonusMap.put((Integer)pair.getKey(), value);
            }
        }
    }

    boolean alreadyExist(int bonusID){
        return bonusMap.containsKey(bonusID);
    }
}
