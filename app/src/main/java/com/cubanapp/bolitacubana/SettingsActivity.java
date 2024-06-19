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

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsActivity extends AppCompatActivity {

    private boolean main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        main = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            main = bundle.getBoolean("main");
        } else {
            main = false;
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(main);
        }
    }

    /*public void loadForm() {
        // Loads a consent form. Must be called on the main thread.

        UserMessagingPlatform.loadConsentForm(
                this,
                consentForm -> {
                    SettingsActivity.this.consentForm = consentForm;
                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(
                                SettingsActivity.this,
                                formError -> {
                                    if (binding != null) {
                                        if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                            // App can start requesting ads.
                                            //initializeMobileAdsSdk();
                                            Log.d(DEBUG_TAG, "consentInformation: OBTAINED");
                                        } else {
                                            //initializeMobileAdsSdk();
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
                        //initializeMobileAdsSdk();
                    }
                }
        );
    }*/
    public static class SettingsFragment extends PreferenceFragmentCompat {

        private long mLastClickTime = 0;
        private long mLastClickTime0 = 0;
        private long mLastClickTime1 = 0;
        private long mLastClickTime2 = 0;
        private long mLastClickTime3 = 0;
        private long mLastClickTime4 = 0;
        private long mLastClickEraseTime = 0;
        private SwitchPreferenceCompat mpromoChannel;
        private SwitchPreferenceCompat mcubanappChannel;
        private SwitchPreferenceCompat mdefaultChannel;
        private SwitchPreferenceCompat mnewyorkChannel;
        private SwitchPreferenceCompat mgeorgiaChannel;

        private ListPreference listPreference;

        //private Preference erasePreference;

        //private Preference adsPreference;

        private ListPreference erase;
        private ListPreference mdarkmode;

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            mLastClickEraseTime = SystemClock.elapsedRealtime();
            mLastClickTime = SystemClock.elapsedRealtime();
            mLastClickTime0 = SystemClock.elapsedRealtime();
            mLastClickTime1 = SystemClock.elapsedRealtime();
            mLastClickTime2 = SystemClock.elapsedRealtime();
            mLastClickTime3 = SystemClock.elapsedRealtime();
            mLastClickTime4 = SystemClock.elapsedRealtime();

            erase = (ListPreference) getPreferenceManager().findPreference("erase");

            listPreference = (ListPreference) getPreferenceManager().findPreference("languagepreference");

            //erasePreference = (Preference) getPreferenceManager().findPreference("resetapp");
            //adsPreference = (Preference) getPreferenceManager().findPreference("adsconfig");

            if (getActivity() != null) {
                mpromoChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference(getString(R.string.promotional));
                mcubanappChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference(getString(R.string.cubanapp_channel_name));
            }

            mdefaultChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("floridaChannel");
            mnewyorkChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("newyorkChannel");
            mgeorgiaChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("georgiaChannel");
            mdarkmode = (ListPreference) getPreferenceManager().findPreference("thememodeselector");
            AtomicBoolean response = new AtomicBoolean(false);
            if (erase != null) {
                erase.setOnPreferenceChangeListener((preference, newvelue) -> {

                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickEraseTime < 1000) {
                        return false;
                    }
                    mLastClickEraseTime = SystemClock.elapsedRealtime();

                    try {
                        response.set(clearData());
                    } catch (IllegalStateException e) {
                        //
                        if (e.getMessage() != null) {
                            Log.e(TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage() != null) {
                            Log.e(TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                    }
                    if (getActivity() != null) {
                        if (response.get()) {
                            Toast.makeText(getActivity(), R.string.completed, Toast.LENGTH_SHORT).show();
                            getActivity().recreate();
                        } else
                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    return response.get();
                });
            }
            if (listPreference != null) {
                listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime0 < 500) {
                        return false;
                    }
                    mLastClickTime0 = SystemClock.elapsedRealtime();

                    if (getActivity() != null) {
                        Locale config = new Locale(newValue.toString());
                        Locale.setDefault(config);
                        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(newValue.toString());
                        // Call this on the main thread as it may require Activity.restart()
                        AppCompatDelegate.setApplicationLocales(appLocale);
                    }
                    return true;
                });
            }
            /*if (erasePreference != null) {
                erasePreference.setOnPreferenceClickListener(v -> clickedPreference());
            }
            if (adsPreference != null) {
                adsPreference.setOnPreferenceClickListener(v -> clickedPreference());
            }*/
            if (mdefaultChannel != null) {
                mdefaultChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                        return false;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "floridaChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("Florida").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("Florida").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        Log.d(TAG, "Config Changed: " + preference.getKey());
                        return true;
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
            if (mcubanappChannel != null) {
                mcubanappChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime2 < 500) {
                        return false;
                    }
                    mLastClickTime2 = SystemClock.elapsedRealtime();

                    FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                    if (Objects.equals(preference.getKey(), getString(R.string.cubanapp_channel_name_topic))) {
                        if (newValue.equals(false)) {
                            if (getActivity() != null) {
                                mFirebaseMessages.unsubscribeFromTopic(getString(R.string.cubanapp_channel_name_topic)).addOnCompleteListener(task -> {

                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);

                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            if (getActivity() != null) {
                                mFirebaseMessages.subscribeToTopic(getString(R.string.cubanapp_channel_name_topic)).addOnCompleteListener(task -> {

                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);

                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }
                    Log.d(TAG, "Config Changed: " + preference.getKey());
                    return true;
                });
            }
            if (mpromoChannel != null) {
                mpromoChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime3 < 500) {
                        return false;
                    }
                    mLastClickTime3 = SystemClock.elapsedRealtime();

                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), getString(R.string.promotional_topic))) {
                            if (newValue.equals(false)) {
                                if (getActivity() != null) {
                                    mFirebaseMessages.unsubscribeFromTopic(getString(R.string.promotional_topic)).addOnCompleteListener(task -> {

                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);

                                        //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                if (getActivity() != null) {
                                    mFirebaseMessages.subscribeToTopic(getString(R.string.promotional_topic)).addOnCompleteListener(task -> {

                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);

                                        //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        }
                        Log.d(TAG, "Config Changed: " + preference.getKey());
                        return true;

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
            if (mgeorgiaChannel != null) {
                mgeorgiaChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime4 < 500) {
                        return false;
                    }
                    mLastClickTime4 = SystemClock.elapsedRealtime();

                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "georgiaChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("Georgia").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("Georgia").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        Log.d(TAG, "Config Changed: " + preference.getKey());
                        return true;

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
            if (mnewyorkChannel != null) {
                mnewyorkChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                    // mis-clicking prevention, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime1 < 500) {
                        return false;
                    }
                    mLastClickTime1 = SystemClock.elapsedRealtime();

                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "newyorkChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("NewYork").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("NewYork").addOnCompleteListener(task -> {
                                    if (getActivity() != null) {
                                        String msg = getString(R.string.msg_subscribed);
                                        if (!task.isSuccessful()) {
                                            msg = getString(R.string.msg_subscribe_failed);
                                        }
                                        Log.d(TAG, msg);
                                    }
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        Log.d(TAG, "Config Changed: " + preference.getKey());
                        return true;

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
            if (mdarkmode != null) {
                mdarkmode.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (getActivity() != null) {

                        // mis-clicking prevention, using threshold of 1000 ms
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                            return false;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        if (Build.VERSION.SDK_INT >= 17) {
                            if (Objects.equals(newValue, "light")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                }
                                return true;
                            } else if (Objects.equals(newValue, "dark")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                }
                                return true;
                            } else if (Objects.equals(newValue, "system")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                }
                                return true;
                            } else
                                return false;

                        } else {
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.sdk17, Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    } else
                        return false;
                });
            }
        }

        /*private boolean clickedPreference() {
            Log.e(TAG, "Clicked");
            return true;
        }*/

        @SuppressLint("ApplySharedPref")
        public boolean clearData() throws IllegalStateException, IllegalArgumentException {
            if (getActivity() != null) {

                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (getActivity().getCacheDir() != null) {
                    deleteCache(getActivity());
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                            getString(R.string.preference_file_key2), Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.clear();

                    TimeZone tz = TimeZone.getTimeZone("America/New_York");
                    TimeZone.setDefault(tz);

                    Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

                    fecha.add(Calendar.SECOND, 5);

                    //edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                    //edit.commit();
                    SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit2 = sharedPreferences2.edit();
                    edit2.clear();
                    edit2.commit();
                    Log.d(TAG, "clearData: Success");
                    return true;
                } else {
                    Log.d(TAG, "clearData: FAIL");
                    return false;
                }
            } else {
                Log.d(TAG, "clearData2: FAIL");
                return false;
            }
        }

        public static void deleteCache(Context context) {
            try {
                File dir = context.getCacheDir();
                deleteDir(dir);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
            }
        }

        public static boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        boolean success;
                        try {
                            success = deleteDir(new File(dir, children[i]));
                        } catch (Exception e) {
                            if (e.getMessage() != null) {
                                Log.e(TAG, e.getMessage());
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
}
