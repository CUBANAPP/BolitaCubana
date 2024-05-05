/*
 * Copyright (c) CUBANAPP LLC 2019-2024 .
 */

package com.cubanapp.bolitacubana.ui.search;

import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentSearchBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SearchFragment extends Fragment {

    private long mLastClickTime = 0;

    private long mLastClickSnackTime = 0;
    private FragmentSearchBinding binding;
    private AlertDialog builder;
    private String apiKey;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private Snackbar mySnackbar;

    private LinearLayout georgiaLayout;

    private TextView georgiaFecha;
    private TextView georgiaCorrido1;
    private TextView georgiaCorrido2;
    private TextView georgiaDiaSemana;
    private TextView georgiaFijo1;
    private TextView georgiaFijo2;
    private TextView georgiaTarde;

    private String locState;
    private static final String DEBUG_TAG = "SearchDate";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mLastClickTime = SystemClock.elapsedRealtime();
        mLastClickSnackTime = SystemClock.elapsedRealtime();
        apiKey = BuildConfig.API_KEY;
        binding.button4.setOnClickListener(this::openDate);
        georgiaLayout = binding.linearGeorgia;
        georgiaCorrido1 = binding.MC11;
        georgiaCorrido2 = binding.MC12;
        georgiaDiaSemana = binding.MSD;
        georgiaTarde = binding.MD;
        georgiaFecha = binding.MD1;
        georgiaFijo1 = binding.MF10;
        georgiaFijo2 = binding.MF11;
        return root;
    }

    private void openDate(View v) {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 200) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();


        builder = new AlertDialog.Builder(v.getContext())
                .create();
        View datepickerView = getLayoutInflater().inflate(R.layout.calendar_view, binding.getRoot(), false);
        DatePicker datePickers = datepickerView.findViewById(R.id.datepicker);

        Spinner locationSpinner = datepickerView.findViewById(R.id.locationSpinner);
        // Configurar el Spinner con las opciones de ubicación
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.location_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);


        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DATE, -1);
        Date currentTimes = fecha.getTime();
        datePickers.setMaxDate(currentTimes.getTime());
        builder.setView(datepickerView);

        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> {
            DatePicker datePicker = builder.findViewById(R.id.datepicker);
            if (datePicker != null) {
                datePicker.setFirstDayOfWeek(2);
                CalendarView calendarView = datePicker.getCalendarView();

                // Obtener la ubicación seleccionada del Spinner
                String selectedLocation = locationSpinner.getSelectedItem().toString();
                searchDate(calendarView.getDate(), selectedLocation);
            }
        });
        builder.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.dismiss), (dialog, which) -> {
            if (builder != null) {
                if (builder.isShowing())
                    builder.dismiss();
            }
        });
        builder.show();
    }

    private void searchDate(Long date, String location) {
        if (binding == null)
            return;

        if (mySnackbar != null) {
            if (mySnackbar.isShown()) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickSnackTime < 200) {
                    return;
                }
                mLastClickSnackTime = SystemClock.elapsedRealtime();
            }
        }

        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickSnackTime < 200) {
            return;
        }
        mLastClickSnackTime = SystemClock.elapsedRealtime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date format = new Date(date);
        String dateString = dateFormat.format(format);

        if (getActivity() != null && binding != null) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }

        if (requestQueue != null && binding != null) {
            if (Objects.equals(location, "Georgia")) {
                georgiaLayout.setVisibility(View.VISIBLE);
                georgiaTarde.setText(getString(R.string.tarde));
                georgiaFecha.setText("--/--/----");
                georgiaDiaSemana.setText("");
                georgiaFijo1.setText("-");
                georgiaFijo2.setText("--");
                georgiaCorrido1.setText("--");
                georgiaCorrido2.setText("--");
            } else {
                georgiaLayout.setVisibility(View.GONE);
            }

            binding.progressBar4.setProgress(0);

            binding.progressBar4.setVisibility(View.VISIBLE);
            locState = location;
            binding.titlefl2.setText(location);

            binding.D1s.setText("--/--/----");
            binding.Ds.setText(getString(R.string.dia));
            binding.SDs.setText("");
            binding.F10s.setText("-");
            binding.F11s.setText("--");
            binding.C11s.setText("--");
            binding.C12s.setText("--");
            binding.N1s.setText("--/--/----");
            binding.Ns.setText(getString(R.string.noche));
            binding.SNs.setText("");
            binding.F20s.setText("-");
            binding.F21s.setText("--");
            binding.C21s.setText("--");
            binding.C22s.setText("--");


            String url;
            url = "https://cubanapp.info/api/searchcache/index.php";
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
                json.put("f", dateString);
                json.put("loc", location);
                Log.d(DEBUG_TAG, "Date to Search: " + dateString);
            } catch (JSONException e) {
                if (getActivity() != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date, location));
                    mySnackbar.show();
                }
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseCrashlytics.sendUnsentReports();
                firebaseCrashlytics.recordException(e);
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);
            }

            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {

                        try {

                            boolean error = (Boolean) response.get("error");
                            if (!error) {
                                if (getActivity() != null && binding != null) {
                                    binding.progressBar4.setProgress(100);

                                    String fijos = (String) response.get("fijos");
                                    String corridos = (String) response.get("corridos");
                                    if (Objects.equals(location, "Georgia")) {
                                        georgiaFecha.setText((String) response.get("date"));
                                        //georgiaTarde.setText(getString(R.string.tarde));
                                        georgiaDiaSemana.setText((String) response.get("semana"));
                                        Log.e(DEBUG_TAG, "FIJO: " + fijos);
                                        try {
                                            georgiaFijo1.setText(fijos.substring(6, 7));
                                            georgiaFijo2.setText(fijos.substring(7, 9));
                                        } catch (StringIndexOutOfBoundsException e) {
                                            if (e.getMessage() != null) {
                                                Log.e(DEBUG_TAG, e.getMessage());
                                            }
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                            launchError();
                                            return;
                                        }


                                        try {
                                            /*
                                             * 0-1-2-3-4
                                             * 4-5-6-7-8
                                             * 8-9-10-11-12
                                             */
                                            georgiaCorrido1.setText(corridos.substring(8, 10));
                                            georgiaCorrido2.setText(corridos.substring(10, 12));
                                        } catch (StringIndexOutOfBoundsException e) {
                                            if (e.getMessage() != null) {
                                                Log.e(DEBUG_TAG, e.getMessage());
                                            }
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                            launchError();
                                            return;
                                        }
                                    }

                                    binding.D1s.setText((String) response.get("date"));
                                    //binding.Ds.setText(getString(R.string.dia));
                                    binding.SDs.setText((String) response.get("semana"));
                                    Log.e(DEBUG_TAG, "FIJO: " + fijos);
                                    try {
                                        binding.F10s.setText(fijos.substring(0, 1));
                                        binding.F11s.setText(fijos.substring(1, 3));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                        launchError();
                                        return;
                                    }


                                    try {
                                        binding.C11s.setText(corridos.substring(0, 2));
                                        binding.C12s.setText(corridos.substring(2, 4));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                        launchError();
                                        return;
                                    }

                                    binding.N1s.setText((String) response.get("date"));
                                    //binding.Ns.setText(getString(R.string.noche));
                                    binding.SNs.setText((String) response.get("semana"));

                                    try {
                                        binding.F20s.setText(fijos.substring(3, 4));
                                        binding.F21s.setText(fijos.substring(4, 6));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                        launchError();
                                        return;
                                    }

                                    try {
                                        binding.C21s.setText(corridos.substring(4, 6));
                                        binding.C22s.setText(corridos.substring(6, 8));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                        launchError();
                                        return;
                                    }
                                    binding.progressBar4.setVisibility(View.GONE);
                                }
                            } else {
                                if (binding != null)
                                    binding.progressBar4.setVisibility(View.GONE);
                                if (getActivity() != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            getString(R.string.noresults), Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                }
                            }

                        } catch (JSONException e) {
                            if (binding != null)
                                binding.progressBar4.setVisibility(View.GONE);
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date, location));
                                mySnackbar.show();
                            }
                            if (e.getMessage() != null) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }
                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                            firebaseCrashlytics.sendUnsentReports();
                            firebaseCrashlytics.recordException(e);
                        }
                    }, error -> {
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);

                if (error instanceof TimeoutError) {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date, location));
                        mySnackbar.show();
                    }

                } else {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date, location));
                        mySnackbar.show();
                    }
                }
                Log.e(DEBUG_TAG, "ERROR");
            }

            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
            if (binding != null)
                binding.progressBar4.setProgress(40);
        }
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
        if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (binding != null) {
            try {
                outState.putString("D1s", binding.D1s.getText().toString());
                outState.putString("Ds", binding.Ds.getText().toString());
                outState.putString("SDs", binding.SDs.getText().toString());
                outState.putString("F10s", binding.F10s.getText().toString());
                outState.putString("F11s", binding.F11s.getText().toString());
                outState.putString("C11s", binding.C11s.getText().toString());
                outState.putString("C12s", binding.C12s.getText().toString());
                outState.putString("N1s", binding.N1s.getText().toString());
                outState.putString("Ns", binding.Ns.getText().toString());
                outState.putString("SNs", binding.SNs.getText().toString());
                outState.putString("F20s", binding.F20s.getText().toString());
                outState.putString("F21s", binding.F21s.getText().toString());
                outState.putString("C21s", binding.C21s.getText().toString());
                outState.putString("C22s", binding.C22s.getText().toString());
                outState.putString("titleText", binding.titlefl2.getText().toString());
                outState.putString("locState", locState);
                if (Objects.equals(locState, "Georgia")) {
                    outState.putString("georgiaTarde", georgiaTarde.getText().toString());
                    outState.putString("georgiaFecha", georgiaFecha.getText().toString());
                    outState.putString("georgiaDiaSemana", georgiaDiaSemana.getText().toString());
                    outState.putString("georgiaFijo1", georgiaFijo1.getText().toString());
                    outState.putString("georgiaFijo2", georgiaFijo2.getText().toString());
                    outState.putString("georgiaCorrido1", georgiaCorrido1.getText().toString());
                    outState.putString("georgiaCorrido2", georgiaCorrido2.getText().toString());
                }
            } catch (NullPointerException e) { //
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) buildView(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    private void buildView(@Nullable Bundle bundle) {
        if (bundle != null && binding != null) {
            if (Objects.equals(bundle.getString("locState", "Florida"), "Georgia")) {
                locState = "Georgia";
                georgiaLayout.setVisibility(View.VISIBLE);
                georgiaTarde.setText(bundle.getString("georgiaTarde", getString(R.string.tarde)));
                georgiaFecha.setText(bundle.getString("georgiaFecha", "--/--/----"));
                georgiaDiaSemana.setText(bundle.getString("georgiaDiaSemana", ""));
                georgiaFijo1.setText(bundle.getString("georgiaFijo1", "-"));
                georgiaFijo2.setText(bundle.getString("georgiaFijo2", "--"));
                georgiaCorrido1.setText(bundle.getString("georgiaCorrido1", "--"));
                georgiaCorrido2.setText(bundle.getString("georgiaCorrido2", "--"));
            } else georgiaLayout.setVisibility(View.GONE);

            binding.titlefl2.setText(bundle.getString("titleText", bundle.getString("locState", "Florida")));
            binding.D1s.setText(bundle.getString("D1s", "--/--/----"));
            binding.Ds.setText(bundle.getString("Ds", getString(R.string.dia)));
            binding.SDs.setText(bundle.getString("SDs", ""));
            binding.F10s.setText(bundle.getString("F10s", "-"));
            binding.F11s.setText(bundle.getString("F11s", "--"));
            binding.C11s.setText(bundle.getString("C11s", "--"));
            binding.C12s.setText(bundle.getString("C12s", "--"));
            binding.N1s.setText(bundle.getString("N1s", "--/--/----"));
            binding.Ns.setText(bundle.getString("Ns", getString(R.string.noche)));
            binding.SNs.setText(bundle.getString("SNs", ""));
            binding.F20s.setText(bundle.getString("F20s", "-"));
            binding.F21s.setText(bundle.getString("F21s", "--"));
            binding.C21s.setText(bundle.getString("C21s", "--"));
            binding.C22s.setText(bundle.getString("C22s", "--"));
        }
    }

    private void launchError() {
        if (binding != null) {
            binding.progressBar4.setVisibility(View.GONE);
            if (getActivity() != null) {
                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                        getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }
}
