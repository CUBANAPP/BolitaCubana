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

package com.cubanapp.bolitacubana.ui.home.sevendays;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentSevendaysBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SevenDaysFragment extends Fragment {

    private long mLastClick = 0;
    private FragmentSevendaysBinding binding;
    private Snackbar mySnackbar;
    private String apiKey;
    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private boolean customLastDays;

    private String nameJSON;

    private SharedPreferences sharedPref;

    //private SwipeRefreshLayout swipeRefreshLayout;

    private SevenData[] myListData;

    private RecyclerView recyclerView;

    private SevenDaysAdapter adapter;

    private static final String DEBUG_TAG = "SevenDaysFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSevendaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        apiKey = BuildConfig.API_KEY;

        customLastDays = false;

        sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        recyclerView = (RecyclerView) binding.linearSeven;

        recyclerView.setHasFixedSize(false);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));


        /*swipeRefreshLayout = binding.swipeRefreshLayoutSeven;
        swipeRefreshLayout.setColorSchemeColors(Color.RED);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    if (binding != null) {
                        if (!customLastDays) checkUpdate();
                        else swipeRefreshLayout.setRefreshing(false);
                    }
                });*/
        getParentFragmentManager().setFragmentResultListener("SevenDays", getViewLifecycleOwner(), (key, bundle) -> {
            if (bundle != null) {
                String name = bundle.getString("name", null);
                if (name != null) {
                    nameJSON = name;
                    customLastDays = true;
                    try {
                        runCustomUpdate();
                    } catch (IOException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
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
                    } catch (ParseException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                    }
                } else checkUpdate();
            }
        });


        return root;

    }

    @Override
    public void onDestroyView() {
        if (mySnackbar != null) {
            if (mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
        recyclerView = null;
        myListData = null;
        binding = null;
        super.onDestroyView();
    }

    private void runCustomUpdate() throws IOException, JSONException, ParseException {
        if (getActivity() != null && binding != null) {
            if (mySnackbar != null) {
                if (mySnackbar.isShown())
                    mySnackbar.dismiss();
            }
            if (requestQueue != null) {
                requestQueue.stop();
                if (stringRequest != null) {
                    stringRequest.cancel();
                }
            }
            if (nameJSON != null) {
                String jsonFile = readData(nameJSON);
                if (jsonFile != null) {
                    JSONObject jsonObject = new JSONObject(jsonFile);
                    Log.d(DEBUG_TAG, jsonObject.toString());
                    buildSevenCustom(jsonObject);
                }
            }
        }
    }

    private void checkUpdate() {

        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClick < 1000) {
            return;
        }
        mLastClick = SystemClock.elapsedRealtime();

        if (binding == null)
            return;
        String saved = null;

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        Date currentTimes = fecha.getTime();

        SimpleDateFormat fechaFormato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String fechaString = fechaFormato.format(currentTimes);

        boolean update = false;

        try {
            Date fechaActual = fechaFormato.parse(fechaString);
            String sDiaGuardado = sharedPref.getString("sevenDUpdate", "2020-01-01");
            saved = sharedPref.getString("lastSevenDays", null);
            Calendar c = Calendar.getInstance();
            c.setTime(fechaFormato.parse(sDiaGuardado));
            c.add(Calendar.DATE, 1);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
            String diaMas = fechaFormato.format(c.getTime());
            Date diaMasSaved = fechaFormato.parse(diaMas);


            if (!sDiaGuardado.equals(fechaActual) && diaMasSaved.before(fechaActual)) {
                update = true;
            } else {
                if (saved == null) {
                    update = true;
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
            throw new RuntimeException(e);
        }

        if (update && !customLastDays) {

            if (getActivity() != null && binding != null) {
                //swipeRefreshLayout.setRefreshing(true);
                requestQueue = Volley.newRequestQueue(getActivity());
            }

            if (requestQueue != null && binding != null) {
                String url;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    url = "https://cubanapp.info/api/suserinfo.php";
                } else {
                    url = "http://cubanapp.info/api/suserinfo.php";
                }

                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                    json.put("func", "old");
                } catch (JSONException e) {

                    if (getActivity() != null && binding != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
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
                // Request a string response from the provided URL.
                stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            JSONArray jsonArray = null;

                            try {

                                boolean error = (Boolean) response.get("error");
                                if (!error) {
                                    if (getActivity() != null && binding != null) {
                                        if (response.get("data") != null || response.get("data") != "") {
                                            jsonArray = (JSONArray) response.get("data");

                                            Log.d(DEBUG_TAG, "Response is: " + error);

                                            String text = jsonArray.toString();
                                            String base64 = null;
                                            byte[] data = new byte[0];
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                data = text.getBytes(StandardCharsets.UTF_8);
                                                base64 = Base64.encodeToString(data, Base64.DEFAULT);
                                            } else {
                                                try {
                                                    data = text.getBytes("UTF-8");
                                                } catch (UnsupportedEncodingException e) {
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
                                                if (data != null)
                                                    base64 = Base64.encodeToString(data, Base64.DEFAULT);
                                            }
                                            if (base64 != null) {
                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                JSONObject s = jsonArray.getJSONObject(0);

                                                editor.putString("sevenDUpdate", (String) s.get("date"));
                                                editor.putString("lastSevenDays", base64);
                                                editor.apply();
                                            }
                                            buildSevenItems(jsonArray);
                                        } else {
                                            if (getActivity() != null && binding != null) {
                                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                        getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                                mySnackbar.show();
                                            }
                                        }
                                    }
                                } else {
                                    //if (binding != null) swipeRefreshLayout.setRefreshing(false);
                                    if (getActivity() != null && !customLastDays && binding != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                        mySnackbar.show();
                                    }
                                }

                            } catch (JSONException e) {
                                //if (binding != null) swipeRefreshLayout.setRefreshing(false);
                                if (getActivity() != null && !customLastDays && binding != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
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
                    //if (binding != null) swipeRefreshLayout.setRefreshing(false);

                    if (error instanceof TimeoutError) {

                        if (getActivity() != null && !customLastDays && binding != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                            mySnackbar.show();
                        }

                    } else {

                        if (getActivity() != null && !customLastDays && binding != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                            mySnackbar.show();
                        }
                    }
                    Log.e(DEBUG_TAG, "ERROR");
                }

                );
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                        3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // Add the request to the RequestQueue.
                requestQueue.add(stringRequest);

            }
        } else {
            if (saved != null && !customLastDays) {

                JSONArray formatted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    byte[] data = Base64.decode(saved, Base64.DEFAULT);
                    String text = new String(data, StandardCharsets.UTF_8);
                    try {
                        formatted = new JSONArray(text);
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
                    if (formatted != null)
                        buildSevenItems(formatted);
                } else {
                    byte[] data = Base64.decode(saved, Base64.DEFAULT);
                    String text = null;
                    try {
                        text = new String(data, "UTF-8");
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
                    if (text != null) {
                        try {
                            formatted = new JSONArray(text);
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
                        if (formatted != null)
                            buildSevenItems(formatted);
                    }
                }
            } else {
                //if (binding != null) swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void buildSevenItems(@NonNull JSONArray json) {
        if (binding == null)
            return;
        else if (binding != null) myListData = null;
        SimpleDateFormat dSemana = new SimpleDateFormat("EEEE", Locale.getDefault());

        String root = json.toString();
        Log.d(DEBUG_TAG, "JSON: " + root);

        for (int i = 0; i < json.length(); i++) {
            if (binding == null || getActivity() == null || customLastDays)
                break;

            try {
                JSONObject s = json.getJSONObject(i);

                /*View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                TextView fecha = header.findViewById(R.id.sFecha);
                TextView hora = header.findViewById(R.id.sD);
                TextView fijo1 = header.findViewById(R.id.sF1_0);
                TextView fijo2 = header.findViewById(R.id.sF1_1);
                TextView corrido1 = header.findViewById(R.id.sC1_1);
                TextView corrido2 = header.findViewById(R.id.sC1_2);*/
                String fecha = "";
                String hora = "";
                String fijo1 = "";
                String fijo2 = "";
                String corrido1 = "";
                String corrido2 = "";


                SimpleDateFormat get = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date d = get.parse((String) s.get("date"));
                fecha = dSemana.format(d);
                int iHora = 0;

                if (((String) s.get("hora")).equals("dia")) {
                    hora = getString(R.string.dia);
                } else {
                    hora = getString(R.string.noche);
                    iHora = 2;
                }
                String s0 = (String) s.get("fijo");
                if (s0.length() < 3) {
                    fijo1 = "0";
                    fijo2 = s0;
                } else {

                    try {
                        fijo1 = s0.substring(0, 1);
                        fijo2 = s0.substring(1, 3);
                    } catch (StringIndexOutOfBoundsException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                        throw new RuntimeException(e);
                    }
                }
                String co1 = (String) s.get("corrido1");
                String co2 = (String) s.get("corrido2");
                if (co1.length() != 2)
                    co1 = "0" + co1;
                corrido1 = co1;
                if (co2.length() != 2)
                    co2 = "0" + co2;
                corrido2 = co2;
                //binding.linearSeven.addView(header);
                buildAdapter(fecha, hora, fijo1, fijo2, corrido1, corrido2, iHora);


            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "Error Building");
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
                break;
            } catch (ParseException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
                break;
            } catch (IllegalStateException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
                break;
            }
        }
        //if (binding != null && getActivity() != null) swipeRefreshLayout.setRefreshing(false);
    }

    private void buildSevenCustom(@NonNull JSONObject json) throws JSONException, ParseException {
        if (binding == null)
            return;
        else if (binding != null) myListData = null;
        //binding.linearSeven.removeAllViews();

        SimpleDateFormat dSemana = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat get = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        JSONArray mid = null;
        JSONArray eve = null;
        JSONArray night = null;
        JSONArray mid4 = null;
        JSONArray eve4 = null;
        JSONArray night4 = null;

        if (json.has("mid")) {
            mid = json.getJSONArray("mid");
        }
        if (json.has("eve")) {
            eve = json.getJSONArray("eve");
        }
        if (json.has("night")) {
            night = json.getJSONArray("night");
        }
        if (json.has("mid4")) {
            mid4 = json.getJSONArray("mid4");
        }
        if (json.has("eve4")) {
            eve4 = json.getJSONArray("eve4");
        }
        if (json.has("night4")) {
            night4 = json.getJSONArray("night4");
        }

        if (mid != null) {
            for (int i = 0; i < mid.length(); i++) {
                if (binding == null || getActivity() == null)
                    break;

                /*View header0 = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                TextView fecha0 = header0.findViewById(R.id.sFecha);
                TextView hora0 = header0.findViewById(R.id.sD);
                TextView fijo10 = header0.findViewById(R.id.sF1_0);
                TextView fijo20 = header0.findViewById(R.id.sF1_1);
                TextView corrido10 = header0.findViewById(R.id.sC1_1);
                TextView corrido20 = header0.findViewById(R.id.sC1_2);*/

                JSONObject s1 = mid.getJSONObject(i);

                /*View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                TextView fecha = header.findViewById(R.id.sFecha);
                TextView hora = header.findViewById(R.id.sD);
                TextView fijo1 = header.findViewById(R.id.sF1_0);
                TextView fijo2 = header.findViewById(R.id.sF1_1);
                TextView corrido1 = header.findViewById(R.id.sC1_1);
                TextView corrido2 = header.findViewById(R.id.sC1_2);*/
                String fecha0 = "";
                String hora0 = "";
                String fijo10 = "";
                String fijo20 = "";
                String corrido10 = "";
                String corrido20 = "";

                Date d0 = get.parse((String) s1.get("fecha"));
                fecha0 = dSemana.format(d0);

                hora0 = getString(R.string.dia);

                String s0m = (String) s1.get("num");
                if (s0m.length() < 3) {
                    fijo10 = "0";
                    fijo20 = s0m;
                } else {

                    try {
                        fijo10 = s0m.substring(0, 1);
                        fijo20 = s0m.substring(1, 3);
                    } catch (StringIndexOutOfBoundsException e) {
                        if (e.getMessage() != null) {
                            Log.e(DEBUG_TAG, e.getMessage());
                        }
                        if (Build.VERSION.SDK_INT >= 19) {
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                        throw new RuntimeException(e);
                    }
                }
                if (mid4 != null) {
                    if (!mid4.isNull(i)) {
                        JSONObject s4 = mid4.getJSONObject(i);
                        String compFecha = (String) s4.get("fecha");
                        if (compFecha.equals((String) s1.get("fecha"))) {
                            String co0m = (String) s4.get("num");
                            String co1m = co0m.substring(0, 2);
                            String co2m = co0m.substring(2, 4);
                            corrido10 = co1m;
                            corrido20 = co2m;
                        }
                    }
                }
                buildAdapter(fecha0, hora0, fijo10, fijo20, corrido10, corrido20, 0);

                if (eve != null) {
                    if (!eve.isNull(i)) {
                        /*View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                        TextView fecha = header.findViewById(R.id.sFecha);
                        TextView hora = header.findViewById(R.id.sD);
                        TextView fijo1 = header.findViewById(R.id.sF1_0);
                        TextView fijo2 = header.findViewById(R.id.sF1_1);
                        TextView corrido1 = header.findViewById(R.id.sC1_1);
                        TextView corrido2 = header.findViewById(R.id.sC1_2);*/
                        String fecha = "";
                        String hora = "";
                        String fijo1 = "";
                        String fijo2 = "";
                        String corrido1 = "";
                        String corrido2 = "";


                        JSONObject s2 = eve.getJSONObject(i);

                        Date d = get.parse((String) s2.get("fecha"));
                        fecha = dSemana.format(d);

                        hora = getString(R.string.tarde);

                        String s0 = (String) s2.get("num");
                        if (s0.length() < 3) {
                            fijo1 = "0";
                            fijo2 = s0;
                        } else {

                            try {
                                fijo1 = s0.substring(0, 1);
                                fijo2 = s0.substring(1, 3);
                            } catch (StringIndexOutOfBoundsException e) {
                                if (e.getMessage() != null) {
                                    Log.e(DEBUG_TAG, e.getMessage());
                                }
                                if (Build.VERSION.SDK_INT >= 19) {
                                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                    firebaseCrashlytics.sendUnsentReports();
                                    firebaseCrashlytics.recordException(e);
                                }
                                throw new RuntimeException(e);
                            }
                        }
                        if (eve4 != null) {
                            if (!eve4.isNull(i)) {
                                JSONObject s5 = eve4.getJSONObject(i);
                                String compFecha = (String) s5.get("fecha");
                                if (compFecha.equals((String) s2.get("fecha"))) {
                                    String co0 = (String) s5.get("num");
                                    String co1 = co0.substring(0, 2);
                                    String co2 = co0.substring(2, 4);
                                    corrido1 = co1;
                                    corrido2 = co2;
                                }
                            }
                        }
                        buildAdapter(fecha, hora, fijo1, fijo2, corrido1, corrido2, 1);
                    }
                }
                if (night != null) {
                    if (!night.isNull(i)) {
                        /*View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                        TextView fecha = header.findViewById(R.id.sFecha);
                        TextView hora = header.findViewById(R.id.sD);
                        TextView fijo1 = header.findViewById(R.id.sF1_0);
                        TextView fijo2 = header.findViewById(R.id.sF1_1);
                        TextView corrido1 = header.findViewById(R.id.sC1_1);
                        TextView corrido2 = header.findViewById(R.id.sC1_2);*/
                        String fecha = "";
                        String hora = "";
                        String fijo1 = "";
                        String fijo2 = "";
                        String corrido1 = "";
                        String corrido2 = "";


                        JSONObject s3 = night.getJSONObject(i);

                        Date d = get.parse((String) s3.get("fecha"));
                        fecha = dSemana.format(d);

                        hora = getString(R.string.noche);
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hora.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                            fecha.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                        } else {
                            hora.setTextColor(getResources().getColor(R.color.navy_blue));
                            fecha.setTextColor(getResources().getColor(R.color.navy_blue));
                        }*/
                        String s0 = (String) s3.get("num");
                        if (s0.length() < 3) {
                            fijo1 = "0";
                            fijo2 = s0;
                        } else {

                            try {
                                fijo1 = s0.substring(0, 1);
                                fijo2 = s0.substring(1, 3);
                            } catch (StringIndexOutOfBoundsException e) {
                                if (e.getMessage() != null) {
                                    Log.e(DEBUG_TAG, e.getMessage());
                                }
                                if (Build.VERSION.SDK_INT >= 19) {
                                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                    firebaseCrashlytics.sendUnsentReports();
                                    firebaseCrashlytics.recordException(e);
                                }
                                throw new RuntimeException(e);
                            }
                        }
                        if (night4 != null) {
                            if (!night4.isNull(i)) {
                                JSONObject s6 = night4.getJSONObject(i);
                                String compFecha = (String) s6.get("fecha");
                                if (compFecha.equals((String) s6.get("fecha"))) {
                                    String co0 = (String) s6.get("num");
                                    String co1 = co0.substring(0, 2);
                                    String co2 = co0.substring(2, 4);
                                    corrido1 = co1;
                                    corrido2 = co2;
                                }
                            }
                        }
                        buildAdapter(fecha, hora, fijo1, fijo2, corrido1, corrido2, 2);
                    }
                }
                Log.d(DEBUG_TAG, s1.toString());
            }
        }
        //if (binding != null && getActivity() != null) swipeRefreshLayout.setRefreshing(false);
    }

    private String readData(String name) throws IOException {

        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            if (!dataFile.exists()) {
                Log.e(DEBUG_TAG, "File do not Exist");
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            if (customLastDays) {
                if (nameJSON != null) {
                    outState.putString("nameJSON", nameJSON);
                    outState.putBoolean("customLastDays", true);
                }
            } else {
                outState.putBoolean("customLastDays", false);
            }
        } catch (NullPointerException e) {
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                buildView(savedInstanceState);
            } catch (IOException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
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
            } catch (ParseException e) {
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
        super.onViewStateRestored(savedInstanceState);
    }

    private void buildView(@Nullable Bundle bundle) throws IOException, JSONException, ParseException {
        if (bundle != null) {
            customLastDays = bundle.getBoolean("customLastDays", false);
            if (customLastDays) {
                nameJSON = bundle.getString("nameJSON", null);
                if (nameJSON != null) {
                    String jsonFile = readData(nameJSON);
                    if (jsonFile != null) {
                        JSONObject jsonObject = new JSONObject(jsonFile);
                        Log.d(DEBUG_TAG, jsonObject.toString());
                        buildSevenCustom(jsonObject);
                    }
                }
            } else {
                checkUpdate();
            }
        }
    }

    private void buildAdapter(String s1, String s2, String s3, String s4, String s5, String s6, int s7) {

        SevenData inputNew = new SevenData(s1, s2, s3, s4, s5, s6, s7);
        ArrayList<SevenData> tempData = new ArrayList<>();

        if (myListData != null) {
            for (SevenData item : myListData) {
                tempData.add(item);

            }
        }
        tempData.add(inputNew);

        myListData = tempData.toArray(new SevenData[0]);

        adapter = new SevenDaysAdapter(myListData);
        if (binding != null && getActivity() != null)
            recyclerView.setAdapter(adapter);
    }
}
