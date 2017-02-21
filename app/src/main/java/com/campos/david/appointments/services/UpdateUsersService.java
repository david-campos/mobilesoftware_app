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
import com.campos.david.appointments.model.UserManager;

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
            CountryCodes countryCodes = new CountryCodes(true);

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
                phones.add(phone);
            }

            c.close();
            ContentValues[] users = (new ApiConnector(ctx)).filterUsers(phones);
            if (users != null) {
                (new UserManager(ctx)).insertUsers(users);
            } else {
                throw new NullPointerException("Filtered users is null (not empty).");
            }
        }
    }
}
