package com.campos.david.appointments.activityMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} supplied by a Cursor
 */
public class AppointmentListRecyclerViewAdapter extends CursorRecyclerViewAdapter<AppointmentListRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = AppointmentListRecyclerViewAdapter.class.getSimpleName();

    private static final int MAX_DISPLAY = 4;

    private final int fIconSidePx;

    private OnListFragmentInteractionListener mListener;
    private Context mContext;
    private Map<Integer, List<String>> mAppointmentWiths = new HashMap<>();
    private Cursor mWithsCursor;
    private boolean mWithsValid;
    private String mApiUri;

    public AppointmentListRecyclerViewAdapter(Context context,
                                              OnListFragmentInteractionListener listener) {
        super(context, null);
        mContext = context;
        mWithsCursor = null;
        mWithsValid = false;
        mListener = listener;
        fIconSidePx = (int) Math.ceil(mContext.getResources().getDimension(R.dimen.appointment_type_icon_side) * Resources.getSystem().getDisplayMetrics().density);
        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_key),
                Context.MODE_PRIVATE);
        mApiUri = mContext.getString(R.string.api_protocol) + preferences.getString(mContext.getString(R.string.api_uri_key),
                mContext.getString(R.string.api_uri_default));
    }

    public Cursor swapWithsCursor(Cursor c) {
        if (mWithsCursor == c) {
            return null;
        }
        Cursor oldCursor = mWithsCursor;
        mWithsCursor = c;
        if (mWithsCursor != null) {
            loadAppointmentWiths();
            mWithsValid = true;
            notifyDataSetChanged();
        } else {
            mWithsValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    /**
     * Called by swapWithsCursor to load the data of the cursor in the map mAppointmentWiths
     */
    private void loadAppointmentWiths() {
        mAppointmentWiths.clear();
        if (mWithsValid && mWithsCursor == null) {
            return;
        }
        if (mWithsCursor.moveToFirst()) {
            do {
                int appointmentId = mWithsCursor.getInt(DBContract.UsersEntry.WITHS_APPOINTMENT_ID_COL);
                String userName = mWithsCursor.getString(DBContract.UsersEntry.WITHS_USER_NAME_COL);
                if (mAppointmentWiths.containsKey(appointmentId)) {
                    mAppointmentWiths.get(appointmentId).add(userName);
                } else {
                    List<String> newList = new ArrayList<>();
                    newList.add(userName);
                    mAppointmentWiths.put(appointmentId, newList);
                }
            } while (mWithsCursor.moveToNext());
        }
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

        long timestampInSeconds = cursor.getLong(AppointmentListFragment.CURSOR_TIMESTAMP_COL);
        Calendar nowCalendar = Calendar.getInstance();
        long distanceInSeconds = timestampInSeconds - (nowCalendar.getTimeInMillis() / 1000);
        if (distanceInSeconds < 0) {
            // Passed
            holder.mTitleView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            // Coming
            holder.mTitleView.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary));
        }
        Calendar appointmentCalendar = Calendar.getInstance();
        // Multiply database value by 1000 bc Date constructor expects milliseconds
        appointmentCalendar.setTimeInMillis(1000 * timestampInSeconds);
        String realDate = mContext.getString(R.string.timestamp_format,
                appointmentCalendar.get(Calendar.YEAR), appointmentCalendar.get(Calendar.MONTH) + 1, appointmentCalendar.get(Calendar.DAY_OF_MONTH),
                appointmentCalendar.get(Calendar.HOUR_OF_DAY), appointmentCalendar.get(Calendar.MINUTE));

        String date;
        if (nowCalendar.get(Calendar.YEAR) == appointmentCalendar.get(Calendar.YEAR)) {
            int dayDifference = appointmentCalendar.get(Calendar.DAY_OF_YEAR) - nowCalendar.get(Calendar.DAY_OF_YEAR);
            if (dayDifference == 1) {
                date = mContext.getString(R.string.tomorrow);
            } else if (dayDifference == -1) {
                date = mContext.getString(R.string.yesterday);
            } else {
                date = realDate;
            }
        } else {
            date = realDate;
        }
        holder.mProposalInfoView.setText(
                mContext.getString(R.string.format_place_and_data, place, date));

        if (mWithsValid) {
            List<String> users = mAppointmentWiths.get(cursor.getInt(AppointmentListFragment.CURSOR_ID_COL));
            StringBuilder stringBuilder = new StringBuilder("With: ");
            if (users != null && users.size() > 0) {
                int count;
                Iterator<String> usersIterator = users.iterator();
                stringBuilder.append(usersIterator.next());
                for (count = 1; count < MAX_DISPLAY && usersIterator.hasNext(); count++) {
                    stringBuilder.append(", ");
                    stringBuilder.append(usersIterator.next());
                }
                if (count < users.size()) {
                    int remaining = users.size() - count;
                    stringBuilder.append(mContext.getResources()
                            .getQuantityString(R.plurals.withs_and_x_more, remaining, remaining));
                }
            }
            holder.mWithInfoView.setText(stringBuilder.toString());
        } else {
            holder.mWithInfoView.setText(R.string.text_loading);
        }

        int iconId = cursor.getInt(AppointmentListFragment.CURSOR_TYPE_ICON_COL);
        String uri = mContext.getString(R.string.api_types_format, mApiUri, iconId);
        Picasso pic = Picasso.with(mContext);
//        pic.setIndicatorsEnabled(true);
        pic.load(uri)
                .noFade()
                .placeholder(R.drawable.unknown_type)
                .resize(fIconSidePx, fIconSidePx)
                .into(holder.mImageView);

        boolean isUserAppointment = cursor.isNull(AppointmentListFragment.CURSOR_CREATOR_COL);
        holder.mUserAppointment.setVisibility(isUserAppointment ? View.VISIBLE : View.INVISIBLE);
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
        public final ImageView mUserAppointment;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.iv_typeIcon);
            mTitleView = (TextView) view.findViewById(R.id.tv_title);
            mProposalInfoView = (TextView) view.findViewById(R.id.tv_proposalInfo);
            mWithInfoView = (TextView) view.findViewById(R.id.tv_withInfo);
            mUserAppointment = (ImageView) view.findViewById(R.id.iv_usersAppointment);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    /**
     * Interface to notify clicks on elements of the adapter
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(int position);
    }
}
