package it.unina.is2project.sensorgames.stats.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.unina.is2project.sensorgames.stats.database.dao.GiocatoreDAO;
import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatTwoPlayerDAO;

/**
 * DatabaseHandler Object
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "statistics_db";
    public static final String TAG = "DatabaseHandler";

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
        Log.d(TAG, "SQL command: " + GiocatoreDAO.CREATE_TABLE);
        db.execSQL(GiocatoreDAO.CREATE_TABLE);

        Log.d(TAG, "SQL command: " + PlayerDAO.CREATE_TABLE);
        db.execSQL(PlayerDAO.CREATE_TABLE);

        Log.d(TAG, "SQL command: " + StatOnePlayerDAO.CREATE_TABLE);
        db.execSQL(StatOnePlayerDAO.CREATE_TABLE);

        Log.d(TAG, "SQL command: " + StatTwoPlayerDAO.CREATE_TABLE);
        db.execSQL(StatTwoPlayerDAO.CREATE_TABLE);

        db.execSQL("INSERT INTO player VALUES(1,'Giovanni')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,30,'2015-03-26',1)");
        db.execSQL("INSERT INTO stat_two_player VALUES(1,1,2)");

        db.execSQL("INSERT INTO player VALUES(2,'Alessandro')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,30,'2015-03-27',2)");
        db.execSQL("INSERT INTO stat_two_player VALUES(2,1,2)");

        db.execSQL("INSERT INTO player VALUES(3,'Francesco')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,30,'2015-03-27',3)");

        db.execSQL("INSERT INTO player VALUES(4,'Gabriele')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,30,'2015-03-27',4)");
        db.execSQL("INSERT INTO stat_two_player VALUES(4,2,3)");

        db.execSQL("INSERT INTO player VALUES(5,'Tim Cook')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,0,'2015-03-27',5)");

        db.execSQL("INSERT INTO player VALUES(6,'Anonymous')");
        db.execSQL("INSERT INTO stat_one_player VALUES(NULL,1300000,'2015-03-29',6)");

    }

    /**
     * Recreates table
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        // DROP table
        Log.d(TAG, "SQL command: " + GiocatoreDAO.UPGRADE_TABLE);
        db.execSQL(GiocatoreDAO.UPGRADE_TABLE);

        Log.d(TAG, "SQL command: " + PlayerDAO.UPGRADE_TABLE);
        db.execSQL(PlayerDAO.UPGRADE_TABLE);

        Log.d(TAG, "SQL command: " + StatOnePlayerDAO.UPGRADE_TABLE);
        db.execSQL(StatOnePlayerDAO.UPGRADE_TABLE);

        Log.d(TAG, "SQL command: " + StatTwoPlayerDAO.UPGRADE_TABLE);
        db.execSQL(StatTwoPlayerDAO.UPGRADE_TABLE);

        // Recreate table
        onCreate(db);
    }

}
