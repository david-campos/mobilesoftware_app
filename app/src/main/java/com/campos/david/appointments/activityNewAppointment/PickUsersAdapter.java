package com.campos.david.appointments.activityNewAppointment;

import android.content.Context;
import android.content.res.ColorStateList;
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

import java.util.ArrayList;
import java.util.List;

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

    private static ColorStateList selectedTint = new ColorStateList(
            new int[][]{
                    new int[]{android.R.attr.state_enabled}},
            new int[]{
                    Color.GRAY});

    private Context mContext = null;
    private PickedUserListener mListener = null;
    private List<Integer> mSelected = null;

    public List<Integer> getSelectedUsers() {
        return mSelected;
    }

    public void selectUser(int id) {
        mSelected.add(id);
    }

    public void deselectUser(int id) {
        mSelected.remove(id);
    }

    public int getSelectedCount() {
        return mSelected.size();
    }

    public PickUsersAdapter(@NonNull Context context, PickedUserListener listener) {
        super(context, null);
        mContext = context;
        mListener = listener;
        mSelected = new ArrayList<>();
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
        final boolean selected = mSelected.contains(cursor.getPosition());

        viewHolder.setUserId(id);
        viewHolder.setBlocked(blocked);

        viewHolder.mNameView.setText(name);
        viewHolder.mPhoneView.setText(phone);
        viewHolder.mNameView.setCompoundDrawablesWithIntrinsicBounds(
                blocked ? R.drawable.ic_lock_outline : 0, 0, 0, 0);
        viewHolder.mView.setAlpha(blocked ? 0.5f : 1.0f);

        if (selected) {
            viewHolder.mView.setBackgroundColor(mContext.getResources().getColor(R.color.colorSelectedUser));
        } else {
            viewHolder.mView.setBackgroundColor(Color.TRANSPARENT);
        }

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userClicked(blocked, selected, viewHolder);
                mListener.onUserPicked(viewHolder);
            }
        });
    }

    public void userClicked(final boolean blocked, final boolean selected, final ViewHolder viewHolder) {
        if (!blocked) {
            if (selected) {
                deselectUser(mSelected.indexOf(viewHolder.getAdapterPosition()));
            } else {
                selectUser(viewHolder.getAdapterPosition());
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

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.tv_userName);
            mPhoneView = (TextView) view.findViewById(R.id.tv_userPhone);
            mProfilePictureView = (ImageView) view.findViewById(R.id.iv_userPicture);
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