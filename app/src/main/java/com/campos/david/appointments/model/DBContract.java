package com.campos.david.appointments.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a>
 */
public class DBContract {
    /**
     * Used by the ContentProvider, it must be unique in the mobile, so it is advised to use
     * the name of the app package
     */
    public static final String CONTENT_AUTHORITY = "com.campos.david.appointments.provider";
    /**
     * Base Uri to get content in this app
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri SESSION_RELATED_DATA_URI = BASE_CONTENT_URI.buildUpon()
            .appendPath("session_related_data").build();

    public static final String PATH_USERS = "users";
    public static final String PATH_APPOINTMENT_TYPES = "appointmentTypes";
    public static final String PATH_REASONS = "reasons";
    public static final String PATH_APPOINTMENTS = "appointments";
    public static final String PATH_PROPOSITIONS = "propositions";
    public static final String PATH_INVITATIONS = "invitations";

    public static final class AppointmentTypesEntry implements BaseColumns {
        public static final String TABLE_NAME = "appointment_types";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ICON = "icon_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_APPOINTMENT_TYPES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENT_TYPES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENT_TYPES;
    }

    public static final class ReasonsEntry implements BaseColumns {
        public static final String TABLE_NAME = "reason";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REASONS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REASONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REASONS;
    }

    public static final class UsersEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";

        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PICTURE = "picture_id";
        public static final String COLUMN_BLOCKED = "blocked";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_WITH_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS + "_with";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        // The query for users_with will always return the merge of two cursors with this same
        // projections. The row type will be 'invited' or 'creator' depending on the type of projection
        public static String[] WITHS_PROJECTION_INVITATIONS = {
                "row_type",
                DBContract.InvitationsEntry.TABLE_NAME + "." + DBContract.InvitationsEntry.COLUMN_APPOINTMENT,
                DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_NAME};
        public static String[] WITHS_PROJECTION_APPOINTMENTS = {
                "row_type",
                AppointmentsEntry.TABLE_NAME + "." + AppointmentsEntry._ID,
                UsersEntry.TABLE_NAME + "." + UsersEntry.COLUMN_NAME
        };
        public static int WITHS_ROW_TYPE_COL = 0;
        public static int WITHS_APPOINTMENT_ID_COL = 1;
        public static int WITHS_USER_NAME_COL = 2;

        public static Uri buildUriInvitedTo(String appointmentId) {
            return CONTENT_URI.buildUpon().appendPath("invited_to").appendPath(appointmentId).build();
        }
        public static Uri buildUriInvitedTo(int appointmentId) {
            return buildUriInvitedTo(Integer.toString(appointmentId));
        }
    }

    public static final class PropositionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "propositions";

        public static final String COLUMN_APPOINTMENT = "appointment";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_PLACE_LON = "placeLon";
        public static final String COLUMN_PLACE_LAT = "placeLat";
        public static final String COLUMN_PLACE_NAME = "name";
        public static final String COLUMN_CREATOR = "creator";
        public static final String COLUMN_REASON = "reason";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROPOSITIONS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROPOSITIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROPOSITIONS;

        public static Uri buildUriForAppointment(int appointmentId) {
            return buildUriForAppointment(Integer.toString(appointmentId));
        }
        public static Uri buildUriForAppointment(String appointmentId) {
            return CONTENT_URI.buildUpon().appendPath(appointmentId).build();
        }

    }

    public static final class AppointmentsEntry implements BaseColumns {
        public static final String TABLE_NAME = "appointments";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CLOSED = "closed";
        public static final String COLUMN_CURRENT_PROPOSAL = "proposal";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_CREATOR = "creator";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_APPOINTMENTS).build();
        public static final String CONTENT_BASIC_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENTS;
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENTS + "/with_current";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENTS + "/with_current";
        public static final String CONTENT_ITEM_WITH_INVITATION_TYPE =
                CONTENT_ITEM_TYPE + "/with_invitation";
        public static final Uri CONTENT_PENDING_URI = Uri.withAppendedPath(AppointmentsEntry.CONTENT_URI, "pending");
        public static final Uri CONTENT_ACCEPTED_URI = Uri.withAppendedPath(AppointmentsEntry.CONTENT_URI, "accepted");
        public static final Uri CONTENT_REFUSED_URI = Uri.withAppendedPath(AppointmentsEntry.CONTENT_URI, "refused");
    }

    public static final class InvitationsEntry implements  BaseColumns {
        public static final String TABLE_NAME = "invited_to";

        public static final String COLUMN_USER = "user";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_APPOINTMENT = "appointment";
        public static final String COLUMN_REASON = "reason";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INVITATIONS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVITATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVITATIONS;
    }
}