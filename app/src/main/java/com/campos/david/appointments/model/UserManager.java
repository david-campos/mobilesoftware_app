package com.campos.david.appointments.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.campos.david.appointments.R;
import com.campos.david.appointments.services.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Operations related with managing users
 */
public class UserManager {
    private Context mContext;
    private SharedPreferences mSessionPreferences;
    private static final String TAG = UserManager.class.getSimpleName();

    public UserManager(Context context) {
        this.mContext = context;
        mSessionPreferences = mContext.getSharedPreferences(mContext.getString(R.string.session_file_key),
                Context.MODE_PRIVATE);
    }

    public void insertUsers(ContentValues users[]) {
        int inserted = mContext.getContentResolver().bulkInsert(DBContract.UsersEntry.CONTENT_URI, users);
        Log.v(mContext.getClass().getSimpleName(), "Inserted " + inserted + " users.");
    }

    @SuppressLint("CommitPrefEdits")
    public void saveMe(JSONObject me) throws JSONException {
        mSessionPreferences.edit()
                .putInt(mContext.getString(R.string.session_user_id_key), me.getInt(mContext.getString(R.string.response_id)))
                .putInt(mContext.getString(R.string.session_user_pic_id_key), me.getInt(mContext.getString(R.string.response_user_picture)))
                .putString(mContext.getString(R.string.session_user_name_key), me.getString(mContext.getString(R.string.response_user_name)))
                .commit();
        if (me.has(mContext.getString(R.string.response_profile_blocked_users))) {
            JSONArray blocked = me.getJSONArray(mContext.getString(R.string.response_profile_blocked_users));
            Parser parser = new Parser(mContext);
            ContentValues[] cvs = new ContentValues[blocked.length()];
            for (int i = 0; i < blocked.length(); i++) {
                cvs[i] = parser.getUserFrom(blocked.getJSONObject(i));
                cvs[i].put(DBContract.UsersEntry.COLUMN_BLOCKED, true);
            }
            // Clear blocked users before inserting
            ContentValues clearBlocked = new ContentValues(1);
            clearBlocked.put(DBContract.UsersEntry.COLUMN_BLOCKED, false);
            mContext.getContentResolver().update(DBContract.UsersEntry.CONTENT_URI, clearBlocked,
                    DBContract.UsersEntry.COLUMN_BLOCKED + "=?", new String[]{"1"});
            insertUsers(cvs);
        }
    }

    public boolean isMe(@NonNull String phoneNumber) {
        return phoneNumber.equals(mSessionPreferences.getString(mContext.getString(R.string.session_phone_key), null));
    }

    public boolean isMe(int userId) {
        return mSessionPreferences.getInt(mContext.getString(R.string.session_user_id_key), -1) == userId;
    }

    /**
     * Inserts the user if it doesn't exist and it isn't the current user of the app
     *
     * @param user ContentValues with all the information we have about the new user
     * @return the id of the user or null if the user is the current user of the app
     * @throws JSONException
     */
    public Integer insertUserIfNotExistent(ContentValues user) throws JSONException {
        if (user.containsKey(DBContract.UsersEntry._ID)) {
            String id = user.getAsString(DBContract.UsersEntry._ID);
            // Checking if it is current user
            if (isMe(Integer.parseInt(id))) {
                return null;
            }

            // Checking if it exists
            Cursor c = mContext.getContentResolver().query(DBContract.UsersEntry.CONTENT_URI,
                    new String[]{DBContract.UsersEntry._ID}, DBContract.UsersEntry._ID + "=?",
                    new String[]{id}, null);
            if (c == null)
                throw new NullPointerException("The cursor shouldn't be null");
            try {
                // Inserting
                if (!c.moveToFirst()) {
                    mContext.getContentResolver().insert(DBContract.UsersEntry.CONTENT_URI, user);
                }
            } finally {
                c.close();
            }
            return Integer.parseInt(id);
        }
        throw new JSONException(DBContract.UsersEntry._ID + " required.");
    }
}
