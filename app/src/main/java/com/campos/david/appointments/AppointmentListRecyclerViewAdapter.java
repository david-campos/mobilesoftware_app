package com.campos.david.appointments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} supplied by a Cursor
 */
public class AppointmentListRecyclerViewAdapter extends CursorRecyclerViewAdapter<AppointmentListRecyclerViewAdapter.ViewHolder> {

    private AppointmentListFragment.OnListFragmentInteractionListener mListener;
    Context mContext;

    public AppointmentListRecyclerViewAdapter(Context context) {
        super(context, null);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        holder.mTitleView.setText(cursor.getString(AppointmentListFragment.CURSOR_NAME_COL));
        String place = cursor.getString(AppointmentListFragment.CURSOR_PLACE_COL);
        String data = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.US).format(
                new Date(cursor.getLong(AppointmentListFragment.CURSOR_TIMESTAMP_COL)));
        holder.mProposalInfoView.setText(place + ", " + data);
        holder.mWithInfoView.setText("With: Josh, Wietske,...");
        holder.mImageView.setImageResource(R.drawable.ic_info_black_24dp);
        holder.mImageView.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.getAdapterPosition());
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTitleView;
        public final TextView mProposalInfoView;
        public final TextView mWithInfoView;
        public int mPosition = -1;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.iv_imageView);
            mTitleView = (TextView) view.findViewById(R.id.tv_title);
            mProposalInfoView = (TextView) view.findViewById(R.id.tv_proposalInfo);
            mWithInfoView = (TextView) view.findViewById(R.id.tv_withInfo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
