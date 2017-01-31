package com.campos.david.appointments;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.campos.david.appointments.model.DBContract;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements AppointmentListFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        if (viewPager != null && tabLayout != null) {
            // Set PagerAdapter so that it can display items
            viewPager.setAdapter(new MainActivityPagerAdapter(getSupportFragmentManager(),
                    MainActivity.this));
            viewPager.setCurrentItem(1);
            // Give the TabLayout the ViewPager
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
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

        switch (id)
        {
            case R.id.action_settings:
                Intent throwSettingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(throwSettingsIntent);
                return true;
            case R.id.action_bd_dummy_data:
                ContentValues values = new ContentValues();
                values.put(DBContract.AppointmentTypesEntry.COLUMN_NAME, "A");
                values.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION, "A");
                values.put(DBContract.AppointmentTypesEntry.COLUMN_ICON, "1");
                Uri uriType = getContentResolver().insert(DBContract.AppointmentTypesEntry.CONTENT_URI, values);
                values.clear();
                values.put(DBContract.UsersEntry.COLUMN_NAME, "A");
                values.put(DBContract.UsersEntry.COLUMN_PICTURE, "0");
                values.put(DBContract.UsersEntry.COLUMN_PHONE, "0");
                Uri uriUser = getContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, values);

                if(uriType != null && uriUser != null) {
                    String[] apps = {"Some beers", "Julien's birthday", "Romantic dinner", "Studying night"};
                    String[] places = {"Biblioteka", "Niebostan", "Tango Steak House", "Lordys"};
                    // Accepted (created)
                    int i;
                    for(i=1; i <= apps.length; i++) {
                        values.clear();
                        values.put(DBContract.AppointmentsEntry._ID, i);
                        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, 0); // There is no foreign key check
                        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType.getLastPathSegment());
                        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, (String) null);
                        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
                        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "descr");
                        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, apps[i-1]);
                        Uri app = getContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
                        if(app == null) continue;
                        values.clear();
                        values.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, i);
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, places[i-1]);
                        values.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
                                (long) ((new Date()).getTime() + Math.round(Math.random() * 10000000000L)));
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, 0);
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, 0);
                        Uri uri = getContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values);
                        if(uri == null) continue;
                        values.clear();
                        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, uri.getLastPathSegment());
                        getContentResolver().update(app, values, null, null);
                    }
                    // Others
                    String[] states = {"pending", "refused"};
                    apps = new String[]{"Buy some books", "Farewell party", "Go shopping", "Cry together", "Do mobile software"};
                    places = new String[]{"Mall", "Narutowicza, nr 30", "Manufaktura", "Lumumby", "Dom studenta nr IX"};
                    for(int j=i; i-j <= apps.length; i++) {
                        values.clear();
                        values.put(DBContract.AppointmentsEntry._ID, i);
                        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, 0); // There is no foreign key check
                        values.put(DBContract.AppointmentsEntry.COLUMN_TYPE, uriType.getLastPathSegment());
                        values.put(DBContract.AppointmentsEntry.COLUMN_CREATOR, uriUser.getLastPathSegment());
                        values.put(DBContract.AppointmentsEntry.COLUMN_CLOSED, "0");
                        values.put(DBContract.AppointmentsEntry.COLUMN_DESCRIPTION, "descr");
                        values.put(DBContract.AppointmentsEntry.COLUMN_NAME, apps[i-j]);
                        Uri app = getContentResolver().insert(DBContract.AppointmentsEntry.CONTENT_URI, values);
                        if(app == null) continue;
                        values.clear();
                        values.put(DBContract.PropositionsEntry.COLUMN_APPOINTMENT, i);
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_NAME, places[i-j]);
                        values.put(DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
                                (long) ((new Date()).getTime() + Math.round(Math.random() * 10000000000L)));
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LAT, 0);
                        values.put(DBContract.PropositionsEntry.COLUMN_PLACE_LON, 0);
                        Uri uri = getContentResolver().insert(DBContract.PropositionsEntry.CONTENT_URI, values);
                        if(uri == null) continue;
                        values.clear();
                        values.put(DBContract.AppointmentsEntry.COLUMN_CURRENT_PROPOSAL, uri.getLastPathSegment());
                        getContentResolver().update(app, values, null, null);
                        values.clear();
                        values.put(DBContract.InvitationsEntry.COLUMN_USER, (String) null);
                        values.put(DBContract.InvitationsEntry.COLUMN_APPOINTMENT, app.getLastPathSegment());
                        values.put(DBContract.InvitationsEntry.COLUMN_STATE,
                                states[(int)Math.floor(Math.random() * states.length)]);
                        getContentResolver().insert(DBContract.InvitationsEntry.CONTENT_URI, values);
                    }
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onListFragmentInteraction(int position) {

    }
}
