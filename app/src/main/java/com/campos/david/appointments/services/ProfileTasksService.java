package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.campos.david.appointments.model.UserManager;

import org.json.JSONException;


public class ProfileTasksService extends IntentService {
    private static final String TAG = ProfileTasksService.class.getSimpleName();

    public static final String ACTION_CHANGE_NAME = "change-name";

    public static final String EXTRA_NAME = "name";

    public ProfileTasksService() {
        super("ProfileTasksService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHANGE_NAME.equals(action)) {
                final String name = intent.getStringExtra(EXTRA_NAME);
                handleChangeName(name);
            }
        }
    }

    private void handleChangeName(String name) {
        ApiConnector connector = new ApiConnector(this);
        UserManager userManager = new UserManager(this);
        try {
            userManager.saveMe(connector.changeName(name));
        } catch (JSONException e) {
            Log.e(TAG, "Exception reading answer from name change", e);
        }
    }
}
