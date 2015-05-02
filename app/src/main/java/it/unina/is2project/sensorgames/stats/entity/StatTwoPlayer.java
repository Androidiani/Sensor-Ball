package it.unina.is2project.sensorgames.stats.entity;

public class StatTwoPlayer {

    private int idPlayer;
    private int partiteGiocate;
    private int partiteVinte;

    public StatTwoPlayer() {
    }

    public StatTwoPlayer(int idPlayer, int partiteGiocate, int partiteVinte) {
        this.idPlayer = idPlayer;
        this.partiteGiocate = partiteGiocate;
        this.partiteVinte = partiteVinte;
    }

    public int getIdPlayer() {
        return idPlayer;
    }

    public void setIdPlayer(int idPlayer) {
        this.idPlayer = idPlayer;
    }

    public int getPartiteGiocate() {
        return partiteGiocate;
    }

    public void setPartiteGiocate(int partiteGiocate) {
        this.partiteGiocate = partiteGiocate;
    }

    public int getPartiteVinte() {
        return partiteVinte;
    }

    public void setPartiteVinte(int partiteVinte) {
        this.partiteVinte = partiteVinte;
    }

    public void increasePartiteGiocate() {
        partiteGiocate++;
    }

    public void increasePartiteVinte() {
        partiteVinte++;
    }
}
