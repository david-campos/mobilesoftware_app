package com.campos.david.appointments.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.campos.david.appointments.model.DBContract.InvitationsEntry;
import com.campos.david.appointments.model.DBContract.AppointmentsEntry;
import com.campos.david.appointments.model.DBContract.PropositionsEntry;
import com.campos.david.appointments.model.DBContract.UsersEntry;
import com.campos.david.appointments.model.DBContract.ReasonsEntry;
import com.campos.david.appointments.model.DBContract.AppointmentTypesEntry;

/**
 * Content provider for the database of the app. The possible Uri's for query are:
 * - /appointments/ Accesses all the appointments
 * - /appointments/{APPOINTMENT_ID} Accesses the appointment with the given _id
 * - /appointments/accepted/ Accesses the accepted appointments
 * - /appointments/pending/ Accesses the pending appointments
 * - /appointments/refused/ Accesses the refused appointments
 * - /users/ Accesses all the users
 * - /users/{USER_ID} Accesses the user with the given _id
 * - /users/invited_to/{APPOINTMENT_ID} Accesses all the user invited to the given appointment
 * - /propositions/{APPOINTMENT_ID} Accesses all the propositions for the given appointment
 * - /reasons/ Accesses all the reasons
 * - /appointment_types/ Accesses all the appointment types
 * - /invitations/ Accesses all the invitations
 * Possible Uri's for insert/update are:
 * - /appointments/ Inserts appointments
 * - /users/ Inserts users
 * - /propositions/ Inserts propositions
 * - /reasons/ Inserts reasons
 * - /appointment_types/ Inserts appointment types
 * - /invitations/ Inserts invitations
 */
public class ContentProvider extends android.content.ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = ContentProvider.class.getSimpleName();
    private DBHelper mOpenHelper;

    static final int APPOINTMENTS = 100;
    static final int APPOINTMENTS_ITEM = 101;
    static final int APPOINTMENTS_ACCEPTED = 110;
    static final int APPOINTMENTS_PENDING = 120;
    static final int APPOINTMENTS_REFUSED = 130;
    static final int USERS = 200;
    static final int USERS_ITEM = 201;
    static final int USERS_INVITED = 210;
    static final int PROPOSITIONS = 300;
    static final int PROPOSITIONS_INSERTION = 310;
    static final int REASONS = 400;
    static final int APPOINTMENT_TYPES = 500;
    static final int INVITATIONS = 600;

    static UriMatcher buildUriMatcher() {
        UriMatcher nUM = new UriMatcher(UriMatcher.NO_MATCH);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS, APPOINTMENTS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/#/", APPOINTMENTS_ITEM);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/accepted/", APPOINTMENTS_ACCEPTED);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/pending/", APPOINTMENTS_PENDING);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/refused/", APPOINTMENTS_REFUSED);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS, USERS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS + "/#/", USERS_ITEM);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS + "/invited_to/#/", USERS_INVITED);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_PROPOSITIONS + "/#/", PROPOSITIONS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_PROPOSITIONS, PROPOSITIONS_INSERTION);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_REASONS, REASONS);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENT_TYPES, APPOINTMENT_TYPES);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_INVITATIONS, INVITATIONS);
        return nUM;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        String tableName, groupBy=null, limit=null;

        switch(match) {
            case APPOINTMENTS:
                tableName = AppointmentsEntry.TABLE_NAME;
                break;
            case APPOINTMENTS_ITEM:
                tableName = AppointmentsEntry.TABLE_NAME;
                selection = AppointmentsEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                limit = "1";
                break;
            case APPOINTMENTS_ACCEPTED: {
                tableName = String.format(
                        "(%s LEFT JOIN %s ON (%s.%s = %s.%s)) LEFT JOIN %s ON (%s.%s = %s.%s)",
                        AppointmentsEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_APPOINTMENT,
                        PropositionsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                        PropositionsEntry.TABLE_NAME, PropositionsEntry._ID);
                String mySelection = String.format("%s.%s IS NULL OR ( %s.%s IS NULL AND %s.%s = 'accepted' )",
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CREATOR,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_USER,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_STATE);
                selection = selection!=null?
                        String.format(
                                "( %s ) AND ( %s )",
                                selection, mySelection):
                        mySelection;
                break; }
            case APPOINTMENTS_PENDING: {
                tableName = String.format(
                        "(%s JOIN %s ON (%s.%s = %s.%s)) LEFT JOIN %s ON (%s.%s = %s.%s)",
                        AppointmentsEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_APPOINTMENT,
                        PropositionsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                        PropositionsEntry.TABLE_NAME, PropositionsEntry._ID);
                String mySelection = String.format("%s.%s IS NULL AND %s.%s = 'pending'",
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_USER,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_STATE);
                selection = selection!=null?
                        String.format(
                                "( %s ) AND ( %s )",
                                selection, mySelection):
                        mySelection;
                break; }
            case APPOINTMENTS_REFUSED:{
                tableName = String.format(
                        "(%s JOIN %s ON (%s.%s = %s.%s)) LEFT JOIN %s ON (%s.%s = %s.%s)",
                        AppointmentsEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_APPOINTMENT,
                        PropositionsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                        PropositionsEntry.TABLE_NAME, PropositionsEntry._ID);
                String mySelection = String.format("%s.%s IS NULL AND %s.%s = 'refused'",
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_USER,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_STATE);
                selection = selection!=null?
                        String.format(
                            "( %s ) AND ( %s )",
                            selection, mySelection):
                        mySelection;
                break; }
            case USERS:
                tableName = UsersEntry.TABLE_NAME;
                break;
            case USERS_ITEM:
                tableName = UsersEntry.TABLE_NAME;
                selection = UsersEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                limit = "1";
                break;
            case USERS_INVITED:
                tableName = String.format(
                        "(%s LEFT JOIN %s ON (%s.%s = %s.%s)) LEFT JOIN %s ON (%s.%s = %s.%s)",
                        UsersEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        UsersEntry.TABLE_NAME, UsersEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry._ID,

                        AppointmentsEntry.TABLE_NAME,
                        UsersEntry.TABLE_NAME, UsersEntry._ID,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CREATOR);

                selection = InvitationsEntry.TABLE_NAME + "." + InvitationsEntry.COLUMN_APPOINTMENT +
                        " = ? OR " + AppointmentsEntry.TABLE_NAME + "." + AppointmentsEntry.COLUMN_CREATOR + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment(), uri.getLastPathSegment()};
                groupBy = UsersEntry.TABLE_NAME + "." + UsersEntry._ID;
                break;
            case PROPOSITIONS:
                tableName = PropositionsEntry.TABLE_NAME;
                selection = selection!=null?
                        String.format("( %s ) AND %s",
                                selection,
                                PropositionsEntry.COLUMN_APPOINTMENT + " = ?"):
                        PropositionsEntry.COLUMN_APPOINTMENT + " = ?";
                // We add the id to the selection args
                if(selectionArgs != null) {
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
                    newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
                    selectionArgs = newSelectionArgs;
                } else {
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }
                break;
            case REASONS:
                tableName = ReasonsEntry.TABLE_NAME;
                break;
            case APPOINTMENT_TYPES:
                tableName = AppointmentTypesEntry.TABLE_NAME;
                break;
            case INVITATIONS:
                tableName = InvitationsEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported.");
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(
                tableName,
                projection, selection, selectionArgs,
                groupBy, null, sortOrder, limit
        );
        Context ctx = getContext();
        if(ctx != null)
            cursor.setNotificationUri(ctx.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case APPOINTMENTS:
            case APPOINTMENTS_ACCEPTED:
            case APPOINTMENTS_PENDING:
            case APPOINTMENTS_REFUSED:
                return AppointmentsEntry.CONTENT_TYPE;
            case APPOINTMENTS_ITEM:
                return AppointmentsEntry.CONTENT_ITEM_TYPE;
            case USERS:
            case USERS_INVITED:
                return UsersEntry.CONTENT_TYPE;
            case USERS_ITEM:
                return UsersEntry.CONTENT_ITEM_TYPE;
            case PROPOSITIONS:
            case PROPOSITIONS_INSERTION:
                return PropositionsEntry.CONTENT_TYPE;
            case REASONS:
                return ReasonsEntry.CONTENT_TYPE;
            case APPOINTMENT_TYPES:
                return AppointmentTypesEntry.CONTENT_TYPE;
            case INVITATIONS:
                return InvitationsEntry.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported on querying.");
        }
    }

    private String[] insertGetTableAndUri(Uri uri) {
        String table, returnUri;
        switch(sUriMatcher.match(uri)) {
            case APPOINTMENTS:
                table = AppointmentsEntry.TABLE_NAME;
                returnUri = AppointmentsEntry.CONTENT_URI.toString();
                break;
            case USERS:
                table = UsersEntry.TABLE_NAME;
                returnUri = UsersEntry.CONTENT_URI.toString();
                break;
            case PROPOSITIONS_INSERTION:
                table = PropositionsEntry.TABLE_NAME;
                returnUri = PropositionsEntry.CONTENT_URI.toString();
                break;
            case REASONS:
                table = ReasonsEntry.TABLE_NAME;
                returnUri = ReasonsEntry.CONTENT_URI.toString();
                break;
            case APPOINTMENT_TYPES:
                table = AppointmentTypesEntry.TABLE_NAME;
                returnUri = AppointmentTypesEntry.CONTENT_URI.toString();
                break;
            case INVITATIONS:
                table = InvitationsEntry.TABLE_NAME;
                returnUri = InvitationsEntry.CONTENT_URI.toString();
                break;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported on insertion.");
        }
        return new String[]{table, returnUri};
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String[] tableAndUri = insertGetTableAndUri(uri);
        Long id;
        try {
            id = db.insertOrThrow(tableAndUri[0], null, values);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + values, e);
            return null;
        }
        Context ctx = getContext();
        Uri returnUri = Uri.parse(tableAndUri[1]);
        if(ctx != null) {
            ctx.getContentResolver().notifyChange(returnUri, null);
        }
        return Uri.withAppendedPath(returnUri, id.toString());
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int numInserted = 0;
        String[] tableAndUri = insertGetTableAndUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = db.insertOrThrow(tableAndUri[0], null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            Context ctx = getContext();
            if(ctx != null)
                ctx.getContentResolver().notifyChange(Uri.parse(tableAndUri[1]), null);
            numInserted = values.length;
        } finally {
            db.endTransaction();
        }
        return numInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        String table;
        Uri notificationUri;
        switch(match) {
            case APPOINTMENTS:
                table = AppointmentsEntry.TABLE_NAME;

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_APPOINTMENTS).build();
                break;
            case APPOINTMENTS_ITEM:
                table = AppointmentsEntry.TABLE_NAME;
                selection = AppointmentsEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_APPOINTMENTS).build();
                break;
            case USERS:
                table = UsersEntry.TABLE_NAME;

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_USERS).build();
                break;
            case USERS_ITEM:
                table = UsersEntry.TABLE_NAME;
                selection = UsersEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_USERS).build();
                break;
            case PROPOSITIONS:
                table = PropositionsEntry.TABLE_NAME;
                selection = selection!=null?
                        String.format("( %s ) AND %s",
                                selection,
                                PropositionsEntry.COLUMN_APPOINTMENT + " = ?"):
                        PropositionsEntry.COLUMN_APPOINTMENT + " = ?";
                if(selectionArgs != null) {
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
                    newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
                    selectionArgs = newSelectionArgs;
                } else {
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_PROPOSITIONS).build();
                break;
            case REASONS:
                table = ReasonsEntry.TABLE_NAME;
                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_REASONS).build();
                break;
            case APPOINTMENT_TYPES:
                table = AppointmentTypesEntry.TABLE_NAME;

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_APPOINTMENT_TYPES).build();
                break;
            case INVITATIONS:
                table = InvitationsEntry.TABLE_NAME;

                notificationUri = Uri.parse(DBContract.CONTENT_AUTHORITY).buildUpon()
                        .appendPath(DBContract.PATH_INVITATIONS).build();
                break;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported.");
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numRows =  db.delete(table, selection, selectionArgs);
        Context ctx = getContext();
        if(ctx != null) {
            ctx.getContentResolver().notifyChange(notificationUri, null);
        }
        return numRows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        String table;
        switch(match) {
            case APPOINTMENTS:
                table = AppointmentsEntry.TABLE_NAME;
                break;
            case APPOINTMENTS_ITEM:
                table = AppointmentsEntry.TABLE_NAME;
                selection = AppointmentsEntry._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case USERS:
                table = UsersEntry.TABLE_NAME;
                break;
            case USERS_ITEM:
                table = UsersEntry.TABLE_NAME;
                selection = UsersEntry._ID + "=?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case PROPOSITIONS:
                table = PropositionsEntry.TABLE_NAME;
                selection = selection!=null?
                        String.format("( %s ) AND %s",
                                selection,
                                PropositionsEntry.COLUMN_APPOINTMENT + " = ?"):
                        PropositionsEntry.COLUMN_APPOINTMENT + " = ?";
                if(selectionArgs != null) {
                    String[] newSelectionArgs = new String[selectionArgs.length+1];
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
                    newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();
                    selectionArgs = newSelectionArgs;
                } else {
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }
                break;
            case REASONS:
                table = ReasonsEntry.TABLE_NAME;
                break;
            case APPOINTMENT_TYPES:
                table = AppointmentTypesEntry.TABLE_NAME;
                break;
            case INVITATIONS:
                table = InvitationsEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported.");
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numRows = db.updateWithOnConflict(table, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_IGNORE);
        Context ctx = getContext();
        if(ctx != null) {
            ctx.getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
