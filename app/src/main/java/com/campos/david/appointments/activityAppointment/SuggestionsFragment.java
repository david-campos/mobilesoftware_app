package com.campos.david.appointments.activityAppointment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.campos.david.appointments.PersonalizedRecyclerView;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.campos.david.appointments.services.AppointmentDiscussionService;

import java.util.Calendar;

/**
 * A fragment presenting a list of Suggestions.
 */
public class SuggestionsFragment extends Fragment
        implements SuggestionsAdapter.OnSuggestionInteractionListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_PROPOSITIONS = 0;
    private static final String ARG_ID = "id";

    private SuggestionsAdapter mAdapter = null;
    private int mAppointmentId;

    /**
     * Mandatory empty constructor for the fragment manager to newInstance the
     * fragment (e.g. upon screen orientation changes).
     */
    public SuggestionsFragment() {
    }

    public static SuggestionsFragment newInstance(int appointmentId) {
        SuggestionsFragment newMe = new SuggestionsFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_ID, appointmentId);
        newMe.setArguments(args);
        return newMe;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAppointmentId = args.getInt(ARG_ID);

            Intent intentToGetProposals = new Intent(getActivity().getApplicationContext(),
                    AppointmentDiscussionService.class);
            intentToGetProposals.setAction(AppointmentDiscussionService.ACTION_GET_PROPOSALS);
            intentToGetProposals.putExtra(AppointmentDiscussionService.EXTRA_APPOINTMENT, mAppointmentId);
            getActivity().startService(intentToGetProposals);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestion_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        PersonalizedRecyclerView recyclerView = (PersonalizedRecyclerView) view.findViewById(R.id.suggestion_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new SuggestionsAdapter(getContext(), this);
        View emptyView = view.findViewById(R.id.on_list_empty);
        ((TextView) emptyView.findViewById(R.id.tv_noElements)).setText(
                getContext().getString(R.string.text_nothing_to_show_yet,
                        getContext().getString(R.string.text_suggestions)));
        recyclerView.setEmptyView(emptyView);
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onSuggestionInteraction(final long timestampMillis, String place) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampMillis);
        String timestampStr = getString(R.string.timestamp_format,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_accept_suggestion)
                .setMessage(getString(R.string.message_accept_suggestion, timestampStr))
                .setIcon(R.drawable.ic_done)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent proposalAcceptIntent = new Intent(getActivity().getApplicationContext(),
                                AppointmentDiscussionService.class);
                        proposalAcceptIntent.setAction(AppointmentDiscussionService.ACTION_ACCEPT_PROPOSAL);
                        proposalAcceptIntent.putExtra(AppointmentDiscussionService.EXTRA_APPOINTMENT, mAppointmentId);
                        proposalAcceptIntent.putExtra(AppointmentDiscussionService.EXTRA_TIMESTAMP, timestampMillis);
                        getActivity().startService(proposalAcceptIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_PROPOSITIONS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                Uri.withAppendedPath(DBContract.PropositionsEntry.CONTENT_URI, Integer.toString(mAppointmentId)),
                SuggestionsAdapter.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null)
            mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null)
            mAdapter.swapCursor(null);
    }
}
