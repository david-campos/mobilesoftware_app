package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.AppointmentManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class UpdateAppointmentsService extends IntentService {
    private static final String TAG = UpdateAppointmentsService.class.getSimpleName();

    public UpdateAppointmentsService() {
        super(UpdateAppointmentsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            ApiConnector connector = new ApiConnector(this);
            Parser parser = new Parser(this);
            AppointmentManager manager = new AppointmentManager(this);

            // To save the last-update time
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            String nowUtc = getString(R.string.api_timestamp_format_detailed,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

            JSONObject[] appointments = connector.getAppointments();
            ContentValues[] appCvs = new ContentValues[appointments.length];
            ContentValues[] currentProposalCvs = new ContentValues[appointments.length];
            ArrayList<ContentValues> invitationsCvs = new ArrayList<>();
            for (int i = 0; i < appointments.length; i++) {
                appCvs[i] = parser.getAppointmentFrom(appointments[i]);
                invitationsCvs.addAll(Arrays.asList(parser.getInvitationsFrom(appointments[i])));
                if (parser.hasCurrentProposal(appointments[i])) {
                    currentProposalCvs[i] = parser.getCurrentPropositionFrom(appointments[i]);
                }
            }
            ContentValues[] invitationsCvsArray = new ContentValues[invitationsCvs.size()];
            invitationsCvsArray = invitationsCvs.toArray(invitationsCvsArray);
            manager.appointmentInsertion(appCvs, invitationsCvsArray, currentProposalCvs);

            // Last update time saving
            getSharedPreferences(getString(R.string.session_file_key), Context.MODE_PRIVATE)
                    .edit()
                    .putString(getString(R.string.session_appointments_last_update), nowUtc)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Connection failed", e);
        }
    }
}
