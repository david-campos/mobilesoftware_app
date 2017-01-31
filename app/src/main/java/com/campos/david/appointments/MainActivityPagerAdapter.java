package com.campos.david.appointments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a>
 */
public class MainActivityPagerAdapter extends FragmentPagerAdapter{
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Refused", "Accepted", "Pending" };
    private String tabAppointmentStates[] = new String[] {
            AppointmentListFragment.APPOINTMENTS_STATE_REFUSED,
            AppointmentListFragment.APPOINTMENTS_STATE_ACCEPTED,
            AppointmentListFragment.APPOINTMENTS_STATE_PENDING};
    private Context context;

    public MainActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }



    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return AppointmentListFragment.newInstance(tabAppointmentStates[position]);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
