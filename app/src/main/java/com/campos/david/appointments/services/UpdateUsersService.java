package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.campos.david.appointments.CountryCodes;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.campos.david.appointments.model.UserManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class UpdateUsersService extends IntentService {
    private static final String TAG = UpdateUsersService.class.getSimpleName();

    public UpdateUsersService() {
        super("UpdateUsersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        usersUpdate(this, true);
    }

    public static void usersUpdate(Context ctx) {
        usersUpdate(ctx, false);
    }

    public static void usersUpdate(Context ctx, boolean includeFromDatabase) {
        ArrayList<String> phones = new ArrayList<>();
        Cursor c = ctx.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                ContactsContract.Data.MIMETYPE + "= ?",
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                null);
        if (c != null) {
            TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            String localPrefix = null;
            String commonIDD = ctx.getString(R.string.common_IDD_for_international_calls);
            if (manager != null) {
                localPrefix = "+" + CountryCodes.getCode(manager.getSimCountryIso());
            }
            while (c.moveToNext()) {
                String phone = c.getString(0).replaceAll("[ \\.\\-\\(\\)]", "");
                // Attempt to check if it has prefix (not very useful)
                if (localPrefix != null && !phone.startsWith("+")) {
                    if (phone.startsWith(commonIDD)) {
                        phone = "+" + phone.substring(commonIDD.length());
                    } else {
                        phone = localPrefix + phone;
                    }
                }
                if (!phones.contains(phone)) {
                    phones.add(phone);
                }
            }
            c.close();
        }

        // Add the saved users
        if (includeFromDatabase) {
            Cursor cursor = ctx.getContentResolver().query(
                    DBContract.UsersEntry.CONTENT_URI,
                    new String[]{DBContract.UsersEntry.COLUMN_PHONE},
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    if (!phones.contains(phone)) {
                        phones.add(phone);
                    }
                }
                cursor.close();
            }
        }

        if (phones.size() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            String nowUtc = ctx.getString(R.string.api_timestamp_format_detailed,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));


            ContentValues[] users = (new ApiConnector(ctx)).filterUsers(phones);
            if (users != null) {
                (new UserManager(ctx)).insertUsers(users);
                // Last update time
                ctx.getSharedPreferences(ctx.getString(R.string.session_file_key), Context.MODE_PRIVATE)
                        .edit()
                        .putString(ctx.getString(R.string.session_users_last_update), nowUtc)
                        .commit();
            } else {
                throw new NullPointerException("Filtered users is null (not empty).");
            }
        }
    }
}
