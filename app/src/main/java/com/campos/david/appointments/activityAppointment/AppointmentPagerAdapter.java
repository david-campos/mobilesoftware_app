package com.campos.david.appointments.activityAppointment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * AppointmentPagerAdapter
 */
public class AppointmentPagerAdapter extends FragmentPagerAdapter {
    public static final String[] PAGE_NAMES = {"Invited", "Main", "Suggestions"};

    public static final int PAGE_INVITED = 0;
    public static final int PAGE_MAIN = 1;
    public static final int PAGE_SUGGESTIONS = 2;


    private int mAppointmentId;
    private boolean mAppointmentClosed;
    private boolean mUserIsCreator;

    public AppointmentPagerAdapter(int appointmentId, boolean userIsCreator, boolean appointmentClosed,
                                   FragmentManager fm) {
        super(fm);
        mUserIsCreator = userIsCreator;
        mAppointmentId = appointmentId;
        mAppointmentClosed = appointmentClosed;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case PAGE_INVITED:
                return InvitedListFragment.newInstance(mAppointmentId);
            case PAGE_MAIN:
                return MainFragment.newInstance(mAppointmentId, mAppointmentClosed, mUserIsCreator);
            case PAGE_SUGGESTIONS:
                return SuggestionsFragment.newInstance(mAppointmentId);
        }
        throw new IndexOutOfBoundsException("Position " + position + " isn't accepted for this pager.");
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
            return PAGE_NAMES[position];
        }
        return null;
    }
}
