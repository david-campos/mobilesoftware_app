package com.campos.david.appointments.activitySettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.campos.david.appointments.LoginActivity;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.campos.david.appointments.services.ProfileTasksService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mSession = null;
    private Preference mUsername = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        final Context context = getActivity();
        if (context != null) {
            mSession = context.getSharedPreferences(getString(R.string.session_file_key), Context.MODE_PRIVATE);

            Preference closeSessionPreference = findPreference(getString(R.string.pref_close_session_key));
            closeSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressLint("CommitPrefEdits")
                public boolean onPreferenceClick(Preference preference) {
                    mSession.edit().clear().commit();
                    context.getContentResolver().delete(DBContract.SESSION_RELATED_DATA_URI, null, null);
                    Intent restartApp = new Intent(context.getApplicationContext(), LoginActivity.class);
                    restartApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(restartApp);
                    return true;
                }
            });

            mUsername = findPreference(getString(R.string.pref_username_key));
            mUsername.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent changeName = new Intent(context.getApplicationContext(), ProfileTasksService.class);
                    changeName.setAction(ProfileTasksService.ACTION_CHANGE_NAME);
                    changeName.putExtra(ProfileTasksService.EXTRA_NAME, (String) newValue);
                    context.startService(changeName);
                    return false;
                }
            });
            mUsername.setSummary(mSession.getString(getString(R.string.session_user_name_key), null));
            mSession.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.session_user_name_key))) {
            if (mUsername != null) {
                mUsername.setSummary(sharedPreferences.getString(key, null));
            }
        }
    }
}
