package com.campos.david.appointments.activityAppointment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.campos.david.appointments.services.AppointmentDiscussionService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Main fragment for the AppointmentActivity when the appointment has not been created by the user
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, ReasonPickerDialog.ReasonPickerDialogListener {
    public static final int LOADER_APPOINTMENT = 0;

    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String[] PROJECTION = {
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_DESCRIPTION,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_LON,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_LAT,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_PLACE_NAME,
            DBContract.PropositionsEntry.TABLE_NAME + "." + DBContract.PropositionsEntry.COLUMN_TIMESTAMP,
            DBContract.AppointmentsEntry.TABLE_NAME + "." + DBContract.AppointmentsEntry.COLUMN_CLOSED,
            DBContract.InvitationsEntry.TABLE_NAME + "." + DBContract.InvitationsEntry.COLUMN_STATE};

    private static final int COL_DESCRIPTION = 0;
    private static final int COL_LON = 1;
    private static final int COL_LAT = 2;
    private static final int COL_PLACE = 3;
    private static final int COL_TIMESTAMP = 4;
    private static final int COL_CLOSED = 5;
    private static final int COL_STATE = 6;

    private static final String APPOINTMENT_ID = "app-id";
    private static final String APPOINTMENT_CLOSE = "app-close";
    private static final String APPOINTMENT_MINE = "app-mine";

    private static final int REQUEST_PROPOSAL = 0;
    private static final int REQUEST_REFUSE = 1;

    private int mAppointmentId = 0;
    private boolean mIsMyAppointment;
    private GoogleMap mMap = null;
    private Cursor mCurrentCursor = null;
    private String mState = null;
    private Boolean mClosed = null;
    private long mPickedTime;

    private boolean mWithMap;
    private MapView mMapView;
    private View mMainView = null;
    private TextView mAddressView;
    private TextView mDescriptionView;
    private TextView mTimestampView;
    private Button mCloseDiscussion;
    private Button mSuggestChange;
    private Button mAcceptInvitation;
    private Button mRefuseInvitation;

    public MainFragment() {
    }

    public static MainFragment newInstance(int appointmentId, boolean appointmentClosed, boolean isMine) {
        Bundle arguments = new Bundle(3);
        arguments.putInt(APPOINTMENT_ID, appointmentId);
        arguments.putBoolean(APPOINTMENT_CLOSE, appointmentClosed);
        arguments.putBoolean(APPOINTMENT_MINE, isMine);
        MainFragment newMe = new MainFragment();
        newMe.setArguments(arguments);
        return newMe;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        mMainView = inflater.inflate(R.layout.fragment_appointment_main, container, false);

        if (args != null) {
            mMapView = (MapView) mMainView.findViewById(R.id.mapView);

            mAddressView = (TextView) mMainView.findViewById(R.id.tv_address);
            mDescriptionView = (TextView) mMainView.findViewById(R.id.tv_description);
            mTimestampView = (TextView) mMainView.findViewById(R.id.tv_timestamp);

            mAppointmentId = args.getInt(APPOINTMENT_ID);
            mClosed = args.getBoolean(APPOINTMENT_CLOSE);
            mIsMyAppointment = args.getBoolean(APPOINTMENT_MINE);

            mWithMap = GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(getContext()) == ConnectionResult.SUCCESS &&
                    (getContext().getResources()
                            .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

            if (mWithMap) {
                mMapView.onCreate(savedInstanceState);
                mMapView.onResume();
                try {
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMapView.getMapAsync(this);
            } else {
                mMapView.setVisibility(View.GONE);
            }

            mAcceptInvitation = (Button) mMainView.findViewById(R.id.btn_acceptInvitation);
            mRefuseInvitation = (Button) mMainView.findViewById(R.id.btn_refuseInvitation);
            mSuggestChange = (Button) mMainView.findViewById(R.id.btn_suggestChange);
            mCloseDiscussion = (Button) mMainView.findViewById(R.id.btn_closeDiscussion);

            mSuggestChange.setText(mIsMyAppointment ? getString(R.string.change_proposal) : getString(R.string.suggest_change));

            mAcceptInvitation.setEnabled(false);
            mSuggestChange.setEnabled(false);
            mRefuseInvitation.setEnabled(false);
            mCloseDiscussion.setEnabled(false);

            mSuggestChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog();
                }
            });

            if (mIsMyAppointment) {
                mAcceptInvitation.setVisibility(View.GONE);
                mRefuseInvitation.setVisibility(View.GONE);
                mCloseDiscussion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleClosed();
                    }
                });
            } else {
                mCloseDiscussion.setVisibility(View.GONE);
                mAcceptInvitation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleAccepted();
                    }
                });
                mRefuseInvitation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleRefused();
                    }
                });
            }
        }
        return mMainView;
    }

    private void throwAppointmentDiscussionService(@NonNull String action) {
        throwAppointmentDiscussionService(action, null);
    }

    private void throwAppointmentDiscussionService(@NonNull String action, @Nullable String reason) {
        Intent throwService = new Intent(getActivity().getApplicationContext(),
                AppointmentDiscussionService.class);
        throwService.setAction(action);
        if (reason != null) {
            throwService.putExtra(AppointmentDiscussionService.EXTRA_REASON, reason);
        }
        throwService.putExtra(AppointmentDiscussionService.EXTRA_APPOINTMENT, mAppointmentId);
        getActivity().startService(throwService);
    }

    public void toggleClosed() {
        if (mClosed != null) {
            if (mClosed) {
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_OPEN);
            } else {
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_CLOSE);
            }
            mClosed = null;
        }
    }

    public void toggleAccepted() {
        if (mState != null) {
            if (mState.equals("accepted")) {
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_SET_PENDING);
            } else {
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_ACCEPT);
            }
            mState = null;
        }
    }

    public void toggleRefused() {
        if (mState != null) {
            if (mState.equals("refused")) {
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_SET_PENDING);
            } else {
                ReasonPickerDialog dialog = new ReasonPickerDialog();
                dialog.setReasonPickedListener(this);
                dialog.setRequestId(REQUEST_REFUSE);
                dialog.show(getFragmentManager(), "ReasonPickerDialog");
            }
            mState = null;
        }
    }

    @Override
    public void reasonPicked(String reasonName, final int requestId) {
        switch (requestId) {
            case REQUEST_REFUSE:
                throwAppointmentDiscussionService(AppointmentDiscussionService.ACTION_REFUSE, reasonName);
                break;
            case REQUEST_PROPOSAL:
                confirmProposalCreation(reasonName);
                break;
        }
    }

    private void showDatePickerDialog() {
        // Get Current Date
        Calendar c = Calendar.getInstance();
        if (mCurrentCursor != null && mCurrentCursor.moveToFirst() && !mCurrentCursor.isNull(COL_TIMESTAMP)) {
            c.setTimeInMillis(mCurrentCursor.getLong(COL_TIMESTAMP) * 1000);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar newTimestampValue = Calendar.getInstance();
                        newTimestampValue.set(Calendar.YEAR, year);
                        newTimestampValue.set(Calendar.MONTH, monthOfYear);
                        newTimestampValue.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        showTimePickerDialog(newTimestampValue);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final Calendar calendar) {
        // Get Current Time
        Calendar c = Calendar.getInstance();
        if (mCurrentCursor != null && mCurrentCursor.moveToFirst() && !mCurrentCursor.isNull(COL_TIMESTAMP)) {
            c.setTimeInMillis(mCurrentCursor.getLong(COL_TIMESTAMP) * 1000);
        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        // Convert to UTC
                        mPickedTime = calendar.getTimeInMillis();
                        showReasonPickerDialogForProposal();

                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showReasonPickerDialogForProposal() {
        if (!mIsMyAppointment) {
            ReasonPickerDialog dialog = new ReasonPickerDialog();
            dialog.setReasonPickedListener(this);
            dialog.setRequestId(REQUEST_PROPOSAL);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonPickerDialog");
        } else {
            // If it is my appointment we can stop here
            confirmProposalCreation(null);
        }
    }

    private void confirmProposalCreation(final String reasonName) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mPickedTime);
        String pickedTimeStr = getString(R.string.timestamp_format,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

        String message;
        if (reasonName != null) {
            message = getString(R.string.message_suggest_change, pickedTimeStr, reasonName);
        } else {
            message = getString(R.string.message_accept_suggestion, pickedTimeStr);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_change_time)
                .setMessage(message)
                .setIcon(R.drawable.ic_done)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent proposalCreationIntent = new Intent(getActivity().getApplicationContext(),
                                AppointmentDiscussionService.class);
                        proposalCreationIntent.setAction(AppointmentDiscussionService.ACTION_CREATE_PROPOSAL);
                        proposalCreationIntent.putExtra(AppointmentDiscussionService.EXTRA_APPOINTMENT, mAppointmentId);
                        proposalCreationIntent.putExtra(AppointmentDiscussionService.EXTRA_REASON, reasonName);
                        proposalCreationIntent.putExtra(AppointmentDiscussionService.EXTRA_TIMESTAMP, mPickedTime);
                        getActivity().startService(proposalCreationIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void updateMap() {
        if (mWithMap && mCurrentCursor != null && mMap != null) {
            mMap.clear();
            if (mCurrentCursor.moveToFirst()) {
                double coordsLat = mCurrentCursor.getDouble(COL_LAT);
                double coordsLon = mCurrentCursor.getDouble(COL_LON);
                String place = mCurrentCursor.getString(COL_PLACE);
                long timestamp = mCurrentCursor.getLong(COL_TIMESTAMP);

                // For dropping a marker at a point on the Map
                float hash = (mAppointmentId * 2267 % 36000) / 100.0f;
                LatLng pos = new LatLng(coordsLat, coordsLon);
                mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(place)
                        .icon(BitmapDescriptorFactory.defaultMarker(hash))
                        .snippet(SimpleDateFormat.getDateTimeInstance(
                                java.text.SimpleDateFormat.LONG, SimpleDateFormat.SHORT)
                                .format(timestamp)));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(12).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    public void onStart() {
        super.onStart();
        GoogleApiAvailability gApiAvailability = GoogleApiAvailability.getInstance();
        int googleServicesResult = gApiAvailability
                .isGooglePlayServicesAvailable(getContext());
        if (googleServicesResult != ConnectionResult.SUCCESS) {
            String text = getContext().getString(R.string.map_impossible_to_display);
            switch (googleServicesResult) {
                case ConnectionResult.SERVICE_MISSING:
                    text += getContext().getString(R.string.google_play_missing);
                    break;
                case ConnectionResult.SERVICE_DISABLED:
                    text += getContext().getString(R.string.google_play_disabled);
                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    text += getContext().getString(R.string.google_play_out_of_date);
                    break;
                case ConnectionResult.SERVICE_UPDATING:
                    text += getContext().getString(R.string.google_play_updating);
                    break;
                case ConnectionResult.SERVICE_INVALID:
                    text += getContext().getString(R.string.google_play_invalid);
                    break;
            }
            final Snackbar snack = Snackbar.make(mMainView, text, Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getContext().getString(R.string.text_accept), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snack.dismiss();
                }
            }).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mCurrentCursor != null) {
            updateMap();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWithMap && mMapView != null)
            mMapView.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWithMap && mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWithMap && mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mWithMap && mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_APPOINTMENT, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mIsMyAppointment) {
            String[] projection = new String[PROJECTION.length - 1];
            System.arraycopy(PROJECTION, 0, projection, 0, projection.length);
            return new CursorLoader(getActivity(),
                    Uri.withAppendedPath(DBContract.AppointmentsEntry.CONTENT_URI, Integer.toString(mAppointmentId)),
                    projection, null, null, null);
        } else {
            return new CursorLoader(getActivity(),
                    Uri.withAppendedPath(DBContract.AppointmentsEntry.CONTENT_URI, "invited/" + mAppointmentId),
                    PROJECTION, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCurrentCursor = data;

        if (data.moveToFirst() &&
                !(data.isNull(COL_PLACE) || data.isNull(COL_DESCRIPTION) ||
                        data.isNull(COL_TIMESTAMP) || data.isNull(COL_CLOSED))) {
            String place = data.getString(COL_PLACE);
            String description = data.getString(COL_DESCRIPTION);
            long timestampLong = data.getLong(COL_TIMESTAMP);
            mClosed = (data.getInt(COL_CLOSED) != 0);

            mAddressView.setText(place);
            mDescriptionView.setText(description);

            Calendar calendar = Calendar.getInstance();
            // Multiply database value by 1000 bc Date constructor expects milliseconds
            calendar.setTimeInMillis(1000 * timestampLong);
            String date = getString(R.string.timestamp_format,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            mTimestampView.setText(date);

            mSuggestChange.setEnabled(!mClosed);
            mCloseDiscussion.setEnabled(true);
            if (mClosed) {
                mCloseDiscussion.setText(R.string.open_discussion);
                mCloseDiscussion.getBackground().setColorFilter(
                        getResources().getColor(R.color.btnClosed), PorterDuff.Mode.SRC_ATOP);
                mCloseDiscussion.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_event_available, 0, 0, 0);
            } else {
                mCloseDiscussion.setText(R.string.close_discussion);
                mCloseDiscussion.getBackground().clearColorFilter();
                mCloseDiscussion.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_event_busy, 0, 0, 0);
            }

            mAcceptInvitation.getBackground().clearColorFilter();
            mRefuseInvitation.getBackground().clearColorFilter();
            mAcceptInvitation.setEnabled(true);
            mRefuseInvitation.setEnabled(true);

            if (!mIsMyAppointment) {
                mState = data.getString(COL_STATE);
                switch (mState) {
                    case "pending":
                        mAcceptInvitation.setText(getString(R.string.text_accept_invitation));
                        mRefuseInvitation.setText(getString(R.string.refuse));
                        break;
                    case "refused":
                        mAcceptInvitation.setText(getString(R.string.text_accept_invitation));
                        mRefuseInvitation.setText(getString(R.string.cancel_refuse));
                        mRefuseInvitation.getBackground().setColorFilter(
                                getResources().getColor(R.color.btnRefused), PorterDuff.Mode.SRC_ATOP);
                        break;
                    case "accepted":
                        mAcceptInvitation.setText(getString(R.string.text_cancel_accepted_invitation));
                        mRefuseInvitation.setText(getString(R.string.refuse));
                        mAcceptInvitation.getBackground().setColorFilter(
                                getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                        break;
                }
            }
            updateMap();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCurrentCursor = null;
    }
}
