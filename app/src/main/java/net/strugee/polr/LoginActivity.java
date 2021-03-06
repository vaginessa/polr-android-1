package net.strugee.polr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private InitialConnectionTask mConnectTask = null;

    // UI references.
    private View mUsernameLoginFormView;
    private EditText mUrlView;
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mInstanceConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUrlView = (EditText) findViewById(R.id.url);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Set up the login form buttons.
        mInstanceConnectButton = (Button) findViewById(R.id.instance_connect_button);
        mInstanceConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptConnect();
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mUseAnonymousButton = (Button) findViewById(R.id.anonymous_sign_in_button);
        mUseAnonymousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                useAnonymously();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mUsernameLoginFormView = findViewById(R.id.username_login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempt to connect to the Polr instance specified by the login form. If
     * there are errors in the URL or connection, the user is asked to try again.
     */
    private void attemptConnect() {
        if (mConnectTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        mUrlView.setError(null);

        // Store values at the time of the connection attempt.
        String url = URLUtil.guessUrl(mUrlView.getText().toString());

        // Check for a valid instance URL.
        if (TextUtils.isEmpty(url)) {
            mUrlView.setError(getString(R.string.error_field_required));
            focusView = mUrlView;
            cancel = true;
        } else if (!URLUtil.isValidUrl(url)) {
            mUrlView.setError(getString(R.string.error_invalid_url));
            focusView = mUrlView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't move on and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Disable elements while we try to connect to Polr
            mUrlView.setEnabled(false);
            mInstanceConnectButton.setEnabled(false);
            mInstanceConnectButton.setText(R.string.connecting);

            // Connect
            // TODO
            mConnectTask = new InitialConnectionTask(url);
            mConnectTask.execute((Void) null);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Prompts to make sure the user really meant to sign in anonymously, then
     * proceeds with account creation.
     */
    private void useAnonymously() {
        DialogFragment confirmAnonymousLoginDialogFragment = new ConfirmAnonymousLoginDialogFragment();
        confirmAnonymousLoginDialogFragment.show(getSupportFragmentManager(), "login");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous connection task used to connect to Polr.
     */
    public class InitialConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;

        InitialConnectionTask(String url) {
            mUrl = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt to connect to Polr

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mConnectTask = null;

            if (success) {
                // Show the rest of the login form
                mInstanceConnectButton.setVisibility(View.GONE);
                mUsernameLoginFormView.setVisibility(View.VISIBLE);
                //finish();
            } else {
                // Something went wrong
                mUrlView.setEnabled(true);
                mInstanceConnectButton.setEnabled(true);
                mInstanceConnectButton.setText(R.string.action_sign_in);
                mUrlView.setError(getString(R.string.error_connection_failure));
                mUrlView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mConnectTask = null;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

