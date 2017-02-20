package com.campos.david.appointments.services;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract.AppointmentsEntry;
import com.campos.david.appointments.model.DBContract.InvitationsEntry;
import com.campos.david.appointments.model.DBContract.PropositionsEntry;
import com.campos.david.appointments.model.DBContract.UsersEntry;
import com.campos.david.appointments.model.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Manages all the tasks related with parsing the JSON to get the content values
 */
public class Parser {
    private static final String TAG = Parser.class.getSimpleName();

    private Context mContext;

    public Parser(Context context) {
        this.mContext = context;
    }

    public ContentValues getAppointmentFrom(@NonNull JSONObject json) {
        ContentValues cv = new ContentValues();
        try {
            String nameKey = mContext.getString(R.string.response_appointment_name);
            String descKey = mContext.getString(R.string.response_appointment_description);
            String closedKey = mContext.getString(R.string.response_appointment_closed);
            String idKey = mContext.getString(R.string.response_id);
            String typeKey = mContext.getString(R.string.response_appointment_type);
            String creatorKey = mContext.getString(R.string.response_appointment_creator);

            if (json.has(nameKey)) cv.put(AppointmentsEntry.COLUMN_NAME, json.getString(nameKey));
            if (json.has(descKey))
                cv.put(AppointmentsEntry.COLUMN_DESCRIPTION, json.getString(descKey));
            if (json.has(closedKey))
                cv.put(AppointmentsEntry.COLUMN_CLOSED, json.getBoolean(closedKey));
            if (json.has(idKey)) cv.put(AppointmentsEntry._ID, json.getInt(idKey));

            if (json.has(typeKey)) {
                int typeId = UpdateTypesAndReasonsService
                        .lookForAppointmentTypeId(mContext, json.getString(typeKey));
                if (typeId == 0)
                    throw new IllegalArgumentException("Couldn't find type for appointment");
                cv.put(AppointmentsEntry.COLUMN_TYPE, typeId);
            }

            if (json.has(creatorKey)) {
                UserManager manager = new UserManager(mContext);
                int creator = json.getInt(creatorKey);
                if (manager.isMe(creator))
                    cv.put(AppointmentsEntry.COLUMN_CREATOR, (Integer) null);
                else
                    cv.put(AppointmentsEntry.COLUMN_CREATOR, json.getInt(creatorKey));
            }

            cv.put(AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, 0); // It can't be null
            return cv;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing appointment", e);
            return null;
        }
    }

    public boolean hasCurrentProposal(JSONObject appointment) {
        return appointment.has(mContext.getString(R.string.response_appointment_current_proposition));
    }

    public ContentValues getCurrentPropositionFrom(JSONObject appointment) {
        try {
            if (hasCurrentProposal(appointment))
                return getPropositionFrom(
                        appointment.getJSONObject(mContext.getString(R.string.response_appointment_current_proposition)));
        } catch (JSONException e) {
            Log.e(TAG, "Error getting current proposition", e);
        }
        return null;
    }

    public ContentValues getPropositionFrom(@NonNull JSONObject proposition) {
        ContentValues cv = new ContentValues();
        try {
            String timeKey = mContext.getString(R.string.response_proposition_time);
            String coordKey = mContext.getString(R.string.response_proposition_coordinates);
            String latitudeKey = mContext.getString(R.string.response_proposition_latitude);
            String longitudeKey = mContext.getString(R.string.response_proposition_longitude);
            String placeKey = mContext.getString(R.string.response_proposition_place);
            String appKey = mContext.getString(R.string.response_proposition_appointment);
            String reasonKey = mContext.getString(R.string.response_proposition_reason_name);
            String reasonDescriptionKey = mContext.getString(R.string.response_proposition_reason_description);
            String creatorKey = mContext.getString(R.string.response_proposition_proposer);

            if (proposition.has(timeKey))
                cv.put(PropositionsEntry.COLUMN_TIMESTAMP, proposition.getLong(timeKey));
            if (proposition.has(coordKey)) {
                JSONObject coordinates = proposition.getJSONObject(coordKey);
                if (coordinates.has(latitudeKey))
                    cv.put(PropositionsEntry.COLUMN_PLACE_LAT, coordinates.getDouble(latitudeKey));
                if (coordinates.has(longitudeKey))
                    cv.put(PropositionsEntry.COLUMN_PLACE_LON, coordinates.getDouble(longitudeKey));
            }
            if (proposition.has(placeKey))
                cv.put(PropositionsEntry.COLUMN_PLACE_NAME, proposition.getString(placeKey));
            if (proposition.has(appKey))
                cv.put(PropositionsEntry.COLUMN_APPOINTMENT, proposition.getInt(appKey));
            if (proposition.has(reasonKey) && proposition.has(reasonDescriptionKey)) {
                // If reasonName is null it is not necessary to put anything in the CV,
                // as the DEFAULT value is NULL
                if (!proposition.isNull(reasonKey)) {
                    int reasonId = UpdateTypesAndReasonsService
                            .lookForReasonOrInsert(mContext,
                                    proposition.getString(reasonKey),
                                    proposition.getString(reasonDescriptionKey));
                    if (reasonId != 0) {
                        cv.put(PropositionsEntry.COLUMN_REASON, reasonId);
                    }
                }
            }
            if (proposition.has(creatorKey)) {
                UserManager manager = new UserManager(mContext);
                ContentValues user = getUserFrom(proposition.getJSONObject(creatorKey));
                if (user.containsKey(UsersEntry._ID)) {
                    Integer id = manager.insertUserIfNotExistent(user);
                    cv.put(PropositionsEntry.COLUMN_CREATOR, id);
                }
            }
            return cv;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing proposition", e);
        }
        return null;
    }

    public ContentValues[] getInvitationsFrom(@NonNull JSONObject appointmentJson) {
        try {
            String appKey = mContext.getString(R.string.response_id);
            String invitationsKey = mContext.getString(R.string.response_appointment_invitations);
            String userKey = mContext.getString(R.string.response_invitation_user);
            String stateKey = mContext.getString(R.string.response_invitation_state);
            String idKey = mContext.getString(R.string.response_id);
            String reasonKey = mContext.getString(R.string.response_invitation_reason_name);
            String reasonDescKey = mContext.getString(R.string.response_invitation_reason_description);

            if (appointmentJson.has(appKey) && appointmentJson.has(invitationsKey)) {
                int appointmentId = appointmentJson.getInt(appKey);
                JSONArray invitations = appointmentJson.getJSONArray(invitationsKey);

                ContentValues[] cvs = new ContentValues[invitations.length()];
                UserManager userManager = new UserManager(mContext);
                for (int i = 0; i < invitations.length(); i++) {
                    JSONObject aInvitation = invitations.getJSONObject(i);
                    cvs[i] = new ContentValues();

                    if (aInvitation.has(userKey)) {
                        JSONObject invitedUser = aInvitation.getJSONObject(userKey);
                        if (invitedUser.has(idKey)) {
                            Integer userId = userManager.insertUserIfNotExistent(getUserFrom(invitedUser));
                            cvs[i].put(InvitationsEntry.COLUMN_USER, userId);
                        }
                    }
                    if (aInvitation.has(stateKey))
                        cvs[i].put(InvitationsEntry.COLUMN_STATE, aInvitation.getString(stateKey));

                    // If reasonName is null it is not necessary to put anything in the CV,
                    // as the DEFAULT value is NULL
                    if (aInvitation.has(reasonKey) && aInvitation.has(reasonDescKey) &&
                            !aInvitation.isNull(reasonKey)) {
                        int reasonId = UpdateTypesAndReasonsService
                                .lookForReasonOrInsert(mContext,
                                        aInvitation.getString(reasonKey),
                                        aInvitation.getString(reasonDescKey));
                        if (reasonId != 0) {
                            cvs[i].put(InvitationsEntry.COLUMN_REASON, reasonId);
                        }
                    }
                    cvs[i].put(InvitationsEntry.COLUMN_APPOINTMENT, appointmentId);
                }
                return cvs;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing invitations", e);
        }
        return null;
    }

    public ContentValues getUserFrom(@NonNull JSONObject user) throws JSONException {
        ContentValues userCv = new ContentValues(5);

        // Get the keys as defined in api_interface.xml
        String idKey = mContext.getString(R.string.response_id);
        String nameKey = mContext.getString(R.string.response_user_name);
        String phoneKey = mContext.getString(R.string.response_user_phone);
        String pictureKey = mContext.getString(R.string.response_user_picture);
        String blockedKey = mContext.getString(R.string.response_user_blocked);

        // Add all that we can find in the JSON
        if (user.has(idKey)) userCv.put(UsersEntry._ID, user.getInt(idKey));
        if (user.has(nameKey)) userCv.put(UsersEntry.COLUMN_NAME, user.getString(nameKey));
        if (user.has(phoneKey)) userCv.put(UsersEntry.COLUMN_PHONE, user.getString(phoneKey));
        if (user.has(pictureKey)) userCv.put(UsersEntry.COLUMN_PICTURE, user.getInt(pictureKey));
        if (user.has(blockedKey))
            userCv.put(UsersEntry.COLUMN_BLOCKED, user.getBoolean(blockedKey));

        return userCv;
    }
}
