package com.campos.david.appointments.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.campos.david.appointments.model.DBContract.AppointmentTypesEntry;
import com.campos.david.appointments.model.DBContract.AppointmentsEntry;
import com.campos.david.appointments.model.DBContract.InvitationsEntry;
import com.campos.david.appointments.model.DBContract.PropositionsEntry;
import com.campos.david.appointments.model.DBContract.ReasonsEntry;
import com.campos.david.appointments.model.DBContract.UsersEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Content provider for the database of the app. The possible Uri's for query are:
 * - /appointments/ Accesses all the appointments
 * - /appointments/{APPOINTMENT_ID} Accesses the appointment with the given _id  (and the current proposal)
 * - /appointments/invited/{APPOINTMENT_ID} Accesses the appointment with the given _id
 *      joining it to the user of the application invitation if he is invited to it (and the current proposal)
 * - /appointments/accepted/ Accesses the accepted appointments
 * - /appointments/pending/ Accesses the pending appointments
 * - /appointments/refused/ Accesses the refused appointments
 * - /users/ Accesses all the users
 * - /users/{USER_ID} Accesses the user with the given _id
 * - /users/invited_to/{APPOINTMENT_ID} Accesses all the user invited to the given appointment
 * - /users/with/ Gets a join between invitations and users invited and merges the cursor with
 *      a selection of appointments to check the creator. In both cases the first returned column
 *      is named "row_type" and indicates the type we are in.
 * - /propositions/{APPOINTMENT_ID} Accesses all the propositions for the given appointment
 * - /reasons/ Accesses all the reasons
 * - /appointment_types/ Accesses all the appointment types
 * - /invitations/ Accesses all the invitations
 * Possible Uri's for insert/update/delete are:
 * - /appointments/ Inserts appointments
 * - /users/ Inserts users
 * - /propositions/ Inserts propositions
 * - /reasons/ Inserts reasons
 * - /appointment_types/ Inserts appointment types
 * - /invitations/ Inserts invitations
 * For delete is also possible:
 * - /session_related_data/ erases all the tables that should be attached to one session
 */
public class ContentProvider extends android.content.ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = ContentProvider.class.getSimpleName();
    private DBHelper mOpenHelper;

    static final int APPOINTMENTS = 100;
    static final int APPOINTMENTS_ITEM = 101;
    static final int APPOINTMENTS_ITEM_WITH_INVITATION = 102;
    static final int APPOINTMENTS_ACCEPTED = 110;
    static final int APPOINTMENTS_PENDING = 120;
    static final int APPOINTMENTS_REFUSED = 130;
    static final int USERS = 200;
    static final int USERS_ITEM = 201;
    static final int USERS_INVITED = 210;
    static final int USERS_WITH = 220;
    static final int PROPOSITIONS = 300;
    static final int PROPOSITIONS_INSERTION = 310;
    static final int REASONS = 400;
    static final int APPOINTMENT_TYPES = 500;
    static final int INVITATIONS = 600;
    static final int SESSION_RELATED_DATA = 700;

    static UriMatcher buildUriMatcher() {
        UriMatcher nUM = new UriMatcher(UriMatcher.NO_MATCH);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS, APPOINTMENTS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/#", APPOINTMENTS_ITEM);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/invited/#", APPOINTMENTS_ITEM_WITH_INVITATION);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/accepted", APPOINTMENTS_ACCEPTED);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/pending", APPOINTMENTS_PENDING);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENTS + "/refused", APPOINTMENTS_REFUSED);


        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS, USERS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS + "/#", USERS_ITEM);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS + "/invited_to/#", USERS_INVITED);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_USERS + "/with", USERS_WITH);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_PROPOSITIONS + "/#", PROPOSITIONS);
        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_PROPOSITIONS, PROPOSITIONS_INSERTION);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_REASONS, REASONS);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_APPOINTMENT_TYPES, APPOINTMENT_TYPES);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, DBContract.PATH_INVITATIONS, INVITATIONS);

        nUM.addURI(DBContract.CONTENT_AUTHORITY, "session_related_data", SESSION_RELATED_DATA);
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
                tableName = String.format("%s LEFT JOIN %s ON (%s.%s = %s.%s)",
                        AppointmentsEntry.TABLE_NAME, PropositionsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                        PropositionsEntry.TABLE_NAME, PropositionsEntry._ID);
                selection = AppointmentsEntry.TABLE_NAME + "." + AppointmentsEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                limit = "1";
                break;
            case APPOINTMENTS_ITEM_WITH_INVITATION:
                tableName = String.format("(%s JOIN %s ON (%s.%s = %s.%s)) LEFT JOIN %s ON (%s.%s = %s.%s)",
                        InvitationsEntry.TABLE_NAME, AppointmentsEntry.TABLE_NAME,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_APPOINTMENT,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry._ID,
                        PropositionsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CURRENT_PROPOSAL,
                        PropositionsEntry.TABLE_NAME, PropositionsEntry._ID);
                selection = AppointmentsEntry.TABLE_NAME + "." + AppointmentsEntry._ID + "=? AND " +
                        InvitationsEntry.TABLE_NAME + "." + InvitationsEntry.COLUMN_USER + " IS NULL";
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
                String appointmentId = uri.getLastPathSegment();

                String[] newProjection = new String[projection.length + 1];
                System.arraycopy(projection, 0, newProjection, 0, projection.length);
                newProjection[projection.length] = String.format(
                        "CASE WHEN %s.%s = %s THEN 0 ELSE 1 END is_creator",
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_APPOINTMENT,
                        appointmentId
                );
                projection = newProjection; // Exchange!

                tableName = String.format(
                        "((%s LEFT JOIN (%s LEFT JOIN %s ON (%s.%s = %s.%s)) ON (%s.%s = %s.%s))) " +
                                "LEFT JOIN %s ON (%s.%s = %s.%s)",
                        UsersEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        ReasonsEntry.TABLE_NAME,
                        ReasonsEntry.TABLE_NAME, ReasonsEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_REASON,

                        UsersEntry.TABLE_NAME, UsersEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_USER,

                        AppointmentsEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CREATOR,
                        UsersEntry.TABLE_NAME, UsersEntry._ID);
                selection = InvitationsEntry.TABLE_NAME + "." + InvitationsEntry.COLUMN_APPOINTMENT + " = ? " +
                        "OR " + AppointmentsEntry.TABLE_NAME + "." + AppointmentsEntry._ID + " =? ";
                selectionArgs = new String[]{appointmentId, appointmentId};
                groupBy = UsersEntry.TABLE_NAME + "." + UsersEntry._ID;
                break;
            case USERS_WITH:
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                Cursor[] cursors = new Cursor[2];

                tableName = String.format(
                        "%s JOIN %s ON (%s.%s = %s.%s)",
                        AppointmentsEntry.TABLE_NAME, UsersEntry.TABLE_NAME,
                        AppointmentsEntry.TABLE_NAME, AppointmentsEntry.COLUMN_CREATOR,
                        UsersEntry.TABLE_NAME, UsersEntry._ID);
                projection = UsersEntry.WITHS_PROJECTION_APPOINTMENTS;
                projection[0] = "'creator' AS row_type";
                cursors[0] = db.query(
                        tableName,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);
                tableName = String.format(
                        "%s JOIN %s ON (%s.%s = %s.%s)",
                        UsersEntry.TABLE_NAME, InvitationsEntry.TABLE_NAME,
                        UsersEntry.TABLE_NAME, UsersEntry._ID,
                        InvitationsEntry.TABLE_NAME, InvitationsEntry.COLUMN_USER);
                projection = UsersEntry.WITHS_PROJECTION_INVITATIONS;
                projection[0] = "'invited' AS row_type";
                cursors[1] = db.query(
                        tableName,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);

                MergeCursor cursor = new MergeCursor(cursors);
                Context ctx = getContext();
                if (ctx != null) {
                    cursor.setNotificationUri(ctx.getContentResolver(), uri);
                }
                return cursor; // Doesn't get to the end!
            case PROPOSITIONS:
                tableName = "(" + PropositionsEntry.TABLE_NAME + " JOIN " + UsersEntry.TABLE_NAME +
                        " ON (" +
                        PropositionsEntry.TABLE_NAME + "." + PropositionsEntry.COLUMN_CREATOR +
                        " = " + UsersEntry.TABLE_NAME + "." + UsersEntry._ID + ")" +
                        ") JOIN " + ReasonsEntry.TABLE_NAME + " ON (" +
                        ReasonsEntry.TABLE_NAME + "." + ReasonsEntry._ID + "=" +
                        PropositionsEntry.TABLE_NAME + "." + PropositionsEntry.COLUMN_REASON + ")";
                selection = selection!=null?
                        String.format("( %s ) AND %s",
                                selection,
                                PropositionsEntry.TABLE_NAME + "." + PropositionsEntry.COLUMN_APPOINTMENT + " = ?") :
                        PropositionsEntry.TABLE_NAME + "." + PropositionsEntry.COLUMN_APPOINTMENT + " = ?";
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
        // Please note that USERS_WITH is a special case
        // that never gets here!! (it has to do a merge of cursors)
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
                return AppointmentsEntry.CONTENT_BASIC_TYPE;
            case APPOINTMENTS_ACCEPTED:
            case APPOINTMENTS_PENDING:
            case APPOINTMENTS_REFUSED:
                return AppointmentsEntry.CONTENT_TYPE;
            case APPOINTMENTS_ITEM:
                return AppointmentsEntry.CONTENT_ITEM_TYPE;
            case APPOINTMENTS_ITEM_WITH_INVITATION:
                return AppointmentsEntry.CONTENT_ITEM_WITH_INVITATION_TYPE;
            case USERS:
            case USERS_INVITED:
                return UsersEntry.CONTENT_TYPE;
            case USERS_WITH:
                return UsersEntry.CONTENT_WITH_TYPE;
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

    public List<Uri> getNotifiedUris(Uri uri) {
        List<Uri> list = new ArrayList<>();
        switch (sUriMatcher.match(uri)) {
            case APPOINTMENTS:
            case APPOINTMENTS_ITEM:
                list.add(AppointmentsEntry.CONTENT_URI);
                break;
            case USERS:
            case USERS_ITEM:
                list.add(UsersEntry.CONTENT_URI);
                break;
            case PROPOSITIONS:
                list.add(PropositionsEntry.CONTENT_URI);
                break;
            case REASONS:
                list.add(ReasonsEntry.CONTENT_URI);
                break;
            case APPOINTMENT_TYPES:
                list.add(AppointmentTypesEntry.CONTENT_URI);
                break;
            case INVITATIONS:
                list.add(InvitationsEntry.CONTENT_URI);
                list.add(AppointmentsEntry.CONTENT_URI.buildUpon().appendPath("invited").build());
                list.add(AppointmentsEntry.CONTENT_ACCEPTED_URI);
                list.add(AppointmentsEntry.CONTENT_REFUSED_URI);
                list.add(AppointmentsEntry.CONTENT_PENDING_URI);
                list.add(UsersEntry.CONTENT_URI.buildUpon().appendPath("invited_to").build());
                list.add(Uri.withAppendedPath(DBContract.UsersEntry.CONTENT_URI, "with"));
                break;
        }
        return list;
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
            for (Uri notifyUri : getNotifiedUris(uri)) {
                ctx.getContentResolver().notifyChange(notifyUri, null);
            }
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
                long newID = db.insertWithOnConflict(tableAndUri[0], null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            Context ctx = getContext();
            if (ctx != null) {
                for (Uri notifyUri : getNotifiedUris(uri)) {
                    ctx.getContentResolver().notifyChange(notifyUri, null);
                }
            }
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
        switch(match) {
            case APPOINTMENTS:
                table = AppointmentsEntry.TABLE_NAME;
                break;
            case APPOINTMENTS_ITEM:
                table = AppointmentsEntry.TABLE_NAME;
                selection = AppointmentsEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case USERS:
                table = UsersEntry.TABLE_NAME;
                break;
            case USERS_ITEM:
                table = UsersEntry.TABLE_NAME;
                selection = UsersEntry._ID + " = ?";
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
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
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
            case SESSION_RELATED_DATA:
                int deleted = 0;
                deleted += delete(AppointmentsEntry.CONTENT_URI, null, null);
                deleted += delete(PropositionsEntry.CONTENT_URI, null, null);
                deleted += delete(InvitationsEntry.CONTENT_URI, null, null);
                deleted += delete(UsersEntry.CONTENT_URI, null, null);
                return deleted;
            default:
                throw new IllegalArgumentException("Uri '" + uri.toString() + "' not supported.");
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numRows =  db.delete(table, selection, selectionArgs);
        Context ctx = getContext();
        if(ctx != null) {
            for (Uri notifyUri : getNotifiedUris(uri)) {
                ctx.getContentResolver().notifyChange(notifyUri, null);
            }
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
            for (Uri notifyUri : getNotifiedUris(uri)) {
                ctx.getContentResolver().notifyChange(notifyUri, null);
            }
        }
        return numRows;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
