package it.unina.is2project.sensorgames.stats.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.stats.database.DatabaseHandler;
import it.unina.is2project.sensorgames.stats.entity.Giocatore;

/**
 * GiocatoreDAO Object
 */

@Deprecated
public class GiocatoreDAO implements IDAO<Giocatore> {

    // Giocatore table name
    private static final String TABLE_NAME = "giocatore";
    private static final String KEY_ID = "id";
    private static final String COL_NOME = "nome";
    private static final String COL_PARTITE_GIOCATE_SINGOLO = "partite_giocate_singolo";
    private static final String COL_PARTITE_GIOCATE_MULTI = "partite_giocate_multi";
    private static final String COL_PARTITE_VINTE_MULTI = "partite_vinte_multi";

    private String[] columns = new String[]{KEY_ID, COL_NOME, COL_PARTITE_GIOCATE_SINGOLO, COL_PARTITE_GIOCATE_MULTI, COL_PARTITE_VINTE_MULTI};

    //IMPORTANTE: Da usare in SQLiteHelper nei metodi onCreate() ed onUpgrade()
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NOME + " TEXT NOT NULL,"
            + COL_PARTITE_GIOCATE_SINGOLO + " INTEGER,"
            + COL_PARTITE_GIOCATE_MULTI + " INTEGER,"
            + COL_PARTITE_VINTE_MULTI + " INTEGER"
            + ");";
    public static final String UPGRADE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SQLiteDatabase db;
    private DatabaseHandler dbHelper;

    public GiocatoreDAO(Context context) {
        dbHelper = new DatabaseHandler(context);
        db = dbHelper.getWritableDatabase();
    }

    // Close the db
    public void close() {
        db.close();
    }

    /**
     * Create new Giocatore object
     *
     * @param giocatore
     */
    public long insert(Giocatore giocatore) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOME, giocatore.getNome());
        contentValues.put(COL_PARTITE_GIOCATE_SINGOLO, giocatore.getPartiteGiocateSingolo());
        contentValues.put(COL_PARTITE_GIOCATE_MULTI, giocatore.getPartiteGiocateMulti());
        contentValues.put(COL_PARTITE_VINTE_MULTI, giocatore.getPartiteVinteMulti());

        // Insert into DB
        return db.insert(TABLE_NAME, null, contentValues);
    }

    // Updating single giocatore
    public int update(Giocatore giocatore) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOME, giocatore.getNome());
        contentValues.put(COL_PARTITE_GIOCATE_SINGOLO, giocatore.getPartiteGiocateSingolo());
        contentValues.put(COL_PARTITE_GIOCATE_MULTI, giocatore.getPartiteGiocateMulti());
        contentValues.put(COL_PARTITE_VINTE_MULTI, giocatore.getPartiteVinteMulti());
        // updating row
        return db.update(TABLE_NAME, contentValues, KEY_ID + " = ?", new String[]{String.valueOf(giocatore.getId())});
    }

    /**
     * Delete TODO object
     *
     * @param id giocatore
     */
    public void delete(int id) {
        // Delete from DB where id match
        db.delete(TABLE_NAME, KEY_ID + " = " + id, null);
    }

    // Getting single todo
    public Giocatore findById(int id) {
        Cursor cursor = db.query(TABLE_NAME, columns, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        Giocatore giocatore = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                giocatore = new Giocatore();
                //In ordine per come sono definite nell'array columns
                giocatore.setId(cursor.getInt(0));
                giocatore.setNome(cursor.getString(1));
                giocatore.setPartiteGiocateSingolo(cursor.getInt(2));
                giocatore.setPartiteGiocateMulti(cursor.getInt(3));
                giocatore.setPartiteVinteMulti(cursor.getInt(4));

                // return todo
                cursor.close();
            }
        }
        //db.close();

        return giocatore;
    }

    /**
     * Get all TODOs.
     *
     * @return
     */
    public List<Giocatore> findAll(boolean ordered) {
        List<Giocatore> list = new ArrayList<Giocatore>();

        // Name of the columns we want to select

        // Query the database
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        // Iterate the results
        while (!cursor.isAfterLast()) {
            Giocatore giocatore = new Giocatore();
            // Take values from the DB
            giocatore.setId(cursor.getInt(0));
            giocatore.setNome(cursor.getString(1));
            giocatore.setPartiteGiocateSingolo(cursor.getInt(2));
            giocatore.setPartiteGiocateMulti(cursor.getInt(3));
            giocatore.setPartiteVinteMulti(cursor.getInt(4));

            // Add to the DB
            list.add(giocatore);

            // Move to the next result
            cursor.moveToNext();
        }

        return list;
    }

    public int count() {
        //String countQuery = "SELECT  * FROM " + TABLE_NAME;
        String countQuery = "SELECT  count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            return cursor.getInt(0);
        } else {
            return 0;
        }
        //cursor.close();
        // return count
        //return cursor.getCount();
    }
}
