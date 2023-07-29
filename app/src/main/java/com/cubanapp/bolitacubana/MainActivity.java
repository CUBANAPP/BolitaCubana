/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.databinding.ActivityMainBinding;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, R.string.notificationdgranted, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, R.string.notificationdeclined,
                            Toast.LENGTH_LONG).show();
                }
            });
    private JsonObjectRequest stringRequest; // Assume this exists.
    private RequestQueue requestQueue;  // Assume this exists.
    private Context context;

    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    private Snackbar mySnackbar;
    private SharedPreferences sharedPref;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private AlertDialog builder;
    private String apiKey;
    private boolean first;

    public InterstitialAd mInterstitialAd;

    private WebView myWebView;

    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView adView;

    private String message;

    private Bundle bundle;

    private ConsentInformation consentInformation;
    private ConsentForm consentForm;

    private static final String IAB_STRING = "1---";

    private static final String DEBUG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        myWebView = binding.webviewpriv2;
        Button button = binding.btnAccept2;
        Button button2 = binding.btnCancel2;

        Typeface font = null;
        if (binding != null && getAssets() != null)
            font = Typeface.createFromAsset(getAssets(), "burbank_normal.otf");

        if (font != null) {
            binding.btnAccept2.setTypeface(font);
            binding.btnCancel2.setTypeface(font);
        }
        BottomNavigationView navigationView = binding.navView;

        ImageView imageView = binding.imageViewBackground2;
        ProgressBar progressBar = binding.progressBarPriv;

        button.setOnClickListener(v -> {
            navigationView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            myWebView.setVisibility(View.GONE);
            myWebView.setFocusable(false);
            button.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            //startLaunch(ConnSuccess, false, "");
            //startSync();
        });

        button2.setOnClickListener(v -> finish());
        myWebView.setWebViewClient(new WebViewClient() {

            boolean loadingFinished = true;
            boolean redirect = false;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                //view.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                loadingFinished = false;
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
                progressBar.setVisibility(View.VISIBLE);
                navigationView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished) {
                    //HIDE LOADING IT HAS FINISHED
                    myWebView.setFocusable(true);
                    imageView.setVisibility(View.VISIBLE);
                    myWebView.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    redirect = false;
                }

            }
        });
        /*ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(DEBUG_TAG, "PackageManager: " + e.getMessage());
            //throw new RuntimeException(e);
        }*/
        /*if(applicationInfo != null)
            apiKey = applicationInfo.metaData.getString("API_KEY");
        else*/
        apiKey = BuildConfig.API_KEY;

        //adView.setVisibility(View.GONE);

        builder = new AlertDialog.Builder(this)
                .create();
        first = false;
        Bundle bundle = getIntent().getExtras();

        boolean connection;
        if (bundle != null) {
            connection = bundle.getBoolean("connection");
            first = bundle.getBoolean("first");
            message = bundle.getString("msg");
            if (message != null && !message.equals("")) {
                if (message.contains("https://")) {
                    if (builder != null) {
                        builder.setTitle(getString(R.string.visitlink));
                        String su = getString(R.string.link);
                        su += message;
                        builder.setMessage(su);
                        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> openlink(message));
                        builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                        builder.show();
                    }
                } else {
                    if (builder != null) {
                        builder.setTitle(getString(R.string.important));
                        builder.setMessage(message);
                        builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                        builder.show();
                    }
                }
            }
            //bundle.clear();
        } else {
            first = true;
            message = "";
            connection = false;
        }


        if (Build.VERSION.SDK_INT >= 19) {
            // Set tag for under age of consent. false means users are not under
            // age.
            ConsentRequestParameters params = new ConsentRequestParameters
                    .Builder()
                    .setTagForUnderAgeOfConsent(false)
                    .build();

            consentInformation = UserMessagingPlatform.getConsentInformation(this);

            consentInformation.requestConsentInfoUpdate(
                    this,
                    params,
                    () -> {
                        if (consentInformation.isConsentFormAvailable()) {

                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                    this,
                                    loadAndShowError -> {
                                        if (binding != null) {
                                            if (loadAndShowError != null) {
                                                // Consent gathering failed.
                                                Log.w(DEBUG_TAG, String.format("%s: %s",
                                                        loadAndShowError.getErrorCode(),
                                                        loadAndShowError.getMessage()));
                                                initializeMobileAdsSdk();
                                            } else {

                                                // Consent has been gathered.
                                                // if (ConsentInformation.canRequestAds) {
                                                initializeMobileAdsSdk();
                                            }
                                        }
                                    });
                        } else {
                            initializeMobileAdsSdk();
                        }
                    },
                    requestConsentError -> {
                        if (binding != null) {
                            // Consent gathering failed.
                            Log.w(DEBUG_TAG, String.format("%s: %s",
                                    requestConsentError.getErrorCode(),
                                    requestConsentError.getMessage()));
                            initializeMobileAdsSdk();
                        }
                    });

            // Check if you can initialize the Google Mobile Ads SDK in parallel
            // while checking for new consent information. Consent obtained in
            // the previous session can be used to request ads.
            /*if (ConsentInformation.canRequestAds) {
                initializeMobileAdsSdk();
            }*/

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
            adView = (AdView) findViewById(R.id.adView);
            //FirebaseApp.initializeApp(this);
            //FirebaseOptions.Builder firebaseOptions = new FirebaseOptions.Builder();
            /*if(connection) {
                FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
                firebaseMessaging.getToken().addOnCompleteListener(v -> Log.d(DEBUG_TAG, "FCM Key: " + v.getResult()));
            }*/
            //FirebaseMessaging.getInstance(FirebaseApp.initializeApp(this));
        }


        //BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_charada, R.id.navigation_adivinanza)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navController.enableOnBackPressed(true);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.e(DEBUG_TAG, "BackPressed");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        callback.setEnabled(false);

        askNotificationPermission();


        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        Date currentTimes = fecha.getTime();

        SimpleDateFormat fechaFormato = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat horaFormato = new SimpleDateFormat("HH:mm:ss", Locale.US);

        String fechaString = fechaFormato.format(currentTimes);
        String horaString = horaFormato.format(currentTimes);

        boolean night = false;

        try {
            Date fechaActual = fechaFormato.parse(fechaString);
            Date horaActual = horaFormato.parse(horaString);
            String sDiaGuardado = sharedPref.getString("removeTemp", "01/01/2006");
            Date fechaDiaSaved = fechaFormato.parse(sDiaGuardado);

            Calendar c = Calendar.getInstance();
            c.setTime(fechaFormato.parse(sDiaGuardado));
            c.add(Calendar.DATE, 1);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
            String diaMas = fechaFormato.format(c.getTime());
            Date diaMasSaved = fechaFormato.parse(diaMas);

            //Log.e(DEBUG_TAG, "nocheMasSaved : " + nocheMasSaved);

            //Date fechaDiaFicticia = fechaFormato.parse("02/04/2023");
            //Date fechaNocheFicticia = fechaFormato.parse("04/04/2023");
            //Date horaFicticia = horaFormato.parse("22:00:00");

            Date horaDia = horaFormato.parse("13:46:00");
            Date horaNoche = horaFormato.parse("21:56:00");


            if ((!fechaActual.equals(fechaDiaSaved) && diaMasSaved.before(fechaActual)) || (fechaActual.equals(diaMasSaved) && horaActual.after(horaDia))) {
                //Log.e(DEBUG_TAG, "UPDATE DIA");
                if (!fechaActual.equals(fechaDiaSaved) && diaMasSaved.before(fechaActual))
                    night = true;
            } else {
                if (diaMasSaved.before(fechaActual) || (fechaActual.equals(diaMasSaved) && horaActual.after(horaNoche))) {
                    //Log.e(DEBUG_TAG, "UPDATE NOCHE");
                    night = true;
                }
            }
            if (night) {
                try {
                    clearTempData(fechaString);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                }

            }
        } catch (ParseException e) {
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
        }

    }

    private void initializeMobileAdsSdk() {
        if (binding != null) {
            if (isMobileAdsInitializeCalled.getAndSet(true)) {
                adView.setVisibility(View.VISIBLE);
                return;
            }

            MobileAds.initialize(this, initializationStatus -> {
                Log.d(DEBUG_TAG, "Ads Running");
                adView.setVisibility(View.VISIBLE);
            });
            //RequestConfiguration.Builder adRequestBuilder = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("27257B0AF4890D7241E824CB06C35D83"));
            Bundle networkExtrasBundle = new Bundle();
            networkExtrasBundle.putInt("rdp", 1);
            networkExtrasBundle.putInt("gad_rdp", 1); // TODO: Añadido por si acaso
            networkExtrasBundle.putString("IABUSPrivacy_String", IAB_STRING);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, networkExtrasBundle)
                    .build();
            String adUnitID = BuildConfig.INTERSTICIAL_ID;
            String adUnitIDTest = "ca-app-pub-3940256099942544/1033173712";
            InterstitialAd.load(this, adUnitIDTest, adRequest,
                    new InterstitialAdLoadCallback() {

                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                            Log.i(DEBUG_TAG, "onAdLoaded");
                            if (getApplicationContext() != null && mInterstitialAd != null) {
                                configureInterstitial();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            if (binding != null) {
                                Log.d(DEBUG_TAG, loadAdError.toString());
                                mInterstitialAd = null;
                            }
                        }
                    });

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                    adView.loadAd(adRequest);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    Log.e(DEBUG_TAG, "Ads Error");
                }

                @Override
                public void onAdImpression() {
                    // Code to be executed when an impression is recorded
                    // for an ad.
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                    Log.d(DEBUG_TAG, "Ads impression");
                }

                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.d(DEBUG_TAG, "Ads Loaded");
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }
            });
            adView.loadAd(adRequest);
            adView.setActivated(true);
            adView.setEnabled(true);
        }
    }

    /*public void loadForm() {
        // Loads a consent form. Must be called on the main thread.

        UserMessagingPlatform.loadConsentForm(
                this,
                consentForm -> {
                    MainActivity.this.consentForm = consentForm;
                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(
                                MainActivity.this,
                                formError -> {
                                    if (binding != null) {
                                        if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                            // App can start requesting ads.
                                            initializeMobileAdsSdk();
                                            Log.d(DEBUG_TAG, "consentInformation: OBTAINED");
                                        } else {
                                            initializeMobileAdsSdk();
                                            Log.d(DEBUG_TAG, "consentInformation: REVOKED");
                                        }

                                        // Handle dismissal by reloading form.
                                    }
                                });
                    }
                },
                formError2 -> {
                    if (binding != null) {
                        // Consent gathering failed.
                        Log.w(DEBUG_TAG, String.format("%s: %s",
                                formError2.getErrorCode(),
                                formError2.getMessage()));
                        initializeMobileAdsSdk();
                    }
                }
        );
    }*/

    private void startSync() {
        if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }
        //ExecutorService executor = Executors.newSingleThreadExecutor();
        //Handler handler = new Handler(Looper.getMainLooper());

        //AtomicBoolean errorStart = new AtomicBoolean(false);
        //executor.execute(() -> {

        if (getApplication() != null) {
            //Background work here
            requestQueue = Volley.newRequestQueue(this);
        }
        String url;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            url = "https://cubanapp.info/api/chksvr.php";
        } else {
            url = "http://cubanapp.info/api/chksvr.php";
        }
        //String url = "https://cubanapp.info/api/chksvr.php";
        JSONObject json = new JSONObject();

        try {
            json.put("apiKey", apiKey);
        } catch (JSONException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
            if (getApplication() != null && requestQueue != null) {
                mySnackbar = Snackbar.make(findViewById(R.id.container),
                        getString(R.string.errorData), Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
            //throw new RuntimeException(e);
            //startLaunch(false);
        }
        //AtomicReference<String> msg = new AtomicReference<>("");
        AtomicReference<Integer> version = new AtomicReference<>(0);
        // Request a string response from the provided URL.
        if (getApplication() != null && requestQueue != null) {
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        // Display the first 500 characters of the response string.
                        try {
                            //JSONObject error = response.getJSONObject("");
                            //response.get("error");
                            boolean error = (Boolean) response.get("error");
                            //msg.set((String) response.get("msg"));
                            message = (String) response.get("msg");
                            /*if(message != null && !message.equals("")) {
                                if (binding != null) {
                                    FrameLayout frame = (FrameLayout) binding.getRoot().findViewById(R.id.framelayout);
                                    frame.setVisibility(View.VISIBLE);
                                    TextView textView = (TextView) binding.getRoot().findViewById(R.id.textViewMessgae);
                                    textView.setText(message);
                                }
                            }*/
                            if (!error) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                version.set((Integer) response.get("version"));
                                boolean fix = (Boolean) response.get("fix");
                                boolean ads = (Boolean) response.get("ads");
                                String fecha = (String) response.get("fecha");
                                String hora = (String) response.get("hora");
                                editor.putString("msg", message);
                                editor.putInt("version", version.get());
                                if (BuildConfig.DEBUG) {
                                    version.set((Integer) response.get("vtest"));
                                    editor.putInt("version", version.get());
                                } else {
                                    editor.putInt("version", version.get());
                                }
                                int vermin = (Integer) response.get("ver-min");
                                editor.putBoolean("fix", fix);
                                editor.putBoolean("ads", ads);
                                editor.putString("fecha", fecha);
                                editor.putString("hora", hora);
                                Log.d(DEBUG_TAG, "Response is: " + error);
                                Log.d(DEBUG_TAG, "Version is: " + version.get());
                                editor.apply();
                                if(vermin >= BuildConfig.VERSION_CODE){
                                    Intent i = new Intent(MainActivity.this, UpdateActivity.class);
                                    startActivity(i);
                                    finish();
                                    return;
                                }
                                if (version.get() > BuildConfig.VERSION_CODE) {
                                    if (builder != null) {
                                        builder.setTitle(getString(R.string.obsolete));
                                        builder.setMessage(getString(R.string.update));
                                        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> updateApp());
                                        builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                                        builder.show();
                                    }
                                } else if (!Objects.equals(message, "")) {
                                    if (message.contains("https://")) {
                                        if (builder != null) {
                                            builder.setTitle(getString(R.string.visitlink));
                                            String s = getString(R.string.link);
                                            s += message;
                                            builder.setMessage(s);
                                            builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> openlink(message));
                                            builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                                            builder.show();
                                        }
                                    } else {
                                        if (builder != null) {
                                            builder.setTitle(getString(R.string.important));
                                            builder.setMessage(message);
                                            builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                                            builder.show();
                                        }
                                    }
                                }
                            }
                            /// REMOVIDO POR UI
                            /*else {
                                if (getApplication() != null && requestQueue != null) {
                                    mySnackbar = Snackbar.make(findViewById(R.id.container),
                                            "Bolita Cubana encontró un error interno", Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                }
                            }*/
                        } catch (JSONException e) {
                            //errorStart.set(true);
                            /// REMOVIDO POR UI
                            if (e.getMessage() != null) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }
                            if (Build.VERSION.SDK_INT >= 19) {
                                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                firebaseCrashlytics.sendUnsentReports();
                                firebaseCrashlytics.recordException(e);
                            }
                            /*if (getApplication() != null && requestQueue != null) {
                                mySnackbar = Snackbar.make(findViewById(R.id.container),
                                        "Ha ocurrido un error grave al sincronizar", Snackbar.LENGTH_LONG);
                                mySnackbar.show();
                            }*/
                            //throw new RuntimeException(e);
                        }

                    }, error -> {
                /// REMOVIDO POR UI
                /*if (error instanceof TimeoutError) {
                    if (getApplication() != null && requestQueue != null) {
                        mySnackbar = Snackbar.make(findViewById(R.id.container),
                                "Su conexión está lenta, y demora descargar datos", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                    }
                } else {
                    if (getApplication() != null && requestQueue != null) {
                        mySnackbar = Snackbar.make(findViewById(R.id.container),
                                "La conexión parece inestable", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                    }
                }*/
                Log.e(DEBUG_TAG, "ERROR");
            });
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
        }
        //handler.post(() -> {

        //});
        //});
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post charada.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without charada.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!first)
            startSync();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main); // R.id.nav_host_fragment_content_main
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.options) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            i.putExtra("main", true);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.about) {
            if (builder != null) {
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(getString(R.string.aboutinfo1) + BuildConfig.VERSION_NAME + getString(R.string.aboutinfo2));
                builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.openprivacy), (dialog, which) -> loadToS());
                builder.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadToS() {
        try {
            String privEng = "PCFET0NUWVBFIGh0bWw+DQo8aHRtbCBsYW5nPSJlbiI+DQo8aGVhZD4NCgk8bGluayByZWw9Imljb24iIGhyZWY9ImRhdGE6aW1hZ2UveC1pY29uO2Jhc2U2NCxBQUFCQUFFQVNFZ0FBQUVBQ0FESUd3QUFGZ0FBQUNnQUFBQklBQUFBa0FBQUFBRUFDQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBS29BQkFDcUFCY0lwQUFSQ2FVQUNncWxBQlVTbndBZkVwNEFHeE9mQUNBVG53QWlGcFlBSEJXZ0FCNFptUUFYRjZJQUl4bVpBQ2tabUFBa0c1b0FLaHliQUNraGxnQXRJWlVBTXlHVkFEQWtqd0E5S25zQUx5T1hBRFlwZ2dBN0tZSUFOQ2lLQURjbWtBQXpKNUlBUnpObUFEd3VnQUJCTG44QVBDdU1BRUFyakFCRU1Ya0FPaXVWQUVBdmlBQlFPV1VBUlRWM0FGUTlXZ0JFTW9NQVNUZHlBRlk5WUFCTU4za0FURGwwQUVFeGt3QmJRbGNBWmtwQUFHRkhUUUJHTlk4QVVqNXlBRzFNUGdCMVV5WUFhRXBIQUU4OWVRQlpRbTRBVHp5R0FIcFpJQUNCV3hvQWpXTUFBSHBZS3dDRFhSMEFoRjRlQUl4aEZ3Q0lZeFFBa1djSUFHRk1jZ0NUYUF3QWVsWlFBSjF0QUFDWGJnQUFtRzhBQUd4VVpBQk9SS0FBbm00REFKcHdBQUJlU28wQW5ISUJBSmh0RndDZGN3UUFublFJQUo5MUN3QnhWWUVBb0hZT0FJZHBRd0NoZHhFQWMxdDdBS0o0RXdDbmZnQUFxSDhBQUtONUZnQmxWWnNBcVlBQ0FLUjZHQUNoZlJRQXFvRUdBSGhnZ0FDbWZCd0FxNElLQUtOL0dBQ3NndzBBcElBYUFLaCtJQUNwZnlFQXFvQWpBS2FDSGdDcmdTVUFwNE1nQUtpRUlnQ3VneWdBcVlVakFLcUdKUUNyaHljQXJJZ29BS2FITUFDdGlTb0Fyb29yQUpoK1hBQ3ZpeTBBc1kwd0FJOTVmQUNzalRnQXRJOHlBSytRUEFDNGt6Z0FzWkkrQUxLVFFBQ3psRUVBdEpWQ0FMV1dRd0MzbDBRQXVKaEdBTHFhU0FDOG5Fb0FwNUowQUxhY1VBQy9vRTRBdVo5VEFMcWdWUUNjanAwQXZLSlhBTHlnWHdDK3BGZ0F3S1phQU1Da1l3RENwbVVBdForQkFNT3BYZ0REcDJZQXhLbG9BTVdxYVFESHEyb0F5YTFzQU1PdGNBREVybkVBeGE5eUFNYXVld0RBc0hjQXlMSjFBTW16ZGdETHMzOEF6TFY1QU0yMmVnRE50WUVBenJkN0FNNjJnZ0RRdUlVQXlycUJBTks2aHdETXVvb0F3TFNqQU0yN2l3Qzhzck1BejcyTkFORytqZ0RGdWFnQTA4Q1JBTlRCa2dET3daVUExY09UQU5MR21RRFV4YUVBMU1pYkFOYkpuQURYeXAwQTJNbWxBTm5McGdEVXk2b0Eyc3luQU52TnFBRGN6cWtBM3MrckFOblFyZ0RnMGEwQTI5S3dBT0xUcndEYzA3SUEzZFN6QU43VnRBRFkwNzBBMDg3UkFOalN3d0RmMXJVQTROYThBT0xadHdEazJjQUE1ZHJCQU4vWXlnRG0yOElBNTl6REFPamV4QURwMzhVQTQ5L0lBT1hlendEazRNa0E3Ti9PQU9iaHlnRG40c3NBNk9QTUFPbmt6UURxNDlRQTZ1WE9BT3ZtendEcjVkWUE3T2ZRQU96bTF3RHU1OWdBNytqWkFQRHAyZ0R5Njl3QTgremRBTzNzNEFEdTdlRUE5TzdlQU8vdTRnRHc4T01BOHUvcUFQSHg1QUR6OE9zQTgvTGxBUFgwNXdEMTh1NEE5dlB2QVBiMTZRRDM5ZkFBK1BieEFQcjM4Z0Q3K1BNQS9QbjBBUDM2OVFELy9QZ0ErZno2QVByOSt3RC8vdmtBKy8vOEFQei8vUUFBQUFBQS8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUYvMGIvU1VsR1NVbEdTZjlHLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUnY5SlNrWkpTVVJOVFVaSlJrWkdUVWxKU1VsR1JVYi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlHUmsxSlNrWkdxc1BLeWNtOXM0UkpTWE9vZWt4TlNVbEpTVVgvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUpTVVJKUkZsR1RVbEp6LzM3K2YzKytWWkdSazNDL2ZUSmxXSkZSa1pFU1UzLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wbEdSVXlvNTk2blRVWkttUDM5K1gyZnkwWkdSa2x6L2YzOS9mdldsRXhFUmtaSi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzBaR1NVMUphZWV2L1ozOXkwWkdhZjM5OUdoR1NVbEpTVVppL2Y3OS9mNzBtTXFDU1VaS1J2Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUmtaSlRVMVM1dXpvNlBuKy9kRlBTZlQ5L2Zuc2xVcEdSa2FmL2ZuTCtmblNpT1QwVFd0SlJrWkovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlKUmtsR2NwM3kvZjM3MC8zNS9idG1SdlQ5L3ZMVzhrWk5hYzM5L2ZtVmNaVzY4L25vYS8zZWFVbEdSa1gvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vMFpHVFlEbS9mdjUvdkM2czdDdmhrMUdlUDc2bFUrWTdVbE5vLzc1L2Yzc1RVbE5nL21OUmxSNllrWkdTVVpKLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Sa2xFc3YzNStmbml3bkpKU1VaR1lHbEp3dDlYVFhyNS9aU0MwTUx4L2YzOWg1QmRUZU5OUmtsSlNVbEpUVWxKU2YvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUpUVnpVK2ZMOStwaEdSa1pHUmtaLytmNTc0bEJOVnZUNStmM1RZWXo5L2YzOXQyWkdSRmhLUmtsR1JrWkdUbUZOU2tuLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JrMUdjUEw1NnMzOStsSnBzbEtEU1VsSmRsU3EvYWhNUmxTNi9idFFxUDM5L2YzKzRFV0RWMFZLUkVaSlJrMUpzZkZ2VFVaRi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1NrbDE5UDM5K3V2KzlNRDVtVWJNcEVaR1JrYTUrZjdLU1VsUytkN3crZjM5L3YzKys1ajNzVVpKVFYxSlJrbDUvZUM1Y2taR1NmLy8vLy8vLy8vLy8vLy8vLy8vLy85RVRYajAvdm45YVZUMy9mM01TVW5DL3VUb2ZFbGs3TjF2WFVaR3pQTzE3UDc5K2ZuU2pvUGcxa2xHcG5WR1JrWGQrYkpyOTJsSlJrWC8vLy8vLy8vLy8vLy8vLy8vLzBaTmIvVDkvWEM5L25YZy9mNk1SbEwwdTRscFJrWkdZT0ROdVYxSlhyUmg2UDc1K2FaR1JrM1M5R1JHUkVsNGtFbmUvZFZVK2V4WVJrbi8vLy8vLy8vLy8vLy8vLy8vU1VaVTYvMzkrWHhLZFZUbi9mdkRpTldQUmtsR1JrbEdSazJucGtsR1JuajAvY1AwekVsTm1JTDcrZm50NjBwejFIajUrZjNWN2MvZ1RFWkcvLy8vLy8vLy8vLy8vLy8vUmthamgvUDkvZXBKUlVuMytmbjUwb0pKVFUxS1NVbEdTWlRhNnFKR1JrbDcrZkQ1dFVaTlJsSzU2dFg1L21sR3kvbjkvZjNLK2JYN2dFbEcvLy8vLy8vLy8vLy8vLzlKUmxKcFJvTDkvZjNzbUtycG5XOUdUVTJJeVlSR1RFWkpSckw1L3NWSlNVWkdhZWo5L2ZUZHdHQkVTVWxjbDA5SmFmVDUrZGFBL2ZMOXVrWkpSZi8vLy8vLy8vLy8vLzlKU2tsSlJrYmQvZjM5K2FkT1RVVkZTVmI1L2YyN1RVWkpSbFRnL2RGTlJrWkpTVlMwK3ZuKy9aMUdYVVpHUmtwR1JubnovZEZ4NFBuOS9ZeE5Sa24vLy8vLy8vLy8vMDFOUmsxR1JrUzkvZjMrdFVsR09UOUZSWTcwZ0lMMHlVbEdSa1pXNlAySlJrbEdSa1pOV0l6RS9aMUo4bkJHU1UxTlNVWk9qUFJYVXZUNStmbDVTVVpHLy8vLy8vLy9Sa2x6eTAxR1Jrbncvdjc5VzBaR01RRWxQNGo2cGtteS9ySktSa2xKWE56NWcwbEdSa1pHVjlhRCtYMUo5S2RHU1VhRGcwWkdVdlR3N2YzOS9mM3lWa2IvLy8vLy8vLy9UVWJEL1lKSlNWUzEzN0g1UmtsR0hRQUFCQ1JGUkVwR2IvQnBTVVpHUmthY3AwWkdTVWxKUnJqNXpFUkd1YzlFVFVaR1JrWkpWSEQwL2YzOXVjem90MFpHLy8vLy8vLy9SbUQrL2ZtZFVrMTFaa1pZUmtSTUhRUUFBQUFMTGtaR1NaS3hSa1pKVFVaSlNVWkpTVVpHUmsvaW5rWkdWS1pHYVZCSlJrbEpzbWxoeWZuKy9zSmQ2RnhKU2YvLy8vOUdTWWY1L2Y2NTZyVkdSVWxHU1VWRkdRQUFCQUFFQUIwL1NrM3J2WFZHWG9OR1JrbEtSa2xKU1VsVWpFbEpTVWxOeWxsTlNYeFdSbFpOUm15WG8zRkpVbEpHUnYvLy8vOUdTVjFjWXVDRHd2NVFTV2xTU1VWTUd3QUFBQUFBQUFBRkxrYUUrWHBHUmtaSlNVbEdUVnhHUmtaR1JrWkdSa1pHeWthQ2duMWNTa1pHUmtsR1JrWkpSVTFHU2YvLy8vOU5UL0Q1Vkx6MC9mbEpSdkw2VkVSSkVnQUVBQUFFQUFBRUFDVkZWMFJKU1VsR1JrWldiWDFXVmxaV1ZsWldWbFpXWTFSR1JrMU5UVWxKU1VaTlNVbEdSRVZHUmtiLy8wWkdsLzM5N1B2OS9mM3M2djM5K2RxY0VRQUFBQUFBQUFRQUFBQW1tRWF6NC9UMC9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzM5UFR5N096cDM5WEt2YVpwU2YvLy8wbE4wLzM5L2YzOS9mMzkvZjM5L2ZueUVRQUVBQUFFQUFBQUFBUUFDd3RUMWY3OS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjdDUmtuLy8wVkc5LzM5L2YzOS9mMzkvZjM5L2YzNkVRQUFBQUFBQUFBQUJBQUFBQVFGTVlYcy9mbjkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMzVEVuL1JVUnUvZjM5L2YzOS9mMzkvZjM5L2YzNUVRRUVBQUFFQUFBQUFBQUFBQUFBQkFBbGtmTDkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMrZUViL1JrU00vZjM5L2YzOS9mMzkvZjM5L2YzOUR3QUFBQUFBQUFBRUFBQUVBQVFBQUFBQUFTcXUrUDM1L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5blVsR1NVV2QvZjM1L2YzOS9mMzkrZjM5L2YzOUN3QUFBQUFFQUFBQUFRRUFBQUFBQkFBQUFBQUVRYnI4K2ZuOS92NzkvZjM5L2YzNStmMzkvZjM5L2YzOS9mMzkvZm45dFVaSi8wYXYvZm41L2YzOS9mMzkvZjM1K2ZuNUN3UUFCQUVCQUFFQkFRQUFBQVFBQUFBQUJBQUFBQVZIcXVEditmcjUvZjM5L3Y3NS9mNzUvZjM5L2Y3OS9mMzUvdm43dWtsSlNrMjUrYmVRek9xSmFsUk1Sa1JGUmtaR0N3QUFBQUFBQVJVNEZBRVVPUWtBQkFBQUFBQUVBQUFFR3poRlRsZGRVRlJVWklQMDhQbjY2Zm4rNUtQOS92blZxdUQ1Z2taRlJrM0MrZXkwNElkSlNVbEpTVVJNVEVSRUN3QUFCQUFBQVJqTW0zZkdyd3NBQUFBQUJBQUFBQUFBQUFBVk9FVkpTVWxKU1Vtb1ZPaUkrZjN6NkUzai9idEdpWFp4aGtsSlNVYkt5Ni9nL1hGR1JrWkdSa1pHUkVWTUN3RUFBQUFFQUIvcy9mMzlqQUFBQkFBQUFBQUVBQUFFQUFBQUFSYzRSVVpHUmtaR1JsMlUvY3FacUlpTSthZEp0VmlZeFVaR1NVekNnMFpOWFYxSlNVbEpTVWxKU1VWTURBQUFBQUFBQUZYOS9mMzlqQWNBQUFBQUFBQUFBQUFBQUFBRUFBQUNSRWxKUmtsSlprbTAvZWlIdmZScDh2VFYzZkhlVjBsRVJrM0s5UERMU1VsR1JrWkdSa1pHUmtSSkNnQUFCQUFFSE1uNS9mMzkrODVhQkFBQUJBQUVBQUFFQUFBQUFRQTFSa1pHU1VaR2cwVzcvZlRUL2YyajFQMzk0SFZHUmtaR1JrbTkrZjNSVFVhQ2wwWkdSa1pHUmtaTUJ3QUVBQUF0VEp6OSsvM2N2OVBJQUFBQUFBQUFBQUFCQUFFQkFpOUpSa1pHUmtaR1JrYTUrWWhHZzgxeWFiTDlXVTFObUVsSlNVYXl1ZjM5bmtueXJFbEpTVWxKU1VsR0N3QUFBQVFBQUFWYTZ1eEREd1FBQkFBQUJBQUJBUUVCQVFFcVAwUkdTVWxKU1VsSlNWRG13a1JKU1VaR1J1bjkwOTFQU1VaRi8wYWsvZjM5L2MvOThMMnhzYkd4c2JHeER3QUVBQUFBQUFBQU5Vd0ZBQUFBQUFBQUFRQUJBQUVBTUhTSWpaZWRwS3l4c2JHenhmSDk1TE94c2JQQTF2MzkvZm1zZlVaSlJrMlAvZjM5L2YzOS9mMzkvZjM5L2YzOUNRQUFBQVFBQUFRQUJDOEFBQVFBQVFFQkFBRUJBUW1yL2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMrcWtsRi8wbHkvZjM5L2YzOS9mMzkvZjM5L2YzOUVRQUFBQUFBQUFBQUFBVUFBUUVBQVFBQUFBQUZYOW45L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM1Z2tsR1JrMUorZjM5L2YzOS9mMzkvZjM5L2YzOUVRRUVBQVFBQUFRQUFBQUFBQUFCQUFFQkFVdkkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM2VUViLy8wbEcyLzM5L2YzOS9mMzkvZjM5L2YzOUZnQUFBQUFBQUFBQUFRQUVBUUVCQUFFaXFmbjkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjNQU1VYLy8wWkpsZjM5K2YzOS92MzkvZm45L2YzNUVnUUFBQVFBQUFRQUFBQUFBUUFBRFlyMC9mMzkvZjM1L2YzOS92MzkvZjM5L2YzNS9mMzkvZm4rL2YzOS9mMzkrZjZZUmtuLy8wWkpUTkwrL2YzOStmbjUrZm45L2YzK0d3QUFBQUVBQUFFQUFBRUJBUVJSMC8zOS9mMzkrZjM1L2ZuOSsvbjUvdjMrL2ZuOS92NzUvdjM5L2Y3Ky9mNzUrZjFZU2YvLy8vOUdSa1o2bTYrd3JLYWZsWmlIZjNWakZBQUFBUUFBQVFBQkFRQUJLanA5Zm9KK2ZuNkFnb0IvZ29LQ2dvYUFpNVdqdWUzOTBaZU1mb0NDZ29lU21MTGUvZGxHUmtiLy8vOUpSa2xOVkdSR1RVWkpSa1pKUmtsR0dnQUVBU3dwQkFBQUFCazRSa1pHU1VsSlNVbEpTVVpKUmtsR1NVbEpSazFHVFZ6M2drWk5ZRWxKU1VsR1JrbEducGhKUlAvLy8vOUdTVVoyejRKSlJrbEdSa1pHU1VaSkdRQUJBVWltTWdBVExrWkdSa2xHUmtaR1JrWkdSa1pHU1VaR1JrWkdXWGhKVWszUW1rbUR5VVpHU2sxSlNVWlowMUpHLy8vLy8vLy9TVVoyMDROUVJrbEdTVWxKU1VaRklBRUFBVFpxZUQ5SlNrbEdTVWxHU1VaSlJrbEpSa2xKUmtaR1NVWkpUbDFjNm1MY3VVbFVWa2FEK2I5T1JrYmpwMGxKLy8vLy8vLy9SVWxONlAzNTlKNUpVRlJHUmtaRUp5OFJHVUJKU1VsR1JrWkpSb05tUm1DZFNVWkdTVVpHU1VaSlJrbEdTVWxYL3YzOS9kVzlyS0tpOU9SVVJrYUpSa1pGLy8vLy8vLy8vMGxLaC83NS91YnMvZjJDU1VaRUtuRnBTVVpHUmsxRlprMUdoRWxHYmZDRFJrWkdSa1pHVFVaSlRVbE5Sa1pHMC9uOStmdjkrZnIrM2xCR1JrbEpTVW4vLy8vLy8vLy8vMFJHU3QvdFVFbGw2ZjNjUmtsR000ZThSa2xKU1VaSjAyTko4VXBHaG5WR1Q3SlBTVWxKbDBsR1JrWkZTVVI2YWZyOXJOdjk4M0xMM1ZCSlNVWkdSa2IvLy8vLy8vLy8vLzlHU1hINzdPajkvZjM5djNwR1NVWkdSa1pHUmtaVy9YTkcya3BOU1VsR1JrWkdTVVZHUmtaR1RVbE5SRWFtU1huNnIwemsvZXhnc3VaelRrWkpTZi8vLy8vLy8vLy8vLy8vUmttZitmMzkvZjM5L2NSSlJsSzUwbVZHUmsxeitWMUdhVTFVUmtaSlNVbEpSa1JRdmMrNW9ZaHZlRlJTUmtaam5tbUEvclJLUnBEOXlrbEdSdi8vLy8vLy8vLy8vLy8vLzBaRmh1djUvZjM5L2RKSlJvTCsvcGxHU1VsajgwWkpTVXpzblo5U1JrWkdUVTJtL2YzOS9mbjUrY0JHU1VsSlJrWlMvdDJTUmtsZGJFWkcvLy8vLy8vLy8vLy8vLy8vLzBsSlRhYjkvZjM5L2YyL1JrYTcrWmxOUmthMHAwbEdSbEwwK2YzRFJrbEpac1g1K2YzOStldWRsWUpKUmtaR1NVbE42UDcrckVaSlJrVC8vLy8vLy8vLy8vLy8vLy8vLy85R1JsZnkvdjM5L2YzOWIwbEppRjFFWHNEdFZFWkdSa2xjbTlYOXpKQktUb2g1Vk1uOXo3bEdSa3BLUmtaR1JrWkdoODMzeTBsSlJ2Ly8vLy8vLy8vLy8vLy8vLy8vLy85SlJrVnA2ZnYrL2YzOXNrWkdsTmJ3K2M5bVJrWkdTVVpPWEVtVitmMXBSa1ZHVGVENTYvMkNTVTFHU1VsSlNVMU5Sa1pHU1VaRy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JrbEVYWjNSNlBuOTVFMUo5UDJ5UmsxR1RseEpTWHowKzJ4US9mMWdSazFFU2ZUNXozaEpSa1pHU1VaR1RWSnZja2xKU1ViLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wWkdTVVpKUnVmOStWbEcvZjZtUm5DQ1puaEdWUFQ5L2VqSy9iSkdSblpkUm9kc1JrbEdTVWxKUmxLVjNmdis5RmxHUnYvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUVSa1pKUnNiKytXUmMvZjdGU1lMMzhITkdkdm4rN2MzVmhFbEdTVnhRU1VaR1JrWkdUVVZXclBUOS9mN3lkVWxHLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9TVWxHU1ZlTmJVMXovdW1YUmtsVWdtUkpTcFNYZmxsR1JrMUdSazFOUmtsTlRVbEpkZWo5NDVENS9kSmdSa2IvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlHU1VaSlNVYVM3RTlFUm1sTVJsUnBURTFHUmtsR1NVbEdTV21odmMvS2hIQ1k3UDM5K2I3dGlFbEdSdi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUmtaR1JrbWNyRW1DU1gzU1VPbjk5SjFOU1gyOWdrWjU0UDc5L2YzOS9mMzkrZm45OHFaT1NVWkUvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy85SlJrbEdTVkR0OU5uNXRPdjkvZjJ5c3YzOTFvejUvZjN5K2YzOTdjLzkrZXlhU1VsSlJrYi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JFWkpSa1p2MHZUZmlmNzUvdjM5L2YzOS9mdisvZjNQL2YzOStmckJna2xHUmtaRy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Sa2xKUmtaR3VlUEN0ZEwrK2YzKy92bTUvdm4rK2ZMQmtGUkpTVVpHU2YvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vMFZHU1VsSlJrWkdSa2FEczd2SnljTzFvNWg2WEVsSlJrWkdSZi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzBWR1JrbEpTVWxKUmtaSlNVWkdTVWxKU1VaR1J2Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wVkdSa1ZHU1VsR1JrbEpSa1pHUnYvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8rb0MvLy8vL0FBQUEvLy8vUUFBQi8vLy9BQUFBLy8vK0FBQUFQLy8vQUFBQS8vL3dBQUFBQi8vL0FBQUEvLy9nQUFBQUEvLy9BQUFBLy84QUFBQUFBZi8vQUFBQS8vNEFBQUFBQUgvL0FBQUEvL3dBQUFBQUFCLy9BQUFBLy9nQUFBQUFBQS8vQUFBQS8vQUFBQUFBQUFmL0FBQUEvK0FBQUFBQUFBUC9BQUFBLzRBQUFBQUFBQUgvQUFBQS80QUFBQUFBQUFEL0FBQUEvd0FBQUFBQUFBQi9BQUFBL2dBQUFBQUFBQUIvQUFBQS9BQUFBQUFBQUFBL0FBQUEvQUFBQUFBQUFBQS9BQUFBK0FBQUFBQUFBQUFmQUFBQStBQUFBQUFBQUFBUEFBQUE4QUFBQUFBQUFBQUhBQUFBNEFBQUFBQUFBQUFQQUFBQTRBQUFBQUFBQUFBSEFBQUE0QUFBQUFBQUFBQURBQUFBd0FBQUFBQUFBQUFEQUFBQXdBQUFBQUFBQUFBREFBQUF3QUFBQUFBQUFBQUJBQUFBZ0FBQUFBQUFBQUFEQUFBQWdBQUFBQUFBQUFBQkFBQUFnQUFBQUFBQUFBQUJBQUFBQUFBQUFBQUFBQUFCQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ0FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFnQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQWdBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUJBQUFBZ0FBQUFBQUFBQUFCQUFBQWdBQUFBQUFBQUFBQkFBQUFnQUFBQUFBQUFBQURBQUFBd0FBQUFBQUFBQUFCQUFBQXdBQUFBQUFBQUFBREFBQUF3QUFBQUFBQUFBQUhBQUFBNEFBQUFBQUFBQUFIQUFBQTRBQUFBQUFBQUFBSEFBQUE4QUFBQUFBQUFBQVBBQUFBOEFBQUFBQUFBQUFQQUFBQStBQUFBQUFBQUFBZkFBQUEvQUFBQUFBQUFBQWZBQUFBL2dBQUFBQUFBQUEvQUFBQS9nQUFBQUFBQUFCL0FBQUEvd0FBQUFBQUFBRC9BQUFBL3dBQUFBQUFBQUgvQUFBQS80QUFBQUFBQUFQL0FBQUEvOEFBQUFBQUFBZi9BQUFBLytBQUFBQUFBQS8vQUFBQS8vQUFBQUFBQUIvL0FBQUEvL3dBQUFBQUFELy9BQUFBLy80QUFBQUFBSC8vQUFBQS8vK0FBQUFBQVAvL0FBQUEvLy9BQUFBQUEvLy9BQUFBLy8vNEFBQUFELy8vQUFBQS8vLzhBQUFBZi8vL0FBQUEvLy8vZ0FBRC8vLy9BQUFBLy8vLzhBQWYvLy8vQUFBQSIgdHlwZT0iaW1hZ2UveC1pY29uIiAvPg0KCTwhLS0gPG1ldGEgaHR0cC1lcXVpdj0iY29udGVudC10eXBlIiBjb250ZW50PSJ0ZXh0L2h0bWwiPiAtLT4NCgk8IS0tIDxtZXRhIGh0dHAtZXF1aXY9ImNvbnRlbnQtc3R5bGUtdHlwZSIgY29udGVudD0idGV4dC9jc3MiPiAtLT4NCgk8bWV0YSBjaGFyc2V0PSJ1dGYtOCIgLz4NCiAgICA8bWV0YSBjb250ZW50PSJJRT1lZGdlLGNocm9tZT0xIiBodHRwLWVxdWl2PSJYLVVBLUNvbXBhdGlibGUiIC8+DQogICAgPG1ldGEgY29udGVudD0iUHJpdmFjeSBQb2xpY3kiIG5hbWU9ImRlc2NyaXB0aW9uIiAvPg0KICAgIDxtZXRhIGNvbnRlbnQ9IndpZHRoPWRldmljZS13aWR0aCwgaW5pdGlhbC1zY2FsZT0xIiBuYW1lPSJ2aWV3cG9ydCIgLz4NCiAgICA8bGluayByZWw9ImNhbm9uaWNhbCIgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvL2FwaS9wcml2Lmh0bWwiIC8+DQogICAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIGhyZWZsYW5nPSJ4LWRlZmF1bHQiIGhyZWY9Imh0dHBzOi8vY3ViYW5hcHAuaW5mby9hcGkvcHJpdi5odG1sIiAvPg0KICAgIDxsaW5rIHJlbD0iYWx0ZXJuYXRlIiBocmVmbGFuZz0iZW4iIGhyZWY9Imh0dHBzOi8vY3ViYW5hcHAuaW5mby9hcGkvcHJpdi5odG1sIiAvPg0KICAgIDxsaW5rIHJlbD0iYWx0ZXJuYXRlIiBocmVmbGFuZz0iZXMiIGhyZWY9Imh0dHBzOi8vY3ViYW5hcHAuaW5mby9hcGkvcHJpdmVzLmh0bWwiIC8+DQoJPG1ldGEgbmFtZT0iYXV0aG9yIiBjb250ZW50PSJDVUJBTkFQUCBMTEMiIC8+DQogICAgPCEtLSA8bGluayBocmVmPSJodHRwczovL21heGNkbi5ib290c3RyYXBjZG4uY29tL2Jvb3RzdHJhcC8zLjMuNy9jc3MvYm9vdHN0cmFwLm1pbi5jc3MiIHJlbD0ic3R5bGVzaGVldCI+DQogICAgPHNjcmlwdCBzcmM9Imh0dHBzOi8vYWpheC5nb29nbGVhcGlzLmNvbS9hamF4L2xpYnMvanF1ZXJ5LzMuMi4xL2pxdWVyeS5taW4uanMiPjwvc2NyaXB0Pg0KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL21heGNkbi5ib290c3RyYXBjZG4uY29tL2Jvb3RzdHJhcC8zLjMuNy9qcy9ib290c3RyYXAubWluLmpzIj48L3NjcmlwdD4NCiAgICA8bGluayBocmVmPSJodHRwczovL21heGNkbi5ib290c3RyYXBjZG4uY29tL2ZvbnQtYXdlc29tZS80LjcuMC9jc3MvZm9udC1hd2Vzb21lLm1pbi5jc3MiIHJlbD0ic3R5bGVzaGVldCI+IC0tPg0KICAgIDxsaW5rIGhyZWY9Imh0dHBzOi8vZm9udHMuZ29vZ2xlYXBpcy5jb20vY3NzP2ZhbWlseT1PcGVuK1NhbnM6MzAwLDMwMGksNDAwLDQwMGksNjAwLDYwMGksNzAwLDcwMGksODAwLDgwMGkiIHJlbD0ic3R5bGVzaGVldCIgLz4NCiAgICA8c3R5bGU+DQogICAgICAgIGJvZHkgew0KICAgICAgICAgICAgZm9udC1mYW1pbHk6ICdPcGVuIFNhbnMnLCAnSGVsdmV0aWNhJywgc2Fucy1zZXJpZjsNCiAgICAgICAgICAgIGNvbG9yOiAjMDAwOw0KICAgICAgICAgICAgcGFkZGluZzogNXB4Ow0KICAgICAgICAgICAgbWFyZ2luOiA1cHg7DQogICAgICAgICAgICBsaW5lLWhlaWdodDogMS40Mjg7DQogICAgICAgIH0NCiAgICAgICAgaDEsIGgyLCBoMywgaDQsIGg1LCBoNiwgcCB7DQogICAgICAgICAgICBwYWRkaW5nOiAwOw0KICAgICAgICAgICAgbWFyZ2luOiAwOw0KICAgICAgICAgICAgY29sb3I6IzMzMzMzMzsNCiAgICAgICAgfQ0KICAgICAgICBoMSB7DQogICAgICAgICAgICBmb250LXNpemU6IDMwcHg7DQogICAgICAgICAgICBmb250LXdlaWdodDogNjAwIWltcG9ydGFudDsNCiAgICAgICAgICAgIGNvbG9yOiAjMzMzOw0KICAgICAgICB9DQogICAgICAgIGgyIHsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMjRweDsNCiAgICAgICAgICAgIGZvbnQtd2VpZ2h0OiA2MDA7DQogICAgICAgIH0NCiAgICAgICAgaDMgew0KICAgICAgICAgICAgZm9udC1zaXplOiAyMnB4Ow0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDYwMDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OiAyOHB4Ow0KICAgICAgICB9DQogICAgICAgIGhyIHsNCiAgICAgICAgICAgIG1hcmdpbi10b3A6IDM1cHg7DQogICAgICAgICAgICBtYXJnaW4tYm90dG9tOiAzNXB4Ow0KICAgICAgICAgICAgYm9yZGVyOiAwOw0KICAgICAgICAgICAgYm9yZGVyLXRvcDogMXB4IHNvbGlkICNiZmJlYmU7DQogICAgICAgIH0NCiAgICAgICAgdWwgew0KICAgICAgICAgICAgbGlzdC1zdHlsZS10eXBlOiBub25lOw0KICAgICAgICAgICAgbWFyZ2luOiAwOw0KICAgICAgICAgICAgcGFkZGluZzogMDsNCiAgICAgICAgfQ0KICAgICAgICBsaSB7DQogICAgICAgICAgICBkaXNwbGF5OiBpbmxpbmUtYmxvY2s7DQogICAgICAgICAgICBmbG9hdDogcmlnaHQ7DQogICAgICAgICAgICBtYXJnaW4tbGVmdDogMjBweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OiAzNXB4Ow0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDEwMDsNCiAgICAgICAgfQ0KICAgICAgICBhIHsNCiAgICAgICAgICAgIHRleHQtZGVjb3JhdGlvbjogbm9uZTsNCiAgICAgICAgICAgIGN1cnNvcjogcG9pbnRlcjsNCiAgICAgICAgICAgIC13ZWJraXQtdHJhbnNpdGlvbjogYWxsIC4zcyBlYXNlLWluLW91dDsNCiAgICAgICAgICAgIC1tb3otdHJhbnNpdGlvbjogYWxsIC4zcyBlYXNlLWluLW91dDsNCiAgICAgICAgICAgIC1tcy10cmFuc2l0aW9uOiBhbGwgLjNzIGVhc2UtaW4tb3V0Ow0KICAgICAgICAgICAgLW8tdHJhbnNpdGlvbjogYWxsIC4zcyBlYXNlLWluLW91dDsNCiAgICAgICAgICAgIHRyYW5zaXRpb246IGFsbCAuM3MgZWFzZS1pbi1vdXQ7DQogICAgICAgIH0NCiAgICAgICAgbGkgYSB7DQogICAgICAgICAgICBjb2xvcjogd2hpdGU7DQogICAgICAgICAgICBtYXJnaW4tbGVmdDogM3B4Ow0KICAgICAgICB9DQogICAgICAgIGxpID4gaSB7DQogICAgICAgICAgICBjb2xvcjogd2hpdGU7DQogICAgICAgIH0NCgkJPCEtLQ0KICAgICAgICAuY29sdW1uLXdyYXAgYSB7DQogICAgICAgICAgICBjb2xvcjogIzVjMzRjMjsNCiAgICAgICAgICAgIGZvbnQtd2VpZ2h0OiA2MDA7DQogICAgICAgICAgICBmb250LXNpemU6MTZweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OjI0cHg7DQogICAgICAgIH0NCiAgICAgICAgLmNvbHVtbi13cmFwIHAgew0KICAgICAgICAgICAgY29sb3I6ICM3MTcxNzE7DQogICAgICAgICAgICBmb250LXNpemU6MTZweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OjI0cHg7DQogICAgICAgICAgICBmb250LXdlaWdodDozMDA7DQogICAgICAgIH0NCiAgICAgICAgLmNvbnRhaW5lciB7DQogICAgICAgICAgICBtYXJnaW4tdG9wOiAxMDBweDsNCiAgICAgICAgfQ0KICAgICAgICAubmF2YmFyIHsNCiAgICAgICAgICAgIHBvc2l0aW9uOiByZWxhdGl2ZTsNCiAgICAgICAgICAgIG1pbi1oZWlnaHQ6IDQ1cHg7DQogICAgICAgICAgICBtYXJnaW4tYm90dG9tOiAyMHB4Ow0KICAgICAgICAgICAgYm9yZGVyOiAxcHggc29saWQgdHJhbnNwYXJlbnQ7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhci1icmFuZCB7DQogICAgICAgICAgICBmbG9hdDogbGVmdDsNCiAgICAgICAgICAgIGhlaWdodDogYXV0bzsNCiAgICAgICAgICAgIHBhZGRpbmc6IDEwcHggMTBweDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMThweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OiAyMHB4Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXItbmF2PmxpPmEgew0KICAgICAgICAgICAgcGFkZGluZy10b3A6IDExcHg7DQogICAgICAgICAgICBwYWRkaW5nLWJvdHRvbTogMTFweDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMTNweDsNCiAgICAgICAgICAgIHBhZGRpbmctbGVmdDogNXB4Ow0KICAgICAgICAgICAgcGFkZGluZy1yaWdodDogNXB4Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXItbmF2PmxpPmE6aG92ZXIgew0KICAgICAgICAgICAgdGV4dC1kZWNvcmF0aW9uOiBub25lOw0KICAgICAgICAgICAgY29sb3I6ICNjZGMzZWEhaW1wb3J0YW50Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXItbmF2PmxpPmEgaSB7DQogICAgICAgICAgICBtYXJnaW4tcmlnaHQ6IDVweDsNCiAgICAgICAgfQ0KICAgICAgICAubmF2LWJhciBpbWcgew0KICAgICAgICAgICAgcG9zaXRpb246IHJlbGF0aXZlOw0KICAgICAgICAgICAgdG9wOiAzcHg7DQogICAgICAgIH0NCiAgICAgICAgLmNvbmdyYXR6IHsNCiAgICAgICAgICAgIG1hcmdpbjogMCBhdXRvOw0KICAgICAgICAgICAgdGV4dC1hbGlnbjogY2VudGVyOw0KICAgICAgICB9DQogICAgICAgIC5tZXNzYWdlOjpiZWZvcmUgew0KICAgICAgICAgICAgY29udGVudDogIiAiOw0KICAgICAgICAgICAgYmFja2dyb3VuZDogdXJsKGh0dHBzOi8vcmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbS9ob3N0aW5nZXIvYmFubmVycy9tYXN0ZXIvaG9zdGluZ2VyX3dlbGNvbWUvaW1hZ2VzL2hvc3Rpbmdlci1kcmFnb24ucG5nKTsNCiAgICAgICAgICAgIHdpZHRoOiAxNDFweDsNCiAgICAgICAgICAgIGhlaWdodDogMTc1cHg7DQogICAgICAgICAgICBwb3NpdGlvbjogYWJzb2x1dGU7DQogICAgICAgICAgICBsZWZ0OiAtMTUwcHg7DQogICAgICAgICAgICB0b3A6IDA7DQogICAgICAgIH0NCiAgICAgICAgLm1lc3NhZ2Ugew0KICAgICAgICAgICAgd2lkdGg6IDUwJTsNCiAgICAgICAgICAgIG1hcmdpbjogMCBhdXRvOw0KICAgICAgICAgICAgaGVpZ2h0OiBhdXRvOw0KICAgICAgICAgICAgcGFkZGluZzogNDBweDsNCiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICNlZWVjZjk7DQogICAgICAgICAgICBtYXJnaW4tYm90dG9tOiAxMDBweDsNCiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDVweDsNCiAgICAgICAgICAgIHBvc2l0aW9uOnJlbGF0aXZlOw0KICAgICAgICB9DQogICAgICAgIC5tZXNzYWdlIHAgew0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDMwMDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMTZweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OiAyNHB4Ow0KICAgICAgICB9DQogICAgICAgICNwYXRoTmFtZSB7DQogICAgICAgICAgICBtYXJnaW46IDIwcHggMTBweDsNCiAgICAgICAgICAgIGNvbG9yOiBncmV5Ow0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDMwMDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZToxOHB4Ow0KICAgICAgICAgICAgZm9udC1zdHlsZTogaXRhbGljOw0KICAgICAgICB9DQogICAgICAgIC5jb2x1bW4tY3VzdG9tIHsNCiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDVweDsNCiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICNlZWVjZjk7DQogICAgICAgICAgICBwYWRkaW5nOiAzNXB4Ow0KICAgICAgICAgICAgbWFyZ2luLWJvdHRvbTogMjBweDsNCiAgICAgICAgfQ0KICAgICAgICAuZm9vdGVyIHsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMTNweDsNCiAgICAgICAgICAgIGNvbG9yOiBncmF5ICFpbXBvcnRhbnQ7DQogICAgICAgICAgICBtYXJnaW4tdG9wOiAyNXB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6IDEuNDsNCiAgICAgICAgICAgIG1hcmdpbi1ib3R0b206IDQ1cHg7DQogICAgICAgIH0NCiAgICAgICAgLmZvb3RlciBhIHsNCiAgICAgICAgICAgIGN1cnNvcjogcG9pbnRlcjsNCiAgICAgICAgICAgIGNvbG9yOiAjNjQ2NDY0ICFpbXBvcnRhbnQ7DQogICAgICAgICAgICBmb250LXNpemU6IDEycHg7DQogICAgICAgIH0NCiAgICAgICAgLmNvcHlyaWdodCB7DQogICAgICAgICAgICBjb2xvcjogIzY0NjQ2NCAhaW1wb3J0YW50Ow0KICAgICAgICAgICAgZm9udC1zaXplOiAxMnB4Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXIgYSB7DQogICAgICAgICAgICBjb2xvcjogd2hpdGUgIWltcG9ydGFudDsNCiAgICAgICAgfQ0KICAgICAgICAubmF2YmFyIHsNCiAgICAgICAgICAgIGJvcmRlci1yYWRpdXM6IDBweCAhaW1wb3J0YW50Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXItaW52ZXJzZSB7DQogICAgICAgICAgICBiYWNrZ3JvdW5kLWNvbG9yOiAjNDM0MzQzOw0KICAgICAgICAgICAgYm9yZGVyOiBub25lOw0KICAgICAgICB9DQogICAgICAgIC5jb2x1bW4tY3VzdG9tLXdyYXB7DQogICAgICAgICAgICBwYWRkaW5nLXRvcDogMTBweCAyMHB4Ow0KICAgICAgICB9DQogICAgICAgIEBtZWRpYSBzY3JlZW4gYW5kIChtYXgtd2lkdGg6IDc2OHB4KSB7DQogICAgICAgICAgICAubWVzc2FnZSB7DQogICAgICAgICAgICAgICAgd2lkdGg6IDc1JTsNCiAgICAgICAgICAgICAgICBwYWRkaW5nOiAzNXB4Ow0KICAgICAgICAgICAgfQ0KICAgICAgICAgICAgLmNvbnRhaW5lciB7DQogICAgICAgICAgICAgICAgbWFyZ2luLXRvcDogMzBweDsNCiAgICAgICAgICAgIH0NCiAgICAgICAgfSAtLT4NCiAgICAgICAgfQ0KICAgIDwvc3R5bGU+DQogICAgPHRpdGxlPlByaXZhY3kgUG9saWN5PC90aXRsZT4NCjwvaGVhZD4NCjxib2R5Pg0KPGJyLz4NCjxoMT48cD48c3Ryb25nPlBSSVZBQ1kgUE9MSUNZIC0gTGFzdCBVcGRhdGUgMjYvMDYvMjAyMDwvc3Ryb25nPjwvcD48L2gxPg0KPGJyLz4NCjxzcGFuIHRpdGxlPSJTcGFuaXNoIj48YSBsYW5nPSJlcyIgaHJlZj0icHJpdmVzLmh0bWwiPkVzcGEmbnRpbGRlO29sPC9hPjwvc3Bhbj4NCjxici8+DQo8YnIvPg0KPHNwYW4gdGl0bGU9IkVuZ2xpc2giPjxhIGxhbmc9ImVuIiBocmVmPSJwcml2Lmh0bWwiPkVuZ2xpc2g8L2E+PC9zcGFuPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGJyLz4NCjxici8+DQo8cD5UbyB1c2UgdGhlIGFwcCBhbGwgcGVybWlzc2lvbnMgbXVzdCBiZSBhY2NlcHRlZC4gQWxzbyB3ZSB1c2UgdGhpcmQtcGFydHkgYXBwbGljYXRpb25zIChmcm9tIEdvb2dsZSkgY2FuIHNlZSB0aGVpciBwcml2YWN5IHBvbGljaWVzIGhlcmU6PGJyLz4NCjxhIGhyZWY9Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vcG9saWNpZXMvcHJpdmFjeS8iPkdvb2dsZSBQbGF5IFNlcnZpY2VzPC9hPjxici8+DQo8YSBocmVmPSJodHRwczovL3N1cHBvcnQuZ29vZ2xlLmNvbS9hZG1vYi9hbnN3ZXIvNjEyODU0Mz9obD1lbiI+QWRNb2I8L2E+PGJyLz4NCjxhIGhyZWY9Imh0dHBzOi8vZmlyZWJhc2UuZ29vZ2xlLmNvbS9wb2xpY2llcy9hbmFseXRpY3MiPkdvb2dsZSBBbmFseXRpY3MgZm9yIEZpcmViYXNlPC9hPjxici8+DQo8YSBocmVmPSJodHRwczovL2ZpcmViYXNlLmdvb2dsZS5jb20vc3VwcG9ydC9wcml2YWN5LyI+RmlyZWJhc2UgQ3Jhc2hseXRpY3M8L2E+PGJyLz48L3A+DQo8YnIvPg0KPGgxPlByb3RlY3Rpb24gb2YgcGVyc29uYWwgZGF0YSBhY2NvcmRpbmcgdG8gdGhlIExPUEQ8L2gxPg0KPGJyLz4NCjxwPjxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgVW5kZXIgY3VycmVudCByZWd1bGF0aW9ucyByZWdhcmRpbmcgdGhlIHByb3RlY3Rpb24gb2YgcGVyc29uYWwgZGF0YSwgcmVwb3J0cyB0aGF0IHRoZSBwZXJzb25hbCBkYXRhIGNvbGxlY3RlZCB0aHJvdWdoIHRoZSBmb3JtcyBvbjo8L3A+DQo8cD48dT48YSBocmVmPSJodHRwczovL2N1YmFuYXBwLmluZm8iPmN1YmFuYXBwLmluZm88L2E+PC91PiwgVGhleSBhcmUgaW5jbHVkZWQgaW4gYXV0b21hdGVkIGZpbGVzIHNwZWNpZmljIHVzZXIgc2VydmljZXMgPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hLjwvc3Ryb25nPjwvcD4NCjxici8+DQo8cD5UaGUgY29sbGVjdGlvbiBhbmQgcHJvY2Vzc2luZyBvZiBwZXJzb25hbCBkYXRhIGlzIGFpbWVkIGF0IG1haW50YWluaW5nIHRoZSBidXNpbmVzcyByZWxhdGlvbnNoaXAgYW5kIHRhc2sgcGVyZm9ybWFuY2UgaW5mb3JtYXRpb24sIHRyYWluaW5nLCBjb3Vuc2VsaW5nIGFuZCBvdGhlciBhY3Rpdml0aWVzIG9mPC9wPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LjwvcD4NCjxwPlRoZXNlIGRhdGEgd2lsbCBvbmx5IGJlIGRpc2Nsb3NlZCB0byB0aG9zZSBlbnRpdGllcyB3aGljaCBhcmUgbmVjZXNzYXJ5IGZvciB0aGUgc29sZSBwdXJwb3NlIG9mIGNvbXBseWluZyB3aXRoIHRoZSBhZm9yZW1lbnRpb25lZCBwdXJwb3NlLjwvcD4NCjxici8+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4gYWRvcHRzIHRoZSBuZWNlc3NhcnkgZGF0YSBjb25maWRlbnRpYWxpdHkgbWVhc3VyZXMgdG8gZW5zdXJlIHRoZSBzZWN1cml0eSwgaW50ZWdyaXR5IGFuZCBpbiBhY2NvcmRhbmNlIHdpdGggdGhlIHByb3Zpc2lvbnMgb2YgUmVndWxhdGlvbiAoRVUpIDIwMTYvNjc5IG9mIHRoZSBFdXJvcGVhbiBQYXJsaWFtZW50IGFuZCBvZiB0aGUgQ291bmNpbCBvZiAyNyBBcHJpbCAyMDE2IG9uIHRoZSBwcm90ZWN0aW9uIG9mIGluZGl2aWR1YWxzIHdpdGggcmVnYXJkIHRvIHRoZSBwcm9jZXNzaW5nIG9mIHBlcnNvbmFsIGRhdGEgYW5kIG9uIHRoZSBmcmVlIG1vdmVtZW50IHRoZXJlb2YuPC9wPg0KPGJyLz4NCjxwPlRoZSB1c2VyIG1heSBhdCBhbnkgdGltZSBleGVyY2lzZSB0aGVpciByaWdodHMgb2YgYWNjZXNzLCBvcHBvc2l0aW9uLCByZWN0aWZpY2F0aW9uIGFuZCBjYW5jZWxsYXRpb24gcmVjb2duaXplZCBpbiBSZWd1bGF0aW9uIChFVSkuIFRoZSBleGVyY2lzZSBvZiB0aGVzZSByaWdodHMgbWF5IGJlIHRoZSB1c2VyIHZpYSBlbWFpbCB0bzogPHN0cm9uZz5oZWxwQGN1YmFuYXBwLmluZm88L3N0cm9uZz4gb3IgYWRkcmVzczogPHU+QyAvIEFsdG8gQXJhZyZvYWN1dGU7biAzIFphcmFnb3phLCA1MDAwMiwgU3BhaW48L3U+PC9wPg0KPGJyLz4NCjxwPlRoZSB1c2VyIHN0YXRlcyB0aGF0IGFsbCBkYXRhIHByb3ZpZGVkIGJ5IGhpbSBhcmUgdHJ1ZSBhbmQgY29ycmVjdCwgYW5kIHVuZGVydGFrZXMgdG8ga2VlcCB0aGVtIHVwZGF0ZWQsIGNvbW11bmljYXRpbmcgY2hhbmdlcyA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4uPC9wPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGgxPlB1cnBvc2Ugb2YgdGhlIHByb2Nlc3Npbmcgb2YgcGVyc29uYWwgZGF0YTo8L2gxPg0KPGJyLz4NCjxwPjxzdHJvbmc+Rm9yIHdoYXQgcHVycG9zZSB3ZSB3aWxsIHRyZWF0IHlvdXIgcGVyc29uYWwgZGV0YWlscz88L3N0cm9uZz48L3A+DQo8YnIvPg0KPHA+SW4gPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LCB0cmVhdCB5b3VyIHBlcnNvbmFsIGRhdGEgY29sbGVjdGVkIHRocm91Z2ggdGhlIFdlYiBzaXRlOjwvcD4NCjxwPjx1PjxhIGhyZWY9Imh0dHBzOi8vY3ViYW5hcHAuaW5mbyI+Y3ViYW5hcHAuaW5mbzwvYT48L3U+LCBXaXRoIHRoZSBmb2xsb3dpbmcgcHVycG9zZXM6PC9wPg0KPGJyLz4NCjx1bD4NCjxsaT5JbiBjYXNlIG9mIHByb2N1cmVtZW50IG9mIGdvb2RzIGFuZCBzZXJ2aWNlcyBvZmZlcmVkIHRocm91Z2ggPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hIDwvc3Ryb25nPlRvIG1haW50YWluIHRoZSBjb250cmFjdHVhbCByZWxhdGlvbnNoaXAgYW5kIG1hbmFnZW1lbnQsIGFkbWluaXN0cmF0aW9uLCBpbmZvcm1hdGlvbiwgcHJvdmlzaW9uIGFuZCBpbXByb3ZlbWVudCBvZiB0aGUgc2VydmljZS48L2xpPg0KPGxpPlNlbmRpbmcgaW5mb3JtYXRpb24gcmVxdWVzdGVkIHRocm91Z2ggdGhlIGZvcm1zIHByb3ZpZGVkIGluPHN0cm9uZz4gQm9saXRhIEN1YmFuYS48L3N0cm9uZz48L2xpPg0KPGxpPlNlbmQgbmV3c2xldHRlcnMgKG5ld3NsZXR0ZXJzKSBhbmQgY29tbWVyY2lhbCBjb21tdW5pY2F0aW9ucyBvZiBwcm9tb3Rpb25zIGFuZCAvIG9yIGFkdmVydGlzaW5nIGZyb20gPHN0cm9uZz5Hb29nbGUgPC9zdHJvbmc+YW5kIHRoZSBzZWN0b3IuPC9saT4NCjwvdWw+DQo8YnIvPg0KPGJyLz4NCjxwPldlIHJlbWluZCB5b3UgdGhhdCB5b3UgY2FuIG9wcG9zZSB0aGUgc2VuZGluZyBvZiBjb21tZXJjaWFsIGNvbW11bmljYXRpb25zIGJ5IGFueSBtZWFucyBhbmQgYXQgYW55IHRpbWUgYnkgc2VuZGluZyBhbiBlbWFpbCB0byB0aGUgYWJvdmUgYWRkcmVzcy48L3A+DQo8YnIvPg0KPHA+RmllbGRzIHN1Y2ggcmVjb3JkcyBhcmUgbWFuZGF0b3J5LCBtYWtpbmcgaXQgaW1wb3NzaWJsZSB0byBjYXJyeSBvdXQgdGhlIGV4cHJlc3NlZCBwdXJwb3NlIGlmIHRoaXMgZGF0YSBpcyBub3QgcHJvdmlkZWQuPC9wPg0KPGJyLz4NCjxici8+DQo8aDE+SG93IGxvbmcgdGhlIGRhdGEgZ2F0aGVyZWQgYXJlIGNvbnNlcnZlZD88L2gxPg0KPGJyLz4NCjxwPlBlcnNvbmFsIGRhdGEgcHJvdmlkZWQgd2lsbCBiZSByZXRhaW5lZCB3aGlsZSB0aGUgY29tbWVyY2lhbCBvciBub24tcmVsYXRpb25zaGlwIGlzIG1haW50YWluZWQgcmVxdWVzdGluZyByZW1vdmFsIGFuZCBkdXJpbmcgdGhlIHBlcmlvZCBmb3Igd2hpY2ggbGVnYWwgbGlhYmlsaXRpZXMgbWF5IGFyaXNlIGZvciB0aGUgc2VydmljZXMgcmVuZGVyZWQuPC9wPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGgxPkxlZ2l0aW1hdGlvbjo8L2gxPg0KPGJyLz4NCjxwPlRoZSBwcm9jZXNzaW5nIG9mIHlvdXIgZGF0YSBpcyBkb25lIHdpdGggdGhlIGZvbGxvd2luZyBsZWdhbCBiYXNlcyBsZWdpdGltaXppbmcgdGhlIHNhbWU6PC9wPg0KPGJyLz4NCjx1bD4NCjxsaT5UaGUgcmVxdWVzdCBmb3IgaW5mb3JtYXRpb24gYW5kIC8gb3IgaGlyaW5nIHNlcnZpY2VzIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgd2hvc2UgdGVybXMgYW5kIGNvbmRpdGlvbnMgYXJlIHB1dCBhdCB5b3VyIGRpc3Bvc2FsIGluIGFueSBjYXNlLCBwcmlvciB0byBldmVudHVhbCByZWNydWl0bWVudCBmb3JtLjwvbGk+DQo8bGk+VGhlLCBzcGVjaWZpYywgZnJlZSBpbmZvcm1lZCBjb25zZW50IGFuZCB1bmFtYmlndW91cywgd2hpbGUgd2UgaW5mb3JtIHBsYWNpbmcgYmVsb3cgdGhlIHByZXNlbnQgcHJpdmFjeSBwb2xpY3ksIHRoYXQgYWZ0ZXIgcmVhZGluZyBpdCwgc2hvdWxkIGJlIHN1YmplY3QsIGNhbiBhY2NlcHQgYnkgYSBzdGF0ZW1lbnQgb3IgYSBjbGVhciBhZmZpcm1hdGl2ZSBhY3Rpb24sIGFzIHRoZSBtYXJraW5nIG9mIGEgYm94IHByb3ZpZGVkIGVmZmVjdC48L2xpPg0KPC91bD4NCjxici8+DQo8cD5TaG91bGQgd2Ugbm90IGZhY2lsaXRlcyB5b3VyIGRhdGEgb3IgZG8gaXQgaW5jb3JyZWN0bHkgb3IgaW5jb21wbGV0ZSwgd2UgY2FuIG5vdCBmdWxmaWxsIHlvdXIgcmVxdWVzdCwgcmVzdWx0aW5nIGZyb20gdGhlIGltcG9zc2libGUgdG8gcHJvdmlkZSBhbGwgdGhlIGluZm9ybWF0aW9uIHJlcXVlc3RlZCBvciB0byBjb25kdWN0IHRoZSBwcm9jdXJlbWVudCBvZiBzZXJ2aWNlcy48L3A+DQo8YnIvPg0KPGJyLz4NCjxici8+DQo8aDE+UmVjaXBpZW50czo8L2gxPg0KPGJyLz4NCjxwPlRoZSBkYXRhIHdpbGwgbm90IGJlIGNvbW11bmljYXRlZCB0byBhbnkgdGhpcmQgcGFydHkgb3V0c2lkZSA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4sIGV4Y2VwdCBsZWdhbCBvYmxpZ2F0aW9uLjwvcD4NCjxici8+DQo8aDE+RGF0YSBjb2xsZWN0ZWQgYnkgc2VydmljZSB1c2VyczwvaDE+DQo8YnIvPg0KPHA+SW4gY2FzZXMgd2hlcmUgdGhlIHVzZXIgZmlsZXMgaW5jbHVkZSBwZXJzb25hbCBkYXRhIG9uIHNlcnZlcnMgc2hhcmVkIGhvc3RpbmcsIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiBpcyBub3QgcmVzcG9uc2libGUgZm9yIHRoZSBmYWlsdXJlIG9mIHRoZSB1c2VyIG9mIHRoZSBSR1BELjwvcD4NCjxici8+DQo8YnIvPg0KPGgxPkRhdGEgcmV0ZW50aW9uIGFjY29yZGluZyB0byBMU1NJPC9oMT4NCjxici8+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmEgPC9zdHJvbmc+aW5mb3JtcyB0aGF0IGFzIGEgaG9zdGluZyBzZXJ2aWNlIHByb3ZpZGVyIGFuZCBkYXRhIHVuZGVyIHRoZSBwcm92aXNpb25zIG9mIExhdyAzNC8yMDAyIG9mIEp1bHkgMTEsIFNlcnZpY2VzIEluZm9ybWF0aW9uIFNvY2lldHkgYW5kIEVsZWN0cm9uaWMgQ29tbWVyY2UgKExTU0kpLCByZXRhaW5lZCBmb3IgYSBtYXhpbXVtIHBlcmlvZCBvZiAxMiBtb250aHMgdGhlIG5lY2Vzc2FyeSBpbmZvcm1hdGlvbiB0byBpZGVudGlmeSB0aGUgc291cmNlIG9mIHRoZSBzdG9yZWQgZGF0YSBhbmQgdGhlIHRpbWUgd2hlbiB0aGUgcHJvdmlzaW9uIG9mIHRoZSBzZXJ2aWNlIHN0YXJ0ZWQuIFJldGVudGlvbiBvZiB0aGlzIGRhdGEgZG9lcyBub3QgYWZmZWN0IHRoZSBzZWNyZWN5IG9mIGNvbW11bmljYXRpb25zIGFuZCBtYXkgb25seSBiZSB1c2VkIHdpdGhpbiB0aGUgZnJhbWV3b3JrIG9mIGEgY3JpbWluYWwgb3IgdG8gc2FmZWd1YXJkIHB1YmxpYyBzZWN1cml0eSBpbnZlc3RpZ2F0aW9uIGJlaW5nIG1hZGUgYXZhaWxhYmxlIHRvIHRoZSBqdWRnZXMgYW5kIC8gb3IgY291cnRzIG9yIHRoZSBNaW5pc3RyeSBzbyByZXF1aXJlcyB0aGVtLjwvcD4NCjxici8+DQo8cD5EYXRhIGNvbW11bmljYXRpb24gdG8gdGhlIEZvcmNlcyBvZiB0aGUgU3RhdGUgc2hhbGwgaW4gYWNjb3JkYW5jZSB3aXRoIHRoZSBwcm92aXNpb25zIG9mIHRoZSByZWd1bGF0aW9ucyBvbiBwcm90ZWN0aW9uIG9mIHBlcnNvbmFsIGRhdGEuPC9wPg0KPGJyLz4NCjxoMT5JbnRlbGxlY3R1YWwgcHJvcGVydHkgcmlnaHRzIGZyb20gQm9saXRhIEN1YmFuYTwvaDE+DQo8YnIvPg0KPHA+PHN0cm9uZz5DVUJBTkFQUDwvc3Ryb25nPiBJdCBvd25zIGFsbCBjb3B5cmlnaHQsIGludGVsbGVjdHVhbCBwcm9wZXJ0eSwgaW5kdXN0cmlhbCwgImtub3cgaG93IiBhbmQgZmV3IG90aGVycyBrZWVwIHJpZ2h0cyByZWdhcmRpbmcgdGhlIGNvbnRlbnRzIG9mIHRoZSBhcHAgQm9saXRhIEN1YmFuIGFuZCB0aGUgc2VydmljZXMgb2ZmZXJlZCB0aGVyZWluLCBhcyB3ZWxsIGFzIHRoZSBzb2Z0d2FyZSBuZWNlc3NhcnkgZm9yIGl0cyBpbXBsZW1lbnRhdGlvbiBhbmQgcmVsYXRlZCBpbmZvcm1hdGlvbi48L3A+DQo8YnIvPg0KPGJyLz4NCjxoMT5Tb2Z0d2FyZSBpbnRlbGxlY3R1YWwgcHJvcGVydHk8L2gxPg0KPGJyLz4NCjxwPlRoZSB1c2VyIG11c3QgcmVzcGVjdCB0aGlyZC1wYXJ0eSBwcm9ncmFtcyBtYWRlIGF2YWlsYWJsZSBieSBCb2xpdGEgQ3ViYW5hLCBhbHRob3VnaCBmcmVlIGFuZCAvIG9yIHRoZSBwdWJsaWMuPC9wPg0KPGJyLz4NCjxwPjxzdHJvbmc+Q1VCQU5BUFA8L3N0cm9uZz4gSXQgaGFzIGV4cGxvaXRhdGlvbiByaWdodHMgYW5kIGludGVsbGVjdHVhbCBwcm9wZXJ0eSByZXF1aXJlZCBvZiB0aGUgc29mdHdhcmUuPC9wPg0KPGJyLz4NCjxwPlRoZSB1c2VyIGRvZXMgbm90IGFjcXVpcmUgYW55IHJpZ2h0IG9yIGxpY2Vuc2UgYnkgdGhlIGNvbnRyYWN0ZWQgc2VydmljZSBvbiB0aGUgc29mdHdhcmUgcmVxdWlyZWQgZm9yIHRoZSBwcm92aXNpb24gb2YgdGhlIHNlcnZpY2UsIG5vciBvbiB0aGUgdGVjaG5pY2FsIGluZm9ybWF0aW9uIHRyYWNraW5nIHNlcnZpY2UsIHdpdGggdGhlIGV4Y2VwdGlvbiBvZiB0aGUgcmlnaHRzIGFuZCBsaWNlbnNlcyBuZWNlc3NhcnkgZm9yIHRoZSBmdWxmaWxsbWVudCBvZiB0aGUgY29udHJhY3RlZCBzZXJ2aWNlcyBhbmQgb25seSBmb3IgdGhlIGR1cmF0aW9uIHRoZXJlb2YuPC9wPg0KPGJyLz4NCjxwPkZvciBhbnkgYWN0aW9uIHRoYXQgZXhjZWVkcyB0aGUgcGVyZm9ybWFuY2Ugb2YgdGhlIGNvbnRyYWN0LCB0aGUgdXNlciB3aWxsIG5lZWQgd3JpdHRlbiBwZXJtaXNzaW9uIGZyb208L3A+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmEgPC9zdHJvbmc+SXMgZm9yYmlkZGVuIHRvIHRoZSB1c2VyIHRvIGFjY2VzcywgbW9kaWZ5LCB2aWV3IHRoZSBjb25maWd1cmF0aW9uLCBzdHJ1Y3R1cmUgYW5kIGZpbGVzIHNlcnZlcnMgb3duZWQgYnkgPHN0cm9uZz5DVUJBTkFQUDwvc3Ryb25nPiwgYXNzdW1pbmcgdGhlIGNpdmlsIGFuZCBjcmltaW5hbCBsaWFiaWxpdHkgb2YgYW55IGluY2lkZW50IHRoYXQgbWlnaHQgb2NjdXIgb24gc2VydmVycyBhbmQgc2VjdXJpdHkgc3lzdGVtcyBhcyBhIGRpcmVjdCByZXN1bHQgb2YgYSBwZXJmb3JtYW5jZSBuZWdsaWdlbnQgb3IgbWFsaWNpb3VzIG9uIGhlciBwYXJ0LjwvcD4NCjxici8+DQo8YnIvPg0KPGgxPkludGVsbGVjdHVhbCBwcm9wZXJ0eSBjb250ZW50IGhvc3RlZDwvaDE+DQo8YnIvPg0KPHA+SXQgaXMgZm9yYmlkZGVuIHRvIHVzZSBjb250cmFyeSB0byB0aGUgbGVnaXNsYXRpb24gb24gaW50ZWxsZWN0dWFsIHByb3BlcnR5IG9mIHRoZSBzZXJ2aWNlcyBwcm92aWRlZCBieTwvcD4NCjxwPjxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiBhbmQgaW4gcGFydGljdWxhciBvZjo8L3A+DQo8YnIvPg0KPHVsPg0KPGxpPlRoZSB1c2UgdGhhdCBpcyBjb250cmFyeSB0byBTcGFuaXNoIGxhdyBvciBpbmZyaW5nZXMgdGhlIHJpZ2h0cyBvZiBvdGhlcnMuPC9saT4NCjxsaT5QdWJsaWNhdGlvbiBvciB0cmFuc21pc3Npb24gb2YgYW55IGNvbnRlbnQgdGhhdCwgYWNjb3JkaW5nIHRvIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgaXMgdmlvbGVudCwgb2JzY2VuZSwgYWJ1c2l2ZSwgaWxsZWdhbCwgcmFjaXN0LCB4ZW5vcGhvYmljIG9yIGRlZmFtYXRvcnkuPC9saT4NCjxsaT5UaGUgY3JhY2tzLCBzb2Z0d2FyZSBzZXJpYWwgbnVtYmVycyBvciBhbnkgb3RoZXIgY29udGVudCB0aGF0IHZpb2xhdGVzIGludGVsbGVjdHVhbCBwcm9wZXJ0eSByaWdodHMgb2YgdGhpcmQgcGFydGllcy48L2xpPg0KPGxpPkNvbGxlY3Rpb24gYW5kIC8gb3IgdXNlIG9mIHBlcnNvbmFsIGRhdGEgb2Ygb3RoZXIgdXNlcnMgd2l0aG91dCB0aGVpciBleHByZXNzIGNvbnNlbnQgb3IgY29udHJhdmVuaW5nIHRoZSBwcm92aXNpb25zIG9mIFJlZ3VsYXRpb24gKEVVKSAyMDE2LzY3OSBvZiB0aGUgRXVyb3BlYW4gUGFybGlhbWVudCBhbmQgb2YgdGhlIENvdW5jaWwgb2YgMjcgQXByaWwgMjAxNiBvbiB0aGUgcHJvdGVjdGlvbiBvZiBpbmRpdmlkdWFscyB3aXRoIHJlZ2FyZCB0byB0aGUgcHJvY2Vzc2luZyBvZiBwZXJzb25hbCBkYXRhIGFuZCBvbiB0aGUgZnJlZSBtb3ZlbWVudCB0aGVyZW9mLjwvbGk+DQo8bGk+VGhlIHVzZSBvZiB0aGUgbWFpbCBzZXJ2ZXIgZG9tYWluIGFuZCBlbWFpbCBhZGRyZXNzZXMgZm9yIHNlbmRpbmcgdW5zb2xpY2l0ZWQgYnVsay48L2xpPg0KPC91bD4NCjxici8+DQo8cD5UaGUgdXNlciBoYXMgZnVsbCByZXNwb25zaWJpbGl0eSBmb3IgdGhlIGNvbnRlbnQgb2YgaXRzIHdlYnNpdGUsIHRoZSBpbmZvcm1hdGlvbiB0cmFuc21pdHRlZCBhbmQgc3RvcmVkLCBoeXBlcnRleHQgbGlua3MsIHRoaXJkLXBhcnR5IGNsYWltcyBhbmQgbGVnYWwgYWN0aW9ucyByZWZlcnJpbmcgdG8gaW50ZWxsZWN0dWFsIHByb3BlcnR5IHJpZ2h0cyBvZiBvdGhlcnMgYW5kIHRoZSBwcm90ZWN0aW9uIG9mIG1pbm9ycy48L3A+DQo8YnIvPg0KPHA+VGhlIHVzZXIgaXMgcmVzcG9uc2libGUgcmVnYXJkaW5nIGxhd3MgYW5kIHJlZ3VsYXRpb25zIGFuZCBydWxlcyB0aGF0IGhhdmUgdG8gZG8gd2l0aCBydW5uaW5nIHRoZSBvbmxpbmUgc2VydmljZSwgZS1jb21tZXJjZSwgY29weXJpZ2h0cywgbWFpbnRlbmFuY2Ugb2YgcHVibGljIG9yZGVyLCBhbmQgdW5pdmVyc2FsIHByaW5jaXBsZXMgb2YgSW50ZXJuZXQgdXNlLjwvcD4NCjxici8+DQo8cD5UaGUgdXNlciBtdXN0IGNvbXBlbnNhdGUgdG8gPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IGJ5IHRoZSBleHBlbnNlcyB0aGF0IGdlbmVyYXRlZCB0aGUgaW1wdXRhdGlvbiBmcm9tPC9wPg0KPHA+PHN0cm9uZz5DVUJBTkFQUDwvc3Ryb25nPiBzb21lIGNhdXNlIHdoaWNoIHJlc3BvbnNpYmlsaXR5IHdhcyBhdHRyaWJ1dGFibGUgdG8gdGhlIHVzZXIsIGluY2x1ZGluZyBmZWVzIGFuZCBsZWdhbCBleHBlbnNlcywgZXZlbiBpbiB0aGUgY2FzZSBvZiBhIG5vbi1maW5hbCBqdWRpY2lhbCBkZWNpc2lvbi48L3A+DQo8YnIvPg0KPGJyLz4NCjxoMT5Qcm90ZWN0aW9uIG9mIGluZm9ybWF0aW9uIHN0b3JlZDwvaDE+DQo8YnIvPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IGJhY2tzIHVwIHRoZSBjb250ZW50IGhvc3RlZCBvbiB0aGVpciBzZXJ2ZXJzLCBob3dldmVyIG5vdCByZXNwb25zaWJsZSBmb3IgbG9zcyBvciBhY2NpZGVudGFsIGRlbGV0aW9uIG9mIGRhdGEgYnkgdXNlcnMuIFNpbWlsYXJseSwgaXQgZG9lcyBub3QgZ3VhcmFudGVlIGZ1bGwgcmVwbGFjZW1lbnQgZGF0YSBkZWxldGVkIGJ5IHVzZXJzLCBzaW5jZSBzdWNoIGRhdGEgY291bGQgaGF2ZSBiZWVuIGRlbGV0ZWQgYW5kIC8gb3IgbW9kaWZpZWQgZHVyaW5nIHRoZSBwZXJpb2Qgb2YgdGltZSBzaW5jZSB0aGUgbGFzdCBiYWNrdXAuPC9wPg0KPGJyLz4NCjxwPlRoZSBzZXJ2aWNlcyBvZmZlcmVkIGV4Y2VwdCBzcGVjaWZpYyBiYWNrdXAgc2VydmljZXMgZG8gbm90IGluY2x1ZGUgdGhlIHJlcGxhY2VtZW50IG9mIHRoZSBjb250ZW50cyBzdG9yZWQgaW4gdGhlIGJhY2t1cHMgbWFkZSBieSBCb2xpdGEgQ3ViYW5hLCB3aGVuIHRoaXMgbG9zcyBpcyBhdHRyaWJ1dGFibGUgdG8gdGhlIHVzZXI7IGluIHRoaXMgY2FzZSwgYSBmZWUgd2lsbCBiZSBkZXRlcm1pbmVkIGFjY29yZGluZyB0byB0aGUgY29tcGxleGl0eSBhbmQgdm9sdW1lIG9mIHRoZSByZWNvdmVyeSwgcHJvdmlkZWQgcHJpb3IgYWNjZXB0YW5jZSBvZiB0aGUgdXNlci48L3A+DQo8YnIvPg0KPHA+UmVwbGVuaXNoaW5nIGRlbGV0ZWQgZGF0YSBpdCBpcyBvbmx5IGluY2x1ZGVkIGluIHRoZSBwcmljZSBvZiB0aGUgc2VydmljZSB3aGVuIHRoZSBjb250ZW50IGxvc3MgaXMgZHVlIHRvIGNhdXNlcyBhdHRyaWJ1dGFibGUgdG8gPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LjwvcD4NCjxici8+DQo8YnIvPg0KPGgxPkNvbW1lcmNpYWwgY29tbXVuaWNhdGlvbnM8L2gxPg0KPGJyLz4NCjxwPlB1cnN1YW50IHRvIExTU0kuIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiBub3Qgc2VuZCBhZHZlcnRpc2luZyBvciBwcm9tb3Rpb25hbCBlLW1haWwgb3Igb3RoZXIgbWVhbnMgb2YgZWxlY3Ryb25pYyBjb21tdW5pY2F0aW9uIHRoYXQgaGF2ZSBub3QgYmVlbiBwcmV2aW91c2x5IHJlcXVlc3RlZCBvciBleHByZXNzbHkgYXV0aG9yaXplZCBieSB0aGUgcmVjaXBpZW50cyB0aGVyZW9mLjwvcD4NCjxici8+DQo8cD5Gb3IgdXNlcnMgd2l0aCB3aG9tIHRoZXJlIGlzIGEgcHJpb3IgY29udHJhY3R1YWwgcmVsYXRpb25zaGlwLCA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4gaWYgYXV0aG9yaXplZCB0byB0aGUgU2hpcHBpbmcgZnJvbSBjb21tdW5pY2F0aW9ucyBjb21tZXJjaWFsIHJlZmVyZW50cyB0byBwcm9kdWN0cyBvciBzZXJ2aWNlcyBmcm9tPC9wPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IHdoaWNoIGFyZSBzaW1pbGFyIHRvIHRob3NlIHRoYXQgd2VyZSBpbml0aWFsbHkgY29udHJhY3RlZCB3aXRoIHRoZSBjdXN0b21lci48L3A+DQo8YnIvPg0KPHA+SW4gYW55IGNhc2UsIHRoZSB1c2VyLCBhZnRlciBwcm9vZiBvZiBpZGVudGl0eSwgbWF5IHJlcXVlc3Qgbm90IHRvIGNvbnZleSB5b3UgbW9yZSBidXNpbmVzcyBpbmZvcm1hdGlvbiB0aHJvdWdoIGNoYW5uZWxzIEN1c3RvbWVyIENhcmUuPC9wPg0KPC9ib2R5Pg0KPC9odG1sPg0K";
            String privSpa = "PCFET0NUWVBFIGh0bWw+DQo8aHRtbCBsYW5nPSJlcyI+DQo8aGVhZD4NCgk8bGluayByZWw9Imljb24iIGhyZWY9ImRhdGE6aW1hZ2UveC1pY29uO2Jhc2U2NCxBQUFCQUFFQVNFZ0FBQUVBQ0FESUd3QUFGZ0FBQUNnQUFBQklBQUFBa0FBQUFBRUFDQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBS29BQkFDcUFCY0lwQUFSQ2FVQUNncWxBQlVTbndBZkVwNEFHeE9mQUNBVG53QWlGcFlBSEJXZ0FCNFptUUFYRjZJQUl4bVpBQ2tabUFBa0c1b0FLaHliQUNraGxnQXRJWlVBTXlHVkFEQWtqd0E5S25zQUx5T1hBRFlwZ2dBN0tZSUFOQ2lLQURjbWtBQXpKNUlBUnpObUFEd3VnQUJCTG44QVBDdU1BRUFyakFCRU1Ya0FPaXVWQUVBdmlBQlFPV1VBUlRWM0FGUTlXZ0JFTW9NQVNUZHlBRlk5WUFCTU4za0FURGwwQUVFeGt3QmJRbGNBWmtwQUFHRkhUUUJHTlk4QVVqNXlBRzFNUGdCMVV5WUFhRXBIQUU4OWVRQlpRbTRBVHp5R0FIcFpJQUNCV3hvQWpXTUFBSHBZS3dDRFhSMEFoRjRlQUl4aEZ3Q0lZeFFBa1djSUFHRk1jZ0NUYUF3QWVsWlFBSjF0QUFDWGJnQUFtRzhBQUd4VVpBQk9SS0FBbm00REFKcHdBQUJlU28wQW5ISUJBSmh0RndDZGN3UUFublFJQUo5MUN3QnhWWUVBb0hZT0FJZHBRd0NoZHhFQWMxdDdBS0o0RXdDbmZnQUFxSDhBQUtONUZnQmxWWnNBcVlBQ0FLUjZHQUNoZlJRQXFvRUdBSGhnZ0FDbWZCd0FxNElLQUtOL0dBQ3NndzBBcElBYUFLaCtJQUNwZnlFQXFvQWpBS2FDSGdDcmdTVUFwNE1nQUtpRUlnQ3VneWdBcVlVakFLcUdKUUNyaHljQXJJZ29BS2FITUFDdGlTb0Fyb29yQUpoK1hBQ3ZpeTBBc1kwd0FJOTVmQUNzalRnQXRJOHlBSytRUEFDNGt6Z0FzWkkrQUxLVFFBQ3psRUVBdEpWQ0FMV1dRd0MzbDBRQXVKaEdBTHFhU0FDOG5Fb0FwNUowQUxhY1VBQy9vRTRBdVo5VEFMcWdWUUNjanAwQXZLSlhBTHlnWHdDK3BGZ0F3S1phQU1Da1l3RENwbVVBdForQkFNT3BYZ0REcDJZQXhLbG9BTVdxYVFESHEyb0F5YTFzQU1PdGNBREVybkVBeGE5eUFNYXVld0RBc0hjQXlMSjFBTW16ZGdETHMzOEF6TFY1QU0yMmVnRE50WUVBenJkN0FNNjJnZ0RRdUlVQXlycUJBTks2aHdETXVvb0F3TFNqQU0yN2l3Qzhzck1BejcyTkFORytqZ0RGdWFnQTA4Q1JBTlRCa2dET3daVUExY09UQU5MR21RRFV4YUVBMU1pYkFOYkpuQURYeXAwQTJNbWxBTm5McGdEVXk2b0Eyc3luQU52TnFBRGN6cWtBM3MrckFOblFyZ0RnMGEwQTI5S3dBT0xUcndEYzA3SUEzZFN6QU43VnRBRFkwNzBBMDg3UkFOalN3d0RmMXJVQTROYThBT0xadHdEazJjQUE1ZHJCQU4vWXlnRG0yOElBNTl6REFPamV4QURwMzhVQTQ5L0lBT1hlendEazRNa0E3Ti9PQU9iaHlnRG40c3NBNk9QTUFPbmt6UURxNDlRQTZ1WE9BT3ZtendEcjVkWUE3T2ZRQU96bTF3RHU1OWdBNytqWkFQRHAyZ0R5Njl3QTgremRBTzNzNEFEdTdlRUE5TzdlQU8vdTRnRHc4T01BOHUvcUFQSHg1QUR6OE9zQTgvTGxBUFgwNXdEMTh1NEE5dlB2QVBiMTZRRDM5ZkFBK1BieEFQcjM4Z0Q3K1BNQS9QbjBBUDM2OVFELy9QZ0ErZno2QVByOSt3RC8vdmtBKy8vOEFQei8vUUFBQUFBQS8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUYvMGIvU1VsR1NVbEdTZjlHLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUnY5SlNrWkpTVVJOVFVaSlJrWkdUVWxKU1VsR1JVYi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlHUmsxSlNrWkdxc1BLeWNtOXM0UkpTWE9vZWt4TlNVbEpTVVgvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUpTVVJKUkZsR1RVbEp6LzM3K2YzKytWWkdSazNDL2ZUSmxXSkZSa1pFU1UzLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wbEdSVXlvNTk2blRVWkttUDM5K1gyZnkwWkdSa2x6L2YzOS9mdldsRXhFUmtaSi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzBaR1NVMUphZWV2L1ozOXkwWkdhZjM5OUdoR1NVbEpTVVppL2Y3OS9mNzBtTXFDU1VaS1J2Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUmtaSlRVMVM1dXpvNlBuKy9kRlBTZlQ5L2Zuc2xVcEdSa2FmL2ZuTCtmblNpT1QwVFd0SlJrWkovLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlKUmtsR2NwM3kvZjM3MC8zNS9idG1SdlQ5L3ZMVzhrWk5hYzM5L2ZtVmNaVzY4L25vYS8zZWFVbEdSa1gvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vMFpHVFlEbS9mdjUvdkM2czdDdmhrMUdlUDc2bFUrWTdVbE5vLzc1L2Yzc1RVbE5nL21OUmxSNllrWkdTVVpKLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Sa2xFc3YzNStmbml3bkpKU1VaR1lHbEp3dDlYVFhyNS9aU0MwTUx4L2YzOWg1QmRUZU5OUmtsSlNVbEpUVWxKU2YvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUpUVnpVK2ZMOStwaEdSa1pHUmtaLytmNTc0bEJOVnZUNStmM1RZWXo5L2YzOXQyWkdSRmhLUmtsR1JrWkdUbUZOU2tuLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JrMUdjUEw1NnMzOStsSnBzbEtEU1VsSmRsU3EvYWhNUmxTNi9idFFxUDM5L2YzKzRFV0RWMFZLUkVaSlJrMUpzZkZ2VFVaRi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1NrbDE5UDM5K3V2KzlNRDVtVWJNcEVaR1JrYTUrZjdLU1VsUytkN3crZjM5L3YzKys1ajNzVVpKVFYxSlJrbDUvZUM1Y2taR1NmLy8vLy8vLy8vLy8vLy8vLy8vLy85RVRYajAvdm45YVZUMy9mM01TVW5DL3VUb2ZFbGs3TjF2WFVaR3pQTzE3UDc5K2ZuU2pvUGcxa2xHcG5WR1JrWGQrYkpyOTJsSlJrWC8vLy8vLy8vLy8vLy8vLy8vLzBaTmIvVDkvWEM5L25YZy9mNk1SbEwwdTRscFJrWkdZT0ROdVYxSlhyUmg2UDc1K2FaR1JrM1M5R1JHUkVsNGtFbmUvZFZVK2V4WVJrbi8vLy8vLy8vLy8vLy8vLy8vU1VaVTYvMzkrWHhLZFZUbi9mdkRpTldQUmtsR1JrbEdSazJucGtsR1JuajAvY1AwekVsTm1JTDcrZm50NjBwejFIajUrZjNWN2MvZ1RFWkcvLy8vLy8vLy8vLy8vLy8vUmthamgvUDkvZXBKUlVuMytmbjUwb0pKVFUxS1NVbEdTWlRhNnFKR1JrbDcrZkQ1dFVaTlJsSzU2dFg1L21sR3kvbjkvZjNLK2JYN2dFbEcvLy8vLy8vLy8vLy8vLzlKUmxKcFJvTDkvZjNzbUtycG5XOUdUVTJJeVlSR1RFWkpSckw1L3NWSlNVWkdhZWo5L2ZUZHdHQkVTVWxjbDA5SmFmVDUrZGFBL2ZMOXVrWkpSZi8vLy8vLy8vLy8vLzlKU2tsSlJrYmQvZjM5K2FkT1RVVkZTVmI1L2YyN1RVWkpSbFRnL2RGTlJrWkpTVlMwK3ZuKy9aMUdYVVpHUmtwR1JubnovZEZ4NFBuOS9ZeE5Sa24vLy8vLy8vLy8vMDFOUmsxR1JrUzkvZjMrdFVsR09UOUZSWTcwZ0lMMHlVbEdSa1pXNlAySlJrbEdSa1pOV0l6RS9aMUo4bkJHU1UxTlNVWk9qUFJYVXZUNStmbDVTVVpHLy8vLy8vLy9Sa2x6eTAxR1Jrbncvdjc5VzBaR01RRWxQNGo2cGtteS9ySktSa2xKWE56NWcwbEdSa1pHVjlhRCtYMUo5S2RHU1VhRGcwWkdVdlR3N2YzOS9mM3lWa2IvLy8vLy8vLy9UVWJEL1lKSlNWUzEzN0g1UmtsR0hRQUFCQ1JGUkVwR2IvQnBTVVpHUmthY3AwWkdTVWxKUnJqNXpFUkd1YzlFVFVaR1JrWkpWSEQwL2YzOXVjem90MFpHLy8vLy8vLy9SbUQrL2ZtZFVrMTFaa1pZUmtSTUhRUUFBQUFMTGtaR1NaS3hSa1pKVFVaSlNVWkpTVVpHUmsvaW5rWkdWS1pHYVZCSlJrbEpzbWxoeWZuKy9zSmQ2RnhKU2YvLy8vOUdTWWY1L2Y2NTZyVkdSVWxHU1VWRkdRQUFCQUFFQUIwL1NrM3J2WFZHWG9OR1JrbEtSa2xKU1VsVWpFbEpTVWxOeWxsTlNYeFdSbFpOUm15WG8zRkpVbEpHUnYvLy8vOUdTVjFjWXVDRHd2NVFTV2xTU1VWTUd3QUFBQUFBQUFBRkxrYUUrWHBHUmtaSlNVbEdUVnhHUmtaR1JrWkdSa1pHeWthQ2duMWNTa1pHUmtsR1JrWkpSVTFHU2YvLy8vOU5UL0Q1Vkx6MC9mbEpSdkw2VkVSSkVnQUVBQUFFQUFBRUFDVkZWMFJKU1VsR1JrWldiWDFXVmxaV1ZsWldWbFpXWTFSR1JrMU5UVWxKU1VaTlNVbEdSRVZHUmtiLy8wWkdsLzM5N1B2OS9mM3M2djM5K2RxY0VRQUFBQUFBQUFRQUFBQW1tRWF6NC9UMC9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzM5UFR5N096cDM5WEt2YVpwU2YvLy8wbE4wLzM5L2YzOS9mMzkvZjM5L2ZueUVRQUVBQUFFQUFBQUFBUUFDd3RUMWY3OS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjdDUmtuLy8wVkc5LzM5L2YzOS9mMzkvZjM5L2YzNkVRQUFBQUFBQUFBQUJBQUFBQVFGTVlYcy9mbjkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMzVEVuL1JVUnUvZjM5L2YzOS9mMzkvZjM5L2YzNUVRRUVBQUFFQUFBQUFBQUFBQUFBQkFBbGtmTDkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMrZUViL1JrU00vZjM5L2YzOS9mMzkvZjM5L2YzOUR3QUFBQUFBQUFBRUFBQUVBQVFBQUFBQUFTcXUrUDM1L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5blVsR1NVV2QvZjM1L2YzOS9mMzkrZjM5L2YzOUN3QUFBQUFFQUFBQUFRRUFBQUFBQkFBQUFBQUVRYnI4K2ZuOS92NzkvZjM5L2YzNStmMzkvZjM5L2YzOS9mMzkvZm45dFVaSi8wYXYvZm41L2YzOS9mMzkvZjM1K2ZuNUN3UUFCQUVCQUFFQkFRQUFBQVFBQUFBQUJBQUFBQVZIcXVEditmcjUvZjM5L3Y3NS9mNzUvZjM5L2Y3OS9mMzUvdm43dWtsSlNrMjUrYmVRek9xSmFsUk1Sa1JGUmtaR0N3QUFBQUFBQVJVNEZBRVVPUWtBQkFBQUFBQUVBQUFFR3poRlRsZGRVRlJVWklQMDhQbjY2Zm4rNUtQOS92blZxdUQ1Z2taRlJrM0MrZXkwNElkSlNVbEpTVVJNVEVSRUN3QUFCQUFBQVJqTW0zZkdyd3NBQUFBQUJBQUFBQUFBQUFBVk9FVkpTVWxKU1Vtb1ZPaUkrZjN6NkUzai9idEdpWFp4aGtsSlNVYkt5Ni9nL1hGR1JrWkdSa1pHUkVWTUN3RUFBQUFFQUIvcy9mMzlqQUFBQkFBQUFBQUVBQUFFQUFBQUFSYzRSVVpHUmtaR1JsMlUvY3FacUlpTSthZEp0VmlZeFVaR1NVekNnMFpOWFYxSlNVbEpTVWxKU1VWTURBQUFBQUFBQUZYOS9mMzlqQWNBQUFBQUFBQUFBQUFBQUFBRUFBQUNSRWxKUmtsSlprbTAvZWlIdmZScDh2VFYzZkhlVjBsRVJrM0s5UERMU1VsR1JrWkdSa1pHUmtSSkNnQUFCQUFFSE1uNS9mMzkrODVhQkFBQUJBQUVBQUFFQUFBQUFRQTFSa1pHU1VaR2cwVzcvZlRUL2YyajFQMzk0SFZHUmtaR1JrbTkrZjNSVFVhQ2wwWkdSa1pHUmtaTUJ3QUVBQUF0VEp6OSsvM2N2OVBJQUFBQUFBQUFBQUFCQUFFQkFpOUpSa1pHUmtaR1JrYTUrWWhHZzgxeWFiTDlXVTFObUVsSlNVYXl1ZjM5bmtueXJFbEpTVWxKU1VsR0N3QUFBQVFBQUFWYTZ1eEREd1FBQkFBQUJBQUJBUUVCQVFFcVAwUkdTVWxKU1VsSlNWRG13a1JKU1VaR1J1bjkwOTFQU1VaRi8wYWsvZjM5L2MvOThMMnhzYkd4c2JHeER3QUVBQUFBQUFBQU5Vd0ZBQUFBQUFBQUFRQUJBQUVBTUhTSWpaZWRwS3l4c2JHenhmSDk1TE94c2JQQTF2MzkvZm1zZlVaSlJrMlAvZjM5L2YzOS9mMzkvZjM5L2YzOUNRQUFBQVFBQUFRQUJDOEFBQVFBQVFFQkFBRUJBUW1yL2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjMrcWtsRi8wbHkvZjM5L2YzOS9mMzkvZjM5L2YzOUVRQUFBQUFBQUFBQUFBVUFBUUVBQVFBQUFBQUZYOW45L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM1Z2tsR1JrMUorZjM5L2YzOS9mMzkvZjM5L2YzOUVRRUVBQVFBQUFRQUFBQUFBQUFCQUFFQkFVdkkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM2VUViLy8wbEcyLzM5L2YzOS9mMzkvZjM5L2YzOUZnQUFBQUFBQUFBQUFRQUVBUUVCQUFFaXFmbjkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjM5L2YzOS9mMzkvZjNQU1VYLy8wWkpsZjM5K2YzOS92MzkvZm45L2YzNUVnUUFBQVFBQUFRQUFBQUFBUUFBRFlyMC9mMzkvZjM1L2YzOS92MzkvZjM5L2YzNS9mMzkvZm4rL2YzOS9mMzkrZjZZUmtuLy8wWkpUTkwrL2YzOStmbjUrZm45L2YzK0d3QUFBQUVBQUFFQUFBRUJBUVJSMC8zOS9mMzkrZjM1L2ZuOSsvbjUvdjMrL2ZuOS92NzUvdjM5L2Y3Ky9mNzUrZjFZU2YvLy8vOUdSa1o2bTYrd3JLYWZsWmlIZjNWakZBQUFBUUFBQVFBQkFRQUJLanA5Zm9KK2ZuNkFnb0IvZ29LQ2dvYUFpNVdqdWUzOTBaZU1mb0NDZ29lU21MTGUvZGxHUmtiLy8vOUpSa2xOVkdSR1RVWkpSa1pKUmtsR0dnQUVBU3dwQkFBQUFCazRSa1pHU1VsSlNVbEpTVVpKUmtsR1NVbEpSazFHVFZ6M2drWk5ZRWxKU1VsR1JrbEducGhKUlAvLy8vOUdTVVoyejRKSlJrbEdSa1pHU1VaSkdRQUJBVWltTWdBVExrWkdSa2xHUmtaR1JrWkdSa1pHU1VaR1JrWkdXWGhKVWszUW1rbUR5VVpHU2sxSlNVWlowMUpHLy8vLy8vLy9TVVoyMDROUVJrbEdTVWxKU1VaRklBRUFBVFpxZUQ5SlNrbEdTVWxHU1VaSlJrbEpSa2xKUmtaR1NVWkpUbDFjNm1MY3VVbFVWa2FEK2I5T1JrYmpwMGxKLy8vLy8vLy9SVWxONlAzNTlKNUpVRlJHUmtaRUp5OFJHVUJKU1VsR1JrWkpSb05tUm1DZFNVWkdTVVpHU1VaSlJrbEdTVWxYL3YzOS9kVzlyS0tpOU9SVVJrYUpSa1pGLy8vLy8vLy8vMGxLaC83NS91YnMvZjJDU1VaRUtuRnBTVVpHUmsxRlprMUdoRWxHYmZDRFJrWkdSa1pHVFVaSlRVbE5Sa1pHMC9uOStmdjkrZnIrM2xCR1JrbEpTVW4vLy8vLy8vLy8vMFJHU3QvdFVFbGw2ZjNjUmtsR000ZThSa2xKU1VaSjAyTko4VXBHaG5WR1Q3SlBTVWxKbDBsR1JrWkZTVVI2YWZyOXJOdjk4M0xMM1ZCSlNVWkdSa2IvLy8vLy8vLy8vLzlHU1hINzdPajkvZjM5djNwR1NVWkdSa1pHUmtaVy9YTkcya3BOU1VsR1JrWkdTVVZHUmtaR1RVbE5SRWFtU1huNnIwemsvZXhnc3VaelRrWkpTZi8vLy8vLy8vLy8vLy8vUmttZitmMzkvZjM5L2NSSlJsSzUwbVZHUmsxeitWMUdhVTFVUmtaSlNVbEpSa1JRdmMrNW9ZaHZlRlJTUmtaam5tbUEvclJLUnBEOXlrbEdSdi8vLy8vLy8vLy8vLy8vLzBaRmh1djUvZjM5L2RKSlJvTCsvcGxHU1VsajgwWkpTVXpzblo5U1JrWkdUVTJtL2YzOS9mbjUrY0JHU1VsSlJrWlMvdDJTUmtsZGJFWkcvLy8vLy8vLy8vLy8vLy8vLzBsSlRhYjkvZjM5L2YyL1JrYTcrWmxOUmthMHAwbEdSbEwwK2YzRFJrbEpac1g1K2YzOStldWRsWUpKUmtaR1NVbE42UDcrckVaSlJrVC8vLy8vLy8vLy8vLy8vLy8vLy85R1JsZnkvdjM5L2YzOWIwbEppRjFFWHNEdFZFWkdSa2xjbTlYOXpKQktUb2g1Vk1uOXo3bEdSa3BLUmtaR1JrWkdoODMzeTBsSlJ2Ly8vLy8vLy8vLy8vLy8vLy8vLy85SlJrVnA2ZnYrL2YzOXNrWkdsTmJ3K2M5bVJrWkdTVVpPWEVtVitmMXBSa1ZHVGVENTYvMkNTVTFHU1VsSlNVMU5Sa1pHU1VaRy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JrbEVYWjNSNlBuOTVFMUo5UDJ5UmsxR1RseEpTWHowKzJ4US9mMWdSazFFU2ZUNXozaEpSa1pHU1VaR1RWSnZja2xKU1ViLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wWkdTVVpKUnVmOStWbEcvZjZtUm5DQ1puaEdWUFQ5L2VqSy9iSkdSblpkUm9kc1JrbEdTVWxKUmxLVjNmdis5RmxHUnYvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vOUVSa1pKUnNiKytXUmMvZjdGU1lMMzhITkdkdm4rN2MzVmhFbEdTVnhRU1VaR1JrWkdUVVZXclBUOS9mN3lkVWxHLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9TVWxHU1ZlTmJVMXovdW1YUmtsVWdtUkpTcFNYZmxsR1JrMUdSazFOUmtsTlRVbEpkZWo5NDVENS9kSmdSa2IvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzlHU1VaSlNVYVM3RTlFUm1sTVJsUnBURTFHUmtsR1NVbEdTV21odmMvS2hIQ1k3UDM5K2I3dGlFbEdSdi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vUmtaR1JrbWNyRW1DU1gzU1VPbjk5SjFOU1gyOWdrWjU0UDc5L2YzOS9mMzkrZm45OHFaT1NVWkUvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy85SlJrbEdTVkR0OU5uNXRPdjkvZjJ5c3YzOTFvejUvZjN5K2YzOTdjLzkrZXlhU1VsSlJrYi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vL1JFWkpSa1p2MHZUZmlmNzUvdjM5L2YzOS9mdisvZjNQL2YzOStmckJna2xHUmtaRy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy9Sa2xKUmtaR3VlUEN0ZEwrK2YzKy92bTUvdm4rK2ZMQmtGUkpTVVpHU2YvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vMFZHU1VsSlJrWkdSa2FEczd2SnljTzFvNWg2WEVsSlJrWkdSZi8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLzBWR1JrbEpTVWxKUmtaSlNVWkdTVWxKU1VaR1J2Ly8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8wVkdSa1ZHU1VsR1JrbEpSa1pHUnYvLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8vLy8rb0MvLy8vL0FBQUEvLy8vUUFBQi8vLy9BQUFBLy8vK0FBQUFQLy8vQUFBQS8vL3dBQUFBQi8vL0FBQUEvLy9nQUFBQUEvLy9BQUFBLy84QUFBQUFBZi8vQUFBQS8vNEFBQUFBQUgvL0FBQUEvL3dBQUFBQUFCLy9BQUFBLy9nQUFBQUFBQS8vQUFBQS8vQUFBQUFBQUFmL0FBQUEvK0FBQUFBQUFBUC9BQUFBLzRBQUFBQUFBQUgvQUFBQS80QUFBQUFBQUFEL0FBQUEvd0FBQUFBQUFBQi9BQUFBL2dBQUFBQUFBQUIvQUFBQS9BQUFBQUFBQUFBL0FBQUEvQUFBQUFBQUFBQS9BQUFBK0FBQUFBQUFBQUFmQUFBQStBQUFBQUFBQUFBUEFBQUE4QUFBQUFBQUFBQUhBQUFBNEFBQUFBQUFBQUFQQUFBQTRBQUFBQUFBQUFBSEFBQUE0QUFBQUFBQUFBQURBQUFBd0FBQUFBQUFBQUFEQUFBQXdBQUFBQUFBQUFBREFBQUF3QUFBQUFBQUFBQUJBQUFBZ0FBQUFBQUFBQUFEQUFBQWdBQUFBQUFBQUFBQkFBQUFnQUFBQUFBQUFBQUJBQUFBQUFBQUFBQUFBQUFCQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBZ0FBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFnQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQWdBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUJBQUFBZ0FBQUFBQUFBQUFCQUFBQWdBQUFBQUFBQUFBQkFBQUFnQUFBQUFBQUFBQURBQUFBd0FBQUFBQUFBQUFCQUFBQXdBQUFBQUFBQUFBREFBQUF3QUFBQUFBQUFBQUhBQUFBNEFBQUFBQUFBQUFIQUFBQTRBQUFBQUFBQUFBSEFBQUE4QUFBQUFBQUFBQVBBQUFBOEFBQUFBQUFBQUFQQUFBQStBQUFBQUFBQUFBZkFBQUEvQUFBQUFBQUFBQWZBQUFBL2dBQUFBQUFBQUEvQUFBQS9nQUFBQUFBQUFCL0FBQUEvd0FBQUFBQUFBRC9BQUFBL3dBQUFBQUFBQUgvQUFBQS80QUFBQUFBQUFQL0FBQUEvOEFBQUFBQUFBZi9BQUFBLytBQUFBQUFBQS8vQUFBQS8vQUFBQUFBQUIvL0FBQUEvL3dBQUFBQUFELy9BQUFBLy80QUFBQUFBSC8vQUFBQS8vK0FBQUFBQVAvL0FBQUEvLy9BQUFBQUEvLy9BQUFBLy8vNEFBQUFELy8vQUFBQS8vLzhBQUFBZi8vL0FBQUEvLy8vZ0FBRC8vLy9BQUFBLy8vLzhBQWYvLy8vQUFBQSIgdHlwZT0iaW1hZ2UveC1pY29uIiAvPg0KCTwhLS0gPG1ldGEgaHR0cC1lcXVpdj0iY29udGVudC10eXBlIiBjb250ZW50PSJ0ZXh0L2h0bWwiPiAtLT4NCgk8IS0tIDxtZXRhIGh0dHAtZXF1aXY9ImNvbnRlbnQtc3R5bGUtdHlwZSIgY29udGVudD0idGV4dC9jc3MiPiAtLT4NCgk8bWV0YSBjaGFyc2V0PSJ1dGYtOCIgLz4NCiAgICA8bWV0YSBjb250ZW50PSJJRT1lZGdlLGNocm9tZT0xIiBodHRwLWVxdWl2PSJYLVVBLUNvbXBhdGlibGUiIC8+DQogICAgPG1ldGEgY29udGVudD0iUG9sJmlhY3V0ZTt0aWNhIGRlIFByaXZhY2lkYWQiIG5hbWU9ImRlc2NyaXB0aW9uIiAvPg0KICAgIDxtZXRhIGNvbnRlbnQ9IndpZHRoPWRldmljZS13aWR0aCwgaW5pdGlhbC1zY2FsZT0xIiBuYW1lPSJ2aWV3cG9ydCIgLz4NCiAgICA8bGluayByZWw9ImNhbm9uaWNhbCIgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvL2FwaS9wcml2ZXMuaHRtbCIgLz4NCiAgICA8bGluayByZWw9ImFsdGVybmF0ZSIgaHJlZmxhbmc9IngtZGVmYXVsdCIgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvL2FwaS9wcml2Lmh0bWwiIC8+DQogICAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIGhyZWZsYW5nPSJlbiIgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvL2FwaS9wcml2Lmh0bWwiIC8+DQogICAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIGhyZWZsYW5nPSJlcyIgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvL2FwaS9wcml2ZXMuaHRtbCIgLz4NCgk8bWV0YSBuYW1lPSJhdXRob3IiIGNvbnRlbnQ9IkNVQkFOQVBQIExMQyIgLz4NCiAgICA8IS0tIDxsaW5rIGhyZWY9Imh0dHBzOi8vbWF4Y2RuLmJvb3RzdHJhcGNkbi5jb20vYm9vdHN0cmFwLzMuMy43L2Nzcy9ib290c3RyYXAubWluLmNzcyIgcmVsPSJzdHlsZXNoZWV0Ij4NCiAgICA8c2NyaXB0IHNyYz0iaHR0cHM6Ly9hamF4Lmdvb2dsZWFwaXMuY29tL2FqYXgvbGlicy9qcXVlcnkvMy4yLjEvanF1ZXJ5Lm1pbi5qcyI+PC9zY3JpcHQ+DQogICAgPHNjcmlwdCBzcmM9Imh0dHBzOi8vbWF4Y2RuLmJvb3RzdHJhcGNkbi5jb20vYm9vdHN0cmFwLzMuMy43L2pzL2Jvb3RzdHJhcC5taW4uanMiPjwvc2NyaXB0Pg0KICAgIDxsaW5rIGhyZWY9Imh0dHBzOi8vbWF4Y2RuLmJvb3RzdHJhcGNkbi5jb20vZm9udC1hd2Vzb21lLzQuNy4wL2Nzcy9mb250LWF3ZXNvbWUubWluLmNzcyIgcmVsPSJzdHlsZXNoZWV0Ij4gLS0+DQogICAgPGxpbmsgaHJlZj0iaHR0cHM6Ly9mb250cy5nb29nbGVhcGlzLmNvbS9jc3M/ZmFtaWx5PU9wZW4rU2FuczozMDAsMzAwaSw0MDAsNDAwaSw2MDAsNjAwaSw3MDAsNzAwaSw4MDAsODAwaSIgcmVsPSJzdHlsZXNoZWV0IiAvPg0KICAgIDxzdHlsZT4NCiAgICAgICAgYm9keSB7DQogICAgICAgICAgICBmb250LWZhbWlseTogJ09wZW4gU2FucycsICdIZWx2ZXRpY2EnLCBzYW5zLXNlcmlmOw0KICAgICAgICAgICAgY29sb3I6ICMwMDA7DQogICAgICAgICAgICBwYWRkaW5nOiA1cHg7DQogICAgICAgICAgICBtYXJnaW46IDVweDsNCiAgICAgICAgICAgIGxpbmUtaGVpZ2h0OiAxLjQyODsNCiAgICAgICAgfQ0KICAgICAgICBoMSwgaDIsIGgzLCBoNCwgaDUsIGg2LCBwIHsNCiAgICAgICAgICAgIHBhZGRpbmc6IDA7DQogICAgICAgICAgICBtYXJnaW46IDA7DQogICAgICAgICAgICBjb2xvcjojMzMzMzMzOw0KICAgICAgICB9DQogICAgICAgIGgxIHsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMzBweDsNCiAgICAgICAgICAgIGZvbnQtd2VpZ2h0OiA2MDAhaW1wb3J0YW50Ow0KICAgICAgICAgICAgY29sb3I6ICMzMzM7DQogICAgICAgIH0NCiAgICAgICAgaDIgew0KICAgICAgICAgICAgZm9udC1zaXplOiAyNHB4Ow0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDYwMDsNCiAgICAgICAgfQ0KICAgICAgICBoMyB7DQogICAgICAgICAgICBmb250LXNpemU6IDIycHg7DQogICAgICAgICAgICBmb250LXdlaWdodDogNjAwOw0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6IDI4cHg7DQogICAgICAgIH0NCiAgICAgICAgaHIgew0KICAgICAgICAgICAgbWFyZ2luLXRvcDogMzVweDsNCiAgICAgICAgICAgIG1hcmdpbi1ib3R0b206IDM1cHg7DQogICAgICAgICAgICBib3JkZXI6IDA7DQogICAgICAgICAgICBib3JkZXItdG9wOiAxcHggc29saWQgI2JmYmViZTsNCiAgICAgICAgfQ0KICAgICAgICB1bCB7DQogICAgICAgICAgICBsaXN0LXN0eWxlLXR5cGU6IG5vbmU7DQogICAgICAgICAgICBtYXJnaW46IDA7DQogICAgICAgICAgICBwYWRkaW5nOiAwOw0KICAgICAgICB9DQogICAgICAgIGxpIHsNCiAgICAgICAgICAgIGRpc3BsYXk6IGlubGluZS1ibG9jazsNCiAgICAgICAgICAgIGZsb2F0OiByaWdodDsNCiAgICAgICAgICAgIG1hcmdpbi1sZWZ0OiAyMHB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6IDM1cHg7DQogICAgICAgICAgICBmb250LXdlaWdodDogMTAwOw0KICAgICAgICB9DQogICAgICAgIGEgew0KICAgICAgICAgICAgdGV4dC1kZWNvcmF0aW9uOiBub25lOw0KICAgICAgICAgICAgY3Vyc29yOiBwb2ludGVyOw0KICAgICAgICAgICAgLXdlYmtpdC10cmFuc2l0aW9uOiBhbGwgLjNzIGVhc2UtaW4tb3V0Ow0KICAgICAgICAgICAgLW1vei10cmFuc2l0aW9uOiBhbGwgLjNzIGVhc2UtaW4tb3V0Ow0KICAgICAgICAgICAgLW1zLXRyYW5zaXRpb246IGFsbCAuM3MgZWFzZS1pbi1vdXQ7DQogICAgICAgICAgICAtby10cmFuc2l0aW9uOiBhbGwgLjNzIGVhc2UtaW4tb3V0Ow0KICAgICAgICAgICAgdHJhbnNpdGlvbjogYWxsIC4zcyBlYXNlLWluLW91dDsNCiAgICAgICAgfQ0KICAgICAgICBsaSBhIHsNCiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZTsNCiAgICAgICAgICAgIG1hcmdpbi1sZWZ0OiAzcHg7DQogICAgICAgIH0NCiAgICAgICAgbGkgPiBpIHsNCiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZTsNCiAgICAgICAgfQ0KCQk8IS0tDQogICAgICAgIC5jb2x1bW4td3JhcCBhIHsNCiAgICAgICAgICAgIGNvbG9yOiAjNWMzNGMyOw0KICAgICAgICAgICAgZm9udC13ZWlnaHQ6IDYwMDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZToxNnB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6MjRweDsNCiAgICAgICAgfQ0KICAgICAgICAuY29sdW1uLXdyYXAgcCB7DQogICAgICAgICAgICBjb2xvcjogIzcxNzE3MTsNCiAgICAgICAgICAgIGZvbnQtc2l6ZToxNnB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6MjRweDsNCiAgICAgICAgICAgIGZvbnQtd2VpZ2h0OjMwMDsNCiAgICAgICAgfQ0KICAgICAgICAuY29udGFpbmVyIHsNCiAgICAgICAgICAgIG1hcmdpbi10b3A6IDEwMHB4Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXIgew0KICAgICAgICAgICAgcG9zaXRpb246IHJlbGF0aXZlOw0KICAgICAgICAgICAgbWluLWhlaWdodDogNDVweDsNCiAgICAgICAgICAgIG1hcmdpbi1ib3R0b206IDIwcHg7DQogICAgICAgICAgICBib3JkZXI6IDFweCBzb2xpZCB0cmFuc3BhcmVudDsNCiAgICAgICAgfQ0KICAgICAgICAubmF2YmFyLWJyYW5kIHsNCiAgICAgICAgICAgIGZsb2F0OiBsZWZ0Ow0KICAgICAgICAgICAgaGVpZ2h0OiBhdXRvOw0KICAgICAgICAgICAgcGFkZGluZzogMTBweCAxMHB4Ow0KICAgICAgICAgICAgZm9udC1zaXplOiAxOHB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6IDIwcHg7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhci1uYXY+bGk+YSB7DQogICAgICAgICAgICBwYWRkaW5nLXRvcDogMTFweDsNCiAgICAgICAgICAgIHBhZGRpbmctYm90dG9tOiAxMXB4Ow0KICAgICAgICAgICAgZm9udC1zaXplOiAxM3B4Ow0KICAgICAgICAgICAgcGFkZGluZy1sZWZ0OiA1cHg7DQogICAgICAgICAgICBwYWRkaW5nLXJpZ2h0OiA1cHg7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhci1uYXY+bGk+YTpob3ZlciB7DQogICAgICAgICAgICB0ZXh0LWRlY29yYXRpb246IG5vbmU7DQogICAgICAgICAgICBjb2xvcjogI2NkYzNlYSFpbXBvcnRhbnQ7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhci1uYXY+bGk+YSBpIHsNCiAgICAgICAgICAgIG1hcmdpbi1yaWdodDogNXB4Ow0KICAgICAgICB9DQogICAgICAgIC5uYXYtYmFyIGltZyB7DQogICAgICAgICAgICBwb3NpdGlvbjogcmVsYXRpdmU7DQogICAgICAgICAgICB0b3A6IDNweDsNCiAgICAgICAgfQ0KICAgICAgICAuY29uZ3JhdHogew0KICAgICAgICAgICAgbWFyZ2luOiAwIGF1dG87DQogICAgICAgICAgICB0ZXh0LWFsaWduOiBjZW50ZXI7DQogICAgICAgIH0NCiAgICAgICAgLm1lc3NhZ2U6OmJlZm9yZSB7DQogICAgICAgICAgICBjb250ZW50OiAiICI7DQogICAgICAgICAgICBiYWNrZ3JvdW5kOiB1cmwoaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2hvc3Rpbmdlci9iYW5uZXJzL21hc3Rlci9ob3N0aW5nZXJfd2VsY29tZS9pbWFnZXMvaG9zdGluZ2VyLWRyYWdvbi5wbmcpOw0KICAgICAgICAgICAgd2lkdGg6IDE0MXB4Ow0KICAgICAgICAgICAgaGVpZ2h0OiAxNzVweDsNCiAgICAgICAgICAgIHBvc2l0aW9uOiBhYnNvbHV0ZTsNCiAgICAgICAgICAgIGxlZnQ6IC0xNTBweDsNCiAgICAgICAgICAgIHRvcDogMDsNCiAgICAgICAgfQ0KICAgICAgICAubWVzc2FnZSB7DQogICAgICAgICAgICB3aWR0aDogNTAlOw0KICAgICAgICAgICAgbWFyZ2luOiAwIGF1dG87DQogICAgICAgICAgICBoZWlnaHQ6IGF1dG87DQogICAgICAgICAgICBwYWRkaW5nOiA0MHB4Ow0KICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogI2VlZWNmOTsNCiAgICAgICAgICAgIG1hcmdpbi1ib3R0b206IDEwMHB4Ow0KICAgICAgICAgICAgYm9yZGVyLXJhZGl1czogNXB4Ow0KICAgICAgICAgICAgcG9zaXRpb246cmVsYXRpdmU7DQogICAgICAgIH0NCiAgICAgICAgLm1lc3NhZ2UgcCB7DQogICAgICAgICAgICBmb250LXdlaWdodDogMzAwOw0KICAgICAgICAgICAgZm9udC1zaXplOiAxNnB4Ow0KICAgICAgICAgICAgbGluZS1oZWlnaHQ6IDI0cHg7DQogICAgICAgIH0NCiAgICAgICAgI3BhdGhOYW1lIHsNCiAgICAgICAgICAgIG1hcmdpbjogMjBweCAxMHB4Ow0KICAgICAgICAgICAgY29sb3I6IGdyZXk7DQogICAgICAgICAgICBmb250LXdlaWdodDogMzAwOw0KICAgICAgICAgICAgZm9udC1zaXplOjE4cHg7DQogICAgICAgICAgICBmb250LXN0eWxlOiBpdGFsaWM7DQogICAgICAgIH0NCiAgICAgICAgLmNvbHVtbi1jdXN0b20gew0KICAgICAgICAgICAgYm9yZGVyLXJhZGl1czogNXB4Ow0KICAgICAgICAgICAgYmFja2dyb3VuZC1jb2xvcjogI2VlZWNmOTsNCiAgICAgICAgICAgIHBhZGRpbmc6IDM1cHg7DQogICAgICAgICAgICBtYXJnaW4tYm90dG9tOiAyMHB4Ow0KICAgICAgICB9DQogICAgICAgIC5mb290ZXIgew0KICAgICAgICAgICAgZm9udC1zaXplOiAxM3B4Ow0KICAgICAgICAgICAgY29sb3I6IGdyYXkgIWltcG9ydGFudDsNCiAgICAgICAgICAgIG1hcmdpbi10b3A6IDI1cHg7DQogICAgICAgICAgICBsaW5lLWhlaWdodDogMS40Ow0KICAgICAgICAgICAgbWFyZ2luLWJvdHRvbTogNDVweDsNCiAgICAgICAgfQ0KICAgICAgICAuZm9vdGVyIGEgew0KICAgICAgICAgICAgY3Vyc29yOiBwb2ludGVyOw0KICAgICAgICAgICAgY29sb3I6ICM2NDY0NjQgIWltcG9ydGFudDsNCiAgICAgICAgICAgIGZvbnQtc2l6ZTogMTJweDsNCiAgICAgICAgfQ0KICAgICAgICAuY29weXJpZ2h0IHsNCiAgICAgICAgICAgIGNvbG9yOiAjNjQ2NDY0ICFpbXBvcnRhbnQ7DQogICAgICAgICAgICBmb250LXNpemU6IDEycHg7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhciBhIHsNCiAgICAgICAgICAgIGNvbG9yOiB3aGl0ZSAhaW1wb3J0YW50Ow0KICAgICAgICB9DQogICAgICAgIC5uYXZiYXIgew0KICAgICAgICAgICAgYm9yZGVyLXJhZGl1czogMHB4ICFpbXBvcnRhbnQ7DQogICAgICAgIH0NCiAgICAgICAgLm5hdmJhci1pbnZlcnNlIHsNCiAgICAgICAgICAgIGJhY2tncm91bmQtY29sb3I6ICM0MzQzNDM7DQogICAgICAgICAgICBib3JkZXI6IG5vbmU7DQogICAgICAgIH0NCiAgICAgICAgLmNvbHVtbi1jdXN0b20td3JhcHsNCiAgICAgICAgICAgIHBhZGRpbmctdG9wOiAxMHB4IDIwcHg7DQogICAgICAgIH0NCiAgICAgICAgQG1lZGlhIHNjcmVlbiBhbmQgKG1heC13aWR0aDogNzY4cHgpIHsNCiAgICAgICAgICAgIC5tZXNzYWdlIHsNCiAgICAgICAgICAgICAgICB3aWR0aDogNzUlOw0KICAgICAgICAgICAgICAgIHBhZGRpbmc6IDM1cHg7DQogICAgICAgICAgICB9DQogICAgICAgICAgICAuY29udGFpbmVyIHsNCiAgICAgICAgICAgICAgICBtYXJnaW4tdG9wOiAzMHB4Ow0KICAgICAgICAgICAgfQ0KICAgICAgICB9IC0tPg0KICAgICAgICB9DQogICAgPC9zdHlsZT4NCiAgICA8dGl0bGU+UG9sJmlhY3V0ZTt0aWNhIGRlIFByaXZhY2lkYWQ8L3RpdGxlPg0KPC9oZWFkPg0KPGJvZHk+DQo8YnIvPg0KPGgxPjxwPjxzdHJvbmc+UE9MJklhY3V0ZTtUSUNBIERFIFBSSVZBQ0lEQUQgLSBVbHRpbWEgYWN0dWFsaXphY2kmb2FjdXRlO24gMjYvMDYvMjAyMDwvc3Ryb25nPjwvcD48L2gxPg0KPGJyLz4NCjxzcGFuIHRpdGxlPSJTcGFuaXNoIj48YSBsYW5nPSJlcyIgaHJlZj0icHJpdmVzLmh0bWwiPkVzcGEmbnRpbGRlO29sPC9hPjwvc3Bhbj4NCjxici8+DQo8YnIvPg0KPHNwYW4gdGl0bGU9IkVuZ2xpc2giPjxhIGxhbmc9ImVuIiBocmVmPSJwcml2Lmh0bWwiPkVuZ2xpc2g8L2E+PC9zcGFuPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGJyLz4NCjxwPlBhcmEgdXNhciBsYSBhcGxpY2FjaSZvYWN1dGU7biBlcyBuZWNlc2FyaW8gYWNlcHRhciB0b2RvcyBsb3MgcGVybWlzb3MuIFRhbWJpJmVhY3V0ZTtuIHNlIHV0aWxpemEgYXBsaWNhY2lvbmVzIGRlIHRlcmNlcm9zIChkZXNkZSBHb29nbGUpIHB1ZWRlIHZlciBzdXMgcG9sJmlhY3V0ZTt0aWNhcyBkZSBwcml2YWNpZGFkIGFxdSZpYWN1dGU7Ojxici8+DQo8YSBocmVmPSJodHRwczovL3d3dy5nb29nbGUuY29tL3BvbGljaWVzL3ByaXZhY3kvIj5Hb29nbGUgUGxheSBTZXJ2aWNlczwvYT48YnIvPg0KPGEgaHJlZj0iaHR0cHM6Ly9zdXBwb3J0Lmdvb2dsZS5jb20vYWRtb2IvYW5zd2VyLzYxMjg1NDM/aGw9ZXMiPkFkTW9iPC9hPjxici8+DQo8YSBocmVmPSJodHRwczovL2ZpcmViYXNlLmdvb2dsZS5jb20vcG9saWNpZXMvYW5hbHl0aWNzIj5Hb29nbGUgQW5hbHl0aWNzIGZvciBGaXJlYmFzZTwvYT48YnIvPg0KPGEgaHJlZj0iaHR0cHM6Ly9maXJlYmFzZS5nb29nbGUuY29tL3N1cHBvcnQvcHJpdmFjeS8iPkZpcmViYXNlIENyYXNobHl0aWNzPC9hPjxici8+PC9wPg0KPGJyLz4NCjxoMT5Qcm90ZWNjaSZvYWN1dGU7biBkZSBkYXRvcyBkZSBjYXImYWFjdXRlO2N0ZXIgcGVyc29uYWwgc2VnJnVhY3V0ZTtuIGxhIExPUEQ8L2gxPg0KPGJyLz4NCjxwPjxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgZW4gYXBsaWNhY2kmb2FjdXRlO24gZGUgbGEgbm9ybWF0aXZhIHZpZ2VudGUgZW4gbWF0ZXJpYSBkZSBwcm90ZWNjaSZvYWN1dGU7biBkZSBkYXRvcyBkZSBjYXImYWFjdXRlO2N0ZXIgcGVyc29uYWwsIGluZm9ybWEgcXVlIGxvcyBkYXRvcyBwZXJzb25hbGVzIHF1ZSBzZSByZWNvZ2VuIGEgdHJhdiZlYWN1dGU7cyBkZSBsb3MgZm9ybXVsYXJpb3MgZGU6PC9wPg0KPHA+PHU+PGEgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvIj5jdWJhbmFwcC5pbmZvPC9hPjwvdT4sIHNlIGluY2x1eWVuIGVuIGxvcyBmaWNoZXJvcyBhdXRvbWF0aXphZG9zIGVzcGVjJmlhY3V0ZTtmaWNvcyBkZSB1c3VhcmlvcyBkZSBsb3Mgc2VydmljaW9zIGRlIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPjwvcD4NCjxici8+DQo8cD5MYSByZWNvZ2lkYSB5IHRyYXRhbWllbnRvIGF1dG9tYXRpemFkbyBkZSBsb3MgZGF0b3MgZGUgY2FyJmFhY3V0ZTtjdGVyIHBlcnNvbmFsIHRpZW5lIGNvbW8gZmluYWxpZGFkIGVsIG1hbnRlbmltaWVudG8gZGUgbGEgcmVsYWNpJm9hY3V0ZTtuIGNvbWVyY2lhbCB5IGVsIGRlc2VtcGUmbnRpbGRlO28gZGUgdGFyZWFzIGRlIGluZm9ybWFjaSZvYWN1dGU7biwgZm9ybWFjaSZvYWN1dGU7biwgYXNlc29yYW1pZW50byB5IG90cmFzIGFjdGl2aWRhZGVzIHByb3BpYXMgZGU8L3A+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4uPC9wPg0KPHA+RXN0b3MgZGF0b3MgJnVhY3V0ZTtuaWNhbWVudGUgc2VyJmFhY3V0ZTtuIGNlZGlkb3MgYSBhcXVlbGxhcyBlbnRpZGFkZXMgcXVlIHNlYW4gbmVjZXNhcmlhcyBjb24gZWwgJnVhY3V0ZTtuaWNvIG9iamV0aXZvIGRlIGRhciBjdW1wbGltaWVudG8gYSBsYSBmaW5hbGlkYWQgYW50ZXJpb3JtZW50ZSBleHB1ZXN0YS48L3A+DQo8YnIvPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IGFkb3B0YSBsYXMgbWVkaWRhcyBuZWNlc2FyaWFzIHBhcmEgZ2FyYW50aXphciBsYSBzZWd1cmlkYWQsIGludGVncmlkYWQgeSBjb25maWRlbmNpYWxpZGFkIGRlIGxvcyBkYXRvcyBjb25mb3JtZSBhIGxvIGRpc3B1ZXN0byBlbiBlbCBSZWdsYW1lbnRvIChVRSkgMjAxNi82NzkgZGVsIFBhcmxhbWVudG8gRXVyb3BlbyB5IGRlbCBDb25zZWpvLCBkZSAyNyBkZSBhYnJpbCBkZSAyMDE2LCByZWxhdGl2byBhIGxhIHByb3RlY2NpJm9hY3V0ZTtuIGRlIGxhcyBwZXJzb25hcyBmJmlhY3V0ZTtzaWNhcyBlbiBsbyBxdWUgcmVzcGVjdGEgYWwgdHJhdGFtaWVudG8gZGUgZGF0b3MgcGVyc29uYWxlcyB5IGEgbGEgbGlicmUgY2lyY3VsYWNpJm9hY3V0ZTtuIGRlIGxvcyBtaXNtb3MuPC9wPg0KPGJyLz4NCjxwPkVsIHVzdWFyaW8gcG9kciZhYWN1dGU7IGVuIGN1YWxxdWllciBtb21lbnRvIGVqZXJjaXRhciBsb3MgZGVyZWNob3MgZGUgYWNjZXNvLCBvcG9zaWNpJm9hY3V0ZTtuLCByZWN0aWZpY2FjaSZvYWN1dGU7biB5IGNhbmNlbGFjaSZvYWN1dGU7biByZWNvbm9jaWRvcyBlbiBlbCBjaXRhZG8gUmVnbGFtZW50byAoVUUpLiBFbCBlamVyY2ljaW8gZGUgZXN0b3MgZGVyZWNob3MgcHVlZGUgcmVhbGl6YXJsbyBlbCBwcm9waW8gdXN1YXJpbyBhIHRyYXYmZWFjdXRlO3MgZGUgZW1haWwgYTogPHN0cm9uZz5oZWxwQGN1YmFuYXBwLmluZm88L3N0cm9uZz4gbyBlbiBsYSBkaXJlY2NpJm9hY3V0ZTtuOiA8dT5DYWxsZSBBbHRvIEFyYWcmb2FjdXRlO24gMywgWmFyYWdvemEsIDUwMDAyLCBFc3BhJm50aWxkZTthPC91PjwvcD4NCjxici8+DQo8cD5FbCB1c3VhcmlvIG1hbmlmaWVzdGEgcXVlIHRvZG9zIGxvcyBkYXRvcyBmYWNpbGl0YWRvcyBwb3IgJmVhY3V0ZTtsIHNvbiBjaWVydG9zIHkgY29ycmVjdG9zLCB5IHNlIGNvbXByb21ldGUgYSBtYW50ZW5lcmxvcyBhY3R1YWxpemFkb3MsIGNvbXVuaWNhbmRvIGxvcyBjYW1iaW9zIGEgPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+PC9wPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGgxPkZpbmFsaWRhZCBkZWwgdHJhdGFtaWVudG8gZGUgbG9zIGRhdG9zIHBlcnNvbmFsZXM6PC9oMT4NCjxici8+DQo8cD48c3Ryb25nPiZpcXVlc3Q7Q29uIHF1JmVhY3V0ZTsgZmluYWxpZGFkIHRyYXRhcmVtb3MgdHVzIGRhdG9zIHBlcnNvbmFsZXM/PC9zdHJvbmc+PC9wPg0KPGJyLz4NCjxwPkVuIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgdHJhdGFyZW1vcyB0dXMgZGF0b3MgcGVyc29uYWxlcyByZWNhYmFkb3MgYSB0cmF2JmVhY3V0ZTtzIGRlbCBTaXRpbyBXZWI6PC9wPg0KPHA+PHU+PGEgaHJlZj0iaHR0cHM6Ly9jdWJhbmFwcC5pbmZvIj5jdWJhbmFwcC5pbmZvPC9hPjwvdT4sIGNvbiBsYXMgc2lndWllbnRlcyBmaW5hbGlkYWRlczo8L3A+DQo8YnIvPg0KPHVsPg0KPGxpPkVuIGNhc28gZGUgY29udHJhdGFjaSZvYWN1dGU7biBkZSBsb3MgYmllbmVzIHkgc2VydmljaW9zIG9mZXJ0YWRvcyBhIHRyYXYmZWFjdXRlO3MgZGUgPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LCBwYXJhIG1hbnRlbmVyIGxhIHJlbGFjaSZvYWN1dGU7biBjb250cmFjdHVhbCwgYXMmaWFjdXRlOyBjb21vIGxhIGdlc3RpJm9hY3V0ZTtuLCBhZG1pbmlzdHJhY2kmb2FjdXRlO24sIGluZm9ybWFjaSZvYWN1dGU7biwgcHJlc3RhY2kmb2FjdXRlO24geSBtZWpvcmEgZGVsIHNlcnZpY2lvLjwvbGk+DQo8bGk+RW52JmlhY3V0ZTtvIGRlIGluZm9ybWFjaSZvYWN1dGU7biBzb2xpY2l0YWRhIGEgdHJhdiZlYWN1dGU7cyBkZSBsb3MgZm9ybXVsYXJpb3MgZGlzcHVlc3RvcyBlbiA8c3Ryb25nPkJvbGl0YSBDdWJhbmEuPC9zdHJvbmc+PC9saT4NCjxsaT5SZW1pdGlyIGJvbGV0aW5lcyAobmV3c2xldHRlcnMpLCBhcyZpYWN1dGU7IGNvbW8gY29tdW5pY2FjaW9uZXMgY29tZXJjaWFsZXMgZGUgcHJvbW9jaW9uZXMgeS9vIHB1YmxpY2lkYWQgZGUgPHN0cm9uZz5Hb29nbGUgPC9zdHJvbmc+eSBkZWwgc2VjdG9yLjwvbGk+DQo8L3VsPg0KPGJyLz4NCjxici8+DQo8cD5UZSByZWNvcmRhbW9zIHF1ZSBwdWVkZXMgb3BvbmVydGUgYWwgZW52JmlhY3V0ZTtvIGRlIGNvbXVuaWNhY2lvbmVzIGNvbWVyY2lhbGVzIHBvciBjdWFscXVpZXIgdiZpYWN1dGU7YSB5IGVuIGN1YWxxdWllciBtb21lbnRvLCByZW1pdGllbmRvIHVuIGNvcnJlbyBlbGVjdHImb2FjdXRlO25pY28gYSBsYSBkaXJlY2NpJm9hY3V0ZTtuIGluZGljYWRhIGFudGVyaW9ybWVudGUuPC9wPg0KPGJyLz4NCjxwPkxvcyBjYW1wb3MgZGUgZGljaG9zIHJlZ2lzdHJvcyBzb24gZGUgY3VtcGxpbWVudGFjaSZvYWN1dGU7biBvYmxpZ2F0b3JpYSwgc2llbmRvIGltcG9zaWJsZSByZWFsaXphciBsYXMgZmluYWxpZGFkZXMgZXhwcmVzYWRhcyBzaSBubyBzZSBhcG9ydGFuIGVzb3MgZGF0b3MuPC9wPg0KPGJyLz4NCjxici8+DQo8aDE+JmlxdWVzdDtQb3IgY3UmYWFjdXRlO250byB0aWVtcG8gc2UgY29uc2VydmFuIGxvcyBkYXRvcyBwZXJzb25hbGVzIHJlY2FiYWRvcz88L2gxPg0KPGJyLz4NCjxwPkxvcyBkYXRvcyBwZXJzb25hbGVzIHByb3BvcmNpb25hZG9zIHNlIGNvbnNlcnZhciZhYWN1dGU7biBtaWVudHJhcyBzZSBtYW50ZW5nYSBsYSByZWxhY2kmb2FjdXRlO24gY29tZXJjaWFsIG8gbm8gc29saWNpdGVzIHN1IHN1cHJlc2kmb2FjdXRlO24geSBkdXJhbnRlIGVsIHBsYXpvIHBvciBlbCBjdSZhYWN1dGU7bCBwdWRpZXJhbiBkZXJpdmFyc2UgcmVzcG9uc2FiaWxpZGFkZXMgbGVnYWxlcyBwb3IgbG9zIHNlcnZpY2lvcyBwcmVzdGFkb3MuPC9wPg0KPGJyLz4NCjxici8+DQo8YnIvPg0KPGgxPkxlZ2l0aW1hY2kmb2FjdXRlO246PC9oMT4NCjxici8+DQo8cD5FbCB0cmF0YW1pZW50byBkZSB0dXMgZGF0b3Mgc2UgcmVhbGl6YSBjb24gbGFzIHNpZ3VpZW50ZXMgYmFzZXMganVyJmlhY3V0ZTtkaWNhcyBxdWUgbGVnaXRpbWFuIGVsIG1pc21vOjwvcD4NCjxici8+DQo8dWw+DQo8bGk+TGEgc29saWNpdHVkIGRlIGluZm9ybWFjaSZvYWN1dGU7biB5L28gbGEgY29udHJhdGFjaSZvYWN1dGU7biBkZSBsb3Mgc2VydmljaW9zIGRlIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgY3V5b3MgdCZlYWN1dGU7cm1pbm9zIHkgY29uZGljaW9uZXMgc2UgcG9uZHImYWFjdXRlO24gYSB0dSBkaXNwb3NpY2kmb2FjdXRlO24gZW4gdG9kbyBjYXNvLCBkZSBmb3JtYSBwcmV2aWEgYSB1bmEgZXZlbnR1YWwgY29udHJhdGFjaSZvYWN1dGU7bi48L2xpPg0KPGxpPkVsIGNvbnNlbnRpbWllbnRvIGxpYnJlLCBlc3BlYyZpYWN1dGU7ZmljbywgaW5mb3JtYWRvIGUgaW5lcXUmaWFjdXRlO3ZvY28sIGVuIHRhbnRvIHF1ZSB0ZSBpbmZvcm1hbW9zIHBvbmllbmRvIGEgdHUgZGlzcG9zaWNpJm9hY3V0ZTtuIGxhIHByZXNlbnRlIHBvbCZpYWN1dGU7dGljYSBkZSBwcml2YWNpZGFkLCBxdWUgdHJhcyBsYSBsZWN0dXJhIGRlIGxhIG1pc21hLCBlbiBjYXNvIGRlIGVzdGFyIGNvbmZvcm1lLCBwdWVkZXMgYWNlcHRhciBtZWRpYW50ZSB1bmEgZGVjbGFyYWNpJm9hY3V0ZTtuIG8gdW5hIGNsYXJhIGFjY2kmb2FjdXRlO24gYWZpcm1hdGl2YSwgY29tbyBlbCBtYXJjYWRvIGRlIHVuYSBjYXNpbGxhIGRpc3B1ZXN0YSBhbCBlZmVjdG8uPC9saT4NCjwvdWw+DQo8YnIvPg0KPHA+RW4gY2FzbyBkZSBxdWUgbm8gbm9zIGZhY2lsaXRlcyB0dXMgZGF0b3MgbyBsbyBoYWdhcyBkZSBmb3JtYSBlcnImb2FjdXRlO25lYSBvIGluY29tcGxldGEsIG5vIHBvZHJlbW9zIGF0ZW5kZXIgdHUgc29saWNpdHVkLCByZXN1bHRhbmRvIGRlbCB0b2RvIGltcG9zaWJsZSBwcm9wb3JjaW9uYXJ0ZSBsYSBpbmZvcm1hY2kmb2FjdXRlO24gc29saWNpdGFkYSBvIGxsZXZhciBhIGNhYm8gbGEgY29udHJhdGFjaSZvYWN1dGU7biBkZSBsb3Mgc2VydmljaW9zLjwvcD4NCjxici8+DQo8YnIvPg0KPGJyLz4NCjxoMT5EZXN0aW5hdGFyaW9zOjwvaDE+DQo8YnIvPg0KPHA+TG9zIGRhdG9zIG5vIHNlIGNvbXVuaWNhciZhYWN1dGU7biBhIG5pbmcmdWFjdXRlO24gdGVyY2VybyBhamVubyBhIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgc2Fsdm8gb2JsaWdhY2kmb2FjdXRlO24gbGVnYWwuPC9wPg0KPGJyLz4NCjxoMT5EYXRvcyByZWNvcGlsYWRvcyBwb3IgdXN1YXJpb3MgZGUgbG9zIHNlcnZpY2lvczwvaDE+DQo8YnIvPg0KPHA+RW4gbG9zIGNhc29zIGVuIHF1ZSBlbCB1c3VhcmlvIGluY2x1eWEgZmljaGVyb3MgY29uIGRhdG9zIGRlIGNhciZhYWN1dGU7Y3RlciBwZXJzb25hbCBlbiBsb3Mgc2Vydmlkb3JlcyBkZSBhbG9qYW1pZW50byBjb21wYXJ0aWRvLCA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4gbm8gc2UgaGFjZSByZXNwb25zYWJsZSBkZWwgaW5jdW1wbGltaWVudG8gcG9yIHBhcnRlIGRlbCB1c3VhcmlvIGRlbCBSR1BELjwvcD4NCjxici8+DQo8YnIvPg0KPGgxPlJldGVuY2kmb2FjdXRlO24gZGUgZGF0b3MgZW4gY29uZm9ybWlkYWQgYSBsYSBMU1NJPC9oMT4NCjxici8+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4gaW5mb3JtYSBkZSBxdWUsIGNvbW8gcHJlc3RhZG9yIGRlIHNlcnZpY2lvIGRlIGFsb2phbWllbnRvIGRlIGRhdG9zIHkgZW4gdmlydHVkIGRlIGxvIGVzdGFibGVjaWRvIGVuIGxhIExleSAzNC8yMDAyIGRlIDExIGRlIGp1bGlvIGRlIFNlcnZpY2lvcyBkZSBsYSBTb2NpZWRhZCBkZSBsYSBJbmZvcm1hY2kmb2FjdXRlO24geSBkZSBDb21lcmNpbyBFbGVjdHImb2FjdXRlO25pY28gKExTU0kpLCByZXRpZW5lIHBvciB1biBwZXJpb2RvIG0mYWFjdXRlO3hpbW8gZGUgMTIgbWVzZXMgbGEgaW5mb3JtYWNpJm9hY3V0ZTtuIGltcHJlc2NpbmRpYmxlIHBhcmEgaWRlbnRpZmljYXIgZWwgb3JpZ2VuIGRlIGxvcyBkYXRvcyBhbG9qYWRvcyB5IGVsIG1vbWVudG8gZW4gcXVlIHNlIGluaWNpJm9hY3V0ZTsgbGEgcHJlc3RhY2kmb2FjdXRlO24gZGVsIHNlcnZpY2lvLiBMYSByZXRlbmNpJm9hY3V0ZTtuIGRlIGVzdG9zIGRhdG9zIG5vIGFmZWN0YSBhbCBzZWNyZXRvIGRlIGxhcyBjb211bmljYWNpb25lcyB5IHMmb2FjdXRlO2xvIHBvZHImYWFjdXRlO24gc2VyIHV0aWxpemFkb3MgZW4gZWwgbWFyY28gZGUgdW5hIGludmVzdGlnYWNpJm9hY3V0ZTtuIGNyaW1pbmFsIG8gcGFyYSBsYSBzYWx2YWd1YXJkaWEgZGUgbGEgc2VndXJpZGFkIHAmdWFjdXRlO2JsaWNhLCBwb25pJmVhY3V0ZTtuZG9zZSBhIGRpc3Bvc2ljaSZvYWN1dGU7biBkZSBsb3MganVlY2VzIHkvbyB0cmlidW5hbGVzIG8gZGVsIE1pbmlzdGVyaW8gcXVlIGFzJmlhY3V0ZTsgbG9zIHJlcXVpZXJhLjwvcD4NCjxici8+DQo8cD5MYSBjb211bmljYWNpJm9hY3V0ZTtuIGRlIGRhdG9zIGEgbGFzIEZ1ZXJ6YXMgeSBDdWVycG9zIGRlbCBFc3RhZG8gc2UgaGFyJmFhY3V0ZTsgZW4gdmlydHVkIGEgbG8gZGlzcHVlc3RvIGVuIGxhIG5vcm1hdGl2YSBzb2JyZSBwcm90ZWNjaSZvYWN1dGU7biBkZSBkYXRvcyBwZXJzb25hbGVzLjwvcD4NCjxici8+DQo8aDE+RGVyZWNob3MgcHJvcGllZGFkIGludGVsZWN0dWFsIGRlIEJvbGl0YSBDdWJhbmE8L2gxPg0KPGJyLz4NCjxwPjxzdHJvbmc+Q1VCQU5BUFA8L3N0cm9uZz4gZXMgdGl0dWxhciBkZSB0b2RvcyBsb3MgZGVyZWNob3MgZGUgYXV0b3IsIHByb3BpZWRhZCBpbnRlbGVjdHVhbCwgaW5kdXN0cmlhbCwgImtub3cgaG93IiB5IGN1YW50b3Mgb3Ryb3MgZGVyZWNob3MgZ3VhcmRhbiByZWxhY2kmb2FjdXRlO24gY29uIGxvcyBjb250ZW5pZG9zIGRlIGxhIGFwcCA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4geSBsb3Mgc2VydmljaW9zIG9mZXJ0YWRvcyBlbiBlbCBtaXNtbywgYXMmaWFjdXRlOyBjb21vIGRlIGxvcyBwcm9ncmFtYXMgbmVjZXNhcmlvcyBwYXJhIHN1IGltcGxlbWVudGFjaSZvYWN1dGU7biB5IGxhIGluZm9ybWFjaSZvYWN1dGU7biByZWxhY2lvbmFkYS48L3A+DQo8YnIvPg0KPGJyLz4NCjxoMT5Qcm9waWVkYWQgaW50ZWxlY3R1YWwgZGVsIHNvZnR3YXJlPC9oMT4NCjxici8+DQo8cD5FbCB1c3VhcmlvIGRlYmUgcmVzcGV0YXIgbG9zIHByb2dyYW1hcyBkZSB0ZXJjZXJvcyBwdWVzdG9zIGEgc3UgZGlzcG9zaWNpJm9hY3V0ZTtuIHBvciA8c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4sIGF1biBzaWVuZG8gZ3JhdHVpdG9zIHkvbyBkZSBkaXNwb3NpY2kmb2FjdXRlO24gcCZ1YWN1dGU7YmxpY2EuPC9wPg0KPGJyLz4NCjxwPjxzdHJvbmc+Q1VCQU5BUFA8L3N0cm9uZz4gZGlzcG9uZSBkZSBsb3MgZGVyZWNob3MgZGUgZXhwbG90YWNpJm9hY3V0ZTtuIHkgcHJvcGllZGFkIGludGVsZWN0dWFsIG5lY2VzYXJpb3MgZGVsIHNvZnR3YXJlLjwvcD4NCjxici8+DQo8cD5FbCB1c3VhcmlvIG5vIGFkcXVpZXJlIGRlcmVjaG8gYWxndW5vIG8gbGljZW5jaWEgcG9yIGVsIHNlcnZpY2lvIGNvbnRyYXRhZG8sIHNvYnJlIGVsIHNvZnR3YXJlIG5lY2VzYXJpbyBwYXJhIGxhIHByZXN0YWNpJm9hY3V0ZTtuIGRlbCBzZXJ2aWNpbywgbmkgdGFtcG9jbyBzb2JyZSBsYSBpbmZvcm1hY2kmb2FjdXRlO24gdCZlYWN1dGU7Y25pY2EgZGUgc2VndWltaWVudG8gZGVsIHNlcnZpY2lvLCBleGNlcGNpJm9hY3V0ZTtuIGhlY2hhIGRlIGxvcyBkZXJlY2hvcyB5IGxpY2VuY2lhcyBuZWNlc2FyaW9zIHBhcmEgZWwgY3VtcGxpbWllbnRvIGRlIGxvcyBzZXJ2aWNpb3MgY29udHJhdGFkb3MgeSAmdWFjdXRlO25pY2FtZW50ZSBkdXJhbnRlIGxhIGR1cmFjaSZvYWN1dGU7biBkZSBsb3MgbWlzbW9zLjwvcD4NCjxici8+DQo8cD5QYXJhIHRvZGEgYWN0dWFjaSZvYWN1dGU7biBxdWUgZXhjZWRhIGRlbCBjdW1wbGltaWVudG8gZGVsIGNvbnRyYXRvLCBlbCB1c3VhcmlvIG5lY2VzaXRhciZhYWN1dGU7IGF1dG9yaXphY2kmb2FjdXRlO24gcG9yIGVzY3JpdG8gcG9yIHBhcnRlIGRlPC9wPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LCBxdWVkYW5kbyBwcm9oaWJpZG8gYWwgdXN1YXJpbyBhY2NlZGVyLCBtb2RpZmljYXIsIHZpc3VhbGl6YXIgbGEgY29uZmlndXJhY2kmb2FjdXRlO24sIGVzdHJ1Y3R1cmEgeSBmaWNoZXJvcyBkZSBsb3Mgc2Vydmlkb3JlcyBwcm9waWVkYWQgZGU8c3Ryb25nPiBDVUJBTkFQUDwvc3Ryb25nPiwgYXN1bWllbmRvIGxhIHJlc3BvbnNhYmlsaWRhZCBjaXZpbCB5IHBlbmFsIGRlcml2YWRhIGRlIGN1YWxxdWllciBpbmNpZGVuY2lhIHF1ZSBzZSBwdWRpZXJhIHByb2R1Y2lyIGVuIGxvcyBzZXJ2aWRvcmVzIHkgc2lzdGVtYXMgZGUgc2VndXJpZGFkIGNvbW8gY29uc2VjdWVuY2lhIGRpcmVjdGEgZGUgdW5hIGFjdHVhY2kmb2FjdXRlO24gbmVnbGlnZW50ZSBvIG1hbGljaW9zYSBwb3Igc3UgcGFydGUuPC9wPg0KPGJyLz4NCjxici8+DQo8aDE+UHJvcGllZGFkIGludGVsZWN0dWFsIGRlIGxvcyBjb250ZW5pZG9zIGFsb2phZG9zPC9oMT4NCjxici8+DQo8cD5TZSBwcm9oJmlhY3V0ZTtiZSBlbCB1c28gY29udHJhcmlvIGEgbGEgbGVnaXNsYWNpJm9hY3V0ZTtuIHNvYnJlIHByb3BpZWRhZCBpbnRlbGVjdHVhbCBkZSBsb3Mgc2VydmljaW9zIHByZXN0YWRvcyBwb3I8L3A+DQo8cD48c3Ryb25nPkJvbGl0YSBDdWJhbmE8L3N0cm9uZz4geSwgZW4gcGFydGljdWxhciBkZTo8L3A+DQo8YnIvPg0KPHVsPg0KPGxpPkxhIHV0aWxpemFjaSZvYWN1dGU7biBxdWUgcmVzdWx0ZSBjb250cmFyaWEgYSBsYXMgbGV5ZXMgZXNwYSZudGlsZGU7b2xhcyBvIHF1ZSBpbmZyaW5qYSBsb3MgZGVyZWNob3MgZGUgdGVyY2Vyb3MuPC9saT4NCjxsaT5MYSBwdWJsaWNhY2kmb2FjdXRlO24gbyBsYSB0cmFuc21pc2kmb2FjdXRlO24gZGUgY3VhbHF1aWVyIGNvbnRlbmlkbyBxdWUsIGEganVpY2lvIGRlIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgcmVzdWx0ZSB2aW9sZW50bywgb2JzY2VubywgYWJ1c2l2bywgaWxlZ2FsLCByYWNpYWwsIHhlbiZvYWN1dGU7Zm9ibyBvPC9saT4NCjxsaT5Mb3MgY3JhY2tzLCBuJnVhY3V0ZTttZXJvcyBkZSBzZXJpZSBkZSBwcm9ncmFtYXMgbyBjdWFscXVpZXIgb3RybyBjb250ZW5pZG8gcXVlIHZ1bG5lcmUgZGVyZWNob3MgZGUgbGEgcHJvcGllZGFkIGludGVsZWN0dWFsIGRlPC9saT4NCjxsaT5MYSByZWNvZ2lkYSB5L28gdXRpbGl6YWNpJm9hY3V0ZTtuIGRlIGRhdG9zIHBlcnNvbmFsZXMgZGUgb3Ryb3MgdXN1YXJpb3Mgc2luIHN1IGNvbnNlbnRpbWllbnRvIGV4cHJlc28gbyBjb250cmF2aW5pZW5kbyBsbyBkaXNwdWVzdG8gZW4gUmVnbGFtZW50byAoVUUpIDIwMTYvNjc5IGRlbCBQYXJsYW1lbnRvIEV1cm9wZW8geSBkZWwgQ29uc2VqbywgZGUgMjcgZGUgYWJyaWwgZGUgMjAxNiwgcmVsYXRpdm8gYSBsYSBwcm90ZWNjaSZvYWN1dGU7biBkZSBsYXMgcGVyc29uYXMgZiZpYWN1dGU7c2ljYXMgZW4gbG8gcXVlIHJlc3BlY3RhIGFsIHRyYXRhbWllbnRvIGRlIGRhdG9zIHBlcnNvbmFsZXMgeSBhIGxhIGxpYnJlIGNpcmN1bGFjaSZvYWN1dGU7biBkZSBsb3MgbWlzbW9zLjwvbGk+DQo8bGk+TGEgdXRpbGl6YWNpJm9hY3V0ZTtuIGRlbCBzZXJ2aWRvciBkZSBjb3JyZW8gZGVsIGRvbWluaW8geSBkZSBsYXMgZGlyZWNjaW9uZXMgZGUgY29ycmVvIGVsZWN0ciZvYWN1dGU7bmljbyBwYXJhIGVsIGVudiZpYWN1dGU7byBkZSBjb3JyZW8gbWFzaXZvLjwvbGk+DQo8L3VsPg0KPGJyLz4NCjxwPkVsIHVzdWFyaW8gdGllbmUgdG9kYSBsYSByZXNwb25zYWJpbGlkYWQgc29icmUgZWwgY29udGVuaWRvIGRlIHN1IHdlYiwgbGEgaW5mb3JtYWNpJm9hY3V0ZTtuIHRyYW5zbWl0aWRhIHkgYWxtYWNlbmFkYSwgbG9zIGVubGFjZXMgZGUgaGlwZXJ0ZXh0bywgbGFzIHJlaXZpbmRpY2FjaW9uZXMgZGUgdGVyY2Vyb3MgeSBsYXMgYWNjaW9uZXMgbGVnYWxlcyBlbiByZWZlcmVuY2lhIGEgcHJvcGllZGFkIGludGVsZWN0dWFsLCBkZXJlY2hvcyBkZSB0ZXJjZXJvcyB5IHByb3RlY2NpJm9hY3V0ZTtuIGRlIG1lbm9yZXMuPC9wPg0KPGJyLz4NCjxwPkVsIHVzdWFyaW8gZXMgcmVzcG9uc2FibGUgcmVzcGVjdG8gYSBsYXMgbGV5ZXMgeSByZWdsYW1lbnRvcyBlbiB2aWdvciB5IGxhcyByZWdsYXMgcXVlIHRpZW5lbiBxdWUgdmVyIGNvbiBlbCBmdW5jaW9uYW1pZW50byBkZWwgc2VydmljaW8gb25saW5lLCBjb21lcmNpbyBlbGVjdHImb2FjdXRlO25pY28sIGRlcmVjaG9zIGRlIGF1dG9yLCBtYW50ZW5pbWllbnRvIGRlbCBvcmRlbiBwJnVhY3V0ZTtibGljbywgYXMmaWFjdXRlOyBjb21vIHByaW5jaXBpb3MgdW5pdmVyc2FsZXMgZGUgdXNvIGRlIEludGVybmV0LjwvcD4NCjxici8+DQo8cD5FbCB1c3VhcmlvIGRlYmUgaW5kZW1uaXphciZhYWN1dGU7IGEgPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IHBvciBsb3MgZ2FzdG9zIHF1ZSBnZW5lcmFyYSBsYSBpbXB1dGFjaSZvYWN1dGU7biBkZTwvcD4NCjxwPjxzdHJvbmc+Q1VCQU5BUFA8L3N0cm9uZz4gZW4gYWxndW5hIGNhdXNhIGN1eWEgcmVzcG9uc2FiaWxpZGFkIGZ1ZXJhIGF0cmlidWlibGUgYWwgdXN1YXJpbywgaW5jbHVpZG9zIGhvbm9yYXJpb3MgeSBnYXN0b3MgZGUgZGVmZW5zYSBqdXImaWFjdXRlO2RpY2EsIGluY2x1c28gZW4gZWwgY2FzbyBkZSB1bmEgZGVjaXNpJm9hY3V0ZTtuIGp1ZGljaWFsIG5vIGRlZmluaXRpdmEuPC9wPg0KPGJyLz4NCjxici8+DQo8aDE+UHJvdGVjY2kmb2FjdXRlO24gZGUgbGEgaW5mb3JtYWNpJm9hY3V0ZTtuIGFsb2phZGE8L2gxPg0KPGJyLz4NCjxwPjxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiByZWFsaXphIGNvcGlhcyBkZSBzZWd1cmlkYWQgZGUgbG9zIGNvbnRlbmlkb3MgYWxvamFkb3MgZW4gc3VzIHNlcnZpZG9yZXMsIHNpbiBlbWJhcmdvIG5vIHNlIHJlc3BvbnNhYmlsaXphIGRlIGxhIHAmZWFjdXRlO3JkaWRhIG8gZWwgYm9ycmFkbyBhY2NpZGVudGFsIGRlIGxvcyBkYXRvcyBwb3IgcGFydGUgZGUgbG9zIHVzdWFyaW9zLiBEZSBpZ3VhbCBtYW5lcmEsIG5vIGdhcmFudGl6YSBsYSByZXBvc2ljaSZvYWN1dGU7biB0b3RhbCBkZSBsb3MgZGF0b3MgYm9ycmFkb3MgcG9yIGxvcyB1c3VhcmlvcywgeWEgcXVlIGxvcyBjaXRhZG9zIGRhdG9zIHBvZHImaWFjdXRlO2FuIGhhYmVyIHNpZG8gc3VwcmltaWRvcyB5L28gbW9kaWZpY2Fkb3MgZHVyYW50ZSBlbCBwZXJpb2RvIGRlbCB0aWVtcG8gdHJhbnNjdXJyaWRvIGRlc2RlIGxhICZ1YWN1dGU7bHRpbWEgY29waWEgZGUgc2VndXJpZGFkLjwvcD4NCjxici8+DQo8cD5Mb3Mgc2VydmljaW9zIG9mZXJ0YWRvcywgZXhjZXB0byBsb3Mgc2VydmljaW9zIGVzcGVjJmlhY3V0ZTtmaWNvcyBkZSBiYWNrdXAsIG5vIGluY2x1eWVuIGxhIHJlcG9zaWNpJm9hY3V0ZTtuIGRlIGxvcyBjb250ZW5pZG9zIGNvbnNlcnZhZG9zIGVuIGxhcyBjb3BpYXMgZGUgc2VndXJpZGFkIHJlYWxpemFkYXMgcG9yIDxzdHJvbmc+Qm9saXRhIEN1YmFuYTwvc3Ryb25nPiwgY3VhbmRvIGVzdGEgcCZlYWN1dGU7cmRpZGEgc2VhIGltcHV0YWJsZSBhbCB1c3VhcmlvOyBlbiBlc3RlIGNhc28sIHNlIGRldGVybWluYXImYWFjdXRlOyB1bmEgdGFyaWZhIGFjb3JkZSBhIGxhIGNvbXBsZWppZGFkIHkgdm9sdW1lbiBkZSBsYSByZWN1cGVyYWNpJm9hY3V0ZTtuLCBzaWVtcHJlIHByZXZpYSBhY2VwdGFjaSZvYWN1dGU7biBkZWwgdXN1YXJpby48L3A+DQo8YnIvPg0KPHA+TGEgcmVwb3NpY2kmb2FjdXRlO24gZGUgZGF0b3MgYm9ycmFkb3MgcyZvYWN1dGU7bG8gZXN0JmFhY3V0ZTsgaW5jbHVpZGEgZW4gZWwgcHJlY2lvIGRlbCBzZXJ2aWNpbyBjdWFuZG8gbGEgcCZlYWN1dGU7cmRpZGEgZGVsIGNvbnRlbmlkbyBzZWEgZGViaWRhIGEgY2F1c2FzIGF0cmlidWlibGVzIGEgPHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+LjwvcD4NCjxici8+DQo8YnIvPg0KPGgxPkNvbXVuaWNhY2lvbmVzIGNvbWVyY2lhbGVzPC9oMT4NCjxici8+DQo8cD5FbiBhcGxpY2FjaSZvYWN1dGU7biBkZSBsYSBMU1NJLjxzdHJvbmc+IEJvbGl0YSBDdWJhbmE8L3N0cm9uZz4gbm8gZW52aWFyJmFhY3V0ZTsgY29tdW5pY2FjaW9uZXMgcHVibGljaXRhcmlhcyBvIHByb21vY2lvbmFsZXMgcG9yIGNvcnJlbyBlbGVjdHImb2FjdXRlO25pY28gdSBvdHJvIG1lZGlvIGRlIGNvbXVuaWNhY2kmb2FjdXRlO24gZWxlY3RyJm9hY3V0ZTtuaWNhIGVxdWl2YWxlbnRlIHF1ZSBwcmV2aWFtZW50ZSBubyBodWJpZXJhbiBzaWRvIHNvbGljaXRhZGFzIG8gZXhwcmVzYW1lbnRlIGF1dG9yaXphZGFzIHBvciBsb3MgZGVzdGluYXRhcmlvcyBkZSBsYXMgbWlzbWFzLjwvcD4NCjxici8+DQo8cD5FbiBlbCBjYXNvIGRlIHVzdWFyaW9zIGNvbiBsb3MgcXVlIGV4aXN0YSB1bmEgcmVsYWNpJm9hY3V0ZTtuIGNvbnRyYWN0dWFsIHByZXZpYSw8c3Ryb25nPiBCb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IHMmaWFjdXRlOyBlc3QmYWFjdXRlOyBhdXRvcml6YWRvIGFsIGVudiZpYWN1dGU7byBkZSBjb211bmljYWNpb25lcyBjb21lcmNpYWxlcyByZWZlcmVudGVzIGEgcHJvZHVjdG9zIG8gc2VydmljaW9zIGRlPC9wPg0KPHA+PHN0cm9uZz5Cb2xpdGEgQ3ViYW5hPC9zdHJvbmc+IHF1ZSBzZWFuIHNpbWlsYXJlcyBhIGxvcyBxdWUgaW5pY2lhbG1lbnRlIGZ1ZXJvbiBvYmpldG8gZGUgY29udHJhdGFjaSZvYWN1dGU7biBjb24gZWwgY2xpZW50ZS48L3A+DQo8YnIvPg0KPHA+RW4gdG9kbyBjYXNvLCBlbCB1c3VhcmlvLCB0cmFzIGFjcmVkaXRhciBzdSBpZGVudGlkYWQsIHBvZHImYWFjdXRlOyBzb2xpY2l0YXIgcXVlIG5vIHNlIGxlIGhhZ2EgbGxlZ2FyIG0mYWFjdXRlO3MgaW5mb3JtYWNpJm9hY3V0ZTtuIGNvbWVyY2lhbCBhIHRyYXYmZWFjdXRlO3MgZGUgbG9zIGNhbmFsZXMgZGUgQXRlbmNpJm9hY3V0ZTtuIGFsIENsaWVudGUuPC9wPg0KPC9ib2R5Pg0KPC9odG1sPg0K";
            //myWebView.loadData(privEng, "text/html; charset=UTF-8;", "base64");
            //myWebView.loadData(privSpa, "text/html; charset=UTF-8;", "base64");
            Locale s = Locale.getDefault();

            //Log.d(DEBUG_TAG, "Idioma: " + s.getDisplayLanguage());

            if (s.getLanguage().equals("es"))
                myWebView.loadData(privSpa, "text/html; charset=UTF-8;", "base64");
            else
                myWebView.loadData(privEng, "text/html; charset=UTF-8;", "base64");
        } catch (StringIndexOutOfBoundsException e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
        }
        //myWebView.loadUrl("http://cubanapp.info/api/prives.html");
        //myWebView.loadUrl("http://cubanapp.info/api/priv.html");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mySnackbar != null) {
            if (mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (builder != null) {
            bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.getString("notifMsg") != null) {
                    if (bundle.getString("notifTitle") != null) {
                        builder.setTitle(bundle.getString("notifTitle"));
                    }
                    builder.setMessage(bundle.getString("notifMsg"));
                    builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                    builder.show();
                }
                bundle.clear();
            }
        }
    }

    private void updateApp() {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            /*Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://cubanapp.info/BolitaCubana.apk"));*/
            startActivity(webIntent);
        }
    }

    private void openlink(String s) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(s));
        startActivity(webIntent);
    }

    /*public void showSnackbar(String s){
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.container),
                s, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }*/

    
    /*@Override
    public void onNewIntent(Intent intent){
        //called when a new intent for this class is created.
        // The main case is when the app was in background, a notification arrives to the tray, and the user touches the notification

        super.onNewIntent(intent);

        Log.d(DEBUG_TAG, "onNewIntent - starting");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(DEBUG_TAG, "Extras received at onNewIntent:  Key: " + key + " Value: " + value);
            }
            String title = extras.getString("title");
            String message = extras.getString("body");
            if (message!=null && message.length()>0) {
                getIntent().removeExtra("body");
                showNotificationInADialog(title, message);
            }
        }
    }


    private void showNotificationInADialog(String title, String message) {

        // show a dialog with the provided title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, whichButton) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }*/

    private void configureInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(DEBUG_TAG, "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(DEBUG_TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(DEBUG_TAG, "Ad failed to show fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(DEBUG_TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(DEBUG_TAG, "Ad showed fullscreen content.");
                }
            });
            /*Random r = new Random();
            int randomInt = r.nextInt(20 - 1) + 1;
            Log.i(DEBUG_TAG, "RANDOM: " + randomInt);
            if (randomInt == 3) {
                if (getApplicationContext() != null && mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                }
            }*/
        }
    }

    private boolean clearTempData(String dateString) throws IllegalStateException, IllegalArgumentException {
        if (context.getCacheDir() != null && getApplicationContext() != null) {
            deleteTempCache(context);
            SharedPreferences sharedPreferences = getSharedPreferences(
                    getString(R.string.preference_file_key2), Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.clear();

            TimeZone tz = TimeZone.getTimeZone("America/New_York");
            TimeZone.setDefault(tz);

            Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

            fecha.add(Calendar.SECOND, 5);

            edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
            edit.apply();
            SharedPreferences.Editor ditor = sharedPref.edit();
            ditor.putString("removeTemp", dateString);
            ditor.apply();
            Log.d(DEBUG_TAG, "clearTempData: Success");
            return true;
        } else {
            Log.d(DEBUG_TAG, "clearTempData: FAIL");
            return false;
        }
    }

    private static void deleteTempCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteTempDir(dir);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            if (Build.VERSION.SDK_INT >= 19) {
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
            }
        }
    }

    private static boolean deleteTempDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success;
                    try {
                        success = deleteTempDir(new File(dir, children[i]));
                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                        success = false;
                    }

                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else
                return false;
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
