package com.campos.david.appointments.activityAppointment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * AppointmentPagerAdapter
 */
public class AppointmentPagerAdapter extends FragmentPagerAdapter {
    public static final String[] PAGE_NAMES = {"Suggestions", "Main", "Invited"};

    private int mAppointmentId;
    private boolean mAppointmentClosed;
    private boolean mUserIsCreator;
    private Context mContext;

    public AppointmentPagerAdapter(int appointmentId, boolean userIsCreator, boolean appointmentClosed,
                                   Context context, FragmentManager fm) {
        super(fm);
        mUserIsCreator = userIsCreator;
        mAppointmentId = appointmentId;
        mAppointmentClosed = appointmentClosed;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return InvitedListFragment.newInstance(mAppointmentId);
        } else if (position == 1) {
            return MainFragment.newInstance(mAppointmentId, mAppointmentClosed, mUserIsCreator);
        } else {
            return PlaceholderFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        if (mUserIsCreator) {
            return 3;
        } else {
            return 2;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < getCount()) {
            if (mUserIsCreator) {
                return PAGE_NAMES[position];
            } else {
                return PAGE_NAMES[position + 1];
            }
        }
        return null;
    }
}
