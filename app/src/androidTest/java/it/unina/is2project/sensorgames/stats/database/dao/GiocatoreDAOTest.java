package it.unina.is2project.sensorgames.stats.database.dao;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.List;

import it.unina.is2project.sensorgames.stats.database.dao.GiocatoreDAO;
import it.unina.is2project.sensorgames.stats.entity.Giocatore;

public class GiocatoreDAOTest extends AndroidTestCase {

    private GiocatoreDAO giocatoreDAO;

    public void setUp() throws Exception {
        Log.d("GiocatoreDAOTest", "setup()");
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");

        giocatoreDAO = new GiocatoreDAO(context);
    }

    public void tearDown() throws Exception {
        Log.d("GiocatoreDAOTest", "tearDown()");
        //giocatoreDAO.close();
        super.tearDown();
    }

    public void testClose() throws Exception {

    }

    public void testInsert() throws Exception {
        //Inserisco un giocatore nel database
        Giocatore g1 = new Giocatore("Giovanni", 10, 11, 12);
        //Salvo l'id del recordappena inserito
        long id = giocatoreDAO.insert(g1);
        //Carico il giocatore con l'id indicato
        Giocatore g2 = giocatoreDAO.findById((int) id);
        assertFalse("Giocatore1 e Giocatore2 sono diversi", g1.equals(g2));
    }

    public void testUpdate() throws Exception {
        //Inserisco un giocatore nel database
        Giocatore g1 = new Giocatore("Giovanni", 2, 1, 3);
        //Salvo l'id del record appena inserito
        long id = giocatoreDAO.insert(g1);

        //findById per aggiornare
        Giocatore g2 = giocatoreDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g2);
        String nome = "Gianluca";
        int pgs = g2.getPartiteGiocateSingolo();
        int pgm = g2.getPartiteGiocateMulti();
        int pvm = g2.getPartiteVinteMulti();

        //Update
        g2.setPartiteGiocateSingolo(++pgs);
        g2.setPartiteGiocateMulti(++pgm);
        g2.setPartiteVinteMulti(++pvm);
        int rows = giocatoreDAO.update(g2);
        assertTrue("Nessun record aggiornato.", rows > 0);

        //Verifica
        Giocatore g3 = giocatoreDAO.findById((int) id);
        assertNotNull("Record con id " + id + " non trovato.", g3);
        assertTrue(pgs == g3.getPartiteGiocateSingolo());
        assertTrue(pgm == g3.getPartiteGiocateMulti());
        assertTrue(pvm == g3.getPartiteVinteMulti());
    }

    public void testDelete() throws Exception {
        //Inserisco un giocatore nel database
        Giocatore g1 = new Giocatore("Giovanni", 10, 11, 12);
        //Salvo l'id del record appena inserito
        long id = giocatoreDAO.insert(g1);

        //Rimuovo dal database
        giocatoreDAO.delete((int) id);

        //Sul db non lo troverò più
        Giocatore g2 = giocatoreDAO.findById((int) id);
        assertNull(g2);
    }

    public void testFindById() throws Exception {
        // vedi testInsert()
    }

    public void testFindById_1() throws Exception {
        Giocatore g = giocatoreDAO.findById(0);
        assertNull(g);
    }

    public void testFindAll() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Giocatore g = new Giocatore("Giocatore" + i, 10 + i, i * i, 17 * i);
            giocatoreDAO.insert(g);
        }

        List<Giocatore> lista = giocatoreDAO.findAll(false);
        assertNotNull("Lista non creata", lista);
        assertTrue("Lista vuota", lista.size() > 0);
        int i = 0;
        for (Giocatore g : lista) {
            assertEquals("Giocatore" + i, g.getNome());
            assertEquals(10 + i, g.getPartiteGiocateSingolo());
            assertEquals(i * i, g.getPartiteGiocateMulti());
            assertEquals(17 * i, g.getPartiteVinteMulti());
            i++;
        }
    }

    public void testCount() throws Exception {
        int n = 10;
        for (int i = 0; i < n; i++) {
            Giocatore g = new Giocatore("Giocatore" + i, 10 + i, i * i, 17 * i);
            giocatoreDAO.insert(g);
        }
        assertEquals(n, giocatoreDAO.count());
    }
}