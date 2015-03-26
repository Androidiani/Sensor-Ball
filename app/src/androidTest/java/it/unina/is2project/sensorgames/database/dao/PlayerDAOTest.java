package it.unina.is2project.sensorgames.database.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.List;

import it.unina.is2project.sensorgames.entity.Player;

public class PlayerDAOTest extends AndroidTestCase {

    private PlayerDAO playerDAO;

    public void setUp() throws Exception {
        Log.d("PlayerDAOTest", "setup()");
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");

        playerDAO = new PlayerDAO(context);
    }

    public void tearDown() throws Exception {
        Log.d("PlayerDAOTest", "tearDown()");
        playerDAO.close();
        super.tearDown();
    }

    public void testClose() throws Exception {

    }

    public void testInsert() throws Exception {
        //Inserisco un player nel database
        Player g1 = new Player("Giovanni");
        //Salvo l'id del recordappena inserito
        long id = playerDAO.insert(g1);
        //Carico il player con l'id indicato
        Player g2 = playerDAO.findById((int) id);
        assertFalse("Player1 e Player2 sono diversi", g1.equals(g2));
    }

    public void testUpdate() throws Exception {
        //Inserisco un player nel database
        Player g1 = new Player("Giovanni");
        //Salvo l'id del record appena inserito
        long id = playerDAO.insert(g1);

        //findById per aggiornare
        Player g2 = playerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g2);
        String nome = "Gianluca";
        int rows = playerDAO.update(g2);
        assertTrue("Nessun record aggiornato.", rows > 0);

        //Verifica
        Player g3 = playerDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g3);
    }

    public void testDelete() throws Exception {
        //Inserisco un player nel database
        Player g1 = new Player("Giovanni");
        //Salvo l'id del record appena inserito
        long id = playerDAO.insert(g1);

        //Rimuovo dal database
        playerDAO.delete((int) id);

        //Sul db non lo troverò più
        Player g2 = playerDAO.findById((int) id);
        assertNull(g2);
    }

    public void testFindById() throws Exception {
        // vedi testInsert()
    }

    public void testFindById_1() throws Exception {
        Player g = playerDAO.findById(0);
        assertNull(g);
    }

    public void testFindAll() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player g = new Player("Player" + i);
            playerDAO.insert(g);
        }

        List<Player> lista = playerDAO.findAll();
        assertNotNull("Lista non creata", lista);
        assertTrue("Lista vuota", lista.size() > 0);
        int i = 0;
        for (Player g : lista) {
            assertEquals("Player" + i, g.getNome());
            i++;
        }
    }

    public void testCount() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Player g = new Player("Player" + i);
            playerDAO.insert(g);
        }
        assertEquals(n, playerDAO.count());
    }
}