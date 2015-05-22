package it.unina.is2project.sensorpong.stats.database.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.List;

import it.unina.is2project.sensorpong.stats.entity.Player;
import it.unina.is2project.sensorpong.stats.entity.StatTwoPlayer;

public class StatTwoPlayerDAOTest extends AndroidTestCase {

    private StatTwoPlayerDAO statTwoPlayerDAO;
    private PlayerDAO playerDAO;

    public void setUp() throws Exception {
        Log.d("StatTwoPlayerDAOTest", "setup()");
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");

        statTwoPlayerDAO = new StatTwoPlayerDAO(context);
        playerDAO = new PlayerDAO(context);
    }

    public void tearDown() throws Exception {
        Log.d("StatTwoPlayerDAOTest", "tearDown()");
        statTwoPlayerDAO.close();
        super.tearDown();
    }

    public void testClose() throws Exception {

    }

    public void testInsert() throws Exception {
        //Inserisco un statTwoPlayer nel database
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatTwoPlayer statTwoPlayer = new StatTwoPlayer((int) idPlayer, 0, 0);
        //Salvo l'id del record appena inserito
        long id = statTwoPlayerDAO.insert(statTwoPlayer);
        //Carico il statTwoPlayer con l'id indicato
        StatTwoPlayer statTwoPlayer1 = statTwoPlayerDAO.findById((int) id);
        assertFalse("StatTwoPlayer1 e StatTwoPlayer2 sono diversi", statTwoPlayer.equals(statTwoPlayer1));
    }

    public void testUpdate() throws Exception {
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatTwoPlayer statTwoPlayer = new StatTwoPlayer((int) idPlayer, 0, 0);
        //Salvo l'id del recordappena inserito
        long id = statTwoPlayerDAO.insert(statTwoPlayer);

        //findById per aggiornare
        StatTwoPlayer stat2 = statTwoPlayerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", stat2);
        //String nome = "Gianluca";
        int partiteGiocate = stat2.getPartiteGiocate();
        int partiteVinte = stat2.getPartiteVinte();
        int player = stat2.getIdPlayer();

        //Update
        stat2.setIdPlayer(player);
        stat2.setPartiteGiocate(++partiteGiocate);
        stat2.setPartiteVinte(++partiteVinte);

        int rows = statTwoPlayerDAO.update(stat2);
        assertTrue("Nessun record aggiornato.", rows > 0);

        //Verifica
        StatTwoPlayer g3 = statTwoPlayerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g3);
        assertTrue(player == g3.getIdPlayer());
        assertTrue(partiteGiocate == g3.getPartiteGiocate());
        assertTrue(partiteVinte == g3.getPartiteVinte());
    }

    public void testDelete() throws Exception {
        Player p1 = new Player("Giovanni");
        long idPlayer = playerDAO.insert(p1);
        StatTwoPlayer statTwoPlayer = new StatTwoPlayer((int) idPlayer, 0, 0);
        //Salvo l'id del record appena inserito
        long id = statTwoPlayerDAO.insert(statTwoPlayer);

        //Rimuovo dal database
        statTwoPlayerDAO.delete((int) id);

        //Sul db non lo troverò più
        StatTwoPlayer stat2 = statTwoPlayerDAO.findById((int) id);
        assertNull(stat2);
    }

    public void testFindById() throws Exception {
        // vedi testInsert()
    }

    public void testFindById_1() throws Exception {
        StatTwoPlayer g = statTwoPlayerDAO.findById(0);
        assertNull(g);
    }

    public void testFindAll() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player p1 = new Player("Player " + i);
            long idPlayer = playerDAO.insert(p1);
            StatTwoPlayer g = new StatTwoPlayer((int) idPlayer, i * i, i + i);
            statTwoPlayerDAO.insert(g);
        }

        List<StatTwoPlayer> lista = statTwoPlayerDAO.findAll(false);
        assertNotNull("Lista non creata", lista);
        assertTrue("Lista vuota", lista.size() > 0);
        int i = 0;
        for (StatTwoPlayer g : lista) {
            assertEquals(i * i, g.getPartiteGiocate());
            assertEquals(i + i, g.getPartiteVinte());
            //assertEquals(i, g.getIdPlayer());
            i++;
        }
    }

    public void testCount() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player p1 = new Player("Player " + i);
            long idPlayer = playerDAO.insert(p1);
            StatTwoPlayer g = new StatTwoPlayer((int) idPlayer, 20 + i, 10 + i);
            statTwoPlayerDAO.insert(g);
        }
        assertEquals(n, statTwoPlayerDAO.count());
    }
}