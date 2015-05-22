package it.unina.is2project.sensorpong.stats.database.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.List;

import it.unina.is2project.sensorpong.stats.entity.Player;
import it.unina.is2project.sensorpong.stats.entity.StatOnePlayer;

public class StatOnePlayerDAOTest extends AndroidTestCase {

    private StatOnePlayerDAO statOnePlayerDAO;
    private PlayerDAO playerDAO;

    public void setUp() throws Exception {
        Log.d("StatOnePlayerDAOTest", "setup()");
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");

        statOnePlayerDAO = new StatOnePlayerDAO(context);
        playerDAO = new PlayerDAO(context);
    }

    public void tearDown() throws Exception {
        Log.d("StatOnePlayerDAOTest", "tearDown()");
        statOnePlayerDAO.close();
        super.tearDown();
    }

    public void testClose() throws Exception {

    }

    public void testInsert() throws Exception {
        //Inserisco un statOnePlayer nel database
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, "2015-03-26", 0);
        //Salvo l'id del recordappena inserito
        long id = statOnePlayerDAO.insert(statOnePlayer);
        //Carico il statOnePlayer con l'id indicato
        StatOnePlayer statOnePlayer1 = statOnePlayerDAO.findById((int) id);
        assertFalse("StatOnePlayer1 e StatOnePlayer2 sono diversi", statOnePlayer.equals(statOnePlayer1));
    }

    public void testUpdate() throws Exception {
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, "2015-03-26", 0);
        //Salvo l'id del recordappena inserito
        long id = statOnePlayerDAO.insert(statOnePlayer);

        //findById per aggiornare
        StatOnePlayer stat2 = statOnePlayerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", stat2);
        long score = stat2.getScore();
        String data = stat2.getData();
        int player = stat2.getIdPlayer();

        //Update
        stat2.setScore(++score);
        stat2.setData(data);
        stat2.setIdPlayer(player);
        int rows = statOnePlayerDAO.update(stat2);
        assertTrue("Nessun record aggiornato.", rows > 0);

        //Verifica
        StatOnePlayer g3 = statOnePlayerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g3);
        assertTrue(score == g3.getScore());
        assertTrue(g3.getData().equals("2015-03-20"));
        assertTrue(player == g3.getIdPlayer());
    }

    public void testDelete() throws Exception {
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, "2015-03-26", 0);
        //Salvo l'id del recordappena inserito
        long id = statOnePlayerDAO.insert(statOnePlayer);

        //Rimuovo dal database
        statOnePlayerDAO.delete((int) id);

        //Sul db non lo troverò più
        StatOnePlayer stat2 = statOnePlayerDAO.findById((int) id);
        assertNull(stat2);
    }

    public void testFindById() throws Exception {
        // vedi testInsert()
    }

    public void testFindById_1() throws Exception {
        StatOnePlayer g = statOnePlayerDAO.findById(0);
        assertNull(g);
    }

    public void testFindAll() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player p1 = new Player("Player " + i);
            long idPlayer = playerDAO.insert(p1);
            StatOnePlayer g = new StatOnePlayer((int) idPlayer, "2015-02-12", i * i);
            statOnePlayerDAO.insert(g);
        }

        List<StatOnePlayer> lista = statOnePlayerDAO.findAll(false);
        assertNotNull("Lista non creata", lista);
        assertTrue("Lista vuota", lista.size() > 0);
        int i = 0;
        for (StatOnePlayer g : lista) {
            assertEquals(i * i, g.getScore());
            assertEquals("2015-02-12", g.getData());
            //assertEquals(i, g.getIdPlayer());
            i++;
        }
    }

    public void testCount() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player p1 = new Player("Player " + i);
            long idPlayer = playerDAO.insert(p1);
            StatOnePlayer g = new StatOnePlayer((int) idPlayer, "2014-02-01", 10 + i);
            statOnePlayerDAO.insert(g);
        }
        assertEquals(n, statOnePlayerDAO.count());
    }
}