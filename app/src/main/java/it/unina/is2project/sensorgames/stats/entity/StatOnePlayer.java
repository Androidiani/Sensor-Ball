package it.unina.is2project.sensorgames.stats.entity;

public class StatOnePlayer {
    private int id;
    private long score;
    private String data;
    private int idPlayer;

    public StatOnePlayer() {
    }

    public StatOnePlayer(int idPlayer, String data, long score) {
        this.idPlayer = idPlayer;
        this.data = data;
        this.score = score;
    }

    public int getIdPlayer() {
        return idPlayer;
    }

    public void setIdPlayer(int idPlayer) {
        this.idPlayer = idPlayer;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatOnePlayer that = (StatOnePlayer) o;

        if (id != that.id) return false;
        if (idPlayer != that.idPlayer) return false;
        if (score != that.score) return false;
        if (!data.equals(that.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (score ^ (score >>> 32));
        result = 31 * result + data.hashCode();
        result = 31 * result + idPlayer;
        return result;
    }
}
