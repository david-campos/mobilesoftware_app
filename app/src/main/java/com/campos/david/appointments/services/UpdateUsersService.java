package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.campos.david.appointments.model.DBContract;

import java.util.ArrayList;

public class UpdateUsersService extends IntentService {
    private static final String TAG = UpdateUsersService.class.getSimpleName();

    public UpdateUsersService() {
        super("UpdateUsersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        usersUpdate(this);
    }

    public static void usersUpdate(Context ctx) {
        Cursor c = ctx.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                ContactsContract.Data.MIMETYPE + "= ?",
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                null);
        if (c != null) {
            ArrayList<String> phones = new ArrayList<>();
            while (c.moveToNext()) {
                phones.add(c.getString(0).replaceAll("[ \\.\\-\\(\\)]", ""));
            }
            c.close();
            ContentValues[] users = (new ApiConnector(ctx)).filterUsers(phones);
            if (users != null) {
                int inserted = ctx.getContentResolver().bulkInsert(DBContract.UsersEntry.CONTENT_URI, users);
                Log.d(ctx.getClass().getSimpleName(), "Inserted " + inserted + " users.");
            } else {
                throw new NullPointerException("Filtered users is null (not empty).");
            }
        }
    }
}
