package televic.project.kuleuven.televicmechanicassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 * Als de user zelfs geen token heeft locaal: blijf op signin page, anders redirect naar MainPage
 * en dan op de MainPage als 401 ->
 * nieuwe token aanvragen + sign in
 */
public class LoginActivity extends AppCompatActivity {
    private final String LOG_TAG = LoginActivity.class.getSimpleName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginHandler mAuthTask = null;

    // UI references.
    private TextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Redirect directly if a local TOKEN is present
        //TODO OVERAL BIJ ERROR 401 moet TOKEN verwijderd worden! anders oneindige redirect lus.
        String token = Utility.getLocalToken(getApplicationContext());
        if (token != null) {
            goToOverviewPage();
        }

        // Set up the login form.
        mEmailView = (TextView) findViewById(R.id.email);
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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
            mAuthTask = new UserLoginHandler(getApplicationContext(), email, password);
            mAuthTask.tryServerLogin();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
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
     * If new login or login with TOKEN succesful: we redirect to the IssueOverviewActivity
     */
    public void goToOverviewPage() {
        Log.v(LOG_TAG,"Creating intent: goToOverviewPage");
        Intent intent = new Intent(this, IssueOverviewActivity.class);
        startActivity(intent);
    }


    public class UserLoginHandler {
        private final String LOG_TAG = UserLoginHandler.class.getSimpleName();

        private String mEmail;
        private String mPassword;
        private Context mContext;

        //For parsing JSON responses
        private final String JSON_TOKEN = "token";
        private final String JSON_ID = "id";
        private final String JSON_NAME = "name";
        private final String JSON_USER = "owner";


        public UserLoginHandler(Context context, String email, String password) {
            this.mEmail = email;
            this.mPassword = password;
            this.mContext = context;
        }

        /**
         * We try a new login attempt to request a new TOKEN.
         *
         * @return (1) true if attempt successful (2) false if attempt unsuccessful
         */
        public void tryServerLogin() {
            //The user can obtain a token by making a POST-request to the following url:
            //$(base_url)/rest/login
            String url = RESTSingleton.BASE_URL + "/" + RESTSingleton.LOGIN_PATH;

            try {
                //Creating JsonStringRequest for REST call
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                            public void onResponse(JSONObject response) {
                                VolleyLog.v(LOG_TAG, "JSONObject response received from REST:" + response);

                                //Parsing and saving data from response
                                int user_id;
                                String user_name;
                                String token;
                                try{
                                    //PARSING
                                    token = response.getString(JSON_TOKEN);

                                    JSONObject user = response.getJSONObject(JSON_USER);
                                    user_id=user.getInt(JSON_ID);
                                    user_name=user.getString(JSON_NAME);

                                    //STORING DATA
                                    Utility.putLocalToken(mContext,token);
                                    Utility.putLocalUserInfo(mContext,user_id,user_name);
                                }catch(JSONException e){
                                    e.printStackTrace();
                                    Log.e(LOG_TAG,"Login attempt failed: fail in parse & save.");
                                }finally{
                                    //CLEANUP
                                    mAuthTask = null;
                                    showProgress(false);

                                    //REDIRECT
                                    goToOverviewPage();
                                    finish();
                                }
                            }
                        }, new Response.ErrorListener() {

                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.e("Error in Login request:" + error.networkResponse);

                                //CLEANUP
                                mAuthTask = null;
                                showProgress(false);

                                //Error Message
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", mEmail);
                        params.put("password", mPassword);
                        return params;
                    }
                };

                //Singleton handles call to REST
                Log.v(LOG_TAG, "Calling RESTSingleton with context:" + mContext);
                RESTSingleton.getInstance(mContext).addToRequestQueue(jsObjRequest);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed REST user login");
            }
        }


    }
}

