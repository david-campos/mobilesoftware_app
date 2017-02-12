package com.campos.david.appointments.activitySettings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.campos.david.appointments.LoginActivity;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference closeSessionPreference = findPreference(getString(R.string.pref_close_session_key));
        closeSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Context context = getActivity();
                if (context != null) {
                    SharedPreferences session = context.getSharedPreferences(
                            getString(R.string.session_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = session.edit();
                    editor.clear();
                    editor.commit();
                    context.getContentResolver().delete(DBContract.SESSION_RELATED_DATA_URI, null, null);
                    Intent restartApp = new Intent(context.getApplicationContext(), LoginActivity.class);
                    restartApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(restartApp);
                    return true;
                }
                return false;
            }
        });
    }
}
