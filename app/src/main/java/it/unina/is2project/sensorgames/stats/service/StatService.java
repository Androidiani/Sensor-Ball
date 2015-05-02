package it.unina.is2project.sensorgames.stats.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.stats.entity.StatOnePlayerRow;
import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatTwoPlayerDAO;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatOnePlayer;
import it.unina.is2project.sensorgames.stats.entity.StatTwoPlayer;

public class StatService {

    private final PlayerDAO playerDAO;
    private final StatOnePlayerDAO statOnePlayerDAO;
    private final StatTwoPlayerDAO statTwoPlayerDAO;

    public StatService(Context context) {
        playerDAO = new PlayerDAO(context);
        statOnePlayerDAO = new StatOnePlayerDAO(context);
        statTwoPlayerDAO = new StatTwoPlayerDAO(context);
    }

    public List<StatOnePlayerRow> getStatOnePlayerList() {
        List<StatOnePlayerRow> ret = new ArrayList<>();
        List<StatOnePlayer> lista = statOnePlayerDAO.findAll(true);
        for (StatOnePlayer s : lista) {
            StatOnePlayerRow statOnePlayerRow = new StatOnePlayerRow();
            Player p = playerDAO.findById(s.getIdPlayer());
            statOnePlayerRow.setPlayer(p);
            statOnePlayerRow.setScore(s.getScore());
            statOnePlayerRow.setData(s.getData());
            ret.add(statOnePlayerRow);
        }
        return ret;
    }

    public StatTwoPlayer getStatTwoPlayer(int idPlayer) {
        return statTwoPlayerDAO.findById(idPlayer);
    }

    public List<Player> getPlayers() {
        return playerDAO.findAll(true);
    }
}
