package com.campos.david.appointments.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Manages appointments
 */
public class AppointmentManager {
    private static final String TAG = AppointmentManager.class.getSimpleName();

    private Context mContext;

    public AppointmentManager(Context context) {
        this.mContext = context;
    }

    public void appointmentInsertion(ContentValues[] appointments, ContentValues[] invitations,
                                     ContentValues[] currentProposals) {
        int appN = mContext.getContentResolver().bulkInsert(
                DBContract.AppointmentsEntry.CONTENT_URI, appointments);
        // We have to do it one by one because we need to update the id in the appointment
        for (ContentValues currentProposal : currentProposals) {
            insertAsCurrentProposal(currentProposal);
        }
        int invN = mContext.getContentResolver().bulkInsert(DBContract.InvitationsEntry.CONTENT_URI, invitations);
        Log.v(TAG, "Inserted " + appN + " appointments (" + invN + " invitations)");
    }

    public void appointmentInsertion(ContentValues appointment, ContentValues[] invitations,
                                     ContentValues currentProposal) {
        mContext.getContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, appointment);
        insertAsCurrentProposal(currentProposal);
        mContext.getContentResolver().bulkInsert(DBContract.InvitationsEntry.CONTENT_URI, invitations);
    }

    private void insertAsCurrentProposal(ContentValues proposal) {
        if (proposal != null) {
            String appointmentId = proposal.getAsString(DBContract.PropositionsEntry.COLUMN_APPOINTMENT);
            Uri insertedProposal = mContext.getContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI,
                    proposal);
            if (insertedProposal != null) {
                int propositionId = Integer.parseInt(insertedProposal.getLastPathSegment());
                if (propositionId == -1) {
                    Log.e(TAG, "Inserted proposition id (" + insertedProposal.getLastPathSegment() +
                            ") couldn't be parsed from uri " + insertedProposal);
                }

                // Delete the old proposition
                Cursor c = mContext.getContentResolver().query(
                        DBContract.AppointmentsEntry.CONTENT_URI,
                        new String[]{DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL},
                        DBContract.AppointmentsEntry._ID + "=?", new String[]{appointmentId},
                        null);
                if (c == null) {
                    return;
                }
                if (!c.moveToFirst()) {
                    c.close();
                    return;
                }
                String oldOneId = c.getString(0);
                c.close();
                mContext.getContentResolver().delete(DBContract.PropositionsEntry.CONTENT_URI,
                        DBContract.PropositionsEntry._ID + "=?", new String[]{oldOneId});

                // The proposition is the current one for the appointment
                ContentValues currentProposalCv = new ContentValues(1);
                currentProposalCv.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, propositionId);
                mContext.getContentResolver().update(
                        DBContract.AppointmentsEntry.CONTENT_URI, currentProposalCv,
                        DBContract.AppointmentsEntry._ID + "=?",
                        new String[]{appointmentId});
            }
        }
    }
}
