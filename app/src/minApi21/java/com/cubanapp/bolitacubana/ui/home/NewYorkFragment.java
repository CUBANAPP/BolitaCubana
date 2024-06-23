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

package com.cubanapp.bolitacubana.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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

    private long mLastClickTime = 0;
    private long mLastClickSnackTime = 0;
    private String apiKey;
    private FragmentNewyorkBinding binding;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private SharedPreferences sharedPref;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Snackbar mySnackbar;
    private static final String DEBUG_TAG = "NewYorkFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNewyorkBinding.inflate(inflater, container, false);
        apiKey = BuildConfig.API_KEY;


        if (getActivity() != null)
            sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        swipeRefreshLayout = binding.newyorkContainer;
        swipeRefreshLayout.setColorSchemeColors(Color.RED);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    if (binding != null) {
                        startSync();
                    }
                });

        binding.button31.setOnClickListener(viewe -> {
            if (binding != null && binding.button31.isClickable() && getActivity() != null) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                Bundle bundle = new Bundle();
                bundle.putString("name", "newyorkSavedFile");
                getParentFragmentManager().setFragmentResult("SevenDays", bundle);
                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_newyork_to_navigation_sevendays, bundle);
                } catch (IllegalArgumentException e) {
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
        View root = binding.getRoot();
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding != null) {
            String savedFechaString = sharedPref.getString("updateCheckDate3", null);
            if (savedFechaString != null)
                binding.updateDate.setText(savedFechaString);

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
            binding.C21.setText(sharedPref.getString("nyCN1", "--"));
            binding.C22.setText(sharedPref.getString("nyCN2", "--"));
        }
        startSync();
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
        binding = null;
        super.onDestroyView();
    }

    private void startSync() {
        if (binding == null)
            return;

        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickSnackTime < 1000) {
            return;
        }
        mLastClickSnackTime = SystemClock.elapsedRealtime();

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


            if ((!fechaActual.equals(fechaDiaSaved) && diaMasSaved.before(fechaActual)) || (fechaActual.equals(diaMasSaved) && horaActual.after(horaDia))) {
                //Log.e(DEBUG_TAG, "UPDATE DIA");
                update = true;
            } else {
                if (nocheMasSaved.before(fechaActual) || (fechaActual.equals(nocheMasSaved) && horaActual.after(horaNoche))) {
                    //Log.e(DEBUG_TAG, "UPDATE NOCHE");
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
        if (update) {

            if (binding != null && getActivity() != null) {
                swipeRefreshLayout.setRefreshing(true);
                requestQueue = Volley.newRequestQueue(getActivity());
            }

            if (requestQueue != null) {
                String url;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    url = "https://cubanapp.info/api/rny.php";
                } else {
                    url = "http://cubanapp.info/api/rny.php";
                }
                JSONObject json = new JSONObject();

                try {
                    json.put("apiKey", apiKey);
                } catch (JSONException e) {
                    //try {
                    if (getActivity() != null && binding != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
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
                }
                // Request a string response from the provided URL.
                stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            // Display the first 500 characters of the response string.
                            if (response != null) {
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
                                            binding.C21.setText(corrido3.substring(0, 2));
                                            binding.C22.setText(corrido3.substring(2, 4));
                                        }
                                        if (binding != null) {
                                            binding.button31.setEnabled(true);
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                } catch (JSONException e) {
                                    //errorStart.set(true);
                                    //try {
                                    if (getActivity() != null && binding != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
                                        mySnackbar.show();
                                    }
                                    // } catch (Exception ei) {
                                    //    Log.e(DEBUG_TAG, "SnackbarError3 : " + ei.getMessage());
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
                                } catch (ParseException e) {
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
                            }

                        }, error -> {
                    if (binding != null) swipeRefreshLayout.setRefreshing(false);
                    if (error instanceof TimeoutError) {
                        //try {
                        if (getActivity() != null && binding != null) {
                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                    getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> startSync());
                            mySnackbar.show();
                        }
                        //} catch (Exception e) {
                        //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                        //}

                    } else {
                        //try {
                        if (getActivity() != null && binding != null) {
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
            if (binding != null) {
                binding.updateDate.setText(fechaStringz);
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString("updateCheckDate3", fechaStringz);
                edit.apply();
            }
        } else {
            if (binding != null) {
                binding.button31.setEnabled(true);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void cacheData(String data, String name) throws IOException {
        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            OutputStreamWriter objectOutputStream = new OutputStreamWriter(
                    new FileOutputStream(dataFile));
            BufferedWriter bufferedWriter = new BufferedWriter(objectOutputStream);
            bufferedWriter.write(data);
            bufferedWriter.close();
        }

    }
}