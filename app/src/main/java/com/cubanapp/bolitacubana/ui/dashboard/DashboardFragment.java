/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.dashboard;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.cubanapp.bolitacubana.databinding.FragmentDashboardBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AlertDialog builder;
    private String apiKey;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private Snackbar mySnackbar;
    private static final String DEBUG_TAG = "SearchDate";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        apiKey = BuildConfig.API_KEY;
        binding.button4.setOnClickListener(this::openDate);

        //final TextView textView = binding;
        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /*private void pickDate(View v){
        CalendarView calendarView = .findViewById(R.id.calendarid);
        calendarView.getDate();
        Log.d(DEBUG_TAG, "DATE Selected: " + calendarView.getDate());

    }*/
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void openDate(View v){
        builder = new AlertDialog.Builder(v.getContext())
                .create();
        //builder.setTitle(getString(R.string.pickDate));
        View datepickerView = getLayoutInflater().inflate(R.layout.calendar_view,binding.getRoot(),false);
        DatePicker datePickers = datepickerView.findViewById(R.id.datepicker);
        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DATE, -1);
        Date currentTimes = fecha.getTime();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            datePickers.setFirstDayOfWeek(2);
        }*/
        datePickers.setMaxDate(currentTimes.getTime());
        CalendarView calendarViews = datePickers.getCalendarView();
        calendarViews.setMaxDate(currentTimes.getTime());
        builder.setView(datepickerView);

        builder.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.open), (dialog, which) -> {
            DatePicker datePicker = builder.findViewById(R.id.datepicker);
            if(datePicker !=null) {
                Date currentTime = Calendar.getInstance().getTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    datePicker.setFirstDayOfWeek(2);
                }
                CalendarView calendarView = datePicker.getCalendarView();
                //calendarView.setDate(currentTime.getTime());
                //Log.e(DEBUG_TAG, "DATE Selected: " + calendarView.getDate());
                //Log.d(DEBUG_TAG, "currentTime: " + currentTime.getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date format = new Date(calendarView.getDate());
                String dateString = dateFormat.format(format);
                //Log.e(DEBUG_TAG, "DATE Parsed: " + dateString);

                searchDate(calendarView.getDate());
            }
        });
        builder.setButton(Dialog.BUTTON_NEGATIVE,getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
        builder.show();
    }

    private void searchDate(Long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date format = new Date(date);
        String dateString = dateFormat.format(format);

        if (getActivity() != null && binding != null) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null) {
            binding.progressBar4.setProgress(0);

            binding.progressBar4.setVisibility(View.VISIBLE);

            String url = "https://cubanapp.info/api/searchcache/index.php";
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
                json.put("f", dateString);
                Log.d(DEBUG_TAG, "Date to Search: " + dateString);
            } catch (JSONException e) {
                //try {
                if (getActivity() != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            "Ha ocurrido un error al generar los datos", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                    mySnackbar.show();
                }
                //} catch (Exception ei) {
                // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                //}
                Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
                //throw new RuntimeException(e);
                //startLaunch(false);
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        // Display the first 500 characters of the response string.
                        try {
                            //JSONObject error = response.getJSONObject("");
                            //response.get("error");
                            boolean error = (Boolean) response.get("error");
                            if (!error) {
                                if (getActivity() != null && binding != null) {
                                    binding.progressBar4.setProgress(100);
                                    //SimpleDateFormat semanaOut = new SimpleDateFormat("EEEE", Locale.getDefault());
                                    //SimpleDateFormat yearOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                                    //Date yearText = semanaOut.parse((String) response.get("date"));
                                    //Date semanaText = yearOut.parse((String) response.get("semana"));


                                    String fijos = (String) response.get("fijos");
                                    String corridos = (String) response.get("corridos");

                                    binding.D1s.setText((String) response.get("date"));
                                    binding.Ds.setText(getString(R.string.dia));
                                    binding.SDs.setText((String) response.get("semana"));
                                    try {
                                        binding.F10s.setText(fijos.substring(0, 1));
                                        binding.F11s.setText(fijos.substring(1, 3));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }


                                    try {
                                        binding.C11s.setText(corridos.substring(0, 2));
                                        binding.C12s.setText(corridos.substring(2, 4));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }

                                    binding.N1s.setText((String) response.get("date"));
                                    binding.Ns.setText(getString(R.string.noche));
                                    binding.SNs.setText((String) response.get("semana"));

                                    try {
                                        binding.F20s.setText(fijos.substring(3, 4));
                                        binding.F21s.setText(fijos.substring(4, 6));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }

                                    try {
                                        binding.C21s.setText(corridos.substring(4, 6));
                                        binding.C22s.setText(corridos.substring(6, 8));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }
                                    binding.progressBar4.setVisibility(View.GONE);
                                }
                            } else {
                                if (binding != null)
                                    binding.progressBar4.setVisibility(View.GONE);
                                if (getActivity() != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            "Bolita Cubana encontró un error interno", Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                }
                            }

                        } catch (JSONException e) {
                            if (binding != null)
                                binding.progressBar4.setVisibility(View.GONE);
                            //errorStart.set(true);
                            //try {
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        "Ha ocurrido un error al obtener los datos", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
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
                    binding.progressBar4.setVisibility(View.GONE);

                if (error instanceof TimeoutError) {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                "Parece que su conexión está lenta", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                        mySnackbar.show();
                    }
                    //} catch (Exception e) {
                    //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                    //}

                } else {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                "Se ha perdido la conexión con los servidores de Bolita Cubana", Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
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
                binding.progressBar4.setProgress(40);
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
            if(mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if(builder != null){
            if(builder.isShowing())
                builder.dismiss();
        }
    }
    
}