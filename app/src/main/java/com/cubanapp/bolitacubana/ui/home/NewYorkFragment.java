/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentNewyorkBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NewYorkFragment extends Fragment {

    private String apiKey;
    private FragmentNewyorkBinding binding;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private SharedPreferences sharedPref;

    private Snackbar mySnackbar;
    private static final String DEBUG_TAG = "NewYorkFragment";

    /*public static FloridaFragment newInstance() {
        return new FloridaFragment();
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiKey = BuildConfig.API_KEY;


        if(getActivity() != null)
            sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        //FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        //firebaseMessaging.getToken().addOnCompleteListener(v -> Log.d(DEBUG_TAG, "FCM Key: " + v.getResult()));
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        /*mViewModel =
                new ViewModelProvider(this).get(FloridaViewModel.class);
        */
        binding = FragmentNewyorkBinding.inflate(inflater, container, false);
        if(binding != null && binding.button31.isClickable()) {
            try {
                binding.button31.setOnClickListener(view1 -> {
                    binding.button31.setClickable(false);
                    //NavHostFragment.findNavController(this).navigate(R.id.action_fragment_georgia_to_fragment_sevendays);
                    if (getActivity() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", "newyorkSavedFile");
                        getParentFragmentManager().setFragmentResult("SevenDays", bundle);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_fragment_newyork_to_fragment_sevendays, bundle);
                    }
                });
            } catch (IllegalArgumentException e) {
                //
            }
        }
        View root = binding.getRoot();
        return root;

    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Typeface font = null;
        if (binding != null && getActivity() != null)
            font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");

        if (binding != null) {
            String savedFechaString = sharedPref.getString("updateCheckDate3", null);
            if(savedFechaString != null)
                binding.updateDate.setText(savedFechaString);
            if (font != null) {
                binding.titleny.setTypeface(font);
                binding.D1.setTypeface(font);
                binding.D.setTypeface(font);
                binding.SD.setTypeface(font);
                binding.N1.setTypeface(font);
                binding.N.setTypeface(font);
                binding.SN.setTypeface(font);
            }

            String fijo1 = sharedPref.getString("nyF1", "---");
            String fijo2 = sharedPref.getString("nyF2", "---");

            binding.D1.setText(sharedPref.getString("nyD", "--/--/----"));
            binding.D.setText(getString(R.string.dia));
            binding.SD.setText(sharedPref.getString("nyDS", "-"));
            if (fijo1.length() < 3) {
                binding.F11.setText(fijo1);
            } else {
                try {
                    binding.F10.setText(fijo1.substring(0, 1));
                    binding.F11.setText(fijo1.substring(1, 3));
                } catch (StringIndexOutOfBoundsException e) {
                    throw new RuntimeException(e);
                }
            }
            binding.C11.setText(sharedPref.getString("nyCD1", "--"));
            binding.C12.setText(sharedPref.getString("nyCD2", "--"));

            binding.N1.setText(sharedPref.getString("nyN", "--/--/----"));
            binding.N.setText(getString(R.string.noche));
            binding.SN.setText(sharedPref.getString("nyNS", "-"));
            if (fijo2.length() < 3) {
                binding.F21.setText(fijo2);
            } else {
                try {
                    binding.F20.setText(fijo2.substring(0, 1));
                    binding.F21.setText(fijo2.substring(1, 3));
                } catch (StringIndexOutOfBoundsException e) {
                    throw new RuntimeException(e);
                }
            }
            binding.C21.setText(sharedPref.getString("nyCN1", "--"));
            binding.C22.setText(sharedPref.getString("nyCN2", "--"));
        }
        startSync();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mySnackbar != null) {
            if(mySnackbar.isShown())
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

    private void startSync() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        TimeZone.setDefault(tz);

        Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

        Date currentTimes = fecha.getTime();

        SimpleDateFormat fechaFormato = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat horaFormato = new SimpleDateFormat("HH:mm:ss", Locale.US);

        String fechaString = fechaFormato.format(currentTimes);
        String horaString = horaFormato.format(currentTimes);

        boolean update = false;

        try {
            Date fechaActual = fechaFormato.parse(fechaString);
            Date horaActual = horaFormato.parse(horaString);
            String sDiaGuardado = sharedPref.getString("nyD", "01/01/2006");
            String sNocheGuardado = sharedPref.getString("nyN", "01/01/2006");
            Date fechaDiaSaved = fechaFormato.parse(sDiaGuardado);
            //Date fechaNocheSaved = fechaFormato.parse(sNocheGuardado);

            //Log.e(DEBUG_TAG, "fechaDiaSaved : " + fechaDiaSaved);
            //Log.e(DEBUG_TAG, "fechaNocheSaved : " + fechaNocheSaved);

            Calendar c = Calendar.getInstance();
            c.setTime(fechaFormato.parse(sDiaGuardado));
            c.add(Calendar.DATE, 1);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
            String diaMas = fechaFormato.format(c.getTime());
            Date diaMasSaved = fechaFormato.parse(diaMas);

            //Log.e(DEBUG_TAG, "diaMasSaved : " + diaMasSaved);

            Calendar cN = Calendar.getInstance();
            cN.setTime(fechaFormato.parse(sNocheGuardado));
            cN.add(Calendar.DATE, 1);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
            String nocheMas = fechaFormato.format(cN.getTime());
            Date nocheMasSaved = fechaFormato.parse(nocheMas);

            //Log.e(DEBUG_TAG, "nocheMasSaved : " + nocheMasSaved);

            //Date fechaDiaFicticia = fechaFormato.parse("02/04/2023");
            //Date fechaNocheFicticia = fechaFormato.parse("04/04/2023");
            //Date horaFicticia = horaFormato.parse("22:00:00");

            Date horaDia = horaFormato.parse("14:31:00");
            Date horaNoche = horaFormato.parse("22:31:00");


            if ((!fechaActual.equals(fechaDiaSaved) && diaMasSaved.before(fechaActual)) || (fechaActual.equals(diaMasSaved) && horaActual.after(horaDia))){
                //Log.e(DEBUG_TAG, "UPDATE DIA");
                update = true;
            }else {
                if (nocheMasSaved.before(fechaActual) || (fechaActual.equals(nocheMasSaved) && horaActual.after(horaNoche))) {
                    //Log.e(DEBUG_TAG, "UPDATE NOCHE");
                    update = true;
                }
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if(update) {

            if (binding != null)
                binding.progressBar2.setVisibility(View.VISIBLE);


            if (getActivity() != null && binding != null) {
                requestQueue = Volley.newRequestQueue(getActivity());
            }

            if (requestQueue != null) {
                String url;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    url = "https://cubanapp.info/api/rny.php";
                }else{
                    url = "http://cubanapp.info/api/rny.php";
                }
                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                } catch (JSONException e) {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
                        mySnackbar.show();
                    }
                    //} catch (Exception ei) {
                    // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                    //}
                    Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
                    //throw new RuntimeException(e);
                    //startLaunch(false);
                }
                // Request a string response from the provided URL.
                stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            // Display the first 500 characters of the response string.
                            if(response != null) {
                                try {
                                    if (response.has("mid") && response.has("night") && response.has("mid4") && response.has("night4")) {
                                        cacheData(response.toString(), "newyorkSavedFile");
                                        JSONArray midArray = response.getJSONArray("mid");
                                        JSONArray nightArray = response.getJSONArray("night");
                                        JSONArray midArray2 = response.getJSONArray("mid4");
                                        JSONArray nightArray2 = response.getJSONArray("night4");

                                        JSONObject resultado1 = (JSONObject) midArray.get(0);
                                        JSONObject resultado3 = (JSONObject) nightArray.get(0);
                                        JSONObject resultado4 = (JSONObject) midArray2.get(0);
                                        JSONObject resultado6 = (JSONObject) nightArray2.get(0);

                                        if (getActivity() != null && binding != null) {
                                            SharedPreferences.Editor editor = sharedPref.edit();

                                            SimpleDateFormat yearIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                            SimpleDateFormat yearOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                                            SimpleDateFormat dSemana = new SimpleDateFormat("EEEE", Locale.getDefault());
                                            Date dSemanaConverted = yearIn.parse((String) resultado1.get("fecha"));
                                            Date dSemanaNocheConverted = yearIn.parse((String) resultado3.get("fecha"));
                                            String diaSemana = (dSemana.format(dSemanaConverted));
                                            String nocheSemana = (dSemana.format(dSemanaNocheConverted));

                                            Date yearText = yearIn.parse((String) resultado1.get("fecha"));
                                            //Date semanaText = semanaIn.parse((String) response.get("DS"));


                                            Date yearText3 = yearIn.parse((String) resultado3.get("fecha"));
                                            //Date semanaText2 = semanaIn.parse((String) response.get("NS"));


                                            editor.putString("nyD", yearOut.format(yearText));
                                            editor.putString("nyDS", diaSemana);
                                            editor.putString("nyF1", (String) resultado1.get("num"));
                                            String corrido = (String) resultado4.get("num");
                                            editor.putString("nyCD1", corrido.substring(0, 2));
                                            editor.putString("nyCD2", corrido.substring(2, 4));
                                            editor.putString("nyN", yearOut.format(yearText3));
                                            editor.putString("nyNS", nocheSemana);
                                            editor.putString("nyF2", (String) resultado3.get("num"));
                                            String corrido3 = (String) resultado6.get("num");
                                            editor.putString("nyCN1", corrido3.substring(0, 2));
                                            editor.putString("nyCN2", corrido3.substring(2, 4));
                                            editor.apply();

                                            String fijo1 = (String) resultado1.get("num");
                                            String fijo2 = (String) resultado3.get("num");

                                            binding.D1.setText(yearOut.format(yearText));
                                            binding.D.setText(getString(R.string.dia));
                                            binding.SD.setText(diaSemana);
                                            if (fijo1.length() < 3) {
                                                binding.F11.setText(fijo1);
                                            } else {
                                                try {
                                                    binding.F10.setText(fijo1.substring(0, 1));
                                                    binding.F11.setText(fijo1.substring(1, 3));
                                                } catch (StringIndexOutOfBoundsException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                            binding.C11.setText(corrido.substring(0, 2));
                                            binding.C12.setText(corrido.substring(2, 4));


                                            binding.N1.setText(yearOut.format(yearText3));
                                            binding.N.setText(getString(R.string.noche));
                                            binding.SN.setText(nocheSemana);
                                            if (fijo2.length() < 3) {
                                                binding.F21.setText(fijo2);
                                            } else {
                                                try {
                                                    binding.F20.setText(fijo2.substring(0, 1));
                                                    binding.F21.setText(fijo2.substring(1, 3));
                                                } catch (StringIndexOutOfBoundsException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                            binding.C21.setText(corrido3.substring(0, 2));
                                            binding.C22.setText(corrido3.substring(2, 4));
                                        }
                                        if (binding != null)
                                            binding.progressBar2.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    //errorStart.set(true);
                                    //try {
                                    if (getActivity() != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
                                        mySnackbar.show();
                                    }
                                    // } catch (Exception ei) {
                                    //    Log.e(DEBUG_TAG, "SnackbarError3 : " + ei.getMessage());
                                    //}
                                    Log.e(DEBUG_TAG, "JSONException2 : " + e.getMessage());
                                    //throw new RuntimeException(e);
                                } catch (ParseException e) {
                                    //throw new RuntimeException(e);
                                } catch (IOException e) {
                                    //throw new RuntimeException(e);
                                }
                            }

                        }, error -> {
                    if (binding != null)
                        binding.progressBar2.setVisibility(View.GONE);
                    if (error instanceof TimeoutError) {
                        //try {
                        if (getActivity() != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
                            mySnackbar.show();
                        }
                        //} catch (Exception e) {
                        //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                        //}

                    } else {
                        //try {
                        if (getActivity() != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
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

            }
            TimeZone tzone = TimeZone.getTimeZone("America/New_York");
            TimeZone.setDefault(tzone);

            Calendar fechaz = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

            Date currentTimesz = fechaz.getTime();

            SimpleDateFormat fechaFormatoz = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

            String fechaStringz = fechaFormatoz.format(currentTimesz);
            binding.updateDate.setText(fechaStringz);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString("updateCheckDate3", fechaStringz);
            edit.apply();
        }
    }


    @Override
    public void onPause() {
        //Log.d(DEBUG_TAG, "onPause()");
        /*if (requestQueue != null) {
            requestQueue.stop();
        }*/
        super.onPause();
    }
    @Override
    public void onResume() {
        //Log.d(DEBUG_TAG, "onResume()");
        /*if (requestQueue != null) {
            requestQueue.start();
        }*/
        super.onResume();
    }

    @Override
    public void onDetach() {
        //Log.d(DEBUG_TAG, "onDetach()");
        super.onDetach();
    }

    @Override
    public void onStop() {
        //Log.d(DEBUG_TAG, "onStop()");
        super.onStop();
    }

    private void cacheData(String data, String name) throws IOException {
        if(getActivity() != null && getContext() != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            OutputStreamWriter objectOutputStream = new OutputStreamWriter(
                    new FileOutputStream(dataFile));
            BufferedWriter bufferedWriter = new BufferedWriter(objectOutputStream);
            bufferedWriter.write(data);
            bufferedWriter.close();
        }

    }
}
