package it.unina.is2project.sensorgames.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.unina.is2project.sensorgames.database.dao.GiocatoreDAO;
import it.unina.is2project.sensorgames.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.database.dao.StatTwoPlayerDAO;

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

    /* I vincoli di integrità referenziale sono supportati ma bisogna abilitarli. */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute create table SQL
        Log.d("DBHandler", "SQL command: " + GiocatoreDAO.CREATE_TABLE);
        db.execSQL(GiocatoreDAO.CREATE_TABLE);

        Log.d("DBHandler", "SQL command: " + PlayerDAO.CREATE_TABLE);
        db.execSQL(PlayerDAO.CREATE_TABLE);

        Log.d("DBHandler", "SQL command: " + StatOnePlayerDAO.CREATE_TABLE);
        db.execSQL(StatOnePlayerDAO.CREATE_TABLE);

        Log.d("DBHandler", "SQL command: " + StatTwoPlayerDAO.CREATE_TABLE);
        db.execSQL(StatTwoPlayerDAO.CREATE_TABLE);
    }

    /**
     * Recreates table
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        // DROP table
        Log.d("DBHandler", "SQL command: " + GiocatoreDAO.UPGRADE_TABLE);
        db.execSQL(GiocatoreDAO.UPGRADE_TABLE);

        Log.d("DBHandler", "SQL command: " + PlayerDAO.UPGRADE_TABLE);
        db.execSQL(PlayerDAO.UPGRADE_TABLE);

        Log.d("DBHandler", "SQL command: " + StatOnePlayerDAO.UPGRADE_TABLE);
        db.execSQL(StatOnePlayerDAO.UPGRADE_TABLE);

        Log.d("DBHandler", "SQL command: " + StatTwoPlayerDAO.UPGRADE_TABLE);
        db.execSQL(StatTwoPlayerDAO.UPGRADE_TABLE);

        // Recreate table
        onCreate(db);
    }

}
