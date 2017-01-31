package com.campos.david.appointments.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.campos.david.appointments.model.DBContract.InvitationsEntry;
import com.campos.david.appointments.model.DBContract.AppointmentsEntry;
import com.campos.david.appointments.model.DBContract.PropositionsEntry;
import com.campos.david.appointments.model.DBContract.UsersEntry;
import com.campos.david.appointments.model.DBContract.ReasonsEntry;
import com.campos.david.appointments.model.DBContract.AppointmentTypesEntry;


/**
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a>
 */
public class DBHelper extends SQLiteOpenHelper{
    // On scheme update, increment the version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "appoint.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TYPES_TABLE = "CREATE TABLE " + AppointmentTypesEntry.TABLE_NAME + " (" +
                AppointmentTypesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                AppointmentTypesEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                AppointmentTypesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                AppointmentTypesEntry.COLUMN_ICON + " INT NOT NULL);";

        final String SQL_CREATE_REASONS_TABLE = "CREATE TABLE " + ReasonsEntry.TABLE_NAME + " (" +
                ReasonsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                ReasonsEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                ReasonsEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL);";

        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + UsersEntry.TABLE_NAME + " (" +
                UsersEntry._ID + " INTEGER PRIMARY KEY, " +

                UsersEntry.COLUMN_PHONE + " TEXT UNIQUE NOT NULL, " +
                UsersEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                UsersEntry.COLUMN_PICTURE + " INTEGER NOT NULL, " +
                UsersEntry.COLUMN_BLOCKED + " INTEGER NOT NULL DEFAULT 0);";

        final String SQL_CREATE_APPOINTMENTS_TABLE = "CREATE TABLE " + AppointmentsEntry.TABLE_NAME + " (" +
                AppointmentsEntry._ID + " INTEGER PRIMARY KEY, " +

                AppointmentsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                AppointmentsEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                AppointmentsEntry.COLUMN_CLOSED + " INTEGER NOT NULL, " +
                AppointmentsEntry.COLUMN_CREATOR + " INTEGER DEFAULT NULL, " +
                AppointmentsEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                AppointmentsEntry.COLUMN_CURRENT_PROPOSAL + " INTEGER NOT NULL, " +

                "FOREIGN KEY (" + AppointmentsEntry.COLUMN_CREATOR + ") " +
                    "REFERENCES " + UsersEntry.TABLE_NAME +  " (" + UsersEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                "FOREIGN KEY (" + AppointmentsEntry.COLUMN_TYPE + ") " +
                    "REFERENCES " + AppointmentTypesEntry.TABLE_NAME +  " (" + AppointmentTypesEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT);";
        // We can't add foreign key to Propositions table (SQLite facts)

        final String SQL_CREATE_PROPOSITIONS_TABLE = "CREATE TABLE " + PropositionsEntry.TABLE_NAME + " (" +
                PropositionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                PropositionsEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                PropositionsEntry.COLUMN_PLACE_LON + " REAL NOT NULL, " +
                PropositionsEntry.COLUMN_PLACE_LAT + " REAL NOT NULL, " +
                PropositionsEntry.COLUMN_PLACE_NAME + " TEXT NOT NULL, " +
                PropositionsEntry.COLUMN_APPOINTMENT + " INTEGER NOT NULL, " +
                PropositionsEntry.COLUMN_CREATOR + " INTEGER DEFAULT NULL, " +
                PropositionsEntry.COLUMN_REASON + " INTEGER DEFAULT NULL, " +

                "FOREIGN KEY (" + PropositionsEntry.COLUMN_APPOINTMENT + ") " +
                    "REFERENCES " + AppointmentsEntry.TABLE_NAME + " (" + AppointmentsEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (" + PropositionsEntry.COLUMN_CREATOR + ") " +
                    "REFERENCES " + UsersEntry.TABLE_NAME + " (" + UsersEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                "FOREIGN KEY (" + PropositionsEntry.COLUMN_CREATOR + ") " +
                    "REFERENCES " + ReasonsEntry.TABLE_NAME + " (" + ReasonsEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                "UNIQUE (" +
                    PropositionsEntry.COLUMN_APPOINTMENT  + ", " +
                    PropositionsEntry.COLUMN_TIMESTAMP + ", " +
                    PropositionsEntry.COLUMN_PLACE_NAME + ")" +
                ");";

        final String SQL_CREATE_INVITATIONS_TABLE = "CREATE TABLE " + InvitationsEntry.TABLE_NAME + " (" +
                InvitationsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                InvitationsEntry.COLUMN_USER + " INTEGER DEFAULT NULL, " +
                InvitationsEntry.COLUMN_STATE + " TEXT NOT NULL, " +
                InvitationsEntry.COLUMN_REASON + " INTEGER DEFAULT NULL, " +
                InvitationsEntry.COLUMN_APPOINTMENT +  " INTEGER NOT NULL, " +

                "FOREIGN KEY (" + InvitationsEntry.COLUMN_USER + ") " +
                    "REFERENCES " + UsersEntry.TABLE_NAME + " (" + UsersEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                "FOREIGN KEY (" + InvitationsEntry.COLUMN_REASON + ") " +
                    "REFERENCES " + ReasonsEntry.TABLE_NAME + " (" + ReasonsEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +
                "FOREIGN KEY (" + InvitationsEntry.COLUMN_APPOINTMENT + ") " +
                    "REFERENCES " + AppointmentsEntry.TABLE_NAME + " (" + AppointmentsEntry._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE RESTRICT, " +

                "UNIQUE (" + InvitationsEntry.COLUMN_USER + ", " +
                    InvitationsEntry.COLUMN_APPOINTMENT + "));";

        db.execSQL(SQL_CREATE_TYPES_TABLE);
        db.execSQL(SQL_CREATE_REASONS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_APPOINTMENTS_TABLE);
        db.execSQL(SQL_CREATE_PROPOSITIONS_TABLE);
        db.execSQL(SQL_CREATE_INVITATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            // Add here how to update from each version to the next
            default:
                // Very simple method of upgrading that consists on deleting
                // the previous tables and recreating them (not the best)
                db.execSQL("DROP TABLE IF EXISTS " + InvitationsEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + AppointmentsEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + PropositionsEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + UsersEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ReasonsEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + AppointmentTypesEntry.TABLE_NAME);
        }
    }
}