package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.campos.david.appointments.model.UserManager;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileTasksService extends IntentService {
    private static final String TAG = ProfileTasksService.class.getSimpleName();

    public static final String ACTION_CHANGE_NAME = "change-name";
    public static final String ACTION_BLOCK_USER = "block-user";
    public static final String ACTION_UNBLOCK_USER = "unblock-user";

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PHONE = "phone";

    public ProfileTasksService() {
        super("ProfileTasksService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            ApiConnector connector = new ApiConnector(this);
            UserManager userManager = new UserManager(this);

            JSONObject me;
            if (ACTION_CHANGE_NAME.equals(action)) {
                final String name = intent.getStringExtra(EXTRA_NAME);
                me = connector.changeName(name);
            } else if (ACTION_BLOCK_USER.equals(action)) {
                final String phone = intent.getStringExtra(EXTRA_PHONE);
                me = connector.blockUser(phone);
            } else if (ACTION_UNBLOCK_USER.equals(action)) {
                final String phone = intent.getStringExtra(EXTRA_PHONE);
                me = connector.unblockUser(phone);
            } else {
                return;
            }

            try {
                userManager.saveMe(me);
            } catch (JSONException e) {
                Log.e(TAG, "Exception reading answer from profile request", e);
            }
        }
    }
}
