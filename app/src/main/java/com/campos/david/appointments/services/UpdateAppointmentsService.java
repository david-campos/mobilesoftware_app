package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.campos.david.appointments.model.AppointmentManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class UpdateAppointmentsService extends IntentService {
    private static final String TAG = UpdateAppointmentsService.class.getSimpleName();

    public UpdateAppointmentsService() {
        super(UpdateAppointmentsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ApiConnector connector = new ApiConnector(this);
        Parser parser = new Parser(this);
        AppointmentManager manager = new AppointmentManager(this);

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
    }
}
