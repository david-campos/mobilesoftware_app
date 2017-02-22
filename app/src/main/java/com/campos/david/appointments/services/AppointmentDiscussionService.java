package com.campos.david.appointments.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.campos.david.appointments.model.AppointmentManager;

import org.json.JSONObject;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AppointmentDiscussionService extends IntentService {
    private static final String TAG = AppointmentDiscussionService.class.getSimpleName();

    public static final String ACTION_OPEN = "open-discussion";
    public static final String ACTION_CLOSE = "close-discussion";
    public static final String ACTION_REFUSE = "refuse-invitation";
    public static final String ACTION_ACCEPT = "accept-invitation";
    public static final String ACTION_SET_PENDING = "set-pending-invitation";
    public static final String EXTRA_APPOINTMENT = "appointment";
    public static final String EXTRA_REASON = "reason";

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
                    }
                    // Reading answer
                    Parser parser = new Parser(this);
                    if (json != null) {
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
}
