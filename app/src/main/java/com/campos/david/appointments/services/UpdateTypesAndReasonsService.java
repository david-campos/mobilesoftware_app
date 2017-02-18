package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;

import com.campos.david.appointments.model.DBContract;

public class UpdateTypesAndReasonsService extends IntentService {
    private static final String TAG = UpdateTypesAndReasonsService.class.getSimpleName();

    public UpdateTypesAndReasonsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        typesAndReasonsUpdate(this);
    }

    public static void typesAndReasonsUpdate(Context ctx) {
        Pair<ContentValues[], ContentValues[]> typesAndReasons = (new ApiConnector(ctx))
                .getAppointmentTypesAndReasons();
        if (typesAndReasons != null) {
            if (typesAndReasons.first != null) {
                int inserted = ctx.getContentResolver().bulkInsert(
                        DBContract.AppointmentTypesEntry.CONTENT_URI,
                        typesAndReasons.first);
                Log.d(ctx.getClass().getSimpleName(), "Inserted " + inserted + " types of appointments.");
            } else {
                throw new NullPointerException("Types is null");
            }
            if (typesAndReasons.second != null) {
                int inserted = ctx.getContentResolver().bulkInsert(
                        DBContract.ReasonsEntry.CONTENT_URI,
                        typesAndReasons.second);
                Log.d(ctx.getClass().getSimpleName(), "Inserted " + inserted + " reasons.");
            } else {
                throw new NullPointerException("Reasons is null");
            }
        } else {
            throw new NullPointerException("Pair of types and reasons is null.");
        }
    }

    public static int lookForAppointmentTypeId(Context context, String appointmentTypeName) {
        String[] projection = new String[]{DBContract.AppointmentTypesEntry._ID};
        String where = DBContract.AppointmentsEntry.COLUMN_NAME + "=?";
        String[] params = new String[]{appointmentTypeName};
        // Look for the appointment type in local database
        Cursor c = context.getContentResolver().query(DBContract.AppointmentTypesEntry.CONTENT_URI,
                projection, where, params, null);
        if (c == null) {
            throw new NullPointerException(); // Shouldn't happen
        }
        if (!c.moveToFirst()) {
            try {
                UpdateTypesAndReasonsService.typesAndReasonsUpdate(context);
            } catch (NullPointerException e) {
                // No connection? Return 0
                return 0;
            }
            c.close();
            c = context.getContentResolver().query(DBContract.AppointmentTypesEntry.CONTENT_URI,
                    projection, where, params, null);
            if (c == null) {
                throw new NullPointerException(); // Shouldn't happen
            }
            if (!c.moveToFirst()) {
                throw new IllegalArgumentException("Appointment type doesn't exist");
            }
        }
        int id = c.getInt(0);
        c.close();
        return id;
    }

    public static int lookForReasonOrInsert(Context context, String name, String description) {
        if (name == null || description == null) {
            return 0;
        }
        String[] projection = new String[]{DBContract.ReasonsEntry._ID};
        String where = DBContract.ReasonsEntry.COLUMN_NAME + "=?";
        String[] params = new String[]{name};
        // Look for the appointment type in local database
        Cursor c = context.getContentResolver().query(DBContract.ReasonsEntry.CONTENT_URI,
                projection, where, params, null);
        if (c == null) {
            throw new NullPointerException(); // Shouldn't happen
        }
        int id;
        if (c.moveToFirst()) {
            id = c.getInt(0);
        } else {
            c.close();
            ContentValues cv = new ContentValues();
            cv.put(DBContract.ReasonsEntry.COLUMN_NAME, name);
            cv.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION, description);
            Uri uri = context.getContentResolver()
                    .insert(DBContract.ReasonsEntry.CONTENT_URI, cv);
            if (uri == null) {
                throw new NullPointerException(); // Shouldn't happen
            }
            id = Integer.parseInt(uri.getLastPathSegment());
        }
        return id;
    }
}
