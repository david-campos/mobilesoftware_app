package com.campos.david.appointments.activityNewAppointment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.campos.david.appointments.R;

import java.util.List;

public class NewAppointmentActivity extends AppCompatActivity implements PickUserFragment.Callbacks {

    private boolean mButtonShown = false;
    private Fragment mCurrentFragment;

    private List<Integer> mSelectedUsers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_appointment);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mCurrentFragment = new PickUserFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mCurrentFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_appointment, menu);
        final MenuItem buttonDone = menu.findItem(R.id.action_users_picked);
        buttonDone.setVisible(mButtonShown);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_users_picked:
                if (mCurrentFragment instanceof PickUserFragment) {
                    // Change to second fragment
                    mSelectedUsers = ((PickUserFragment) mCurrentFragment).getSelectedUsers();
                    hideDone();
                    mCurrentFragment = new AppointmentDefinitionFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, mCurrentFragment)
                            .commit();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showDone() {
        mButtonShown = true;
        invalidateOptionsMenu();
    }

    @Override
    public void hideDone() {
        mButtonShown = false;
        invalidateOptionsMenu();
    }
}
