package com.campos.david.appointments.activityAppointment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

public class ReasonPickerDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public interface ReasonPickerDialogListener {
        void reasonPicked(String reasonName);
    }

    private static final int REASONS_LOADER = 0;

    private interface Query {
        Uri URI = DBContract.ReasonsEntry.CONTENT_URI;
        String[] PROJECTION = {
                DBContract.ReasonsEntry._ID,
                DBContract.ReasonsEntry.COLUMN_NAME,
                DBContract.ReasonsEntry.COLUMN_DESCRIPTION};
        int COL_ID = 0;
        int COL_NAME = 1;
        int COL_DESCRIPTION = 2;

        String[] FROM = {
                DBContract.ReasonsEntry.COLUMN_NAME
        };
    }

    private Spinner mReasons;
    private TextView mDescription;
    private ReasonPickerDialogListener mListener;
    private SimpleCursorAdapter mAdapter;

    public ReasonPickerDialogListener getReasonPickedListener() {
        return mListener;
    }

    public void setReasonPickedListener(ReasonPickerDialogListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View mainView = inflater.inflate(R.layout.dialog_reason_picker, null);
        mReasons = (Spinner) mainView.findViewById(R.id.sp_reason);
        mDescription = (TextView) mainView.findViewById(R.id.tv_reason_description);


        mReasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDescription.setText(
                        ((Cursor) mAdapter.getItem(position)).getString(Query.COL_DESCRIPTION));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                Query.FROM,
                new int[]{android.R.id.text1},
                0);
        mReasons.setAdapter(mAdapter);

        builder.setView(mainView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.reasonPicked(((Cursor) mReasons.getSelectedItem()).getString(Query.COL_NAME));
                        }
                        ReasonPickerDialog.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReasonPickerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(REASONS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Query.URI, Query.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
