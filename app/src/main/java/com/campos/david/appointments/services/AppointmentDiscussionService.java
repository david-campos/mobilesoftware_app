package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.AppointmentManager;
import com.campos.david.appointments.model.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class AppointmentDiscussionService extends IntentService {
    private static final String TAG = AppointmentDiscussionService.class.getSimpleName();

    public static final String ACTION_OPEN = "open-discussion";
    public static final String ACTION_CLOSE = "close-discussion";
    public static final String ACTION_REFUSE = "refuse-invitation";
    public static final String ACTION_ACCEPT = "accept-invitation";
    public static final String ACTION_SET_PENDING = "set-pending-invitation";
    public static final String ACTION_CREATE_PROPOSAL = "new-proposal";
    public static final String ACTION_GET_PROPOSALS = "get-proposals";
    public static final String ACTION_ACCEPT_PROPOSAL = "accept-proposal";

    public static final String EXTRA_APPOINTMENT = "appointment";
    public static final String EXTRA_REASON = "reason";
    /**
     * Key for the timestamp extra on the intent, should be of type long and be ON MILLIS
     */
    public static final String EXTRA_TIMESTAMP = "timestamp";

    public AppointmentDiscussionService() {
        super("AppointmentDiscussionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final int appointmentId = intent.getIntExtra(EXTRA_APPOINTMENT, -1);
            if (appointmentId != -1) {
                ApiConnector connector = new ApiConnector(this);
                try {
                    JSONObject json = null;
                    if (ACTION_OPEN.equals(action)) {
                        json = connector.openAppointment(appointmentId);
                    } else if (ACTION_CLOSE.equals(action)) {
                        json = connector.closeAppointment(appointmentId);
                    } else if (ACTION_ACCEPT.equals(action)) {
                        json = connector.acceptInvitation(appointmentId);
                    } else if (ACTION_SET_PENDING.equals(action)) {
                        json = connector.setInvitationPending(appointmentId);
                    } else if (ACTION_REFUSE.equals(action)) {
                        String reason = intent.getStringExtra(EXTRA_REASON);
                        if (reason == null) {
                            return;
                        }
                        json = connector.refuseInvitation(appointmentId, reason);
                    } else if (ACTION_ACCEPT_PROPOSAL.equals(action)) {
                        json = handleCreateAcceptProposal(action, intent, appointmentId, connector);
                    } else if (ACTION_CREATE_PROPOSAL.equals(action)) {
                        handleCreateAcceptProposal(action, intent, appointmentId, connector);
                        return; // Answer is different, it's a proposal, not the complete appointment
                    } else if (ACTION_GET_PROPOSALS.equals(action)) {
                        handleGetProposals(appointmentId, connector);
                        return; // Answer is different
                    }
                    // Reading answer
                    if (json != null) {
                        Parser parser = new Parser(this);
                        ContentValues appCvs = parser.getAppointmentFrom(json);
                        ContentValues currentProposalCvs = parser.getCurrentPropositionFrom(json);
                        ContentValues[] invitationsCvs = parser.getInvitationsFrom(json);

                        (new AppointmentManager(this)).appointmentInsertion(appCvs, invitationsCvs, currentProposalCvs);
                    }
                } catch (ApiConnector.ApiError e) {
                    // On API error ignore, so the app doesn't crash
                    // maybe in the future would be better to send some kind of broadcast
                    // to show the user a toast or something
                    Log.e(TAG, "Error in API opening/closing/accepting/refusing appointment", e);
                }
            }
        }
    }

    private JSONObject handleCreateAcceptProposal(String action, @NonNull Intent intent, int appointmentId,
                                                  @NonNull ApiConnector connector) {
        // Get timestamp
        long timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, -1);
        if (timestamp == -1) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(timestamp);
        String timestampStr = getString(R.string.api_timestamp_format,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        // Get current proposal info
        Cursor cur = getContentResolver().query(
                Uri.withAppendedPath(DBContract.AppointmentsEntry.CONTENT_URI, Integer.toString(appointmentId)),
                new String[]{
                        DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_LAT,
                        DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_LON,
                        DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_NAME},
                null, null, null);
        if (cur != null) {
            if (cur.moveToNext()) {
                double lat = cur.getDouble(0);
                double lon = cur.getDouble(1);
                String place = cur.getString(2);
                cur.close();

                if (action.equals(ACTION_ACCEPT_PROPOSAL)) {
                    return connector.acceptProposal(place, timestampStr, appointmentId);
                } else {
                    String reason = intent.getStringExtra(EXTRA_REASON);
                    JSONObject json = connector.createProposal(place, timestampStr, lon, lat, reason,
                            appointmentId);
                    if (json != null) {
                        Parser parser = new Parser(this);
                        ContentValues cv = parser.getPropositionFrom(json);
                        String creatorId = cv.getAsString(DBContract.PropositionsEntry.COLUMN_CREATOR);
                        // Check if it exists one from the same creator for the same appointment
                        String where;
                        String[] args;
                        if (creatorId != null) {
                            where = DBContract.PropositionsEntry.TABLE_NAME + "." +
                                    DBContract.PropositionsEntry.COLUMN_CREATOR + "=? AND " +
                                    DBContract.PropositionsEntry.TABLE_NAME + "." +
                                    DBContract.PropositionsEntry.COLUMN_APPOINTMENT + "=?";
                            args = new String[]{creatorId, Integer.toString(appointmentId)};
                        } else {
                            where = DBContract.PropositionsEntry.TABLE_NAME + "." +
                                    DBContract.PropositionsEntry.COLUMN_CREATOR + " IS NULL AND " +
                                    DBContract.PropositionsEntry.TABLE_NAME + "." +
                                    DBContract.PropositionsEntry.COLUMN_APPOINTMENT + "=?";
                            args = new String[]{Integer.toString(appointmentId)};
                        }
                        Cursor cursor = getContentResolver().query(
                                DBContract.PropositionsEntry.CONTENT_URI,
                                new String[]{DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry._ID},
                                where, args, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                // If it exists it might be the current one, better exchange it.
                                getContentResolver().update(
                                        DBContract.PropositionsEntry.CONTENT_URI, cv,
                                        DBContract.PropositionsEntry._ID + "=?",
                                        new String[]{cursor.getString(0)});
                            } else {
                                // If it doesn't exist we are interested only if it is our appointment,
                                // because only if the appointment is ours a list of all the propositions
                                // will be shown. If the appointment is not ours, we just need to have
                                // the current proposal.
                                if (creatorId == null) {
                                    getContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, cv);
                                }
                            }
                            cursor.close();
                        }
                    } else {
                        Log.e(TAG, "Some error creatingProposal");
                    }
                    return json; // Will be ignored, but just in case
                }
            }
            cur.close();
        }
        return null;
    }

    private void handleGetProposals(int appointmentId, @NonNull ApiConnector connector) {
        JSONArray answer = connector.getAppointmentPropositions(appointmentId);
        if (answer != null) {
            Parser parser = new Parser(this);
            try {
                ContentValues[] cvs = new ContentValues[answer.length()];
                for (int i = 0; i < answer.length(); i++) {
                    cvs[i] = parser.getPropositionFrom(answer.getJSONObject(i));
                }
                int inserted = getContentResolver().bulkInsert(DBContract.PropositionsEntry.CONTENT_URI, cvs);
                Log.v(TAG, "Inserted " + inserted + " proposals");
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing API answer", e);
            }
        }
    }
}
