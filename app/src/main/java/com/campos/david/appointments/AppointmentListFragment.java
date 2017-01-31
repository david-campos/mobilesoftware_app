package com.campos.david.appointments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.campos.david.appointments.model.DBContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AppointmentListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_APPOINTMENTS_STATE = "appointments-state";

    public static final String APPOINTMENTS_STATE_REFUSED = "refused";
    public static final String APPOINTMENTS_STATE_ACCEPTED = "accepted";
    public static final String APPOINTMENTS_STATE_PENDING = "pending";

    public static String[] CURSOR_PROJECTION = {
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry._ID,
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_NAME,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_NAME
    };
    public static int CURSOR_ID_COL = 0;
    public static int CURSOR_NAME_COL = 1;
    public static int CURSOR_TIMESTAMP_COL = 2;
    public static int CURSOR_PLACE_COL = 3;

    private static int LOADER_APPOINTMENTS = 0;

    private AppointmentListRecyclerViewAdapter mAdapter;
    private String mAppointmentsState = null;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppointmentListFragment() {}

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

        Bundle args = getArguments();
        if(args != null) {
            mAppointmentsState = args.getString(ARG_APPOINTMENTS_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new AppointmentListRecyclerViewAdapter(getActivity());
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_APPOINTMENTS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        return new CursorLoader(getActivity(), uri, CURSOR_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(int position);
    }
}
