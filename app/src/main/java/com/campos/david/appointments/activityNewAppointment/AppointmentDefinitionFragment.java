package com.campos.david.appointments.activityNewAppointment;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;
import com.campos.david.appointments.services.CreateAppointmentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentDefinitionFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public AppointmentDefinitionFragment() {
        // Required empty public constructor
    }

    private static String TAG = AppointmentDefinitionFragment.class.getSimpleName();

    private interface AppointmentTypesQuery {
        String[] PROJECTION = {
                DBContract.AppointmentsEntry._ID,
                DBContract.AppointmentTypesEntry.COLUMN_NAME,
                DBContract.AppointmentTypesEntry.COLUMN_ICON
        };

        int COL_NAME = 1;
        int COL_ICON = 2;

        String[] FROM_COLS = {
                DBContract.AppointmentTypesEntry.COLUMN_NAME
        };
    }

    public static final int PLACE_PICKER_REQUEST = 0;
    private static final int REQUEST_FINE_LOCATION = 0;

    private EditText mNameEditText;
    private EditText mDescriptionText;
    private ImageView mSelectedTypeIcon;
    private Spinner mTypeSpinner;
    private Switch mCloseSwitch;
    private TextView mTimestampTextView;
    private Button mTimestampButton;
    private MapView mPlaceMapView;
    private Button mPlaceButton;
    private TextView mPlaceTextView;

    private SimpleCursorAdapter mAdapterTypes;
    private GoogleApiClient mGoogleApiClient;
    private Long mTimestampValue = null;
    private GoogleMap mMap = null;
    private Place mPlace = null;
    private int mTextViewsColor;

    private boolean mLocationRequired = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_appointment_definition, container, false);
        saveViews(mainView);

        mTextViewsColor = mPlaceTextView.getCurrentTextColor();

        updateTimestampText();
        mPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceTextView.setTextColor(mTextViewsColor);
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                    displayPlacePicker();
                else
                    mLocationRequired = true;
            }
        });

        initAppointmentTypesAdapter();
        initTimestampButton();
        initMap(savedInstanceState);
        initGoogleApi();

        mNameEditText.requestFocus();

        return mainView;
    }

    private void initAppointmentTypesAdapter() {
        SharedPreferences preferences = getContext().getSharedPreferences(
                getContext().getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        final String apiUri = getContext().getString(R.string.api_protocol) +
                preferences.getString(getContext().getString(R.string.api_uri_key),
                        getContext().getString(R.string.api_uri_default));

        mAdapterTypes = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_spinner_item,
                null, AppointmentTypesQuery.FROM_COLS, new int[]{android.R.id.text1}, 0);
        mAdapterTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                int iconId = cursor.getInt(AppointmentTypesQuery.COL_ICON);
                String uri = getString(R.string.api_types_format, apiUri, iconId);
                Picasso.with(getContext())
                        .load(uri)
                        .placeholder(R.drawable.unknown_type)
                        .into(mSelectedTypeIcon);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        mTypeSpinner.setAdapter(mAdapterTypes);
    }

    private void initTimestampButton() {
        mTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimestampTextView.setTextColor(mTextViewsColor);
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        // Get Current Date
        Calendar c = Calendar.getInstance();
        if (mTimestampValue != null) {
            c.setTimeInMillis(mTimestampValue);
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
        if (mTimestampValue != null) {
            c.setTimeInMillis(mTimestampValue);
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
                        mTimestampValue = calendar.getTimeInMillis();
                        updateTimestampText();
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void updateTimestampText() {
        if (mTimestampValue != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mTimestampValue);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            mTimestampTextView.setText(
                    getString(R.string.appointment_timestamp,
                            getString(R.string.timestamp_format, year, month, day, hour, minute)));
        } else {
            mTimestampTextView.setText(getString(R.string.appointment_timestamp,
                    getString(R.string.text_not_selected)));
        }
    }

    private void initMap(Bundle savedInstanceState) {
        mPlaceMapView.onCreate(savedInstanceState);
        mPlaceMapView.onResume();
        new Thread() {
            @Override
            public void run() {
                try {
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        mPlaceMapView.getMapAsync(AppointmentDefinitionFragment.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void saveViews(View view) {
        mDescriptionText = (EditText) view.findViewById(R.id.et_appointment_description);
        mSelectedTypeIcon = (ImageView) view.findViewById(R.id.iv_selected_type_icon);
        mCloseSwitch = (Switch) view.findViewById(R.id.sw_open_discussion);
        mTimestampButton = (Button) view.findViewById(R.id.btn_change_timestamp);
        mNameEditText = (EditText) view.findViewById(R.id.et_appointment_name);
        mTypeSpinner = (Spinner) view.findViewById(R.id.sp_type_of_appointments);
        mPlaceMapView = (MapView) view.findViewById(R.id.mv_locationMap);
        mTimestampTextView = (TextView) view.findViewById(R.id.tv_selected_timestamp);
        mPlaceTextView = (TextView) view.findViewById(R.id.tv_place_name);
        mPlaceButton = (Button) view.findViewById(R.id.btn_change_place);
    }

    private void initGoogleApi() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        } else {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Callbacks received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We have the FINE_LOCATION permission, try to connect now
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Called when mGoogleApiClient gets to connect.
     *
     * @param bundle a bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLocationRequired)
            displayPlacePicker();
    }

    private void displayPlacePicker() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            getActivity().startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            mPlace = PlacePicker.getPlace(getActivity(), data);
            mPlaceTextView.setText(mPlace.getName().toString());
            updateMapToLocation();
        }
    }

    private void updateMapToLocation() {
        if (mMap != null) {
            mMap.clear();
            LatLng pos = mPlace.getLatLng();
            MarkerOptions options = new MarkerOptions()
                    .position(pos);

            if (mPlace.getName() != "")
                options.title(mPlace.getName().toString());
            else
                options.title("Unknown");

            mMap.addMarker(options);

            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pos).zoom(12).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Log.e(TAG, "mMap is null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlaceMapView != null)
            mPlaceMapView.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlaceMapView != null)
            mPlaceMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlaceMapView != null)
            mPlaceMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mPlaceMapView != null)
            mPlaceMapView.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), DBContract.AppointmentTypesEntry.CONTENT_URI,
                AppointmentTypesQuery.PROJECTION, null, null, PickUsersAdapter.Query.PROJECTION[0]);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapterTypes.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapterTypes.swapCursor(null);
    }

    public Bundle onTryToCreateAppointment() {
        View requestFocus = null;
        boolean failed = false;
        // Check that everything is set, if something is not indicate it
        if (mPlace == null) {
            mPlaceTextView.setTextColor(getResources().getColor(R.color.colorBlock));
            failed = true;
        }

        if (mTimestampValue == null) {
            mTimestampTextView.setTextColor(getResources().getColor(R.color.colorBlock));
            failed = true;
        }

        String description = stylizeText(mDescriptionText.getText().toString());
        if (description.equals("")) {
            mDescriptionText.setText(description);
            mDescriptionText.setError(getString(R.string.description_not_valid_text));
            requestFocus = mDescriptionText;
            failed = true;
        }

        String name = stylizeText(mNameEditText.getText().toString());
        if (name.equals("")) {
            mNameEditText.setText(name);
            mNameEditText.setError(getString(R.string.name_not_valid_text));
            requestFocus = mNameEditText;
            failed = true;
        }

        if (failed) {
            if (requestFocus != null)
                requestFocus.requestFocus();
            return null;
        }

        Bundle result = new Bundle();
        result.putString(CreateAppointmentService.ExtrasKeys.PLACE, mPlace.getName().toString());
        result.putDouble(CreateAppointmentService.ExtrasKeys.LAT, mPlace.getLatLng().latitude);
        result.putDouble(CreateAppointmentService.ExtrasKeys.LON, mPlace.getLatLng().longitude);
        // mTimestampValue should be UTC
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(mTimestampValue);
        result.putString(CreateAppointmentService.ExtrasKeys.TIMESTAMP,
                getString(R.string.api_timestamp_format,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE)));
        result.putBoolean(CreateAppointmentService.ExtrasKeys.CLOSED, !mCloseSwitch.isChecked());
        result.putString(CreateAppointmentService.ExtrasKeys.TYPE,
                ((Cursor) mTypeSpinner.getSelectedItem()).getString(AppointmentTypesQuery.COL_NAME));
        result.putString(CreateAppointmentService.ExtrasKeys.DESCRIPTION, description);
        result.putString(CreateAppointmentService.ExtrasKeys.NAME, name);
        return result;
    }

    private String stylizeText(String text) {
        // Trim the text, delete double line jumps
        if (text == null) {
            return null;
        } else {
            return text.replaceAll("^(\\s+)|(\\s+)$|\\n(\\n)", "");
        }
    }
}
