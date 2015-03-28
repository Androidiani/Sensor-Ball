package it.unina.is2project.sensorgames.stats.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.stats.database.DatabaseHandler;
import it.unina.is2project.sensorgames.stats.entity.StatTwoPlayer;

/**
 * StatTwoPlayerDAO Object
 */
public class StatTwoPlayerDAO implements IDAO<StatTwoPlayer> {

    // StatTwoPlayer table name
    private static final String TABLE_NAME = "statTwoPlayer";
    private static final String KEY_ID_FK = "id_player";
    private static final String COL_PARTITE_GIOCATE = "partite_giocate";
    private static final String COL_PARTITE_VINTE = "partite_vinte";

    //Array contenente i nomi delle colonne. Serve in findById, findAll
    private String[] columns = new String[]{KEY_ID_FK, COL_PARTITE_GIOCATE, COL_PARTITE_VINTE};

    //IMPORTANTE: Da usare in SQLiteHelper nei metodi onCreate() ed onUpgrade()
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + KEY_ID_FK + " INTEGER PRIMARY KEY, "
            + COL_PARTITE_GIOCATE + " INTEGER,"
            + COL_PARTITE_VINTE + " INTEGER,"
            + "FOREIGN KEY(" + KEY_ID_FK + ") REFERENCES " + PlayerDAO.TABLE_NAME + "(" + PlayerDAO.KEY_ID + ")"
            + ");";
    public static final String UPGRADE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SQLiteDatabase db;
    private DatabaseHandler dbHelper;

    public StatTwoPlayerDAO(Context context) {
        dbHelper = new DatabaseHandler(context);
        db = dbHelper.getWritableDatabase();
    }

    // Close the db
    public void close() {
        db.close();
    }

    /**
     * Create new StatTwoPlayer object
     *
     * @param statTwoPlayer
     */
    public long insert(StatTwoPlayer statTwoPlayer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID_FK, statTwoPlayer.getIdPlayer());
        contentValues.put(COL_PARTITE_GIOCATE, statTwoPlayer.getPartiteGiocate());
        contentValues.put(COL_PARTITE_VINTE, statTwoPlayer.getPartiteVinte());

        // Insert into DB
        return db.insert(TABLE_NAME, null, contentValues);
    }

    // Updating single statTwoPlayer
    public int update(StatTwoPlayer statTwoPlayer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID_FK, statTwoPlayer.getIdPlayer());
        contentValues.put(COL_PARTITE_GIOCATE, statTwoPlayer.getPartiteGiocate());
        contentValues.put(COL_PARTITE_VINTE, statTwoPlayer.getPartiteVinte());
        // updating row
        return db.update(TABLE_NAME, contentValues, KEY_ID_FK + " = ?", new String[]{String.valueOf(statTwoPlayer.getIdPlayer())});
    }

    /**
     * @param id statTwoPlayer
     */
    public void delete(int id) {
        // Delete from DB where id match
        db.delete(TABLE_NAME, KEY_ID_FK + " = " + id, null);
    }

    // Getting single todo
    public StatTwoPlayer findById(int id) {
        Cursor cursor = db.query(TABLE_NAME, columns, KEY_ID_FK + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        StatTwoPlayer statTwoPlayer = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                statTwoPlayer = new StatTwoPlayer();
                //In ordine per come sono definite nell'array columns
                //TODO da provare cursor.getColumnIndex
                //statTwoPlayer.setIdPlayer(cursor.getInt(cursor.getColumnIndex(KEY_ID_FK)));
                statTwoPlayer.setIdPlayer(cursor.getInt(0));
                statTwoPlayer.setPartiteGiocate(cursor.getInt(1));
                statTwoPlayer.setPartiteVinte(cursor.getInt(2));

                cursor.close();
            }
        }
        //db.close();

        return statTwoPlayer;
    }

    /**
     * Get all TODOs.
     *
     * @return
     */
    public List<StatTwoPlayer> findAll() {
        List<StatTwoPlayer> list = new ArrayList<StatTwoPlayer>();

        // Name of the columns we want to select

        // Query the database
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        // Iterate the results
        while (!cursor.isAfterLast()) {
            StatTwoPlayer statTwoPlayer = new StatTwoPlayer();
            // Take values from the DB
            statTwoPlayer.setIdPlayer(cursor.getInt(0));
            statTwoPlayer.setPartiteGiocate(cursor.getInt(1));
            statTwoPlayer.setPartiteVinte(cursor.getInt(2));

            // Add to the DB
            list.add(statTwoPlayer);

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
