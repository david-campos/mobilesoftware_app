package com.campos.david.appointments.activityAppointment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.campos.david.appointments.R;

public class AppointmentActivity extends AppCompatActivity {

    public static final String EXTRA_APPOINTMENT_ID = "app-id";
    public static final String EXTRA_USER_IS_CREATOR = "user-creator";
    public static final String EXTRA_APPOINTMENT_CLOSED = "app-closed";
    public static final String EXTRA_APPOINTMENT_NAME = "app-name";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private AppointmentPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        Intent intent = getIntent();
        if (intent != null) {
            int id = intent.getIntExtra(EXTRA_APPOINTMENT_ID, 0);
            boolean userIsCreator = intent.getBooleanExtra(EXTRA_USER_IS_CREATOR, false);
            boolean appointmentClosed = intent.getBooleanExtra(EXTRA_APPOINTMENT_CLOSED, false);
            String appointmentName = intent.getStringExtra(EXTRA_APPOINTMENT_NAME);

            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                bar.setSubtitle(appointmentName);
            }

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new AppointmentPagerAdapter(id, userIsCreator, appointmentClosed, getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter is done in onResume
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(1);
            mViewPager.setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(this.findViewById(android.R.id.content), R.string.something_wrong,
                    Snackbar.LENGTH_INDEFINITE);
        }
    }
}
