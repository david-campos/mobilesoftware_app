package com.campos.david.appointments.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.campos.david.appointments.R;

import org.json.JSONException;

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
        Log.d(mContext.getClass().getSimpleName(), "Inserted " + inserted + " users.");
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
