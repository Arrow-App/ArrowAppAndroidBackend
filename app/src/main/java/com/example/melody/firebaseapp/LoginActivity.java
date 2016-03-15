package com.example.melody.firebaseapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.client.FirebaseError;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    JNCryptor cryptor = new AES256JNCryptor();
    private String JNCPassword = "Secret password";
    Firebase myFirebaseRef;

    // ========FIREBASE METHODS ===================
    public class User {
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;

        public User() {}

        public User(String fn, String ln, String pn, String pw){
            this.password = pw;
            this.lastName = ln;
            this.firstName = fn;
            this.phoneNumber = pn;
        }

        public String getPassword(){return password;}
        public String getfirstName(){return firstName;}
        public String getlastName(){return lastName;}
        public String getPhoneNumber(){return phoneNumber;}
    }

    String encryptPassword(String password){
        byte[] plaintext = Charset.forName("UTF-8").encode(password).array();
        try {
            byte[] ciphertext = cryptor.encryptData(plaintext, JNCPassword.toCharArray());
            return new String(ciphertext, Charset.forName("UTF-8"));
        } catch (CryptorException e) {
            // Something went wrong
            e.printStackTrace();
        }
        return null;
    }

    String decryptPassword(String password){
        byte[] plaintext = Charset.forName("UTF-8").encode(password).array();
        try {
            byte[] deciphertext = cryptor.decryptData(plaintext, JNCPassword.toCharArray());
            return new String(deciphertext, Charset.forName("UTF-8"));
        } catch (CryptorException e){
            e.printStackTrace();
        }
        return null;
    }

    void phoneNumberExists(String phoneNumber){
        Query query = myFirebaseRef.child("phoneLogin").orderByKey().equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println("Phone # is in there");
                    //TODO: send them to type in password
                }
                else {
                    System.out.println("Phone # is not in there");
                    //TODO: send them to signup
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    void addPhoneNumber(final String phoneNumber, final String password, final String username){
        Query query = myFirebaseRef.child("phoneLogin").orderByKey().equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println("Phone # is in there");
                }
                else {
                    System.out.println("creating phoneLogin");
                    Map<String, String> phoneMap = new HashMap<String, String>();
                    phoneMap.put("username", username);
                    phoneMap.put("password", password);
                    Map<String, Map<String, String>> phone = new HashMap<String, Map<String, String>>();
                    phone.put(phoneNumber, phoneMap);
                    myFirebaseRef.child("phoneLogin").child(phoneNumber).setValue(phoneMap);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    void addAccount(String firstName, String lastName, final String phoneNumber, final String username, final String password){
        final User u = new User(firstName, lastName, phoneNumber, password);
        Query query = myFirebaseRef.child("userData").orderByKey().equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println("Username is in there");
                }
                else {
                    System.out.println("creating account");
                    addPhoneNumber(phoneNumber, password, username);
                    myFirebaseRef.child("userData").child(username).setValue(u);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    void addStaff(final String adminKey, final String password){
        Query query = myFirebaseRef.child("venueStaff").orderByKey().equalTo(adminKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println("AdminKey is in there");
                }
                else {
                    System.out.println("creating account");
                    Map<String, String> staffMap = new HashMap<String, String>();
                    staffMap.put("password", password);
                    Map<String, Map<String, String>> staff = new HashMap<String, Map<String, String>>();
                    staff.put(adminKey, staffMap);
                    myFirebaseRef.child("venueStaff").child(adminKey).setValue(staff);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    void authenticateStaff(final String adminKey, final String password){
        Query query = myFirebaseRef.child("venueStaff").orderByKey().equalTo(adminKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println(snapshot.getValue().toString());
                    if (snapshot.child(adminKey).child("password").getValue().toString().equals(password)){
                        System.out.println("staff: password accepted");
                    }
                    else {
                        System.out.println("staff: password incorrect");
                    }
                }
                else {
                    System.out.println("AdminKey does not exist");
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
    void authenticatePhone(final String phoneNumber, final String password){
        Query query = myFirebaseRef.child("phoneLogin").orderByKey().equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println(snapshot.getValue().toString());
                    if (snapshot.child(phoneNumber).child("password").getValue().toString().equals(password)){
                        System.out.println("phone: password accepted");
                    }
                    else {
                        System.out.println("phone: password incorrect");
                    }
                }
                else {
                    System.out.println("phone number does not exist");
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
    void authenticateUsername(final String username, final String password){
        Query query = myFirebaseRef.child("userData").orderByKey().equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                if(snapshot.exists()){
                    System.out.println(snapshot.getValue().toString());
                    if (snapshot.child(username).child("password").getValue().toString().equals(password)){
                        System.out.println("user: password accepted");
                    }
                    else {
                        System.out.println("user: password incorrect");
                    }
                }
                else {
                    System.out.println("username does not exist");
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }



    // ============================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://arrowappbackend.firebaseio.com/accounts");
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

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

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
            addPhoneNumber(mEmail, mPassword, mEmail);
            authenticatePhone(mEmail, mPassword);
            addAccount("Melody","Spencer","7742898615","mjs585", "Android");
            authenticateUsername("mjs585", "Android");
            addStaff("34567", "Secret Agent");
            authenticateStaff("34567","Secret Agent");
            // TODO: register the new account here
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

