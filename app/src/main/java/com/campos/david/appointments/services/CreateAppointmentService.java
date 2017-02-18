package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateAppointmentService extends IntentService {
    private static final String TAG = CreateAppointmentService.class.getSimpleName();


    private ApiConnector mApiConnector = null;

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
        mApiConnector = new ApiConnector(this);
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
                json = mApiConnector.createAppointment(invitations, name, description, closed,
                        type, timestamp, place, latitude, longitude);
            } catch (ApiConnector.ApiError e) {
                // On API error ignore, so the app doesn't crash
                // maybe in the future would be better to send some kind of broadcast
                // to show the user a toast or something
                Log.e(TAG, "Error in API", e);
                return;
            }

            // Reading answer
            if (json != null) {
                // Insert appointment
                ContentValues appointment = getAppointmentFrom(json);
                if (appointment == null || getContentResolver().insert(
                        DBContract.AppointmentsEntry.CONTENT_URI, appointment) == null) {
                    return; // Some problem parsing or inserting appointment
                }

                // Insert proposition
                ContentValues proposition = getInitialPropositionFrom(json);
                Uri propositionUri = getContentResolver().insert(
                        DBContract.PropositionsEntry.CONTENT_URI, proposition);
                if (propositionUri != null) {
                    int propositionId = Integer.parseInt(propositionUri.getLastPathSegment());
                    if (proposition != null && propositionId != -1) {
                        // The proposition is the current one for the appointment
                        ContentValues currentProposalCv = new ContentValues(1);
                        currentProposalCv.put(
                                DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, propositionId);
                        getContentResolver().update(
                                DBContract.AppointmentsEntry.CONTENT_URI, currentProposalCv,
                                DBContract.AppointmentsEntry._ID + "=?",
                                new String[]{appointment.getAsString(DBContract.AppointmentsEntry._ID)});
                    }
                }
                ContentValues[] invitationsCvs = getInvitationsFrom(json);
                if (invitationsCvs == null) {
                    return; // Some problem parsing invitations
                }
                getContentResolver().bulkInsert(DBContract.InvitationsEntry.CONTENT_URI, invitationsCvs);
            }
            // TODO: save the request to try again later if null
        }
    }

    private ContentValues getAppointmentFrom(@NonNull JSONObject json) {
        ContentValues cv = new ContentValues();
        try {
            cv.put(DBContract.AppointmentsEntry.COLUMN_NAME, json.getString("name"));
            cv.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, json.getString("description"));
            cv.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, json.getBoolean("closed"));
            cv.put(DBContract.AppointmentsEntry._ID, json.getInt(getString(R.string.response_id)));
            int typeId = UpdateTypesAndReasonsService
                    .lookForAppointmentTypeId(this, json.getString("typeName"));
            if (typeId == 0) return null; // Couldn't find type
            cv.put(DBContract.AppointmentsEntry.COLUMN_TYPE, typeId);
            cv.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, (Integer) null);
            cv.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, 0); // It can't be null
            return cv;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing appointment", e);
            return null;
        }
    }

    private ContentValues getInitialPropositionFrom(@NonNull JSONObject json) {
        ContentValues cv = new ContentValues();
        try {
            JSONObject proposition = json.getJSONObject("currentProposition");
            cv.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP, proposition.getLong("time"));
            JSONObject coordinates = proposition.getJSONObject("coordinates");
            cv.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, coordinates.getDouble("lat"));
            cv.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, coordinates.getDouble("lon"));
            cv.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, proposition.getString("placeName"));
            cv.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, proposition.getInt("appointment"));
            cv.put(DBContract.PropositionsEntry.COLUMN_REASON, (Integer) null);
            cv.put(DBContract.PropositionsEntry.COLUMN_CREATOR, (Integer) null);
            return cv;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing proposition", e);
            return null;
        }
    }

    private ContentValues[] getInvitationsFrom(@NonNull JSONObject json) {
        try {
            int appointmentId = json.getInt(getString(R.string.response_id));
            JSONArray invitations = json.getJSONArray("invitations");
            ContentValues[] cvs = new ContentValues[invitations.length()];
            JSONObject aInvitation;
            for (int i = 0; i < invitations.length(); i++) {
                aInvitation = invitations.getJSONObject(i);
                cvs[i] = new ContentValues();
                JSONObject invitedUser = aInvitation.getJSONObject("user");
                insertUserIfNotExistent(invitedUser);
                cvs[i].put(DBContract.InvitationsEntry.COLUMN_USER,
                        invitedUser.getInt(getString(R.string.response_id)));
                cvs[i].put(DBContract.InvitationsEntry.COLUMN_STATE,
                        aInvitation.getString("state"));
                // If reasonName is null it is not necessary to put anything in the CV,
                // as the DEFAULT value is NULL
                if (!aInvitation.isNull("reasonName")) {
                    int reasonId = UpdateTypesAndReasonsService
                            .lookForReasonOrInsert(this,
                                    aInvitation.getString("reasonName"), aInvitation.getString("reasonDescription"));
                    if (reasonId != 0) {
                        cvs[i].put(DBContract.InvitationsEntry.COLUMN_REASON, reasonId);
                    }
                }
                cvs[i].put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, appointmentId);
            }
            return cvs;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing invitations", e);
            return null;
        }
    }

    private void insertUserIfNotExistent(JSONObject user) throws JSONException {
        int id = user.getInt(getString(R.string.response_id));
        Cursor c = getContentResolver().query(DBContract.UsersEntry.CONTENT_URI,
                new String[]{DBContract.UsersEntry._ID}, DBContract.UsersEntry._ID + "=?",
                new String[]{Integer.toString(id)}, null);
        if (c == null)
            throw new NullPointerException("The cursor shouldn't be null");
        try {
            if (!c.moveToFirst()) {
                getContentResolver().insert(DBContract.UsersEntry.CONTENT_URI,
                        mApiConnector.userJsonToContentValues(user));
            }
        } finally {
            c.close();
        }
    }
}
