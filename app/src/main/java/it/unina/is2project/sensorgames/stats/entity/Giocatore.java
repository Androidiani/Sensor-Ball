package it.unina.is2project.sensorgames.stats.entity;

@Deprecated
public class Giocatore {
    private int id;
    private String nome;
    private int partiteGiocateSingolo;
    private int partiteGiocateMulti;
    private int partiteVinteMulti;

    public Giocatore() {
    }

    public Giocatore(String nome, int partiteGiocateSingolo, int partiteGiocateMulti, int partiteVinteMulti) {
        this.nome = nome;
        this.partiteGiocateSingolo = partiteGiocateSingolo;
        this.partiteGiocateMulti = partiteGiocateMulti;
        this.partiteVinteMulti = partiteVinteMulti;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPartiteGiocateSingolo() {
        return partiteGiocateSingolo;
    }

    public void setPartiteGiocateSingolo(int partiteGiocateSingolo) {
        this.partiteGiocateSingolo = partiteGiocateSingolo;
    }

    public int getPartiteGiocateMulti() {
        return partiteGiocateMulti;
    }

    public void setPartiteGiocateMulti(int partiteGiocateMulti) {
        this.partiteGiocateMulti = partiteGiocateMulti;
    }

    public int getPartiteVinteMulti() {
        return partiteVinteMulti;
    }

    public void setPartiteVinteMulti(int partiteVinteMulti) {
        this.partiteVinteMulti = partiteVinteMulti;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Giocatore giocatore = (Giocatore) o;

        if (id != giocatore.id) return false;
        if (partiteGiocateMulti != giocatore.partiteGiocateMulti) return false;
        if (partiteGiocateSingolo != giocatore.partiteGiocateSingolo) return false;
        if (partiteVinteMulti != giocatore.partiteVinteMulti) return false;
        if (!nome.equals(giocatore.nome)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + nome.hashCode();
        result = 31 * result + partiteGiocateSingolo;
        result = 31 * result + partiteGiocateMulti;
        result = 31 * result + partiteVinteMulti;
        return result;
    }
}
