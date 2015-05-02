package it.unina.is2project.sensorgames.stats.entity;

public class Player {
    private int id;
    private String nome;

    public Player() {
    }

    public Player(String nome) {
        this.nome = nome;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id == player.id && nome.equals(player.nome);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + nome.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
