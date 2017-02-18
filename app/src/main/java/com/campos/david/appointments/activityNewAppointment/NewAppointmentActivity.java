package com.campos.david.appointments.activityNewAppointment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.campos.david.appointments.R;
import com.campos.david.appointments.activityMain.MainActivity;
import com.campos.david.appointments.services.CreateAppointmentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Collection;

public class NewAppointmentActivity extends AppCompatActivity implements PickUsersFragment.Callbacks {
    private final static String TAG = NewAppointmentActivity.class.getSimpleName();

    private boolean mShowButtonNext = false;
    private Fragment mCurrentFragment = null;
    private Fragment mOtherFragment = null;

    private Collection<String> mSelectedUsers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_appointment);

        if (checkMapsAvailable()) {
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            mCurrentFragment = new PickUsersFragment();
            // We add the first fragment
            // the second one will be added in onResume
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, mCurrentFragment)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppointmentDefinitionFragment.PLACE_PICKER_REQUEST) {
            if (mCurrentFragment instanceof AppointmentDefinitionFragment) {
                mCurrentFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private boolean checkMapsAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Dialog dialog = availability.getErrorDialog(this, result, 0);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onBackPressed();
                }
            });
            dialog.show();
        }
        return result == ConnectionResult.SUCCESS;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_appointment, menu);
        final MenuItem buttonNext = menu.findItem(R.id.action_users_picked);
        if (buttonNext != null)
            buttonNext.setVisible(mShowButtonNext);
        final MenuItem buttonDone = menu.findItem(R.id.action_create_appointment);
        if (buttonDone != null)
            buttonDone.setVisible(mCurrentFragment instanceof AppointmentDefinitionFragment);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (mCurrentFragment instanceof PickUsersFragment) {
                    onBackPressed();
                } else if (mCurrentFragment instanceof AppointmentDefinitionFragment) {
                    // Exchange current and other fragments
                    Fragment aux = mCurrentFragment;
                    mCurrentFragment = mOtherFragment;
                    mOtherFragment = aux;
                    getSupportFragmentManager().beginTransaction()
                            .hide(mOtherFragment)
                            .show(mCurrentFragment)
                            .commit();
                    showNext();
                }
                break;
            case R.id.action_users_picked:
                if (mCurrentFragment instanceof PickUsersFragment) {
                    // Take selected users
                    mSelectedUsers = ((PickUsersFragment) mCurrentFragment).getSelectedUsers();
                    // Add the other fragment if not added yet
                    if (mOtherFragment == null) {
                        mOtherFragment = new AppointmentDefinitionFragment();
                        getSupportFragmentManager().beginTransaction()
                                .add(android.R.id.content, mOtherFragment)
                                .hide(mOtherFragment)
                                .commit();
                    }
                    Fragment aux = mCurrentFragment;
                    mCurrentFragment = mOtherFragment;
                    mOtherFragment = aux;
                    getSupportFragmentManager().beginTransaction()
                            .hide(mOtherFragment)
                            .show(mCurrentFragment)
                            .commit();
                    hideNext();
                }
                break;
            case R.id.action_create_appointment:
                tryToCreateAppointment();
        }
        return super.onOptionsItemSelected(item);
    }

    public void tryToCreateAppointment() {
        if (!(mCurrentFragment instanceof AppointmentDefinitionFragment))
            return;
        Bundle result = ((AppointmentDefinitionFragment) mCurrentFragment).onTryToCreateAppointment();
        if (result != null) {
            // Add the list of invited users to the result
            String[] invitations = new String[mSelectedUsers.size()];
            invitations = mSelectedUsers.toArray(invitations);
            result.putStringArray(CreateAppointmentService.ExtrasKeys.USERS, invitations);
            // Create intent and start the service
            Intent throwingService = new Intent(getApplicationContext(), CreateAppointmentService.class);
            throwingService.putExtras(result);
            startService(throwingService);
            // Go to main activity
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivityIntent);
        }
    }

    @Override
    public void showNext() {
        mShowButtonNext = true;
        invalidateOptionsMenu();
    }

    @Override
    public void hideNext() {
        mShowButtonNext = false;
        invalidateOptionsMenu();
    }
}
