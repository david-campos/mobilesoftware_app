package com.campos.david.appointments.activityMain;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.campos.david.appointments.R;
import com.campos.david.appointments.activityAppointment.AppointmentActivity;
import com.campos.david.appointments.model.DBContract;

/**
 * A fragment representing the list of appointments.
 */
public class AppointmentListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AppointmentListRecyclerViewAdapter.OnListFragmentInteractionListener {
    private static final String ARG_APPOINTMENTS_STATE = "appointments-state";

    public static final String APPOINTMENTS_STATE_REFUSED = "refused";
    public static final String APPOINTMENTS_STATE_ACCEPTED = "accepted";
    public static final String APPOINTMENTS_STATE_PENDING = "pending";

    public static String[] APPOINTMENTS_PROPOSITIONS_PROJECTION = {
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry._ID,
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_NAME,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_NAME,
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CREATOR,
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CLOSED
    };
    public static final int CURSOR_ID_COL = 0;
    public static final int CURSOR_NAME_COL = 1;
    public static final int CURSOR_TIMESTAMP_COL = 2;
    public static final int CURSOR_PLACE_COL = 3;
    public static final int CURSOR_CREATOR_COL = 4;
    public static final int CURSOR_CLOSED_COL = 5;

    private static final int LOADER_APPOINTMENTS = 0;
    private static final int LOADER_WITHS = 1;

    private AppointmentListRecyclerViewAdapter mAdapter;
    private String mAppointmentsState = null;

    private View mView = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppointmentListFragment() {
    }

    public static AppointmentListFragment newInstance(String appointmentsState) {
        AppointmentListFragment fragment = new AppointmentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_APPOINTMENTS_STATE, appointmentsState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new AppointmentListRecyclerViewAdapter(getActivity(), this);
        Bundle args = getArguments();
        if (args != null) {
            mAppointmentsState = args.getString(ARG_APPOINTMENTS_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_appointment_list, container, false);

        // Set the adapter
        if (mView instanceof RecyclerView) {
            Context context = mView.getContext();
            RecyclerView recyclerView = (RecyclerView) mView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mAdapter);
        }
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_APPOINTMENTS, null, this);
        getLoaderManager().initLoader(LOADER_WITHS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_WITHS:
                return new CursorLoader(
                        getActivity(),
                        Uri.withAppendedPath(DBContract.UsersEntry.CONTENT_URI, "with"),
                        null, null, null, null);
            case LOADER_APPOINTMENTS:
                Uri uri;
                switch (mAppointmentsState) {
                    case APPOINTMENTS_STATE_REFUSED:
                        uri = DBContract.AppointmentsEntry.CONTENT_REFUSED_URI;
                        break;
                    case APPOINTMENTS_STATE_ACCEPTED:
                        uri = DBContract.AppointmentsEntry.CONTENT_ACCEPTED_URI;
                        break;
                    case APPOINTMENTS_STATE_PENDING:
                        uri = DBContract.AppointmentsEntry.CONTENT_PENDING_URI;
                        break;
                    default:
                        throw new IllegalArgumentException(mAppointmentsState + " is not a valid appointment state");
                }
                return new CursorLoader(getActivity(), uri, APPOINTMENTS_PROPOSITIONS_PROJECTION, null, null, null);
            default:
                throw new IllegalArgumentException("Unknown loader id '" + id + "'");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_WITHS:
                mAdapter.swapWithsCursor(data);
                break;
            case LOADER_APPOINTMENTS:
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_WITHS:
                mAdapter.swapWithsCursor(null);
                break;
            case LOADER_APPOINTMENTS:
                mAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(int position) {
        if (mAdapter != null) {
            Cursor c = mAdapter.getRow(position);
            int id = (int) c.getLong(CURSOR_ID_COL);
            boolean userIsCreator = c.isNull(CURSOR_CREATOR_COL);
            boolean closed = (c.getInt(CURSOR_CLOSED_COL) != 0);
            String name = c.getString(CURSOR_NAME_COL);
            if (id != 0) {
                Intent launchAppointmentActivity =
                        new Intent(getContext().getApplicationContext(), AppointmentActivity.class);
                launchAppointmentActivity.putExtra(AppointmentActivity.EXTRA_APPOINTMENT_ID, id);
                launchAppointmentActivity.putExtra(AppointmentActivity.EXTRA_USER_IS_CREATOR, userIsCreator);
                launchAppointmentActivity.putExtra(AppointmentActivity.EXTRA_APPOINTMENT_CLOSED, closed);
                launchAppointmentActivity.putExtra(AppointmentActivity.EXTRA_APPOINTMENT_NAME, name);
                getContext().startActivity(launchAppointmentActivity);
            }
        } else {
            Snackbar.make(mView, R.string.cant_open_appointment, Snackbar.LENGTH_SHORT).show();
        }
    }
}
