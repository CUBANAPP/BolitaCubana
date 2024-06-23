/*
 *      Bolita Cubana
 *      Copyright (C) 2019-2024 CUBANAPP LLC
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *      Email contact: help@cubanapp.info
 */

package com.cubanapp.bolitacubana;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.databinding.ActivityLauncherBinding;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class LauncherActivity extends AppCompatActivity {
    //DialogNew permissionDialog;

    private long mLastClickTime = 0;
    private long mLastStartSyncTime = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MaterialAlertDialogBuilder builder;
    //DialogNew2 permissionDialog2;
    private ProgressBar progressBar;
    boolean ConnSuccess;
    private Context context;
    private SharedPreferences sharedPref;
    boolean firstTime;
    private Snackbar mySnackbar;

    private String apiKey;

    private JsonObjectRequest stringRequest; // Assume this exists.
    private RequestQueue requestQueue;  // Assume this exists.

    private FirebaseMessaging mFirebaseMessages;

    private ActivityLauncherBinding binding;


    private String notifTitle;

    private String notifMsg;

    private static final String DEBUG_TAG = "LauncherActivity";

    private static final String IAB_STRING = "1---";
    //private PublicKey certClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //certClient = null;
        mLastClickTime = SystemClock.elapsedRealtime();
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder builderConfig = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("B3EEABB8EE11C2BE770B684D95219ECB"));
            builderConfig.build();
        }

        //setContentView(R.layout.activity_launcher);
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
        } catch (GooglePlayServicesNotAvailableException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }

        } catch (NullPointerException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
            //throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            FirebaseApp.initializeApp(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show charada.

            if (BuildConfig.DEBUG) {
                String channelId0 = "debug";
                String channelName0 = "Debug";
                NotificationManager notificationManager0 =
                        getSystemService(NotificationManager.class);
                notificationManager0.createNotificationChannel(new NotificationChannel(channelId0,
                        channelName0, NotificationManager.IMPORTANCE_HIGH));
            }
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));


            String channelId2 = getString(R.string.cubanapp_channel_id);
            String channelName2 = getString(R.string.cubanapp_channel_name);
            NotificationManager notificationManager2 =
                    getSystemService(NotificationManager.class);
            notificationManager2.createNotificationChannel(new NotificationChannel(channelId2,
                    channelName2, NotificationManager.IMPORTANCE_DEFAULT));
        }
        boolean error = false;
        progressBar = findViewById(R.id.progressBar);


        context = getApplicationContext();

        apiKey = BuildConfig.API_KEY;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("notifMsg") != null) {
                if (bundle.getString("notifTitle") != null) {
                    notifTitle = bundle.getString("notifTitle");
                }
                notifMsg = bundle.getString("notifMsg");
            }
        }

        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        builder = new MaterialAlertDialogBuilder(this, R.style.Theme_BolitaCubana_Dialog)
                .setMessage(R.string.dialog_permission)
                .setPositiveButton(getString(R.string.open), (dialog, id) -> openSettings())
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> finish());
        builder.create();
        //builder.show();

        if (Build.VERSION.SDK_INT >= 19) {
            mFirebaseMessages = FirebaseMessaging.getInstance();
            mFirebaseMessages.setAutoInitEnabled(true);
        }
        startSync();

    }

    /*private SSLSocketFactory getSocketFactory(Context context)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Load CAs from an InputStream (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.cubanapp));
        // I paste my myFile.crt in raw folder under res.
        X509Certificate ca;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            ca = (X509Certificate) cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }
        certClient = ca.getPublicKey();

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext;

        if (Build.VERSION.SDK_INT < 29)
            sslContext = SSLContext.getInstance("TLSv1.2");
        else
            sslContext = SSLContext.getInstance("TLSv1.3");

        if (sslContext == null) {
            sslContext = SSLContext.getInstance("TLS");
            if (sslContext == null)
                sslContext = SSLContext.getInstance("SSL");
        }

        sslContext.init(null, tmf.getTrustManagers(), null);
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(true);

        return sslContext.getSocketFactory();
    }*/

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (internetPermission()) {
            builder.show();
        } else if (networkPermission()) {
            builder.setMessage(getString(R.string.dialog_permission2));
            builder.show();
        } else {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connMgr != null) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isWifiConn;
                if (networkInfo != null) {
                    try {
                        isWifiConn = networkInfo.isConnectedOrConnecting();
                        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
                    } catch (NullPointerException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                        isWifiConn = true;
                    }
                } else {
                    isWifiConn = true;
                }
                networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                boolean isMobileConn;

                if (networkInfo != null) {
                    try {
                        isMobileConn = networkInfo.isConnectedOrConnecting();
                        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);
                    } catch (NullPointerException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                        isMobileConn = true;
                    }
                } else {
                    isMobileConn = true;
                }
                ConnSuccess = isMobileConn || isWifiConn;
                Log.d(DEBUG_TAG, "ConnSuccess: " + ConnSuccess);
            } else {
                ConnSuccess = true;
            }
            firstTime = sharedPref.getBoolean("root", true);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (Build.VERSION.SDK_INT >= 19) {
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
                    SharedPreferences.Editor sharedPrefEditor = preferences.edit();
                    sharedPrefEditor.putInt("version_install", BuildConfig.VERSION_CODE);
                    sharedPrefEditor.putInt("gad_rdp", 1);
                    sharedPrefEditor.putInt("rdp", 1);
                    sharedPrefEditor.putString("IABUSPrivacy_String", IAB_STRING);
                    sharedPrefEditor.apply();
                    //mFirebaseMessages = FirebaseMessaging.getInstance();
                    if (BuildConfig.DEBUG)
                        mFirebaseMessages.subscribeToTopic("Debug");
                    if (preferences.getBoolean("floridaChannel", false))
                        mFirebaseMessages.subscribeToTopic("Florida");
                    if (preferences.getBoolean("georgiaChannel", false))
                        mFirebaseMessages.subscribeToTopic("Georgia");
                    if (preferences.getBoolean("newyorkChannel", false))
                        mFirebaseMessages.subscribeToTopic("NewYork");
                    if (preferences.getBoolean(getString(R.string.cubanapp_channel_name_topic), false))
                        mFirebaseMessages.subscribeToTopic(getString(R.string.cubanapp_channel_name_topic));
                    if (preferences.getBoolean(getString(R.string.promotional_topic), false))
                        mFirebaseMessages.subscribeToTopic(getString(R.string.promotional_topic));

                }
                Locale language = Locale.getDefault();

                String idiomaSave;
                if (language.getLanguage().equals("es")) {
                    idiomaSave = preferences.getString("languagepreference", "es");
                } else {
                    idiomaSave = "en";
                    SharedPreferences.Editor d = preferences.edit();
                    d.putString("languagepreference", idiomaSave);
                    d.apply();
                }
                Locale newLanguage = new Locale(idiomaSave);
                Locale.setDefault(newLanguage);


                //myWebView.loadUrl("http://cubanapp.info/api/prives.html");
                //myWebView.loadUrl("http://cubanapp.info/api/priv.html");

            } else if (firstTime) {
                if (builder != null) {
                    builder.setMessage(getString(R.string.connection));
                    builder.setPositiveButton(getString(R.string.retry), (dialog, which) -> {
                        //builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.retry), (dialog, which) -> {
                        startSync();

                    });
                    builder.show();
                }
            } else {
                if (ConnSuccess) {
                    //TODO: FIX for old versions upgrading
                    if (BuildConfig.VERSION_CODE > preferences.getInt("version_install", 125)) {
                        if (preferences.getInt("version_install", 125) > 107 && BuildConfig.VERSION_CODE == 112) {
                            SharedPreferences.Editor sharedPrefEditor = preferences.edit();
                            sharedPrefEditor.putInt("version_install", BuildConfig.VERSION_CODE);
                            sharedPrefEditor.putInt("gad_rdp", 1);
                            sharedPrefEditor.putInt("rdp", 1);
                            sharedPrefEditor.putString("IABUSPrivacy_String", IAB_STRING);
                            sharedPrefEditor.apply();
                        } else if (preferences.getInt("version_install", 125) == 107) {
                            SharedPreferences.Editor sharedPrefEditor = preferences.edit();
                            sharedPrefEditor.putInt("version_install", BuildConfig.VERSION_CODE);
                            mFirebaseMessages.unsubscribeFromTopic("Default");
                            sharedPrefEditor.putInt("rdp", 1);
                            Log.d(DEBUG_TAG, "Update DEBUG");
                            sharedPrefEditor.apply();
                        } else if (preferences.getInt("version_install", 125) < 107 && preferences.getInt("version_install", 125) > 100) {
                            SharedPreferences.Editor sharedPrefEditor = preferences.edit();
                            sharedPrefEditor.putInt("version_install", BuildConfig.VERSION_CODE);
                            sharedPrefEditor.putInt("gad_rdp", 1);
                            sharedPrefEditor.putInt("rdp", 1);
                            sharedPrefEditor.putString("IABUSPrivacy_String", IAB_STRING);
                            sharedPrefEditor.apply();
                            //mFirebaseMessages = FirebaseMessaging.getInstance();
                            mFirebaseMessages.unsubscribeFromTopic("Default");
                            if (BuildConfig.DEBUG)
                                mFirebaseMessages.subscribeToTopic("Debug");
                            if (preferences.getBoolean("floridaChannel", true))
                                mFirebaseMessages.subscribeToTopic("Florida");
                            if (preferences.getBoolean("georgiaChannel", true))
                                mFirebaseMessages.subscribeToTopic("Georgia");
                            if (preferences.getBoolean("newyorkChannel", true))
                                mFirebaseMessages.subscribeToTopic("NewYork");
                            if (preferences.getBoolean(getString(R.string.cubanapp_channel_name_topic), true))
                                mFirebaseMessages.subscribeToTopic(getString(R.string.cubanapp_channel_name_topic));
                            if (preferences.getBoolean(getString(R.string.promotional_topic), true))
                                mFirebaseMessages.subscribeToTopic(getString(R.string.promotional_topic));
                            Log.d(DEBUG_TAG, "Update DEBUG");
                        }
                    } else {
                        Log.d(DEBUG_TAG, "VERSION_CODE " + BuildConfig.VERSION_CODE);
                    }
                }
                startLaunch(ConnSuccess, false, "");
            }

        }
    }

    private void startSync() {


        if (SystemClock.elapsedRealtime() - mLastStartSyncTime < 200) {
            return;
        }
        mLastStartSyncTime = SystemClock.elapsedRealtime();

        //progressBar.setVisibility(View.VISIBLE);
        //progressBar.animate();
        if (progressBar != null)
            progressBar.setProgress(50);
        /*if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }*/
        //Drawable photo = getDrawable(R.drawable.habana2);// this is your image.
        //photo.to
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();
        //Background work here
        if (getApplication() != null) {
            //SSLSocketFactory socketFactory = getSocketFactory(context);
            //HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
            /*HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // Perform hostname verification based on certificate information
                    X509Certificate cert = null;
                    try {
                        cert = (X509Certificate) session.getPeerCertificates()[0];
                    } catch (SSLPeerUnverifiedException e) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics.getInstance().log(Objects.requireNonNull(e.getMessage()));
                        }
                        System.out.println(Objects.requireNonNull(e.getMessage()));
                        throw new RuntimeException(e);
                    }
                    // Compare hostname with CN or SAN in the certificate
                    // Example:
                    Log.i(DEBUG_TAG, "certificate verified: " + hostname);
                    if (hostname.equals("cubanapp.info")) {
                        PublicKey serverCert = cert.getPublicKey();
                        return certClient.equals(serverCert);
                    } else return true;
                    // Or use a library like javax.net.ssl.SNIHostName to handle SANs
                }
            });*/

            /*HurlStack hurlStack = new HurlStack() {
                @Override
                protected HttpURLConnection createConnection(URL url) throws IOException {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (url.getProtocol().toLowerCase().equals("https")) {
                        ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
                    }
                    return connection;
                }
            };*/
            requestQueue = Volley.newRequestQueue(this);
            if (BuildConfig.DEBUG) {
                requestQueue.getCache().clear(); // Clear cache to ensure logging is enabled

                requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                    @Override
                    public void onRequestFinished(Request<Object> request) {
                        Log.d("Volley", "Request finished: " + request.getUrl());
                    }
                });

                VolleyLog.DEBUG = true;
            }
        }
        if (requestQueue == null)
            return;

        String url;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            url = "https://cubanapp.info/api/chksvr.php";
        } else {
            url = "http://cubanapp.info/api/chksvr.php";
        }
        JSONObject json = new JSONObject();
        try {
            json.put("apiKey", apiKey);
        } catch (JSONException e) {
            if (requestQueue != null) {
                mySnackbar = Snackbar.make(findViewById(R.id.launcherlayout),
                        getString(R.string.errorData), Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics.getInstance().log(Objects.requireNonNull(e.getMessage()));
            }
            System.out.println(Objects.requireNonNull(e.getMessage()));
            throw new RuntimeException(e);
            //startLaunch(false);
        }
        // Request a string response from the provided URL.
        if (getApplication() != null && requestQueue != null) {
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        // Display the first 500 characters of the response string.
                        Log.d(DEBUG_TAG, response.toString());
                        try {
                            //JSONObject error = response.getJSONObject("");
                            //response.get("error");
                            boolean error = (Boolean) response.get("error");
                            String msg = (String) response.get("msg");
                            if (!error) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("root", false);
                                int version = (Integer) response.get("version");
                                if (BuildConfig.DEBUG) {
                                    int version2 = (Integer) response.get("vtest");
                                    editor.putInt("version", version2);
                                } else {
                                    editor.putInt("version", version);
                                }
                                boolean fix = (Boolean) response.get("fix");
                                boolean ads = (Boolean) response.get("ads");
                                String fecha = (String) response.get("fecha");
                                String hora = (String) response.get("hora");
                                //editor.putInt("version", version);
                                editor.putBoolean("fix", fix);
                                editor.putBoolean("ads", ads);
                                editor.putString("fecha", fecha);
                                editor.putString("hora", hora);
                                editor.putString("msg", msg);
                                Log.d(DEBUG_TAG, "Response is: " + error);
                                Log.d(DEBUG_TAG, "Version is: " + version);
                                editor.apply();

                                /*if (builder != null)
                                    if (builder.isShowing())
                                        builder.dismiss();*/
                                progressBar.setProgress(100);
                                Log.d(DEBUG_TAG, "UPDATED Launcher");
                                startLaunch(true, true, msg);

                            } else {
                                if (builder != null) {
                                    builder.setMessage(msg);
                                    builder.setPositiveButton(getString(R.string.retry), (dialog, which) -> {
                                        startSync();
                                    });
                                    builder.show();
                                }
                            }
                        } catch (JSONException e) {
                            if (e.getMessage() != null) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }
                            if (Build.VERSION.SDK_INT >= 19) {
                                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                firebaseCrashlytics.sendUnsentReports();
                                firebaseCrashlytics.recordException(e);
                            }
                            if (requestQueue != null) {
                                mySnackbar = Snackbar.make(findViewById(R.id.launcherlayout),
                                        getString(R.string.erroSync), Snackbar.LENGTH_LONG);
                                mySnackbar.show();
                            }
                            if (Build.VERSION.SDK_INT >= 19) {
                                FirebaseCrashlytics.getInstance().log(Objects.requireNonNull(e.getMessage()));
                            }
                            System.out.println(Objects.requireNonNull(e.getMessage()));
                            throw new RuntimeException(e);
                        }
                        //startLaunch(true);
                    }, volleyerror -> {
                if (volleyerror instanceof TimeoutError) {
                    if (builder != null) {
                        builder.setMessage(getString(R.string.connslow));
                        builder.setPositiveButton(getString(R.string.retry), (dialog, which) -> {
                            startSync();
                        });
                        builder.show();
                    }
                } else {
                    if (builder != null) {
                        builder.setMessage(getString(R.string.lostconn));
                        builder.setPositiveButton(getString(R.string.retry), (dialog, which) -> {
                            startSync();
                        });
                        builder.show();
                    }
                }
                if (BuildConfig.DEBUG) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("root", false);
                    editor.putInt("version", BuildConfig.VERSION_CODE);
                    editor.putBoolean("fix", true);
                    editor.putBoolean("ads", false);
                    editor.putString("msg", "");
                    editor.apply();
                    startLaunch(true, true, "NO CONNECTION");
                }
                Log.e(DEBUG_TAG, "ERROR");
            }

            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
        }
        if (progressBar != null)
            progressBar.setProgress(50);
    }

    @Override
    protected void onStop() {
        if (mySnackbar != null) {
            if (mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        /*if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }*/
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
        mLastStartSyncTime = 0;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        context = null;
        binding = null;
        super.onDestroy();
    }

    private void startLaunch(boolean connection, boolean first, String msg) {
        Intent myIntent = new Intent(LauncherActivity.this, MainActivity.class);

        if (first)
            myIntent.putExtra("first", first); //Optional parameters

        myIntent.putExtra("connection", connection); //Optional parameters
        myIntent.putExtra("msg", msg);

        if (notifMsg != null) {
            myIntent.putExtra("notifMsg", notifMsg);
            if (notifTitle != null) {
                myIntent.putExtra("notifTitle", notifTitle);
            }
        }
        startActivity(myIntent);
        finish();
    }

    private boolean internetPermission() {
        String permission = Manifest.permission.INTERNET;
        int res = getApplicationContext().checkCallingPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean networkPermission() {
        String permission = Manifest.permission.ACCESS_NETWORK_STATE;
        int res = getApplicationContext().checkCallingPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
