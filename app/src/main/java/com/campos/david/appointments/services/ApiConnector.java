package com.campos.david.appointments.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.model.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Works as interface for the connections to the API.
 */
public class ApiConnector {
    private static final String TAG = ApiConnector.class.getSimpleName();

    private Context mContext;
    private SharedPreferences mSessionPreferences;
    private SharedPreferences mGeneralPreferences;

    public ApiConnector(@NonNull Context mContext) {
        this.mContext = mContext;
        mSessionPreferences = mContext.getSharedPreferences(mContext.getString(R.string.session_file_key),
                Context.MODE_PRIVATE);
        mGeneralPreferences = mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_key),
                Context.MODE_PRIVATE);
    }

    /**
     * Tries to do login (get a session id and key for a given phone)
     *
     * @param phoneNumber String phone number of the user that wants to do login
     * @return Pair with the session id and the session key or null if some problem happens
     */
    public Pair<Integer, String> login(@NonNull String phoneNumber) {
        if (phoneNumber.equals("")) {
            return null;
        }
        ContentValues cv = new ContentValues();
        cv.put(mContext.getString(R.string.query_session_phone_key), phoneNumber);
        cv.put(mContext.getString(R.string.query_request_key), mContext.getString(R.string.req_init_session));
        JSONObject result = getObjectFromApi(cv);
        if (result != null) {
            try {
                String key = result.getString(mContext.getString(R.string.response_session_key_key));
                Integer id = result.getInt(mContext.getString(R.string.response_session_id_key));
                return new Pair<>(id, key);
            } catch (JSONException e) {
                // Shouldn't happen
            }
        }
        return null;
    }

    /**
     * Tries to filter the list of phones to find that ones that are registered in the
     * API database.
     * @param phones list of phones to filter
     * @return content values with the users ready to insert into the database
     */
    public ContentValues[] filterUsers(@NonNull List<String> phones) {
        Iterator<String> iterator = phones.iterator();
        StringBuilder builder = new StringBuilder();
        for (; ; ) {
            builder.append(iterator.next());
            if (!iterator.hasNext()) {
                break;
            }
            builder.append(",");
        }
        ContentValues cv = new ContentValues();
        cv.put(mContext.getString(R.string.query_param_phones), builder.toString());
        cv.put(mContext.getString(R.string.query_request_key), mContext.getString(R.string.req_filter_user_list));
        JSONArray result = getArrayFromApi(cv);
        if (result != null) {
            try {
                ContentValues[] list = new ContentValues[result.length()];
                for (int i = 0; i < result.length(); i++) {
                    JSONObject user = result.getJSONObject(i);
                    ContentValues userCv = new ContentValues(4);
                    userCv.put(DBContract.UsersEntry._ID,
                            user.getInt(mContext.getString(R.string.response_id)));
                    userCv.put(DBContract.UsersEntry.COLUMN_NAME,
                            user.getString(mContext.getString(R.string.response_user_name)));
                    userCv.put(DBContract.UsersEntry.COLUMN_PHONE,
                            user.getString(mContext.getString(R.string.response_user_phone)));
                    userCv.put(DBContract.UsersEntry.COLUMN_PICTURE,
                            user.getInt(mContext.getString(R.string.response_user_picture)));
                    userCv.put(DBContract.UsersEntry.COLUMN_BLOCKED,
                            user.getBoolean(mContext.getString(R.string.response_user_blocked)));
                    list[i] = userCv;
                }
                return list;
            } catch (JSONException e) {
                // Shouldn't happen
            }
        }
        return null;
    }

    public Pair<ContentValues[], ContentValues[]> getAppointmentTypesAndReasons() {
        ContentValues cv = new ContentValues(1);
        cv.put(mContext.getString(R.string.query_request_key),
                mContext.getString(R.string.req_types_and_reasons));
        JSONObject result = getObjectFromApi(cv);
        try {
            JSONArray types = result.getJSONArray("appointmentTypes");
            JSONArray reasons = result.getJSONArray("reasons");

            ContentValues[] listTypes = new ContentValues[types.length()];
            for (int i = 0; i < types.length(); i++) {
                JSONObject type = types.getJSONObject(i);
                ContentValues typeCv = new ContentValues(3);
                typeCv.put(DBContract.AppointmentTypesEntry.COLUMN_NAME,
                        type.getString(mContext.getString(R.string.response_type_name)));
                typeCv.put(DBContract.AppointmentTypesEntry.COLUMN_DESCRIPTION,
                        type.getString(mContext.getString(R.string.response_type_description)));
                typeCv.put(DBContract.AppointmentTypesEntry.COLUMN_ICON,
                        type.getInt(mContext.getString(R.string.response_type_icon)));
                listTypes[i] = typeCv;
                //"name" "description" "icon"
            }

            ContentValues[] listReasons = new ContentValues[reasons.length()];
            for (int i = 0; i < reasons.length(); i++) {
                JSONObject reason = reasons.getJSONObject(i);
                ContentValues reasonCv = new ContentValues(3);
                reasonCv.put(DBContract.ReasonsEntry.COLUMN_NAME,
                        reason.getString(mContext.getString(R.string.response_reason_name)));
                reasonCv.put(DBContract.ReasonsEntry.COLUMN_DESCRIPTION,
                        reason.getString(mContext.getString(R.string.response_reason_description)));
                listReasons[i] = reasonCv;
                //"name" "description" "icon"
            }

            return new Pair<>(listTypes, listReasons);
        } catch (JSONException e) {
            // Shouldn't happen
        }
        return null;
    }

    private JSONObject getObjectFromApi(ContentValues params) {
        return getFromApi(params, JSONObject.class);
    }

    private JSONArray getArrayFromApi(ContentValues params) {
        return getFromApi(params, JSONArray.class);
    }

    /**
     * @param params Params to send to the api
     * @param type class of the desired return type (JSONArray or JSONObject)
     * @return The result sent by the api or null if there is some problem
     */
    private <T> T getFromApi(ContentValues params, Class<T> type) {
        if (!type.equals(JSONObject.class) && !type.equals(JSONArray.class))
            throw new IllegalArgumentException("The indicated type is not valid");

        T result = null;
        HttpURLConnection urlConnection = null;
        try {
            Uri connectionUri = buildConnectionUri(params);
            urlConnection = connect(connectionUri);
            String jsonStr = readInputStream(urlConnection);
            JSONObject response = new JSONObject(jsonStr);
            // Performance info (ignored)
            // JSONObject performance = response.getJSONObject("performance_info");
            if (urlConnection.getResponseCode() == mContext.getResources().getInteger(
                    R.integer.response_error_code)) {
                JSONObject errorInfo = response.getJSONObject(mContext.getString(R.string.response_result_key));
                // Here we can throw a different error following the code of API error
                throw new ApiError(
                        errorInfo.getString(mContext.getString(R.string.response_error_key)),
                        errorInfo.getInt(mContext.getString(R.string.response_error_code_key)));
            } else {
                if (type.equals(JSONObject.class)) {
                    result = type.cast(response.getJSONObject(mContext.getString(R.string.response_result_key)));
                } else if (type.equals(JSONArray.class)) {
                    result = type.cast(response.getJSONArray(mContext.getString(R.string.response_result_key)));
                }
            }
        } catch (IOException e) {
            // TODO: Maybe we are not connected, check it and display something to user
            Log.e(TAG, "error on communication", e);
        } catch (JSONException e) {
            // Response was malformed?
            Log.e(TAG, "Malformed answer?", e);
        } finally {
            // Close connection if opened
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    private Uri buildConnectionUri(ContentValues params) {
        Uri.Builder uriBdr = Uri.parse(mContext.getString(R.string.api_protocol) +
                mGeneralPreferences.getString(
                        mContext.getString(R.string.api_uri_key),
                        mContext.getString(R.string.api_uri_default))).buildUpon();
        String sessionPhone = mSessionPreferences.getString(mContext.getString(R.string.session_phone_key), null);
        String sessionKey = mSessionPreferences.getString(mContext.getString(R.string.session_key_key), null);
        Integer sessionId = mSessionPreferences.getInt(mContext.getString(R.string.session_id_key), 0);
        if (sessionPhone != null && sessionKey != null && sessionId != 0) {
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_phone_key), sessionPhone);
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_key_key), sessionKey);
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_id_key), sessionId.toString());
        }
        for (String key : params.keySet()) {
            if (params.containsKey(key)) {
                uriBdr.appendQueryParameter(key, params.getAsString(key));
            }
        }
        return uriBdr.build();
    }

    private HttpURLConnection connect(Uri uri) throws IOException {
        URL url = new URL(uri.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            //Shouldn't happen
        }
        urlConnection.connect();
        return urlConnection;
    }

    private String readInputStream(HttpURLConnection connection) throws IOException {
        BufferedReader reader = null;
        try {
            InputStream inputStream = connection.getInputStream();

            StringBuilder stringBuilder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException closingException) {
                    Log.e(TAG, "Error closing reader", closingException);
                }
            }
            throw e;
        }
    }

    public static class ApiError extends Error {
        int mErrorCode;

        public ApiError(int mErrorCode) {
            this.mErrorCode = mErrorCode;
        }

        public ApiError(String message, int mErrorCode) {
            super(message);
            this.mErrorCode = mErrorCode;
        }

        public ApiError(String message, Throwable cause, int mErrorCode) {
            super(message, cause);
            this.mErrorCode = mErrorCode;
        }

        public ApiError(Throwable cause, int mErrorCode) {
            super(cause);
            this.mErrorCode = mErrorCode;
        }

        public int getErrorCode() {
            return mErrorCode;
        }
    }
}
