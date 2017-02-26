package com.campos.david.appointments.activityMain;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.campos.david.appointments.AboutActivity;
import com.campos.david.appointments.ApiUrlDialog;
import com.campos.david.appointments.R;
import com.campos.david.appointments.activityNewAppointment.NewAppointmentActivity;
import com.campos.david.appointments.activitySettings.SettingsActivity;
import com.campos.david.appointments.services.UpdateAppointmentsService;
import com.campos.david.appointments.services.UpdateUsersService;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        FloatingActionButton fabNewAppointment =
                (FloatingActionButton) findViewById(R.id.fab_newAppointment);

        // Update appointments
        Intent throwingService = new Intent(getApplicationContext(), UpdateAppointmentsService.class);
        startService(throwingService);
        // Update users
        Intent throwingUsersUpdate = new Intent(getApplicationContext(), UpdateUsersService.class);
        startService(throwingUsersUpdate);

        if (viewPager != null && tabLayout != null) {
            // Set PagerAdapter so that it can display items
            viewPager.setAdapter(new MainActivityPagerAdapter(getSupportFragmentManager(),
                    MainActivity.this));
            viewPager.setCurrentItem(1);
            // Give the TabLayout the ViewPager
            tabLayout.setupWithViewPager(viewPager);
        }

        if (fabNewAppointment != null) {
            fabNewAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newAppointment = new Intent(getApplicationContext(), NewAppointmentActivity.class);
                    startActivity(newAppointment);
                }
            });
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                // Display settings
                Intent throwSettingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(throwSettingsIntent);
                return true;
            case R.id.action_about:
                Intent throwAboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(throwAboutIntent);
                return true;
            case R.id.action_change_ip:
                ApiUrlDialog dialog = new ApiUrlDialog();
                dialog.show(getFragmentManager(), "NoticeDialogFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
