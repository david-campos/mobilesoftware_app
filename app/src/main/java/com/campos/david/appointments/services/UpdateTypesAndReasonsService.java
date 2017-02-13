package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
}
