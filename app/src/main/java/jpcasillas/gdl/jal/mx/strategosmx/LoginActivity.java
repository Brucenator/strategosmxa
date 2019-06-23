package jpcasillas.gdl.jal.mx.strategosmx;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jpcasillas.gdl.jal.mx.strategosmx.parser.Ejecutivo;
import jpcasillas.gdl.jal.mx.strategosmx.parser.Ksoap2ResultParser;
import jpcasillas.gdl.jal.mx.strategosmx.parser.TMovil;
import jpcasillas.gdl.jal.mx.strategosmx.parser.TProcesos;
import jpcasillas.gdl.jal.mx.strategosmx.utilities.Utilities;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    //private static final int REQUEST_READ_CONTACTS = 0;
    public final static String EJECUTIVO_KEY = "ejecutivo_key";
    public final static String MODULOS_KEY = "modulos_key";
    public final static String IMEI_KEY = "imei_key";
    final String urlString = "http://strategosmx.com/strategosmxWS/ServiceStrategos.svc";
    private static final int REQUEST_PERMISSIONS = 100;
    private static final String PERMISSIONS_REQUIRED[] = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    boolean resul = false;
    private String IMEI;
    private String mensaje;
    private StringBuffer tprocesos = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utilities.CheckInternetConnection(LoginActivity.this)) {
                    attemptLogin();
                }else{
                    Toast.makeText(getApplicationContext(), "Es necesario contar con servicio de datos o señal WIFI activo.", Toast.LENGTH_LONG).show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        checkPermissions();

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                IMEI = manager != null ? manager.getDeviceId() : null;
            }
            if (!TextUtils.isEmpty(IMEI)) {
                ab.setSubtitle("Versión: " + versionNumber + "-" + versionName + " (Equipo: " + IMEI + ")");
            } else {
                ab.setSubtitle("Versión: " + versionNumber + "-" + versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!Patterns.WEB_URL.matcher("http://strategosmx.com/strategosmxWS/ServiceStrategos.svc").matches()) {
            Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
        }
    }

    /*private void requestPermission() {
        this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CODE_PHONE_STATE_READ);
    }*/

    /*private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }*/

    /*private boolean mayRequestContacts() {
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
    }*/

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MainActivity", "requestCode: " + requestCode);
        Log.d("MainActivity", "Permissions:" + Arrays.toString(permissions));
        Log.d("MainActivity", "grantResults: " + Arrays.toString(grantResults));

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean hasGrantedPermissions = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    hasGrantedPermissions = false;
                    break;
                }
            }

            if (!hasGrantedPermissions) {
                finish();
            }
            recreateActivityCompat(this);
        } else {
            finish();
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
            //} else if (!isEmailValid(email)) {
            //    mEmailView.setError(getString(R.string.error_invalid_email));
            //    focusView = mEmailView;
            //    cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                int responseCode = urlConnection.getResponseCode();
                urlConnection.disconnect();

                if (responseCode != 200) {
                    Toast.makeText(getApplicationContext(), "Intente nuevamente, fallo al intentar conectarse al servidor.", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){

            }

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    //private boolean isEmailValid(String email) {
    //    //TODO: Replace this with your own logic
    //    return email.contains("@");
    //}

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

    /*@Override
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
    }*/

    /*@Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        //addEmailsToAutoComplete(emails);
    }*/

    /*@Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }*/

    /*private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }*/


    /*private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        //int IS_PRIMARY = 1;
    }*/

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        Ejecutivo ejecutivo = new Ejecutivo();
        TMovil movil = new TMovil();
        //TProcesos procesos = new TProcesos();

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

            /*for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");

                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }*/
            final String NAMESPACE = "http://tempuri.org/";
            String METHOD_NAME = "LoginEjecutivo";
            String SOAP_ACTION = "http://tempuri.org/IServiceStrategos/LoginEjecutivo";
            //int resultado_xml = 0;
            SoapObject result = null;
            //Integer count = 1;

            try {
                Thread.sleep(1000);
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                try {
                    request.addProperty("idejecutivo", mEmail);
                    request.addProperty("passwd", mPassword);

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.implicitTypes = true;
                    envelope.setOutputSoapObject(request);
                    envelope.dotNet = true;

                    HttpTransportSE httpTransport = new HttpTransportSE(urlString);
                    httpTransport.debug = true;

                    try {
                        httpTransport.call(SOAP_ACTION, envelope);
                        result = (SoapObject) envelope.bodyIn;
                        Log.i("RESPONSE Login", String.valueOf(result));
                        String[] separa = result.getProperty(0).toString().split(";");

                        httpTransport.reset();

                        parsingSupportedBanksList(result);
                    } catch (HttpResponseException e) {
                        Log.e("HTTPLOG", e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e("IOLOG", e.getMessage());
                        e.printStackTrace();
                        mensaje = "Fallo en la conexión. Intente nuevamente";
                    } catch (XmlPullParserException e) {
                        Log.e("XMLLOG", e.getMessage());
                        e.printStackTrace();
                    } catch (InternalError e) {
                        Log.e("Internal", e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Ksoap2ResultParser.parseBusinessObject(result.getProperty(0).toString(), ejecutivo);
                    Ksoap2ResultParser.parseBusinessObject(result.getProperty(0).toString(), movil);

                    if (ejecutivo.getHabilitado() == 1) {
                        if (movil.getImei().equals(IMEI)) {
                            METHOD_NAME = "ValidaIMEI";
                            SOAP_ACTION = "http://tempuri.org/IServiceStrategos/ValidaIMEI";
                            request = new SoapObject(NAMESPACE, METHOD_NAME);

                            try {
                                request.addProperty("imei", movil.getImei());
                                request.addProperty("idejecutivo", mEmail);

                                envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                                envelope.implicitTypes = true;
                                envelope.setOutputSoapObject(request);
                                envelope.dotNet = true;

                                httpTransport = new HttpTransportSE(urlString);
                                httpTransport.debug = true;

                                try {
                                    httpTransport.call(SOAP_ACTION, envelope);
                                    result = (SoapObject) envelope.bodyIn;
                                    Log.i("RESPONSE Valida IMEI", String.valueOf(result));
                                    httpTransport.reset();
                                } catch (HttpResponseException e) {
                                    Log.e("HTTPLOG", e.getMessage());
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    Log.e("IOLOG", e.getMessage());
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    Log.e("XMLLOG", e.getMessage());
                                    e.printStackTrace();
                                } catch (InternalError e) {
                                    Log.e("Internal", e.getMessage());
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            int validaimei = Integer.parseInt(result.getProperty(0).toString());
                            if (validaimei == 1) {
                                resul = true;
                            } else {
                                mensaje = "Telefono móvil no habilitado";
                            }
                        } else {
                            mensaje = "Móvil no corresponde con el regitrado para usar la aplicación";
                        }
                    } else {
                        mensaje = ejecutivo.getNombre();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra(EJECUTIVO_KEY, mEmail).putExtra(MODULOS_KEY, tprocesos.toString()).putExtra(IMEI_KEY, IMEI));
                finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.setError(mensaje);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    //obtener los modulos que tiene acceso para habilitar botones
    public void parsingSupportedBanksList(SoapObject response) {

        for (int i = 0; i < response.getPropertyCount(); i++) {

            PropertyInfo pi = new PropertyInfo();
            response.getPropertyInfo(i, pi);
            Object property = response.getProperty(i);
            if (pi.name.equals("LoginEjecutivoResult") && property instanceof SoapObject) {
                SoapObject transDetail = (SoapObject) property;

                //getting object properties
                SoapObject objeto = (SoapObject) transDetail.getProperty("TProcesos");

                for (int p = 0; p < objeto.getPropertyCount(); p++) {
                    PropertyInfo info = objeto.getPropertyInfo(p);
                    objeto.getPropertyInfo(p, info);
                    SoapObject tproc = (SoapObject) objeto.getProperty(p);
                    String valor = tproc.getProperty("proceso").toString();
                    Log.i("Modulo: ", valor);
                    tprocesos.append(valor + ",");
                }
            }
        }
    }

    private boolean checkPermission(String permissions[]) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissions() {
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if (permissionsGranted) {
            try {
                android.support.v7.app.ActionBar ab = getSupportActionBar();
                PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionNumber = pinfo.versionCode;
                String versionName = pinfo.versionName;
                if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    IMEI = manager != null ? manager.getDeviceId() : null;
                }
                if (!TextUtils.isEmpty(IMEI)) {
                    ab.setSubtitle("Versión: " + versionNumber + "-" + versionName + " (Equipo: " + IMEI + ")");
                } else {
                    ab.setSubtitle("Versión: " + versionNumber + "-" + versionName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            boolean showRationale = true;
            for (String permission : PERMISSIONS_REQUIRED) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    break;
                }
            }

            String dialogMsg = showRationale ? "Se necesita habilitar algunos permisos para ejecutar esta aplicación!" : "Por favor habilita los permisos solicitados";

            new AlertDialog.Builder(this)
                    .setTitle("Permisos necesarios")
                    .setMessage(dialogMsg)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS_REQUIRED, REQUEST_PERMISSIONS);
                        }
                    }).create().show();

        }
    }

    /**
     Current Activity instance will go through its lifecycle to onDestroy() and a new instance then created after it.
     */
    @SuppressLint("NewApi")
    public static final void recreateActivityCompat(final Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            a.recreate();
        } else {
            final Intent intent = a.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            a.finish();
            a.overridePendingTransition(0, 0);
            a.startActivity(intent);
            a.overridePendingTransition(0, 0);
        }
    }
}

