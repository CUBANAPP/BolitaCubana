/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Launcher extends AppCompatActivity {
    //DialogNew permissionDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean error;
    private AlertDialog builder;
    //DialogNew2 permissionDialog2;
    private ProgressBar progressBar;
    boolean ConnSuccess;
    private Context context;
    private SharedPreferences sharedPref;
    boolean firstTime;
    private Snackbar mySnackbar;

    private String apiKey;

    public static final String TAG = "FirstCheck";
    private JsonObjectRequest stringRequest; // Assume this exists.
    private RequestQueue requestQueue;  // Assume this exists.

    private FirebaseMessaging mFirebaseMessages;

    private WebView myWebView;
    private ImageView imageView;
    private Button button;
    private Button button2;
    private static final String DEBUG_TAG = "Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if(Build.VERSION.SDK_INT >= 19) {
            FirebaseApp.initializeApp(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        error = false;
        progressBar = findViewById(R.id.progressBar);

        Typeface typeface = Typeface.createFromAsset(getAssets(),"burbank_normal.otf");
        TextView text = findViewById(R.id.textView7);
        if(Build.VERSION.SDK_INT <= 25) {
            text.setTypeface(typeface);
        }
        context = getApplicationContext();

        apiKey = BuildConfig.API_KEY;

        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        builder = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_permission)
                .setPositiveButton(getString(R.string.open), (dialog, id) -> openSettings())
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> finish())
                .create();

        mFirebaseMessages = FirebaseMessaging.getInstance();
        mFirebaseMessages.setAutoInitEnabled(true);

        imageView = (ImageView) findViewById(R.id.imageViewBackground);
        button = (Button) findViewById(R.id.btn_accept);
        button.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            myWebView.setVisibility(View.GONE);
            myWebView.setFocusable(false);
            button.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            startSync();
        });
        button2 = (Button) findViewById(R.id.btn_cancel);

        button2.setOnClickListener(v -> finish());

        myWebView = (WebView) findViewById(R.id.webviewpriv);

        myWebView.setWebViewClient(new WebViewClient() {

            boolean loadingFinished = true;
            boolean redirect = false;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                view.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                loadingFinished = false;
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(!redirect){
                    loadingFinished = true;
                }

                if(loadingFinished && !redirect){
                    //HIDE LOADING IT HAS FINISHED
                    myWebView.setFocusable(true);
                    imageView.setVisibility(View.VISIBLE);
                    myWebView.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else{
                    redirect = false;
                }

            }
        });
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(internetPermission()){
            builder.show();
        }
        else if(networkPermission()){
            builder.setMessage(getString(R.string.dialog_permission2));
            builder.show();
        }
        else {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifiConn = networkInfo.isConnected();
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            boolean isMobileConn = networkInfo.isConnected();
            Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
            Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);
            ConnSuccess = isMobileConn || isWifiConn;
            Log.d(DEBUG_TAG, "ConnSuccess: " + ConnSuccess);
            firstTime = sharedPref.getBoolean("root", true);

            if (Build.VERSION.SDK_INT >= 19) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                Boolean bool = preferences.getBoolean("dataCollection", true);
                FirebaseApp firebaseApp = FirebaseApp.getInstance();
                firebaseApp.setDataCollectionDefaultEnabled(bool);

                firebaseApp.setAutomaticResourceManagementEnabled(preferences.getBoolean("resourceManagement", false));

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                //Log.d(DEBUG_TAG, "mFirebaseAnalytics: " + mFirebaseAnalytics.getFirebaseInstanceId());
                boolean prebool = preferences.getBoolean("allowAdsPerso", true);

                mFirebaseAnalytics.setAnalyticsCollectionEnabled(preferences.getBoolean("analytics", true));
                mFirebaseAnalytics.setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, Boolean.toString(prebool));
                mFirebaseAnalytics.setUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD, "google");

                mFirebaseMessages.setNotificationDelegationEnabled(preferences.getBoolean("delegation", true));
                mFirebaseMessages.setDeliveryMetricsExportToBigQuery(preferences.getBoolean("exportMetrics", false));


                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.setCrashlyticsCollectionEnabled(preferences.getBoolean("crashlytics", true));

                FirebasePerformance firebasePerformance = FirebasePerformance.getInstance();
                firebasePerformance.setPerformanceCollectionEnabled(preferences.getBoolean("performance", true));
            }

            progressBar.setProgress(20);
            if (firstTime && ConnSuccess) {
                if (Build.VERSION.SDK_INT >= 19) {
                    mFirebaseMessages = FirebaseMessaging.getInstance();
                    mFirebaseMessages.subscribeToTopic("Default");
                    mFirebaseMessages.subscribeToTopic("Promo");
                }

                Locale s = Locale.getDefault();
                if (s.getDisplayLanguage().equals("es"))
                    myWebView.loadUrl("https://cubanapp.info/api/prives.html");
                else
                    myWebView.loadUrl("https://cubanapp.info/api/priv.html");

            } else if (firstTime) {
                if(builder != null) {
                    builder.setMessage(getString(R.string.connection));
                    builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.retry), (dialog, which) -> startSync());
                    builder.show();
                }
            } else {
                startLaunch(ConnSuccess, false, "");
            }

        }
    }

    private void startSync() {
        //progressBar.setVisibility(View.VISIBLE);
        //progressBar.animate();
        if(progressBar != null)
            progressBar.setProgress(50);
        if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }
        //Drawable photo = getDrawable(R.drawable.habana2);// this is your image.
        //photo.to
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();
        //Background work here
        if(getApplication() != null)
            requestQueue = Volley.newRequestQueue(this);
        String url = "https://cubanapp.info/api/chksvr.php";
        JSONObject json = new JSONObject();
        try {
            json.put("apiKey", apiKey);
        } catch (JSONException e) {
            if(requestQueue != null) {
                mySnackbar = Snackbar.make(findViewById(R.id.launcherlayout),
                        "Ha ocurrido un error al obtener los datos", Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
            throw new RuntimeException(e);
            //startLaunch(false);
        }
        // Request a string response from the provided URL.
        if(getApplication() != null && requestQueue != null) {
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        // Display the first 500 characters of the response string.
                        try {
                            //JSONObject error = response.getJSONObject("");
                            //response.get("error");
                            boolean error = (Boolean) response.get("error");
                            String msg = (String) response.get("msg");
                            if (!error) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("root", false);
                                int version = (Integer) response.get("version");
                                boolean fix = (Boolean) response.get("fix");
                                boolean ads = (Boolean) response.get("ads");
                                String fecha = (String) response.get("fecha");
                                String hora = (String) response.get("hora");
                                editor.putString("msg", msg);
                                editor.putInt("version", version);
                                editor.putBoolean("fix", fix);
                                editor.putBoolean("ads", ads);
                                editor.putString("fecha", fecha);
                                editor.putString("hora", hora);
                                editor.putString("msg", msg);
                                Log.d(DEBUG_TAG, "Response is: " + error);
                                Log.i(DEBUG_TAG, "Version is: " + version);
                                editor.apply();

                                if (builder != null)
                                    if (builder.isShowing())
                                        builder.dismiss();
                                progressBar.setProgress(100);
                                startLaunch(true, true, msg);

                            } else {
                                if (builder != null) {
                                    builder.setMessage(msg);
                                    builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.retry), (dialog, which) -> startSync());
                                    builder.show();
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(DEBUG_TAG, "JSON ERROR");
                            if (requestQueue != null) {
                                mySnackbar = Snackbar.make(findViewById(R.id.launcherlayout),
                                        "Ha ocurrido un error grave al sincronizar datos", Snackbar.LENGTH_LONG);
                                mySnackbar.show();
                            }
                            throw new RuntimeException(e);
                        }
                        //startLaunch(true);
                    }, volleyerror -> {
                if (volleyerror instanceof TimeoutError) {
                    if (builder != null) {
                        builder.setMessage("Su conexión está lenta, intente de nuevo.");
                        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.retry), (dialog, which) -> startSync());
                        builder.show();
                    }
                } else {
                    if (builder != null) {
                        builder.setMessage("Se ha perdido la conexión, intente de nuevo.");
                        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.retry), (dialog, which) -> startSync());
                        builder.show();
                    }
                }
                Log.e(DEBUG_TAG, "ERROR");
            }

            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
        }
        if(progressBar != null)
            progressBar.setProgress(50);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if(mySnackbar != null) {
            if(mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if(builder != null){
            if(builder.isShowing())
                builder.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
    }
    private void startLaunch(boolean connection, boolean first, String msg){
        Intent myIntent = new Intent(Launcher.this, MainActivity.class);

        if(first)
            myIntent.putExtra("first", first); //Optional parameters

        myIntent.putExtra("connection", connection); //Optional parameters
        myIntent.putExtra("msg", msg);
        startActivity(myIntent);
        finish();
    }
    private boolean internetPermission()
    {
        String permission = Manifest.permission.INTERNET;
        int res = getApplicationContext().checkCallingPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    private boolean networkPermission()
    {
        String permission = Manifest.permission.ACCESS_NETWORK_STATE;
        int res = getApplicationContext().checkCallingPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}