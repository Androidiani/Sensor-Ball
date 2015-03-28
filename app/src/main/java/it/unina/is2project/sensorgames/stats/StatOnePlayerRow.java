package it.unina.is2project.sensorgames.stats;

import it.unina.is2project.sensorgames.stats.entity.Player;

public class StatOnePlayerRow {
    private Player player;
    private String data;
    private int score;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
