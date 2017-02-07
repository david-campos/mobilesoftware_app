package com.campos.david.appointments.activityAppointment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import com.campos.david.appointments.model.DBContract;

/**
 * A fragment representing a list of Items.
 * <p>
 */
public class InvitedListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        InvitedListAdapter.OnListFragmentInteractionListener {

    private static final int LOADER_USERS = 0;

    private static final String ARG_APPOINTMENT_ID = "app-id";

    private InvitedListAdapter mAdapter;
    private int mAppointmentId = 0;
    private Snackbar snackbar = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InvitedListFragment() {
    }

    public static InvitedListFragment newInstance(int appointmentId) {
        InvitedListFragment fragment = new InvitedListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_APPOINTMENT_ID, appointmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppointmentId = getArguments().getInt(ARG_APPOINTMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitedlist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new InvitedListAdapter(getActivity(), this);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_USERS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                DBContract.UsersEntry.buildUriInvitedTo(mAppointmentId),
                InvitedListAdapter.PROJECTION, null, null, "is_creator DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListFragmentInteraction(int position) {
        if (snackbar != null) {
            snackbar.dismiss();
        }

        String[] values = mAdapter.getItem(position);
        final String userId = values[InvitedListAdapter.COL_ID];
        boolean blocked = (Integer.parseInt(values[InvitedListAdapter.COL_BLOCKED]) != 0);
        String text = String.format("%s (%s)%s",
                values[InvitedListAdapter.COL_NAME], values[InvitedListAdapter.COL_PHONE],
                blocked ? getResources().getString(R.string.text_x_is_blocked) : "");
        View view = getView();
        if (view != null) {
            snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
            View.OnClickListener listener;
            if (blocked) {
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        cv.put(DBContract.UsersEntry.COLUMN_BLOCKED, 0);
                        getActivity().getContentResolver().update(
                                DBContract.UsersEntry.CONTENT_URI,
                                cv, DBContract.UsersEntry._ID + "=?", new String[]{userId});
                    }
                };
            } else {
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues cv = new ContentValues();
                        cv.put(DBContract.UsersEntry.COLUMN_BLOCKED, 1);
                        getActivity().getContentResolver().update(
                                DBContract.UsersEntry.CONTENT_URI,
                                cv, DBContract.UsersEntry._ID + "=?", new String[]{userId});
                    }
                };
            }
            snackbar.setAction(blocked ? R.string.text_unblock : R.string.text_block, listener)
                    .setActionTextColor(blocked ?
                            getResources().getColorStateList(R.color.snackbar_action_unblock_colors) :
                            getResources().getColorStateList(R.color.snackbar_action_block_colors))
                    .show();
        }
    }
}
