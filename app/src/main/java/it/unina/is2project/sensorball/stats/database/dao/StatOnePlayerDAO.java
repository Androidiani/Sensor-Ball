package it.unina.is2project.sensorball.stats.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorball.stats.database.DatabaseHandler;
import it.unina.is2project.sensorball.stats.entity.StatOnePlayer;

/**
 * StatOnePlayerDAO Object
 */
public class StatOnePlayerDAO implements IDAO<StatOnePlayer> {

    // StatOnePlayer table name
    private static final String TABLE_NAME = "stat_one_player";
    private static final String KEY_ID = "id";
    private static final String COL_SCORE = "score";
    private static final String COL_DATA = "data";
    private static final String COL_ID_PLAYER = "id_player";

    private final String[] columns = new String[]{KEY_ID, COL_SCORE, COL_DATA, COL_ID_PLAYER};

    //IMPORTANTE: Da usare in SQLiteHelper nei metodi onCreate() ed onUpgrade()
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SCORE + " INTEGER NOT NULL,"
            + COL_DATA + " TEXT NOT NULL,"
            + COL_ID_PLAYER + " INTEGER,"
            + "FOREIGN KEY(" + COL_ID_PLAYER + ") REFERENCES " + PlayerDAO.TABLE_NAME + "(" + PlayerDAO.KEY_ID + ")"
            + ");";
    public static final String UPGRADE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SQLiteDatabase db;

    public StatOnePlayerDAO(Context context) {
        DatabaseHandler dbHelper = new DatabaseHandler(context);
        db = dbHelper.getWritableDatabase();
    }

    // Close the db
    public void close() {
        db.close();
    }

    /**
     * Create new StatOnePlayer object
     */
    public long insert(StatOnePlayer statOnePlayer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SCORE, statOnePlayer.getScore());
        contentValues.put(COL_DATA, statOnePlayer.getData());
        contentValues.put(COL_ID_PLAYER, statOnePlayer.getIdPlayer());

        // Insert into DB
        return db.insert(TABLE_NAME, null, contentValues);
    }

    // Updating single statOnePlayer
    public int update(StatOnePlayer statOnePlayer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SCORE, statOnePlayer.getScore());
        contentValues.put(COL_DATA, statOnePlayer.getData());
        contentValues.put(COL_ID_PLAYER, statOnePlayer.getIdPlayer());
        // Updating row
        return db.update(TABLE_NAME, contentValues, KEY_ID + " = ?", new String[]{String.valueOf(statOnePlayer.getId())});
    }

    /**
     * Delete from DB
     *
     * @param id statOnePlayer
     */
    public void delete(int id) {
        // Delete from DB where id match
        db.delete(TABLE_NAME, KEY_ID + " = " + id, null);
    }

    public StatOnePlayer findById(int id) {
        Cursor cursor = db.query(TABLE_NAME, columns, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        StatOnePlayer statOnePlayer = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                statOnePlayer = new StatOnePlayer();
                //In ordine per come sono definite nell'array columns
                statOnePlayer.setId(cursor.getInt(0));
                statOnePlayer.setScore(cursor.getInt(1));
                statOnePlayer.setData(cursor.getString(2));
                statOnePlayer.setIdPlayer(cursor.getInt(3));

                cursor.close();
            }
        }

        return statOnePlayer;
    }

    /**
     * Restituisce tutti i risultati one player.
     * La lista è ordinata per punteggio decrescente.
     *
     * @return List<StatOnePlayer>
     */
    @Override
    public List<StatOnePlayer> findAll(boolean ordered) {
        List<StatOnePlayer> list = new ArrayList<>();

        // Name of the columns we want to select

        // Query the database
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, (ordered ? COL_SCORE + " DESC" : null));
        cursor.moveToFirst();

        // Iterate the results
        while (!cursor.isAfterLast()) {
            StatOnePlayer statOnePlayer = new StatOnePlayer();
            // Take values from the DB
            statOnePlayer.setId(cursor.getInt(0));
            statOnePlayer.setScore(cursor.getInt(1));
            statOnePlayer.setData(cursor.getString(2));
            statOnePlayer.setIdPlayer(cursor.getInt(3));

            // Add to the DB
            list.add(statOnePlayer);

            // Move to the next result
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }

    public int count() {
        String countQuery = "SELECT  count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            cursor.close();
            return cursor.getInt(0);
        } else {
            return 0;
        }
    }
}
