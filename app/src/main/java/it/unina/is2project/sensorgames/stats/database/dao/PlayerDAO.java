package it.unina.is2project.sensorgames.stats.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.stats.database.DatabaseHandler;
import it.unina.is2project.sensorgames.stats.entity.Player;

/**
 * PlayerDAO Object
 */

public class PlayerDAO implements IDAO<Player> {

    // Player table name
    public static final String TABLE_NAME = "player";
    public static final String KEY_ID = "id";
    private static final String COL_NOME = "nome";

    private String[] columns = new String[]{KEY_ID, COL_NOME};

    //IMPORTANTE: Da usare in SQLiteHelper nei metodi onCreate() ed onUpgrade()
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NOME + " TEXT NOT NULL"
            + ");";
    public static final String UPGRADE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SQLiteDatabase db;
    private DatabaseHandler dbHelper;

    public PlayerDAO(Context context) {
        dbHelper = new DatabaseHandler(context);
        db = dbHelper.getWritableDatabase();
    }

    // Close the db
    public void close() {
        db.close();
    }

    /**
     * Create new Player object
     *
     * @param player
     */
    public long insert(Player player) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOME, player.getNome());

        // Insert into DB
        return db.insert(TABLE_NAME, null, contentValues);
    }

    // Updating single player
    public int update(Player player) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOME, player.getNome());

        // updating row
        return db.update(TABLE_NAME, contentValues, KEY_ID + " = ?", new String[]{String.valueOf(player.getId())});
    }

    /**
     * Delete TODO object
     *
     * @param id player
     */
    public void delete(int id) {
        // Delete from DB where id match
        db.delete(TABLE_NAME, KEY_ID + " = " + id, null);
    }

    public Player findById(int id) {
        Cursor cursor = db.query(TABLE_NAME, columns, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        Player player = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                player = new Player();
                //In ordine per come sono definite nell'array columns
                player.setId(cursor.getInt(0));
                player.setNome(cursor.getString(1));

                cursor.close();
            }
        }
        //db.close();

        return player;
    }

    /**
     * Get all TODOs.
     *
     * @return
     */
    public List<Player> findAll() {
        List<Player> list = new ArrayList<Player>();

        // Name of the columns we want to select

        // Query the database
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        // Iterate the results
        while (!cursor.isAfterLast()) {
            Player player = new Player();
            // Take values from the DB
            player.setId(cursor.getInt(0));
            player.setNome(cursor.getString(1));

            // Add to the DB
            list.add(player);

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
