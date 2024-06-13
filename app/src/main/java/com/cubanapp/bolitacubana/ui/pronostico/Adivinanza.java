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

package com.cubanapp.bolitacubana.ui.pronostico;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.MainActivity;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentAdivinanzasBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class Adivinanza extends Fragment implements AdivinanzaAdapter.AdivinanzaView.PhotoListener {

    private long mLastClickTime = 0;

    private long mLastClickSnackTime = 0;

    private FragmentAdivinanzasBinding binding;

    private SharedPreferences sharedPref;

    private Snackbar mySnackbar;
    private String apiKey;
    private JsonObjectRequest stringRequest;

    private ArrayList<String> keyNames;
    private RequestQueue requestQueue;

    private JSONObject filenames;

    private Set<String> nFiles;

    private PronosticoData[] myListData;

    private RecyclerView recyclerView;

    private AdivinanzaAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isClicked;

    private boolean forceDownload;

    private static final String DEBUG_TAG = "Adivinanza";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdivinanzasBinding.inflate(inflater, container, false);
        apiKey = BuildConfig.API_KEY;
        keyNames = new ArrayList<>();
        forceDownload = false;
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLastClickTime = SystemClock.elapsedRealtime();

        nFiles = null;
        if (getActivity() != null) {
            if (Build.VERSION.SDK_INT >= 19)
                showAds();
            if (binding != null) {

                isClicked = false;

                recyclerView = (RecyclerView) binding.recicleradivinanza;

                recyclerView.setHasFixedSize(false);
                //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

                swipeRefreshLayout = binding.adivinanzaLayout;
                swipeRefreshLayout.setColorSchemeColors(Color.RED);
                swipeRefreshLayout.setOnRefreshListener(
                        () -> {
                            if (binding != null) {
                                try {
                                    getSaves();
                                } catch (JSONException e) {
                                    if (binding != null)
                                        swipeRefreshLayout.setRefreshing(false);
                                    if (e.getMessage() != null) {
                                        Log.e(DEBUG_TAG, e.getMessage());
                                    }
                                    if (Build.VERSION.SDK_INT >= 19) {
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    if (binding != null)
                                        swipeRefreshLayout.setRefreshing(false);
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
                        });
            }

            sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key2), Context.MODE_PRIVATE);

            try {
                getSaves();
            } catch (JSONException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
            } catch (UnsupportedEncodingException e) {
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
    }

    private void getSaves() throws JSONException, UnsupportedEncodingException {

        String saves = sharedPref.getString("filenames", null);
        nFiles = sharedPref.getStringSet("trueFiles", null);

        if (saves != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                byte[] data = Base64.decode(saves, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                Log.d(DEBUG_TAG, "SAVED: " + text);
                filenames = new JSONObject(text);
            } else {
                byte[] data = Base64.decode(saves, Base64.DEFAULT);
                String text = new String(data, "UTF-8");
                Log.d(DEBUG_TAG, "SAVED: " + text);
                filenames = new JSONObject(text);
            }
            try {
                loadFile(filenames);
            } catch (UnsupportedEncodingException e0) {
                if (e0.getMessage() != null) {
                    Log.e(DEBUG_TAG, e0.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e0);
                }
            } catch (JSONException e1) {
                if (e1.getMessage() != null) {
                    Log.e(DEBUG_TAG, e1.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e1);
                }
            } catch (IOException e2) {
                if (e2.getMessage() != null) {
                    Log.e(DEBUG_TAG, e2.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e2);
                }
            }
        }

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        long i = sharedPref.getLong("checkUpdateImages", 0);
        if (fecha.getTimeInMillis() > i) {
            Log.i(DEBUG_TAG, "YA PASO 15m");
            if (binding != null)
                swipeRefreshLayout.setRefreshing(true);
            downloadData();
        } else {
            Log.i(DEBUG_TAG, "NO PASO 15m");
            if (nFiles != null) {
                //downloadData();
                Log.w(DEBUG_TAG, "Pero hay descargas pendientes, nFiles no NULL");
            } else if (forceDownload) {
                if (binding != null)
                    swipeRefreshLayout.setRefreshing(true);
                downloadData();
            } else if (binding != null) swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setSaves(JSONObject files) throws UnsupportedEncodingException {
        SharedPreferences.Editor edit = sharedPref.edit();
        String text = files.toString();
        String base64;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } else {
            byte[] data = text.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        }
        filenames = files;
        edit.putString("filenames", base64);

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        fecha.add(Calendar.SECOND, 5);

        edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
        edit.apply();
    }

    private boolean needUpdateFile(String x, Integer z) throws JSONException {
        boolean res = false;
        if (filenames != null) {
            JSONArray jsonArray = filenames.names();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String key = jsonArray.getString(i);
                    if (Objects.equals(x, key)) {
                        if (z != null) {
                            int value = filenames.getInt(key);
                            if (value < z) {
                                res = true;
                                break;
                            } else {
                                if (nFiles != null)
                                    if (!nFiles.contains(x)) {
                                        res = true;
                                        break;
                                    }
                            }
                        }
                    }
                }
            }
        } else {
            res = true;
        }
        return res;
    }

    @Override
    public void onDestroyView() {
        if (mySnackbar != null) {
            if (mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (requestQueue != null) {
            try {
                requestQueue.cancelAll(stringRequest);
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

            requestQueue.stop();

            //if (stringRequest != null) {
            //    stringRequest.cancel();
            //}
        }
        binding = null;
        recyclerView = null;
        adapter = null;
        //mInterstitialAd = null;
        super.onDestroyView();
    }

    private void downloadData() {

        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickSnackTime < 500) {
            return;
        }
        mLastClickSnackTime = SystemClock.elapsedRealtime();

        if (getActivity() != null && binding != null) {
            keyNames.clear();
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null && binding != null && getActivity() != null) {
            binding.textViewProgress.setVisibility(View.VISIBLE);
            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/photo/index.php";
            } else {
                url = "http://cubanapp.info/api/photo/index.php";
            }
            //String url = "https://cubanapp.info/api/suserinfo.php";
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
            } catch (JSONException e) {
                //try {
                if (getActivity() != null && binding != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                    mySnackbar.show();
                }
                //} catch (Exception ei) {
                // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                //}
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
                //throw new RuntimeException(e);
                //startLaunch(false);
                swipeRefreshLayout.setRefreshing(false);
                binding.textViewProgress.setVisibility(View.GONE);
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        try {
                            if (getActivity() != null && binding != null) {
                                if (response.toString() != null) {
                                    Iterator<String> keys = response.keys();
                                    boolean error = false;
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        if (key.equals("error")) {
                                            error = true;
                                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                    response.getString("msg"), Snackbar.LENGTH_LONG);
                                            mySnackbar.show();
                                            swipeRefreshLayout.setRefreshing(false);
                                            binding.textViewProgress.setVisibility(View.GONE);
                                            break;
                                        } else {
                                            if (filenames != null) {
                                                if (filenames.has(key)) {
                                                    if (needUpdateFile(key, response.getInt(key))) {
                                                        keyNames.add(key);
                                                        Log.i(DEBUG_TAG, "File Updated: " + key);
                                                    }

                                                } else {
                                                    Log.i(DEBUG_TAG, "File Added: " + key);
                                                    keyNames.add(key);
                                                }
                                            } else {
                                                Log.i(DEBUG_TAG, "No savegame, File Added: " + key);
                                                keyNames.add(key);
                                            }
                                        }
                                    }
                                    if (!error) {
                                        if (keyNames.size() > 0) {
                                            Log.d(DEBUG_TAG, "Descarga nueva");
                                            // TODO: Guardar sólo los archivos descargados correctamente si no se omite.
                                            setSaves(response);
                                            //filenames = response;
                                            String msg = getString(R.string.filesdownload) + " 0 / " + keyNames.size();
                                            if (getActivity() != null && binding != null) {

                                                binding.textViewProgress.setText(msg);

                                            }

                                            downloadFiles(keyNames);

                                        } else { // Nada nuevo que descargar
                                            Log.i(DEBUG_TAG, "Nada nuevo que descargar");
                                            swipeRefreshLayout.setRefreshing(false);
                                            binding.textViewProgress.setVisibility(View.GONE);
                                            SharedPreferences.Editor edit = sharedPref.edit();

                                            TimeZone tz = TimeZone.getTimeZone("America/New_York");
                                            TimeZone.setDefault(tz);

                                            Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

                                            fecha.add(Calendar.MINUTE, 15);

                                            edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                                            edit.apply();
                                        }

                                    }


                                    //Log.d(DEBUG_TAG, "Response is: " + response);

                                } else {
                                    /*if (getActivity() != null && binding != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                        mySnackbar.show();
                                    }*/
                                    if (binding != null && getActivity() != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                        binding.textViewProgress.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            if (binding != null) {
                                swipeRefreshLayout.setRefreshing(false);
                                binding.textViewProgress.setVisibility(View.GONE);
                            }
                            if (getActivity() != null && binding != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
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
                        } catch (UnsupportedEncodingException e) {
                            if (binding != null) {
                                swipeRefreshLayout.setRefreshing(false);
                                binding.textViewProgress.setVisibility(View.GONE);
                            }
                            if (getActivity() != null && binding != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
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
                        }

                    }, error -> {
                if (binding != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    binding.textViewProgress.setVisibility(View.GONE);
                }

                if (error instanceof TimeoutError) {
                    //try {
                    /*if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                        mySnackbar.show();
                    }*/
                    //} catch (Exception e) {
                    //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                    //}

                } else {
                    //try {
                    /*if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                        mySnackbar.show();
                    }*/
                    //} catch (Exception e) {
                    //   Log.e(DEBUG_TAG, "SnackbarError5 : " + e.getMessage());
                    //}
                }
                Log.e(DEBUG_TAG, "ERROR");
            });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
        }
    }

    private void downloadFiles(ArrayList<String> files) throws UnsupportedEncodingException {
        if (files.get(0) == null)
            return;
        if (Objects.equals(files.get(0), ""))
            return;

        if (getActivity() != null && binding != null) {
            swipeRefreshLayout.setRefreshing(true);
            requestQueue = Volley.newRequestQueue(getActivity());
        }

        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null) {
            SharedPreferences.Editor edit = sharedPref.edit();
            TimeZone tz = TimeZone.getTimeZone("America/New_York");
            TimeZone.setDefault(tz);

            Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/photo/index.php";
            } else {
                url = "http://cubanapp.info/api/photo/index.php";
            }
            ArrayList<JsonObjectRequest> jsonObjectRequestArrayList = new ArrayList<>();
            //ArrayList<String> filesSuccess = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {

                int d = i;
                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                    json.put("file", files.get(i));
                } catch (JSONException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                }
                // Request a string response from the provided URL.
                stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            try {
                                if (getActivity() != null && binding != null) {
                                    if (!response.has("error")) {
                                        if (response.has("type")) {
                                            String type = response.getString("type");
                                            Log.d(DEBUG_TAG, type);
                                            String base64 = response.getString("base64");
                                            Log.d(DEBUG_TAG, base64);
                                            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);

                                            String msg = getString(R.string.filesdownload) + " " + (d + 1) + " / " + keyNames.size();
                                            if (binding != null) {

                                                binding.textViewProgress.setText(msg);

                                            }

                                            // TODO: Guardar esto para despues?
                                            if (recyclerView != null && getActivity() != null && binding != null) {
                                                try {
                                                    buildAdapter(files.get(d), type, decodedString);
                                                } catch (IndexOutOfBoundsException e) {
                                                    if (e.getMessage() != null) {
                                                        Log.e(DEBUG_TAG, e.getMessage());
                                                    }
                                                    if (Build.VERSION.SDK_INT >= 19) {
                                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                                        firebaseCrashlytics.sendUnsentReports();
                                                        firebaseCrashlytics.recordException(e);
                                                    }
                                                } catch (IllegalStateException e) {
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

                                            if (d == (files.size() - 1)) {
                                                saveFile(files.get(d), response, true);


                                                fecha.add(Calendar.MINUTE, 15);

                                                edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                                                edit.apply();

                                                if (binding != null && getActivity() != null) {
                                                    swipeRefreshLayout.setRefreshing(false);
                                                    binding.textViewProgress.setVisibility(View.GONE);
                                                }
                                            } else {
                                                try {
                                                    saveFile(files.get(d), response, false);
                                                } catch (IndexOutOfBoundsException e) {
                                                    if (e.getMessage() != null) {
                                                        Log.e(DEBUG_TAG, e.getMessage());
                                                    }
                                                    if (Build.VERSION.SDK_INT >= 19) {
                                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                                        firebaseCrashlytics.sendUnsentReports();
                                                        firebaseCrashlytics.recordException(e);
                                                    }
                                                } catch (IllegalStateException e) {
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

                                        }

                                    } else {
                                        if (d == (files.size() - 1)) {
                                            fecha.add(Calendar.SECOND, 2);

                                            edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                                            edit.apply();

                                            if (binding != null) {
                                                swipeRefreshLayout.setRefreshing(false);
                                                binding.textViewProgress.setVisibility(View.GONE);
                                            }
                                        }
                                        //filesSuccess.remove(files.get(d));
                                        Log.i(DEBUG_TAG, "No response");
                                    }
                                }
                            } catch (JSONException e) {
                                if (d == (files.size() - 1)) {
                                    fecha.add(Calendar.SECOND, 2);

                                    edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                                    edit.apply();
                                    if (binding != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                        binding.textViewProgress.setVisibility(View.GONE);
                                    }
                                }
                                //filesSuccess.remove(files.get(d));
                                if (e.getMessage() != null) {
                                    Log.e(DEBUG_TAG, e.getMessage());
                                }
                                if (Build.VERSION.SDK_INT >= 19) {
                                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                    firebaseCrashlytics.sendUnsentReports();
                                    firebaseCrashlytics.recordException(e);
                                }
                            } catch (UnsupportedEncodingException e) {
                                if (d == (files.size() - 1))
                                    if (binding != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                        binding.textViewProgress.setVisibility(View.GONE);
                                    }
                                if (e.getMessage() != null) {
                                    Log.e(DEBUG_TAG, e.getMessage());
                                }
                                if (Build.VERSION.SDK_INT >= 19) {
                                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                    firebaseCrashlytics.sendUnsentReports();
                                    firebaseCrashlytics.recordException(e);
                                }
                                //throw new RuntimeException(e);
                            } catch (IOException e) {
                                if (d == (files.size() - 1))
                                    if (binding != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                        binding.textViewProgress.setVisibility(View.GONE);
                                    }
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


                        }, error -> {
                    Log.e(DEBUG_TAG, "ERROR");
                    if (d == (files.size() - 1)) {
                        fecha.add(Calendar.SECOND, 2);

                        edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                        edit.apply();
                        if (binding != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            binding.textViewProgress.setVisibility(View.GONE);
                        }
                    }
                });

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                        3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // Add the request to the RequestQueue.
                jsonObjectRequestArrayList.add(stringRequest);

            }

            if (jsonObjectRequestArrayList.size() > 0)
                for (JsonObjectRequest s : jsonObjectRequestArrayList) {
                    requestQueue.add(s);
                }

        }
    }

    private void buildAdapter(String name, String tipo, byte[] bytes) {
        //myListData = new PronosticoData[]{new PronosticoData(name, tipo, bytes)};

        PronosticoData inputNew = new PronosticoData(name, tipo, bytes);
        //PronosticoData[] newData = myListData;
        ArrayList<PronosticoData> tempData = new ArrayList<>();

        if (myListData != null) {
            for (PronosticoData item : myListData) {
                tempData.add(item);

            }
        }
        tempData.add(inputNew);

        myListData = tempData.toArray(new PronosticoData[0]);

        adapter = new AdivinanzaAdapter(myListData, this);
        if (binding != null && getActivity() != null)
            recyclerView.setAdapter(adapter);
    }

    private void saveFile(String filename, JSONObject file, boolean last) throws IOException {
        SharedPreferences.Editor edit = sharedPref.edit();
        if (!last) {
            if (nFiles == null) {
                nFiles = new HashSet<>();
                nFiles.add(filename);
            } else
                nFiles.add(filename);
        } else
            nFiles = null;
        String text = file.toString();
        /*String base64;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } else {
            byte[] data = text.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        }*/

        cacheData(text, filename);
        edit.putStringSet("trueFiles", nFiles);
        edit.apply();

    }

    @SuppressLint("ApplySharedPref")
    private void loadFile(JSONObject file) throws IOException, JSONException {

        JSONArray jsonArray = file.names();
        if (jsonArray != null) {

            ArrayList<PronosticoData> tempData = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                String key = jsonArray.getString(i);

                String saves = readData(key);

                if (saves != null) {
                    JSONObject jsonFile = new JSONObject(saves);

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            byte[] data = Base64.decode(saves, Base64.DEFAULT);
                            String text = new String(data, StandardCharsets.UTF_8);
                            jsonFile = new JSONObject(text);
                        } else {
                            byte[] data = Base64.decode(saves, Base64.DEFAULT);
                            String text = new String(data, "UTF-8");
                            jsonFile = new JSONObject(text);
                        }*/
                    //Log.e(DEBUG_TAG, "File loaded: " + jsonFile);
                    String type = jsonFile.getString("type");
                    String base64 = jsonFile.getString("base64");
                    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                    tempData.add(new PronosticoData(key, type, decodedString));
                } else {
                    //TODO: DOWNLOAD OR BREAK? WHEN FILE DO NOT EXIST?
                    //downloadData();
                    Log.d(DEBUG_TAG, "EJECUCION DEL BREAK");
                    break;
                }
            }

            if (tempData.size() > 0 && recyclerView != null && getActivity() != null && binding != null) {
                myListData = tempData.toArray(new PronosticoData[0]);

                adapter = new AdivinanzaAdapter(myListData, this);
                //recyclerView.setHasFixedSize(false);
                //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

                recyclerView.setAdapter(adapter);

            } else if (tempData.isEmpty() && recyclerView != null && getActivity() != null && binding != null) {
                forceDownload = true;
                filenames = null;
                //sharedPref.edit().putString("filenames", null).commit();
                Log.d(DEBUG_TAG, "forceDownload");
            }
        }
    }

    private void showAds() {
        if (getActivity() != null && binding != null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                if (mainActivity.mInterstitialAd != null)
                    mainActivity.mInterstitialAd.show(getActivity());
                else
                    Log.d(DEBUG_TAG, "AD Still Not Loaded");
            } else
                Log.w(DEBUG_TAG, "MainActivity NOT FOUND");
        } else
            Log.w(DEBUG_TAG, "NO ACTIVITY FOUND");
    }

    @Override
    public void onItemClick(int position, byte[] bytes, String name, String type) {
        if (getActivity() != null && binding != null) {

            // mis-clicking prevention, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();


            Bundle bundle = new Bundle();
            bundle.putByteArray("base64", bytes);
            bundle.putString("name", name);
            bundle.putString("type", type);
            getParentFragmentManager().setFragmentResult("CUBANAPPImage", bundle);
            if (binding != null && !isClicked && getActivity() != null) {
                try {
                    isClicked = true;
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_adivinanza_to_navigation_imagefullscreen, bundle);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                    //
                } catch (IllegalStateException e) {
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
        }
    }

    public void cacheData(String data, String name) throws IOException {
        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            OutputStreamWriter objectOutputStream = new OutputStreamWriter(
                    new FileOutputStream(dataFile));
            BufferedWriter bufferedWriter = new BufferedWriter(objectOutputStream);
            bufferedWriter.write(data);
            bufferedWriter.close();
        }

    }

    public String readData(String name) throws IOException {

        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            if (!dataFile.exists()) {
                Log.w(DEBUG_TAG, "File do not Exist");
                return null;
            }
            FileInputStream fileInputStream = new FileInputStream(dataFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String data = bufferedReader.readLine();
            bufferedReader.close();
            return data;
        } else
            return null;
    }
}
