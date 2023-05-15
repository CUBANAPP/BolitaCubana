/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.notifications;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentNotificationsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private RecyclerView recyclerView;
    private CharadaData[] myListData;

    private CharadaAdapter adapter;

    private static final String DEBUG_TAG = "Charada";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextInputEditText edittext = binding.edittext;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), edittext::setText);
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.d(DEBUG_TAG, "beforeTextChanged: " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(DEBUG_TAG, "onTextChanged: " + s);
                searchWord(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.d(DEBUG_TAG, "afterTextChanged: " + s);
            }

        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Resources res = getResources();
        myListData = new CharadaData[]{
                new CharadaData(res.getStringArray(R.array.n1), 1),
                new CharadaData(res.getStringArray(R.array.n2), 2),
                new CharadaData(res.getStringArray(R.array.n3), 3),
                new CharadaData(res.getStringArray(R.array.n4), 4),
                new CharadaData(res.getStringArray(R.array.n5), 5),
                new CharadaData(res.getStringArray(R.array.n6), 6),
                new CharadaData(res.getStringArray(R.array.n7), 7),
                new CharadaData(res.getStringArray(R.array.n8), 8),
                new CharadaData(res.getStringArray(R.array.n9), 9),
                new CharadaData(res.getStringArray(R.array.n10), 10),
                new CharadaData(res.getStringArray(R.array.n11), 11),
                new CharadaData(res.getStringArray(R.array.n12), 12),
                new CharadaData(res.getStringArray(R.array.n13), 13),
                new CharadaData(res.getStringArray(R.array.n14), 14),
                new CharadaData(res.getStringArray(R.array.n15), 15),
                new CharadaData(res.getStringArray(R.array.n16), 16),
                new CharadaData(res.getStringArray(R.array.n17), 17),
                new CharadaData(res.getStringArray(R.array.n18), 18),
                new CharadaData(res.getStringArray(R.array.n19), 19),
                new CharadaData(res.getStringArray(R.array.n20), 20),
                new CharadaData(res.getStringArray(R.array.n21), 21),
                new CharadaData(res.getStringArray(R.array.n22), 22),
                new CharadaData(res.getStringArray(R.array.n23), 23),
                new CharadaData(res.getStringArray(R.array.n24), 24),
                new CharadaData(res.getStringArray(R.array.n25), 25),
                new CharadaData(res.getStringArray(R.array.n26), 26),
                new CharadaData(res.getStringArray(R.array.n27), 27),
                new CharadaData(res.getStringArray(R.array.n28), 28),
                new CharadaData(res.getStringArray(R.array.n29), 29),
                new CharadaData(res.getStringArray(R.array.n30), 30),
                new CharadaData(res.getStringArray(R.array.n31), 31),
                new CharadaData(res.getStringArray(R.array.n32), 32),
                new CharadaData(res.getStringArray(R.array.n33), 33),
                new CharadaData(res.getStringArray(R.array.n34), 34),
                new CharadaData(res.getStringArray(R.array.n35), 35),
                new CharadaData(res.getStringArray(R.array.n36), 36),
                new CharadaData(res.getStringArray(R.array.n37), 37),
                new CharadaData(res.getStringArray(R.array.n38), 38),
                new CharadaData(res.getStringArray(R.array.n39), 39),
                new CharadaData(res.getStringArray(R.array.n40), 40),
                new CharadaData(res.getStringArray(R.array.n41), 41),
                new CharadaData(res.getStringArray(R.array.n42), 42),
                new CharadaData(res.getStringArray(R.array.n43), 43),
                new CharadaData(res.getStringArray(R.array.n44), 44),
                new CharadaData(res.getStringArray(R.array.n45), 45),
                new CharadaData(res.getStringArray(R.array.n46), 46),
                new CharadaData(res.getStringArray(R.array.n47), 47),
                new CharadaData(res.getStringArray(R.array.n48), 48),
                new CharadaData(res.getStringArray(R.array.n49), 49),
                new CharadaData(res.getStringArray(R.array.n50), 50),
                new CharadaData(res.getStringArray(R.array.n51), 51),
                new CharadaData(res.getStringArray(R.array.n52), 52),
                new CharadaData(res.getStringArray(R.array.n53), 53),
                new CharadaData(res.getStringArray(R.array.n54), 54),
                new CharadaData(res.getStringArray(R.array.n55), 55),
                new CharadaData(res.getStringArray(R.array.n56), 56),
                new CharadaData(res.getStringArray(R.array.n57), 57),
                new CharadaData(res.getStringArray(R.array.n58), 58),
                new CharadaData(res.getStringArray(R.array.n59), 59),
                new CharadaData(res.getStringArray(R.array.n60), 60),
                new CharadaData(res.getStringArray(R.array.n61), 61),
                new CharadaData(res.getStringArray(R.array.n62), 62),
                new CharadaData(res.getStringArray(R.array.n63), 63),
                new CharadaData(res.getStringArray(R.array.n64), 64),
                new CharadaData(res.getStringArray(R.array.n65), 65),
                new CharadaData(res.getStringArray(R.array.n66), 66),
                new CharadaData(res.getStringArray(R.array.n67), 67),
                new CharadaData(res.getStringArray(R.array.n68), 68),
                new CharadaData(res.getStringArray(R.array.n69), 69),
                new CharadaData(res.getStringArray(R.array.n70), 70),
                new CharadaData(res.getStringArray(R.array.n71), 71),
                new CharadaData(res.getStringArray(R.array.n72), 72),
                new CharadaData(res.getStringArray(R.array.n73), 73),
                new CharadaData(res.getStringArray(R.array.n74), 74),
                new CharadaData(res.getStringArray(R.array.n75), 75),
                new CharadaData(res.getStringArray(R.array.n76), 76),
                new CharadaData(res.getStringArray(R.array.n77), 77),
                new CharadaData(res.getStringArray(R.array.n78), 78),
                new CharadaData(res.getStringArray(R.array.n79), 79),
                new CharadaData(res.getStringArray(R.array.n80), 80),
                new CharadaData(res.getStringArray(R.array.n81), 81),
                new CharadaData(res.getStringArray(R.array.n82), 82),
                new CharadaData(res.getStringArray(R.array.n83), 83),
                new CharadaData(res.getStringArray(R.array.n84), 84),
                new CharadaData(res.getStringArray(R.array.n85), 85),
                new CharadaData(res.getStringArray(R.array.n86), 86),
                new CharadaData(res.getStringArray(R.array.n87), 87),
                new CharadaData(res.getStringArray(R.array.n88), 88),
                new CharadaData(res.getStringArray(R.array.n89), 89),
                new CharadaData(res.getStringArray(R.array.n90), 90),
                new CharadaData(res.getStringArray(R.array.n91), 91),
                new CharadaData(res.getStringArray(R.array.n92), 92),
                new CharadaData(res.getStringArray(R.array.n93), 93),
                new CharadaData(res.getStringArray(R.array.n94), 94),
                new CharadaData(res.getStringArray(R.array.n95), 95),
                new CharadaData(res.getStringArray(R.array.n96), 96),
                new CharadaData(res.getStringArray(R.array.n97), 97),
                new CharadaData(res.getStringArray(R.array.n98), 98),
                new CharadaData(res.getStringArray(R.array.n99), 99),
                new CharadaData(res.getStringArray(R.array.n100), 100)
        };

        if (binding != null) {
            recyclerView = (RecyclerView) binding.linearCharada;
            adapter = new CharadaAdapter(myListData);

            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }

    private void searchWord(CharSequence s) {
        String compare = s.toString().toLowerCase();
        //s = compare.toLowerCase();
        String comp1 = null;
        String comp2 = null;
        String comp3 = null;
        String comp4 = null;
        String comp5 = null;
        String comp6 = null;
        String comp7 = null;
        String comp8 = null;
        String comp9 = null;
        String comp10 = null;
        String comp11 = null;
        String comp12 = null;
        String comp13 = null;
        String comp14 = null;
        String comp15 = null;
        if(compare.contains("o")){
            comp1 = compare.replaceFirst("o","ó");
        }
        if(compare.contains("i")){
            comp2 = compare.replaceFirst("i","í");
        }
        if(compare.contains("e")){
            comp3 = compare.replaceFirst("e","é");
        }
        if(compare.contains("a")){
            comp4 = compare.replaceFirst("a","á");
        }
        if(compare.contains("u")){
            comp5 = compare.replaceFirst("u","ú");
        }
        if(compare.contains("s")){
            comp6 = compare.replaceFirst("s","c");
            comp11 = compare.replaceFirst("s","z");
        }
        if(compare.contains("c")){
            comp7 = compare.replaceFirst("c","s");
            comp12 = compare.replaceFirst("c","z");
        }
        if(compare.contains("z")){
            comp8 = compare.replaceFirst("z","s");
            comp13 = compare.replaceFirst("z","c");
        }
        if(compare.contains("v")){
            comp9 = compare.replaceFirst("v","b");
        }
        if(compare.contains("b")){
            comp10 = compare.replaceFirst("b","v");
        }
        if(compare.contains("g")){
            comp14 = compare.replaceFirst("g","j");
        }
        if(compare.contains("j")){
            comp15 = compare.replaceFirst("j","g");
        }

        // empezar a contar
        int indice = 0;
        // Crear lista dinámica
        ArrayList<Integer> sws = new ArrayList<>();
        // Comprobar si el carácter es un numero (Si puede convertirse a entero)
        try {
            Integer number = Integer.valueOf(compare);
            sws.add(number);
            // generar la tabla con el numero
            recyclerView.setAdapter(new CharadaAdapter(charada(sws)));
        }
        catch (NumberFormatException e) {
            // Es una letra
            for (CharadaData data : adapter.getListdata()) {
                // Cada numero de la charada (del grupo)
                indice++;
                for (String palabra : data.getPalabras()) {
                    // hacerlo minúsculas
                    String lowecase = palabra.toLowerCase();
                    // Cada palabra individual (de la cadena)
                    boolean coincidencia = lowecase.contains(compare);
                    boolean c1 = false;
                    boolean c2 = false;
                    boolean c3 = false;
                    boolean c4 = false;
                    boolean c5 = false;
                    boolean c6 = false;
                    boolean c7 = false;
                    boolean c8 = false;
                    boolean c9 = false;
                    boolean c10 = false;
                    boolean c11 = false;
                    boolean c12 = false;
                    boolean c13 = false;
                    boolean c14 = false;
                    boolean c15 = false;
                    if(comp1 != null)
                        c1 = lowecase.contains(comp1);
                    if(comp2 != null)
                        c2 = lowecase.contains(comp2);
                    if(comp3 != null)
                        c3 = lowecase.contains(comp3);
                    if(comp4 != null)
                        c4 = lowecase.contains(comp4);
                    if(comp5 != null)
                        c5 = lowecase.contains(comp5);
                    if(comp6 != null)
                        c6 = lowecase.contains(comp6);
                    if(comp7 != null)
                        c7 = lowecase.contains(comp7);
                    if(comp8 != null)
                        c8 = lowecase.contains(comp8);
                    if(comp9 != null)
                        c9 = lowecase.contains(comp9);
                    if(comp10 != null)
                        c10 = lowecase.contains(comp10);
                    if(comp11 != null)
                        c11 = lowecase.contains(comp11);
                    if(comp12 != null)
                        c12 = lowecase.contains(comp12);
                    if(comp13 != null)
                        c13 = lowecase.contains(comp13);
                    if(comp14 != null)
                        c14 = lowecase.contains(comp14);
                    if(comp15 != null)
                        c15 = lowecase.contains(comp15);


                    if (coincidencia|| c1 || c2 || c3 || c4 || c5 || c6 || c7 || c8 || c9 || c10 || c11 || c12 || c13 || c14 || c15) {
                        sws.add(indice);
                        break;
                    }
                }
            }
            //Ejecutar la parte de crear la UI con las coincidencias
            recyclerView.setAdapter(new CharadaAdapter(charada(sws)));
        }
    }

    private CharadaData[] charada(ArrayList<Integer> id) {
            ArrayList<CharadaData> charadaDataArrayList = new ArrayList<>();
            for (int i = 0; i < id.size(); i++) {
                if (id.get(i) == 1) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n1), id.get(i)));
                }
                if (id.get(i) == 2) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n2), id.get(i)));
                }
                if (id.get(i) == 3) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n3), id.get(i)));
                }
                if (id.get(i) == 4) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n4), id.get(i)));
                }
                if (id.get(i) == 5) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n5), id.get(i)));
                }
                if (id.get(i) == 6) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n6), id.get(i)));
                }
                if (id.get(i) == 7) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n7), id.get(i)));
                }
                if (id.get(i) == 8) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n8), id.get(i)));
                }
                if (id.get(i) == 9) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n9), id.get(i)));
                }
                if (id.get(i) == 10) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n10), id.get(i)));
                }
                if (id.get(i) == 11) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n11), id.get(i)));
                }
                if (id.get(i) == 12) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n12), id.get(i)));
                }
                if (id.get(i) == 13) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n13), id.get(i)));
                }
                if (id.get(i) == 14) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n14), id.get(i)));
                }
                if (id.get(i) == 15) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n15), id.get(i)));
                }
                if (id.get(i) == 16) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n16), id.get(i)));
                }
                if (id.get(i) == 17) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n17), id.get(i)));
                }
                if (id.get(i) == 18) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n18), id.get(i)));
                }
                if (id.get(i) == 19) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n19), id.get(i)));
                }
                if (id.get(i) == 20) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n20), id.get(i)));
                }
                if (id.get(i) == 21) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n21), id.get(i)));
                }
                if (id.get(i) == 22) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n22), id.get(i)));
                }
                if (id.get(i) == 23) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n23), id.get(i)));
                }
                if (id.get(i) == 24) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n24), id.get(i)));
                }
                if (id.get(i) == 25) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n25), id.get(i)));
                }
                if (id.get(i) == 26) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n26), id.get(i)));
                }
                if (id.get(i) == 27) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n27), id.get(i)));
                }
                if (id.get(i) == 28) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n28), id.get(i)));
                }
                if (id.get(i) == 29) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n29), id.get(i)));
                }
                if (id.get(i) == 30) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n30), id.get(i)));
                }
                if (id.get(i) == 31) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n31), id.get(i)));
                }
                if (id.get(i) == 32) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n32), id.get(i)));
                }
                if (id.get(i) == 33) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n33), id.get(i)));
                }
                if (id.get(i) == 34) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n34), id.get(i)));
                }
                if (id.get(i) == 35) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n35), id.get(i)));
                }
                if (id.get(i) == 36) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n36), id.get(i)));
                }
                if (id.get(i) == 37) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n37), id.get(i)));
                }
                if (id.get(i) == 38) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n38), id.get(i)));
                }
                if (id.get(i) == 39) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n39), id.get(i)));
                }
                if (id.get(i) == 40) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n40), id.get(i)));
                }
                if (id.get(i) == 41) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n41), id.get(i)));
                }
                if (id.get(i) == 42) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n42), id.get(i)));
                }
                if (id.get(i) == 43) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n43), id.get(i)));
                }
                if (id.get(i) == 44) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n44), id.get(i)));
                }
                if (id.get(i) == 45) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n45), id.get(i)));
                }
                if (id.get(i) == 46) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n46), id.get(i)));
                }
                if (id.get(i) == 47) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n47), id.get(i)));
                }
                if (id.get(i) == 48) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n48), id.get(i)));
                }
                if (id.get(i) == 49) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n49), id.get(i)));
                }
                if (id.get(i) == 50) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n50), id.get(i)));
                }
                if (id.get(i) == 51) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n51), id.get(i)));
                }
                if (id.get(i) == 52) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n52), id.get(i)));
                }
                if (id.get(i) == 53) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n53), id.get(i)));
                }
                if (id.get(i) == 54) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n54), id.get(i)));
                }
                if (id.get(i) == 55) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n55), id.get(i)));
                }
                if (id.get(i) == 56) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n56), id.get(i)));
                }
                if (id.get(i) == 57) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n57), id.get(i)));
                }
                if (id.get(i) == 58) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n58), id.get(i)));
                }
                if (id.get(i) == 59) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n59), id.get(i)));
                }
                if (id.get(i) == 60) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n60), id.get(i)));
                }
                if (id.get(i) == 61) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n61), id.get(i)));
                }
                if (id.get(i) == 62) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n62), id.get(i)));
                }
                if (id.get(i) == 63) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n63), id.get(i)));
                }
                if (id.get(i) == 64) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n64), id.get(i)));
                }
                if (id.get(i) == 65) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n65), id.get(i)));
                }
                if (id.get(i) == 66) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n66), id.get(i)));
                }
                if (id.get(i) == 67) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n67), id.get(i)));
                }
                if (id.get(i) == 68) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n68), id.get(i)));
                }
                if (id.get(i) == 69) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n69), id.get(i)));
                }
                if (id.get(i) == 70) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n70), id.get(i)));
                }
                if (id.get(i) == 71) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n71), id.get(i)));
                }
                if (id.get(i) == 72) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n72), id.get(i)));
                }
                if (id.get(i) == 73) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n73), id.get(i)));
                }
                if (id.get(i) == 74) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n74), id.get(i)));
                }
                if (id.get(i) == 75) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n75), id.get(i)));
                }
                if (id.get(i) == 76) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n76), id.get(i)));
                }
                if (id.get(i) == 77) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n77), id.get(i)));
                }
                if (id.get(i) == 78) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n78), id.get(i)));
                }
                if (id.get(i) == 79) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n79), id.get(i)));
                }
                if (id.get(i) == 80) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n80), id.get(i)));
                }
                if (id.get(i) == 81) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n81), id.get(i)));
                }
                if (id.get(i) == 82) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n82), id.get(i)));
                }
                if (id.get(i) == 83) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n83), id.get(i)));
                }
                if (id.get(i) == 84) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n84), id.get(i)));
                }
                if (id.get(i) == 85) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n85), id.get(i)));
                }
                if (id.get(i) == 86) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n86), id.get(i)));
                }
                if (id.get(i) == 87) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n87), id.get(i)));
                }
                if (id.get(i) == 88) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n88), id.get(i)));
                }
                if (id.get(i) == 89) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n89), id.get(i)));
                }
                if (id.get(i) == 90) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n90), id.get(i)));
                }
                if (id.get(i) == 91) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n91), id.get(i)));
                }
                if (id.get(i) == 92) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n92), id.get(i)));
                }
                if (id.get(i) == 93) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n93), id.get(i)));
                }
                if (id.get(i) == 94) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n94), id.get(i)));
                }
                if (id.get(i) == 95) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n95), id.get(i)));
                }
                if (id.get(i) == 96) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n96), id.get(i)));
                }
                if (id.get(i) == 97) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n97), id.get(i)));
                }
                if (id.get(i) == 98) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n98), id.get(i)));
                }
                if (id.get(i) == 99) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n99), id.get(i)));
                }
                if (id.get(i) == 100) {
                    charadaDataArrayList.add(new CharadaData(getResources().getStringArray(R.array.n100), id.get(i)));
                }
            }

            return charadaDataArrayList.toArray(new CharadaData[0]);

        }
}

