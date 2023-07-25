/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SevenDaysFragment extends Fragment {

    private FragmentSevendaysBinding binding;
    private Snackbar mySnackbar;
    private String apiKey;
    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private boolean customLastDays;

    private String nameJSON;

    //private Set<String> lists;

    private SharedPreferences sharedPref;

    private static final String DEBUG_TAG = "SevenDaysFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiKey = BuildConfig.API_KEY;
        customLastDays = false;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*SevenViewModel sevenViewModel =
                new ViewModelProvider(this).get(SevenViewModel.class);
*/
        binding = FragmentSevendaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        getParentFragmentManager().setFragmentResultListener("SevenDays", getViewLifecycleOwner(), (key, bundle) -> {
            if (bundle != null) {
                String name = bundle.getString("name", null);
                if (name != null) {
                    nameJSON = name;
                    customLastDays = true;
                    try {
                        //Log.e(DEBUG_TAG, "runCustomUpdate()");
                        runCustomUpdate();
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                    } catch (JSONException e){
                        //throw new RuntimeException(e);
                    } catch (ParseException e) {
                        //throw new RuntimeException(e);
                    }
                }
            }
        });


        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null)
            sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        checkUpdate();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        binding = null;
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
            throw new RuntimeException(e);
        }


        if (binding != null) {
            binding.progressBar3.setVisibility(View.VISIBLE);
            //binding.linearSeven.removeAllViews();
        }


        if (update && !customLastDays) {


            if (getActivity() != null && binding != null) {
                requestQueue = Volley.newRequestQueue(getActivity());
            }
            //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
            if (requestQueue != null && binding != null) {
                binding.progressBar3.setVisibility(View.VISIBLE);
                String url;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    url = "https://cubanapp.info/api/suserinfo.php";
                } else {
                    url = "http://cubanapp.info/api/suserinfo.php";
                }
                //String url = "https://cubanapp.info/api/suserinfo.php";
                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                    json.put("func", "old");
                } catch (JSONException e) {
                    //try {
                    if (getActivity() != null && binding != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                        mySnackbar.show();
                    }
                    //} catch (Exception ei) {
                    // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                    //}
                    Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
                    //throw new RuntimeException(e);
                    //startLaunch(false);
                    binding.progressBar3.setVisibility(View.GONE);
                }
                // Request a string response from the provided URL.
                stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            JSONArray jsonArray = null;
                            // Display the first 500 characters of the response string.
                            try {
                                //JSONObject error = response.getJSONObject("");
                                //response.get("error");
                                boolean error = (Boolean) response.get("error");
                                if (!error) {
                                    if (getActivity() != null && binding != null) {
                                        binding.progressBar3.setProgress(40);
                                        if (response.get("data") != null || response.get("data") != "") {
                                            jsonArray = (JSONArray) response.get("data");

                                            Log.d(DEBUG_TAG, "Response is: " + error);


                                            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                Set<String> save = new ArraySet<>(Collections.singleton(jsonArray.toString()));
                                                try {
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    JSONObject s = jsonArray.getJSONObject(0);
                                                    editor.putString("sevenDUpdate", (String) s.get("date"));
                                                    editor.putStringSet("ultimosSiete", save);
                                                    editor.apply();
                                                }
                                                catch (JSONException e){
                                                    //throw new RuntimeException(e);
                                                }
                                            }*/

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
                                    binding.progressBar3.setVisibility(View.GONE);
                                    if (getActivity() != null && !customLastDays && binding != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                        mySnackbar.show();
                                    }
                                }

                            } catch (JSONException e) {
                                if (binding != null)
                                    binding.progressBar3.setVisibility(View.GONE);
                                //errorStart.set(true);
                                //try {
                                if (getActivity() != null && !customLastDays && binding != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                                    mySnackbar.show();
                                }
                                // } catch (Exception ei) {
                                //    Log.e(DEBUG_TAG, "SnackbarError3 : " + ei.getMessage());
                                //}
                                Log.e(DEBUG_TAG, "JSONException2 : " + e.getMessage());
                                //throw new RuntimeException(e);
                            }

                        }, error -> {
                    if (binding != null)
                        binding.progressBar3.setVisibility(View.GONE);

                    if (error instanceof TimeoutError) {
                        //try {
                        if (getActivity() != null && !customLastDays && binding != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                            mySnackbar.show();
                        }
                        //} catch (Exception e) {
                        //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                        //}

                    } else {
                        //try {
                        if (getActivity() != null && !customLastDays && binding != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                            mySnackbar.show();
                        }
                        //} catch (Exception e) {
                        //   Log.e(DEBUG_TAG, "SnackbarError5 : " + e.getMessage());
                        //}
                    }
                    Log.e(DEBUG_TAG, "ERROR");
                }

                );
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                        3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // Add the request to the RequestQueue.
                requestQueue.add(stringRequest);
                if (binding != null)
                    binding.progressBar3.setProgress(20);
            }
        } else {
            if (saved != null && !customLastDays) {
                JSONArray formatted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    byte[] data = Base64.decode(saved, Base64.DEFAULT);
                    String text = new String(data, StandardCharsets.UTF_8);
                    //Log.d(DEBUG_TAG, "SAVED: " + text);
                    try {
                        formatted = new JSONArray(text);
                    } catch (JSONException e) {
                        //throw new RuntimeException(e);
                    }
                    if (formatted != null)
                        buildSevenItems(formatted);
                } else {
                    byte[] data = Base64.decode(saved, Base64.DEFAULT);
                    String text = null;
                    try {
                        text = new String(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //throw new RuntimeException(e);
                    }
                    if (text != null) {
                        try {
                            formatted = new JSONArray(text);
                        } catch (JSONException e) {
                            //throw new RuntimeException(e);
                        }
                        if (formatted != null)
                            buildSevenItems(formatted);
                    }
                }
            } else {
                if (binding != null)
                    binding.progressBar3.setVisibility(View.GONE);
            }
        }
    }

    private void buildSevenItems(@NonNull JSONArray json) {
        /*ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        //AtomicBoolean errorStart = new AtomicBoolean(false);
        executor.execute(() -> {*/
        //json.length()
        SimpleDateFormat dSemana = new SimpleDateFormat("EEEE", Locale.getDefault());

        String root = json.toString();
        Log.d(DEBUG_TAG, "JSON: " + root);

        Typeface font = null;
        if (binding != null && getActivity() != null && !customLastDays)
            font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");

        for (int i = 0; i < json.length(); i++) {
            if (binding == null || getActivity() == null || customLastDays)
                break;

            try {
                /*if (list != null)
                    list.add(json.getJSONObject(i).toString());*/

                JSONObject s = json.getJSONObject(i);

                //editor.putString(String.valueOf(i), s.toString());

                View header = getLayoutInflater().inflate(R.layout.sevenday_item, null,false);
                TextView fecha = header.findViewById(R.id.sFecha);
                TextView hora = header.findViewById(R.id.sD);
                TextView fijo1 = header.findViewById(R.id.sF1_0);
                TextView fijo2 = header.findViewById(R.id.sF1_1);
                TextView corrido1 = header.findViewById(R.id.sC1_1);
                TextView corrido2 = header.findViewById(R.id.sC1_2);

                fecha.setTypeface(font);
                hora.setTypeface(font);
                /*fijo1.setTypeface(font);
                fijo2.setTypeface(font);
                corrido1.setTypeface(font);
                corrido2.setTypeface(font);*/

                SimpleDateFormat get = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                //get.parse((String) s.get("date"));
                Date d = get.parse((String) s.get("date"));
                fecha.setText(dSemana.format(d));

                if (((String) s.get("hora")).equals("dia")) {
                    hora.setText(getString(R.string.dia));
                } else {
                    hora.setText(getString(R.string.noche));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hora.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                        fecha.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                    } else {
                        hora.setTextColor(getResources().getColor(R.color.navy_blue));
                        fecha.setTextColor(getResources().getColor(R.color.navy_blue));
                    }
                }
                String s0 = (String) s.get("fijo");
                if (s0.length() < 3) {
                    fijo1.setText("0");
                    fijo2.setText(s0);
                } else {

                    try {
                        fijo1.setText(s0.substring(0, 1));
                        fijo2.setText(s0.substring(1, 3));
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new RuntimeException(e);
                    }
                }
                String co1 = (String) s.get("corrido1");
                String co2 = (String) s.get("corrido2");
                if(co1.length() != 2)
                    co1 = "0" + co1;
                corrido1.setText(co1);
                if(co2.length() != 2)
                    co2 = "0" + co2;
                corrido2.setText(co2);
                binding.linearSeven.addView(header);


            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "Error Building");
                //throw new RuntimeException(e);
                break;
            } catch (ParseException e) {
                //throw new RuntimeException(e);
                break;
            }
            catch (IllegalStateException e) {
                break;
            }
        }
        if (binding != null && getActivity() != null) {
            binding.progressBar3.setProgress(100);
            binding.progressBar3.setVisibility(View.GONE);
        }
    }

    private void buildSevenCustom(@NonNull JSONObject json) throws JSONException, ParseException {
        binding.progressBar3.setVisibility(View.VISIBLE);
        binding.linearSeven.removeAllViews();
        //binding.linearSeven.removeAllViewsInLayout();

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

        Typeface font = null;
        if (binding != null && getActivity() != null)
            font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");

        if (mid != null) {
            for (int i = 0; i < mid.length(); i++) {
                if (binding == null || getActivity() == null)
                    break;

                View header0 = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                TextView fecha0 = header0.findViewById(R.id.sFecha);
                TextView hora0 = header0.findViewById(R.id.sD);
                TextView fijo10 = header0.findViewById(R.id.sF1_0);
                TextView fijo20 = header0.findViewById(R.id.sF1_1);
                TextView corrido10 = header0.findViewById(R.id.sC1_1);
                TextView corrido20 = header0.findViewById(R.id.sC1_2);

                fecha0.setTypeface(font);
                hora0.setTypeface(font);

                JSONObject s1 = mid.getJSONObject(i);

                Date d0 = get.parse((String) s1.get("fecha"));
                fecha0.setText(dSemana.format(d0));

                hora0.setText(getString(R.string.dia));

                String s0m = (String) s1.get("num");
                if (s0m.length() < 3) {
                    fijo10.setText("0");
                    fijo20.setText(s0m);
                } else {

                    try {
                        fijo10.setText(s0m.substring(0, 1));
                        fijo20.setText(s0m.substring(1, 3));
                    } catch (StringIndexOutOfBoundsException e) {
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
                            corrido10.setText(co1m);
                            corrido20.setText(co2m);
                        }
                    }
                }
                binding.linearSeven.addView(header0);

                if (eve != null) {
                    if (!eve.isNull(i)) {
                        View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                        TextView fecha = header.findViewById(R.id.sFecha);
                        TextView hora = header.findViewById(R.id.sD);
                        TextView fijo1 = header.findViewById(R.id.sF1_0);
                        TextView fijo2 = header.findViewById(R.id.sF1_1);
                        TextView corrido1 = header.findViewById(R.id.sC1_1);
                        TextView corrido2 = header.findViewById(R.id.sC1_2);

                        fecha.setTypeface(font);
                        hora.setTypeface(font);

                        JSONObject s2 = eve.getJSONObject(i);

                        Date d = get.parse((String) s2.get("fecha"));
                        fecha.setText(dSemana.format(d));

                        hora.setText(getString(R.string.tarde));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hora.setTextColor(getResources().getColor(R.color.holo_red, requireContext().getTheme()));
                            fecha.setTextColor(getResources().getColor(R.color.holo_red, requireContext().getTheme()));
                        } else {
                            hora.setTextColor(getResources().getColor(R.color.holo_red));
                            fecha.setTextColor(getResources().getColor(R.color.holo_red));
                        }
                        String s0 = (String) s2.get("num");
                        if (s0.length() < 3) {
                            fijo1.setText("0");
                            fijo2.setText(s0);
                        } else {

                            try {
                                fijo1.setText(s0.substring(0, 1));
                                fijo2.setText(s0.substring(1, 3));
                            } catch (StringIndexOutOfBoundsException e) {
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
                                    corrido1.setText(co1);
                                    corrido2.setText(co2);
                                }
                            }
                        }
                        binding.linearSeven.addView(header);
                    }
                }
                if (night != null) {
                    if (!night.isNull(i)) {
                        View header = getLayoutInflater().inflate(R.layout.sevenday_item, null, false);
                        TextView fecha = header.findViewById(R.id.sFecha);
                        TextView hora = header.findViewById(R.id.sD);
                        TextView fijo1 = header.findViewById(R.id.sF1_0);
                        TextView fijo2 = header.findViewById(R.id.sF1_1);
                        TextView corrido1 = header.findViewById(R.id.sC1_1);
                        TextView corrido2 = header.findViewById(R.id.sC1_2);

                        fecha.setTypeface(font);
                        hora.setTypeface(font);

                        JSONObject s3 = night.getJSONObject(i);

                        Date d = get.parse((String) s3.get("fecha"));
                        fecha.setText(dSemana.format(d));

                        hora.setText(getString(R.string.noche));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hora.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                            fecha.setTextColor(getResources().getColor(R.color.navy_blue, requireContext().getTheme()));
                        } else {
                            hora.setTextColor(getResources().getColor(R.color.navy_blue));
                            fecha.setTextColor(getResources().getColor(R.color.navy_blue));
                        }
                        String s0 = (String) s3.get("num");
                        if (s0.length() < 3) {
                            fijo1.setText("0");
                            fijo2.setText(s0);
                        } else {

                            try {
                                fijo1.setText(s0.substring(0, 1));
                                fijo2.setText(s0.substring(1, 3));
                            } catch (StringIndexOutOfBoundsException e) {
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
                                    corrido1.setText(co1);
                                    corrido2.setText(co2);
                                }
                            }
                        }
                        binding.linearSeven.addView(header);
                    }
                }
                Log.d(DEBUG_TAG, s1.toString());
            }
        }
        if (binding != null && getActivity() != null) {
            binding.progressBar3.setProgress(100);
            binding.progressBar3.setVisibility(View.GONE);
        }
    }
    private String readData(String name) throws IOException {

        if(getActivity() != null && getContext() != null && binding != null) {
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
        }
        else
            return null;
    }
}
