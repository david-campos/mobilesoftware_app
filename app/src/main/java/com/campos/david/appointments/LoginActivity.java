package com.campos.david.appointments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.campos.david.appointments.activityMain.MainActivity;
import com.campos.david.appointments.services.ApiConnector;
import com.campos.david.appointments.services.UpdateTypesAndReasonsService;
import com.campos.david.appointments.services.UpdateUsersService;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * LoginActivity activity of the app
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private EditText mEditText;
    private TextView mLabel;
    private TextView mInfoTV;
    private ProgressBar mLoadBar;
    private Button mLoginButton;
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
        Boolean firstUpdateDone = mSession.getBoolean(getString(R.string.session_users_update_done_key), false);
        Boolean typesAndReasonsDone = mSession.getBoolean(getString(R.string.session_types_reasons_done_key), false);
        if (session_key != null && firstUpdateDone && typesAndReasonsDone) {
            throwMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        mEditText = (EditText) findViewById(R.id.etPhoneNumberInput);
        mLoginButton = (Button) findViewById(R.id.btnLogin);
        mLabel = (TextView) findViewById(R.id.tvTextPhoneNumber);
        mLoadBar = (ProgressBar) findViewById(R.id.pb_doing_login_bar);
        mInfoTV = (TextView) findViewById(R.id.tv_loginInfo);

        if (session_key != null) {
            new DoLoginTask(this).execute("");
        } else {
            mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        login(mEditText.getText().toString());
                        return true;
                    }
                    return false;
                }
            });
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(mEditText.getText().toString());
                }
            });
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
        }
    }

    private void login(String phone) {
        if (phone != null) {
            phone = phone.replaceAll("[ \\.\\-\\(\\)]", "");
            if (phone.matches("\\+?.+")) {
                new DoLoginTask(this).execute(phone);
                return;
            }
        }
        mEditText.setError(getString(R.string.invalid_phone));
        mEditText.requestFocus();
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        return false;
    }

    /**
     * Callbacks received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                (new DoLoginTask(this)).execute("");
            }
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginButton.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginButton.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mLabel.setVisibility(show ? View.GONE : View.VISIBLE);
            mLabel.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLabel.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mEditText.setVisibility(show ? View.GONE : View.VISIBLE);
            mEditText.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEditText.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mLoadBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoadBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
            mInfoTV.setVisibility(show ? View.VISIBLE : View.GONE);
            mInfoTV.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInfoTV.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginButton.setVisibility(show ? View.VISIBLE : View.GONE);
            mEditText.setVisibility(show ? View.VISIBLE : View.GONE);
            mLabel.setVisibility(show ? View.VISIBLE : View.GONE);

            mLoadBar.setVisibility(show ? View.GONE : View.VISIBLE);
            mInfoTV.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class DoLoginTask extends AsyncTask<String, String, Boolean> {
        private Context mContext;

        public DoLoginTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length > 0 && values[0] != null) {
                mInfoTV.setText(values[0]);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String session_key = mSession.getString(getString(R.string.session_key_key), null);
            if (session_key == null) {
                publishProgress(getString(R.string.text_creating_session));
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
            }

            Boolean typesAndReasonsDone = mSession.getBoolean(getString(R.string.session_types_reasons_done_key), false);
            if (!typesAndReasonsDone) {
                publishProgress(getString(R.string.text_updating_types_and_reasons));
                try {
                    UpdateTypesAndReasonsService.typesAndReasonsUpdate(LoginActivity.this);
                    SharedPreferences.Editor editor = mSession.edit();
                    editor.putBoolean(mContext.getString(R.string.session_types_reasons_done_key), true);
                    editor.commit();
                } catch (NullPointerException e) {
                    // Some problem with the API. Ignore, we will try next time!
                }
            }

            Boolean firstUpdateDone = mSession.getBoolean(getString(R.string.session_users_update_done_key), false);
            if (!firstUpdateDone && mayRequestContacts()) {
                publishProgress(getString(R.string.text_updating_users));
                try {
                    UpdateUsersService.usersUpdate(LoginActivity.this);
                    SharedPreferences.Editor editor = mSession.edit();
                    editor.putBoolean(mContext.getString(R.string.session_users_update_done_key), true);
                    editor.commit();
                } catch (NullPointerException e) {
                    // Some problem with the API. Ignore, we will try next time!
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                throwMainActivity();
            } else {
                showProgress(false);
                onLoginError();
            }
        }
    }
}
