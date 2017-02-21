package com.campos.david.appointments.activityNewAppointment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.campos.david.appointments.CursorRecyclerViewAdapter;
import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.HashSet;

public class PickUsersAdapter extends CursorRecyclerViewAdapter<PickUsersAdapter.ViewHolder> {
    public interface PickedUserListener {
        void onUserPicked(final ViewHolder holder);
    }

    public interface Query {
        Uri URI = DBContract.UsersEntry.CONTENT_URI;
        String[] PROJECTION = {
                DBContract.UsersEntry._ID,
                DBContract.UsersEntry.COLUMN_NAME,
                DBContract.UsersEntry.COLUMN_PHONE,
                DBContract.UsersEntry.COLUMN_PICTURE,
                DBContract.UsersEntry.COLUMN_BLOCKED
        };
        int COL_ID = 0;
        int COL_NAME = 1;
        int COL_PHONE = 2;
        int COL_PICTURE = 3;
        int COL_BLOCKED = 4;

        String ORDER_BY = DBContract.UsersEntry.COLUMN_BLOCKED + " ASC, " + DBContract.UsersEntry.COLUMN_NAME + " ASC";
    }

    private Context mContext = null;
    private PickedUserListener mListener = null;
    private HashSet<String> mSelected = null;
    private String mApiUri;

    public Collection<String> getSelectedUsers() {
        return mSelected;
    }

    public void selectUser(String phone) {
        if (!mSelected.contains(phone))
            mSelected.add(phone);
    }

    public void deselectUser(String phone) {
        mSelected.remove(phone);
    }

    public int getSelectedCount() {
        return mSelected.size();
    }

    public PickUsersAdapter(@NonNull Context context, PickedUserListener listener) {
        super(context, null);
        mContext = context;
        mListener = listener;
        mSelected = new HashSet<>();

        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_key),
                Context.MODE_PRIVATE);
        mApiUri = mContext.getString(R.string.api_protocol) + preferences.getString(mContext.getString(R.string.api_uri_key),
                mContext.getString(R.string.api_uri_default));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        final int id = cursor.getInt(Query.COL_ID);
        final String name = cursor.getString(Query.COL_NAME);
        final String phone = cursor.getString(Query.COL_PHONE);
        final boolean blocked = (cursor.getInt(Query.COL_BLOCKED) != 0);
        final boolean selected = mSelected.contains(phone);
        final int iconId = cursor.getInt(Query.COL_PICTURE);
        String uri = mContext.getString(R.string.api_profile_pics_format, mApiUri, iconId);

        viewHolder.setUserId(id);
        viewHolder.setPhone(phone);
        viewHolder.setBlocked(blocked);

        viewHolder.mNameView.setText(name);
        viewHolder.mPhoneView.setText(phone);
        viewHolder.mNameView.setCompoundDrawablesWithIntrinsicBounds(
                blocked ? R.drawable.ic_lock_outline : 0, 0, 0, 0);
        viewHolder.mView.setAlpha(blocked ? 0.5f : 1.0f);
        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.drawable.unknown_user)
                .into(viewHolder.mProfilePictureView);

        if (selected) {
            viewHolder.mView.setBackgroundColor(mContext.getResources().getColor(R.color.colorSelectedUser));
        } else {
            viewHolder.mView.setBackgroundColor(Color.TRANSPARENT);
        }

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userClicked(blocked, viewHolder);
                mListener.onUserPicked(viewHolder);
            }
        });
    }

    public void userClicked(final boolean blocked, final ViewHolder viewHolder) {
        if (!blocked) {
            if (mSelected.contains(viewHolder.getPhone())) {
                deselectUser(viewHolder.getPhone());
            } else {
                selectUser(viewHolder.getPhone());
            }
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mPhoneView;
        public final ImageView mProfilePictureView;

        private int mUserId = 0;
        private boolean mBlocked = false;
        private String mPhone = null;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.tv_userName);
            mPhoneView = (TextView) view.findViewById(R.id.tv_userPhone);
            mProfilePictureView = (ImageView) view.findViewById(R.id.iv_userPicture);
        }

        public String getPhone() {
            return mPhone;
        }

        public void setPhone(String phone) {
            this.mPhone = phone;
        }

        public int getUserId() {
            return mUserId;
        }

        public void setUserId(int mUserId) {
            this.mUserId = mUserId;
        }

        public boolean isBlocked() {
            return mBlocked;
        }

        public void setBlocked(boolean mBlocked) {
            this.mBlocked = mBlocked;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
