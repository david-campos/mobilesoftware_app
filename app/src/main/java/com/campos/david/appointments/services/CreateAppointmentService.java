package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.campos.david.appointments.model.AppointmentManager;

import org.json.JSONObject;

public class CreateAppointmentService extends IntentService {
    private static final String TAG = CreateAppointmentService.class.getSimpleName();

    public interface ExtrasKeys {
        String PLACE = "place";
        String LAT = "lat";
        String LON = "lon";
        String TIMESTAMP = "timestamp";
        String CLOSED = "closed";
        String TYPE = "type";
        String DESCRIPTION = "desc";
        String NAME = "name";
        String USERS = "users";
    }

    public CreateAppointmentService() {
        super(CreateAppointmentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ApiConnector apiConnector = new ApiConnector(this);
        if (intent != null) {
            String[] invitations = intent.getStringArrayExtra(ExtrasKeys.USERS);
            String name = intent.getStringExtra(ExtrasKeys.NAME);
            String description = intent.getStringExtra(ExtrasKeys.DESCRIPTION);
            boolean closed = intent.getBooleanExtra(ExtrasKeys.CLOSED, false);
            String type = intent.getStringExtra(ExtrasKeys.TYPE);
            String timestamp = intent.getStringExtra(ExtrasKeys.TIMESTAMP);
            String place = intent.getStringExtra(ExtrasKeys.PLACE);
            double latitude = intent.getDoubleExtra(ExtrasKeys.LAT, 0.0);
            double longitude = intent.getDoubleExtra(ExtrasKeys.LON, 0.0);

            // Creating appointment
            JSONObject json;
            try {
                json = apiConnector.createAppointment(invitations, name, description, closed,
                        type, timestamp, place, latitude, longitude);

                // Reading answer
                Parser parser = new Parser(this);
                if (json != null) {
                    ContentValues appCvs = parser.getAppointmentFrom(json);
                    ContentValues currentProposalCvs = parser.getCurrentPropositionFrom(json);
                    ContentValues[] invitationsCvs = parser.getInvitationsFrom(json);

                    (new AppointmentManager(this)).appointmentInsertion(appCvs, invitationsCvs, currentProposalCvs);
                }
                // TODO: save the request to try again later if it fails
            } catch (ApiConnector.ApiError e) {
                // On API error ignore, so the app doesn't crash
                // maybe in the future would be better to send some kind of broadcast
                // to show the user a toast or something
                Log.e(TAG, "Error in API", e);
            }
        }
    }
}
