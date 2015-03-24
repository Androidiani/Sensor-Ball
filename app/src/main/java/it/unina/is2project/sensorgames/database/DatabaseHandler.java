package it.unina.is2project.sensorgames.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.unina.is2project.sensorgames.database.dao.GiocatoreDAO;

/**
 * DatabaseHandler Object
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "statistics_db";

    public DatabaseHandler(Context context) {
        // Databse: todos_db, Version: 1
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute create table SQL
        Log.d("DBHandler", "SQL command: " + GiocatoreDAO.CREATE_TABLE);
        db.execSQL(GiocatoreDAO.CREATE_TABLE);
        ;
    }

    /**
     * Recreates table
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        // DROP table
        Log.d("DBHandler", "SQL command: " + GiocatoreDAO.UPGRADE_TABLE);
        db.execSQL(GiocatoreDAO.UPGRADE_TABLE);
        // Recreate table
        onCreate(db);
    }

}
