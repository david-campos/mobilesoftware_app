package com.campos.david.appointments.activityAppointment;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.campos.david.appointments.CursorRecyclerViewAdapter;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SuggestionsAdapter extends CursorRecyclerViewAdapter<SuggestionsAdapter.ViewHolder> {
    public static final String[] PROJECTION = {
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry._ID,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_NAME,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_NAME,
            DBContract.ReasonsEntry.TABLE_NAME + "." + DBContract.ReasonsEntry.COLUMN_NAME,
            DBContract.ReasonsEntry.TABLE_NAME + "." + DBContract.ReasonsEntry.COLUMN_DESCRIPTION
    };

    public static final int COL_TIMESTAMP = 0;
    public static final int COL_USER = 0;
    public static final int COL_REASON_NAME = 0;
    public static final int COL_REASON_DESCRIPTION = 0;


    private final OnSuggestionInteractionListener mListener;
    private final Context mContext;

    public SuggestionsAdapter(Context context, OnSuggestionInteractionListener listener) {
        super(context, null);
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        String timestamp = SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.LONG, SimpleDateFormat.SHORT).format(
                new Date(cursor.getLong(COL_TIMESTAMP)));
        holder.mTitle.setText(
                mContext.getString(R.string.text_suggestion,
                        cursor.getString(COL_USER),
                        timestamp));
        holder.mReason.setText(
                mContext.getString(R.string.text_reason,
                        cursor.getString(COL_REASON_NAME),
                        cursor.getString(COL_REASON_DESCRIPTION)));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSuggestionInteraction(holder.getAdapterPosition());
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitle;
        public final TextView mReason;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.tv_proposalTitle);
            mReason = (TextView) view.findViewById(R.id.tv_proposalReason);
        }
    }

    public interface OnSuggestionInteractionListener {
        void onSuggestionInteraction(int number);
    }
}
