package com.campos.david.appointments.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.campos.david.appointments.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

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
        JSONObject result = getFromApi(cv);
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
     * @param params Params to send to the api
     * @return JSONObject The result sent by the api or null if there is some problem
     */
    private JSONObject getFromApi(ContentValues params) {
        JSONObject result = null;
        HttpURLConnection urlConnection = null;
        try {
            Uri connectionUri = buildConnectionUri(params);
            urlConnection = connect(connectionUri);
            String jsonStr = readInputStream(urlConnection);
            JSONObject response = new JSONObject(jsonStr);
            result = response.getJSONObject(mContext.getString(R.string.response_result_key));
            // Performance info (ignored)
            // JSONObject performance = response.getJSONObject("performance_info");
            if (urlConnection.getResponseCode() == mContext.getResources().getInteger(
                    R.integer.response_error_code)) {
                // Here we can throw a different error following the code of API error
                throw new ApiError(
                        result.getString(mContext.getString(R.string.response_error_key)),
                        result.getInt(mContext.getString(R.string.response_error_code_key)));
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
        Uri.Builder uriBdr = Uri.parse("http://" +
                mGeneralPreferences.getString(
                        mContext.getString(R.string.api_uri_key),
                        mContext.getString(R.string.api_uri_default))).buildUpon();
        String sessionPhone = mSessionPreferences.getString(mContext.getString(R.string.session_phone_key), null);
        String sessionKey = mSessionPreferences.getString(mContext.getString(R.string.session_key_key), null);
        String sessionId = mSessionPreferences.getString(mContext.getString(R.string.session_id_key), null);
        if (sessionPhone != null && sessionKey != null && sessionId != null) {
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_phone_key), sessionPhone);
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_key_key), sessionKey);
            uriBdr.appendQueryParameter(mContext.getString(R.string.query_session_id_key), sessionId);
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
