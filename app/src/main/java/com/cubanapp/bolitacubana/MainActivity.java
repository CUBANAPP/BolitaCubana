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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, R.string.notificationdgranted,Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, R.string.notificationdeclined,
                            Toast.LENGTH_LONG).show();
                }
            });
    public static final String TAG = "Check";
    private static final String DEBUG_TAG = "MainActivity";
    private JsonObjectRequest stringRequest; // Assume this exists.
    private RequestQueue requestQueue;  // Assume this exists.
    private Context context;

    private Snackbar mySnackbar;
    private SharedPreferences sharedPref;
    private ActivityMainBinding binding;
	private AppBarConfiguration appBarConfiguration;
    private AlertDialog builder;
    private String apiKey;
    private boolean first;

    private boolean connection;

    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);


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
        if(bundle != null) {
            connection = bundle.getBoolean("connection");
            first = bundle.getBoolean("first");
            message = bundle.getString("msg");
            if(message != null && !message.equals("")) {
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
        }else{
            first = true;
            message = "";
            connection = false;
        }

        if(Build.VERSION.SDK_INT >= 19) {
            AdView adView = (AdView) findViewById(R.id.adView);
            //FirebaseApp.initializeApp(this);
            //FirebaseOptions.Builder firebaseOptions = new FirebaseOptions.Builder();
            if(connection) {
                FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
                firebaseMessaging.getToken().addOnCompleteListener(v -> Log.d(DEBUG_TAG, "FCM Key: " + v.getResult()));
            }
            //FirebaseMessaging.getInstance(FirebaseApp.initializeApp(this));
            MobileAds.initialize(this, initializationStatus -> {
                Log.d(DEBUG_TAG, "Ads Running");
                adView.setVisibility(View.VISIBLE);
            });
            RequestConfiguration.Builder adRequestBuilder = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("27257B0AF4890D7241E824CB06C35D83"));
            AdRequest adRequest = new AdRequest.Builder().build();
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


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navController.enableOnBackPressed(false);

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
    }
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
        String url = "https://cubanapp.info/api/chksvr.php";
        JSONObject json = new JSONObject();

        try {
            json.put("apiKey", apiKey);
        } catch (JSONException e) {
            if (getApplication() != null && requestQueue != null) {
                mySnackbar = Snackbar.make(findViewById(R.id.container),
                        "Ha ocurrido un error al generar los datos", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
            throw new RuntimeException(e);
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
                                editor.putBoolean("fix", fix);
                                editor.putBoolean("ads", ads);
                                editor.putString("fecha", fecha);
                                editor.putString("hora", hora);
                                Log.d(DEBUG_TAG, "Response is: " + error);
                                Log.i(DEBUG_TAG, "Version is: " + version);
                                editor.apply();
                                if (version.get() > BuildConfig.VERSION_CODE) {
                                    if (builder != null) {
                                        builder.setTitle(getString(R.string.obsolete));
                                        builder.setMessage(getString(R.string.update));
                                        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> updateApp());
                                        builder.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
                                        builder.show();
                                    }
                                }
                                else if (!Objects.equals(message, "")) {
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
                            } else {
                                if (getApplication() != null && requestQueue != null) {
                                    mySnackbar = Snackbar.make(findViewById(R.id.container),
                                            "Bolita Cubana encontró un error interno", Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                }
                            }
                        } catch (JSONException e) {
                            //errorStart.set(true);
                            Log.e(DEBUG_TAG, "JSON ERROR");
                            if (getApplication() != null && requestQueue != null) {
                                mySnackbar = Snackbar.make(findViewById(R.id.container),
                                        "Ha ocurrido un error grave al sincronizar", Snackbar.LENGTH_LONG);
                                mySnackbar.show();
                            }
                            throw new RuntimeException(e);
                        }

                    }, error -> {

                if (error instanceof TimeoutError) {
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
                }
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
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!first)
            startSync();
    }
	@Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main); // R.id.nav_host_fragment_content_main
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
            Intent i = new Intent(MainActivity.this,SettingsActivity.class);
            i.putExtra("main", true);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void updateApp(){
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
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
                    Uri.parse("https://play.google.com/store/apps/details?id="+appId));
            startActivity(webIntent);
        }
    }
    private void openlink(String s){
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(s));
        startActivity(webIntent);
    }

    /*public void showSnackbar(String s){
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.container),
                s, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }*/

}