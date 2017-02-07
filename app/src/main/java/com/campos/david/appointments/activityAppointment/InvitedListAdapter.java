package com.campos.david.appointments.activityAppointment;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.campos.david.appointments.CursorRecyclerViewAdapter;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

/**
 * {@link RecyclerView.Adapter}
 */
public class InvitedListAdapter extends CursorRecyclerViewAdapter<InvitedListAdapter.ViewHolder> {
    public static final String[] PROJECTION = {
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry._ID,
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_BLOCKED,
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_PICTURE,
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_PHONE,
            DBContract.UsersEntry.TABLE_NAME + "." + DBContract.UsersEntry.COLUMN_NAME,
            DBContract.InvitationsEntry.TABLE_NAME + "." + DBContract.InvitationsEntry.COLUMN_STATE,
            DBContract.ReasonsEntry.TABLE_NAME + "." + DBContract.ReasonsEntry.COLUMN_NAME,
            DBContract.ReasonsEntry.TABLE_NAME + "." + DBContract.ReasonsEntry.COLUMN_DESCRIPTION};

    public static final int COL_ID = 0;
    public static final int COL_BLOCKED = 1;
    public static final int COL_PICTURE = 2;
    public static final int COL_PHONE = 3;
    public static final int COL_NAME = 4;
    public static final int COL_STATE = 5;
    public static final int COL_REASON_NAME = 6;
    public static final int COL_REASON_DESC = 7;
    public static final int COL_IS_CREATOR = 8; // The ContentProvider add's this column to the projection

    private OnListFragmentInteractionListener mListener;
    private Context mContext;

    public InvitedListAdapter(Context context, OnListFragmentInteractionListener listener) {
        super(context, null);
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_invitedlist, parent, false);
        return new ViewHolder(view);
    }

    private String getTextForState(String state) {
        switch (state) {
            case "pending":
                return mContext.getString(R.string.text_pending);
            case "refused":
                return mContext.getString(R.string.text_refused);
            case "accepted":
                return mContext.getString(R.string.text_accepted);
            default:
                return "";
        }
    }

    public String[] getItem(int position) {
        if (mCursor.moveToPosition(position)) {
            String[] result = new String[COL_IS_CREATOR + 1];
            for (int i = 0; i < result.length; i++) {
                result[i] = mCursor.getString(i);
            }
            return result;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        boolean isInvited = (cursor.getInt(COL_IS_CREATOR) == 0);

        holder.mNameView.setText(cursor.getString(COL_NAME));
        holder.mNumberView.setText(cursor.getString(COL_PHONE));
        //TODO: set the correct picture for the image view with picasso
        holder.mPictureView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo));
        boolean blocked = (cursor.getInt(COL_BLOCKED) != 0);
        if (blocked) {
            holder.mNameView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_outline, 0, 0, 0);
        } else {
            holder.mNameView.setCompoundDrawables(null, null, null, null);
        }
        if (isInvited) {
            holder.mStateView.setText(getTextForState(cursor.getString(COL_STATE)));
        } else {
            holder.mStateView.setText(R.string.text_creator);
        }
        boolean hasReason = (isInvited && !cursor.isNull(COL_REASON_NAME));
        holder.mReasonNameView.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        holder.mReasonDescriptionView.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        if (hasReason) {
            holder.mReasonNameView.setText(cursor.getString(COL_REASON_NAME));
            holder.mReasonDescriptionView.setText(cursor.getString(COL_REASON_DESC));
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.getAdapterPosition());
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mStateView;
        public final TextView mNumberView;
        public final TextView mReasonNameView;
        public final TextView mReasonDescriptionView;
        public final ImageView mPictureView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) mView.findViewById(R.id.tv_user);
            mStateView = (TextView) mView.findViewById(R.id.tv_state);
            mNumberView = (TextView) mView.findViewById(R.id.tv_phoneNumber);
            mReasonNameView = (TextView) mView.findViewById(R.id.tv_reasonName);
            mReasonDescriptionView = (TextView) mView.findViewById(R.id.tv_reasonDescription);
            mPictureView = (ImageView) mView.findViewById(R.id.iv_userProfilePicture);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + ":" + mStateView.getText() + "'";
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(int position);
    }
}
