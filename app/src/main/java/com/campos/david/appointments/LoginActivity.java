package com.campos.david.appointments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.campos.david.appointments.activityMain.MainActivity;
import com.campos.david.appointments.services.ApiConnector;

/**
 * LoginActivity activity of the app
 */
public class LoginActivity extends AppCompatActivity implements ApiUrlDialog.NoticeDialogListener {
    private EditText mEditText;
    private TextView mLabel;
    private ProgressBar mLoadBar;
    private Button mLoginButton;
    private ColorStateList mOriginalForegroundColors;
    private SharedPreferences mSession;

    private void throwMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void onLoginError() {
        Snackbar.make(findViewById(R.id.login_layout), R.string.login_failed_text, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSession = getSharedPreferences(getString(R.string.session_file_key), Context.MODE_PRIVATE);
        String session_key = mSession.getString(getString(R.string.session_key_key), null);
        if (session_key != null) {
            throwMainActivity();
        } else {
            setContentView(R.layout.activity_login);
            mEditText = (EditText) findViewById(R.id.etPhoneNumberInput);
            mOriginalForegroundColors = mEditText.getTextColors();
            mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mEditText.setTextColor(mOriginalForegroundColors);
                    }
                }
            });
            mLoginButton = (Button) findViewById(R.id.btnLogin);
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(mEditText.getText().toString());
                }
            });
            mLabel = (TextView) findViewById(R.id.tvTextPhoneNumber);
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                public void onLongPress(MotionEvent e) {
                    ApiUrlDialog dialog = new ApiUrlDialog();
                    dialog.show(getFragmentManager(), "NoticeDialogFragment");
                }
            });
            mLabel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            mLoadBar = (ProgressBar) findViewById(R.id.pb_doing_login_bar);
        }
    }

    private void login(String phone) {
        if (phone != null) {
            phone = phone.replaceAll("[ .-]", "");
            if (phone.matches("\\+?.+")) {
                new DoLoginTask(this).execute(phone);
                return;
            }
        }
        mEditText.setBackgroundColor(getResources().getColor(R.color.backgroundIncorrectField));
    }

    @Override
    public void newApiUrl(String newUrl) {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.api_uri_key), newUrl);
        editor.commit();
    }

    private class DoLoginTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;

        public DoLoginTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mLoginButton.setVisibility(View.GONE);
            mLabel.setVisibility(View.GONE);
            mEditText.setVisibility(View.GONE);
            mLoadBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length < 0) {
                return false;
            }
            ApiConnector connector = new ApiConnector(mContext);
            Pair<Integer, String> result = connector.login(params[0]);
            if (result == null) {
                return false;
            }
            SharedPreferences.Editor editor = mSession.edit();
            editor.putInt(mContext.getString(R.string.session_id_key), result.first);
            editor.putString(mContext.getString(R.string.session_key_key), result.second);
            editor.putString(mContext.getString(R.string.session_phone_key), params[0]);
            editor.commit();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                throwMainActivity();
            } else {
//                mLoginButton.setVisibility(View.VISIBLE);
//                mLabel.setVisibility(View.VISIBLE);
//                mEditText.setVisibility(View.VISIBLE);
//                mLoadBar.setVisibility(View.GONE);
                onLoginError();
            }
        }
    }
}
