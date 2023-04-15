/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class SevenDays extends Fragment {

    private FragmentSevendaysBinding binding;
    private Snackbar mySnackbar;
    private String apiKey;
    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    //private Set<String> lists;

    private SharedPreferences sharedPref;

    private final String DEBUG_TAG = "SevenDays";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiKey = BuildConfig.API_KEY;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SevenViewModel sevenViewModel =
                new ViewModelProvider(this).get(SevenViewModel.class);

        binding = FragmentSevendaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null)
            sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        //lists = sharedPref.getStringSet("sevenDays", null);
        checkUpdate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkUpdate() {
        Set<String> saved = null;

        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        Date currentTimes = fecha.getTime();

        SimpleDateFormat fechaFormato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String fechaString = fechaFormato.format(currentTimes);

        boolean update = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                Date fechaActual = fechaFormato.parse(fechaString);
                String sDiaGuardado = sharedPref.getString("sevenDUpdate", "2020-01-01");
                saved = sharedPref.getStringSet("ultimosSiete", null);
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
        }
        else{
            update = true;
        }
        /*if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            update = true;
        }*/


        if (binding != null) {
            binding.progressBar3.setVisibility(View.VISIBLE);
            //binding.linearSeven.removeAllViews();
        }


        if (update) {

            // TODO: Aquí la comprobación


            if (getActivity() != null) {
                requestQueue = Volley.newRequestQueue(getActivity());
            }
            //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
            if (requestQueue != null && binding != null) {
                binding.progressBar3.setVisibility(View.VISIBLE);

                String url = "https://cubanapp.info/api/suserinfo.php";
                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                    json.put("func", "old");
                } catch (JSONException e) {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                "Ha ocurrido un error al generar los datos", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
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


                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                Set<String> save = new ArraySet<String>(Collections.singleton(jsonArray.toString()));
                                                try {
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    JSONObject s = jsonArray.getJSONObject(0);
                                                    editor.putString("sevenDUpdate", (String) s.get("date"));
                                                    editor.putStringSet("ultimosSiete", save);
                                                    editor.apply();
                                                }
                                                catch (JSONException e){
                                                    throw new RuntimeException(e);
                                                }
                                            }

                                            buildSevenItems(jsonArray);
                                        } else {

                                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                    "Bolita Cubana encontró un error interno", Snackbar.LENGTH_LONG);
                                            mySnackbar.show();

                                        }
                                    }
                                } else {
                                    binding.progressBar3.setVisibility(View.GONE);
                                    if (getActivity() != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                "Bolita Cubana encontró un error interno", Snackbar.LENGTH_LONG);
                                        mySnackbar.show();
                                    }
                                }

                            } catch (JSONException e) {
                                if (binding != null)
                                    binding.progressBar3.setVisibility(View.GONE);
                                //errorStart.set(true);
                                //try {
                                if (getActivity() != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            "Ha ocurrido un error al obtener los datos", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
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
                        if (getActivity() != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    "Parece que su conexión está lenta", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
                            mySnackbar.show();
                        }
                        //} catch (Exception e) {
                        //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                        //}

                    } else {
                        //try {
                        if (getActivity() != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    "Se ha perdido la conexión con los servidores de Bolita Cubana", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> checkUpdate());
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
        } else{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                if (saved != null) {
                    try {
                        JSONArray jsonSaved = new JSONArray(saved.toArray());
                        String s = (String) jsonSaved.get(0);
                        JSONArray formatted = new JSONArray(s);
                        buildSevenItems(formatted);
                    }
                    catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                } else {
                    if (binding != null)
                        binding.progressBar3.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "onDestroy()");
        /*if (requestQueue != null) {
            requestQueue.cancelAll(stringRequest);
        }*/
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


        for (int i = 0; i < json.length(); i++) {
            if (binding == null || getActivity() == null)
                break;

            try {
                /*if (list != null)
                    list.add(json.getJSONObject(i).toString());*/

                JSONObject s = json.getJSONObject(i);

                //editor.putString(String.valueOf(i), s.toString());

                View header = getLayoutInflater().inflate(R.layout.sevenday_item, null);
                TextView fecha = header.findViewById(R.id.sFecha);
                TextView hora = header.findViewById(R.id.sD);
                TextView fijo1 = header.findViewById(R.id.sF1_0);
                TextView fijo2 = header.findViewById(R.id.sF1_1);
                TextView corrido1 = header.findViewById(R.id.sC1_1);
                TextView corrido2 = header.findViewById(R.id.sC1_2);

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
                    fijo2.setText(s0);
                } else {

                    try {
                        fijo1.setText(s0.substring(0, 1));
                        fijo2.setText(s0.substring(1, 3));
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new RuntimeException(e);
                    }
                }
                corrido1.setText((String) s.get("corrido1"));
                corrido2.setText((String) s.get("corrido2"));
                binding.linearSeven.addView(header);


            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "Error Building");
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (binding != null && getActivity() != null) {
            binding.progressBar3.setProgress(100);
            binding.progressBar3.setVisibility(View.GONE);
        }
    }
}
