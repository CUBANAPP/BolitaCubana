/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.notifications;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private final String DEBUG_TAG = "Charada";

    private final String[] numeros = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74","75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99","100"};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textNotifications;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void buildCharada(){

        if(binding != null) {
            binding.progressbar5.setVisibility(View.VISIBLE);
            binding.progressbar5.setProgress(0);
        }

        Resources res = getResources();

        CharadaData[] myListData = new CharadaData[]{
                new CharadaData(res.getStringArray(R.array.n1)),
                new CharadaData(res.getStringArray(R.array.n2)),
                new CharadaData(res.getStringArray(R.array.n3)),
                new CharadaData(res.getStringArray(R.array.n4)),
                new CharadaData(res.getStringArray(R.array.n5)),
                new CharadaData(res.getStringArray(R.array.n6)),
                new CharadaData(res.getStringArray(R.array.n7)),
                new CharadaData(res.getStringArray(R.array.n8)),
                new CharadaData(res.getStringArray(R.array.n9)),
                new CharadaData(res.getStringArray(R.array.n10)),
                new CharadaData(res.getStringArray(R.array.n11)),
                new CharadaData(res.getStringArray(R.array.n12)),
                new CharadaData(res.getStringArray(R.array.n13)),
                new CharadaData(res.getStringArray(R.array.n14)),
                new CharadaData(res.getStringArray(R.array.n15)),
                new CharadaData(res.getStringArray(R.array.n16)),
                new CharadaData(res.getStringArray(R.array.n17)),
                new CharadaData(res.getStringArray(R.array.n18)),
                new CharadaData(res.getStringArray(R.array.n19)),
                new CharadaData(res.getStringArray(R.array.n20)),
                new CharadaData(res.getStringArray(R.array.n21)),
                new CharadaData(res.getStringArray(R.array.n22)),
                new CharadaData(res.getStringArray(R.array.n23)),
                new CharadaData(res.getStringArray(R.array.n24)),
                new CharadaData(res.getStringArray(R.array.n25)),
                new CharadaData(res.getStringArray(R.array.n26)),
                new CharadaData(res.getStringArray(R.array.n27)),
                new CharadaData(res.getStringArray(R.array.n28)),
                new CharadaData(res.getStringArray(R.array.n29)),
                new CharadaData(res.getStringArray(R.array.n30)),
                new CharadaData(res.getStringArray(R.array.n31)),
                new CharadaData(res.getStringArray(R.array.n32)),
                new CharadaData(res.getStringArray(R.array.n33)),
                new CharadaData(res.getStringArray(R.array.n34)),
                new CharadaData(res.getStringArray(R.array.n35)),
                new CharadaData(res.getStringArray(R.array.n36)),
                new CharadaData(res.getStringArray(R.array.n37)),
                new CharadaData(res.getStringArray(R.array.n38)),
                new CharadaData(res.getStringArray(R.array.n39)),
                new CharadaData(res.getStringArray(R.array.n40)),
                new CharadaData(res.getStringArray(R.array.n41)),
                new CharadaData(res.getStringArray(R.array.n42)),
                new CharadaData(res.getStringArray(R.array.n43)),
                new CharadaData(res.getStringArray(R.array.n44)),
                new CharadaData(res.getStringArray(R.array.n45)),
                new CharadaData(res.getStringArray(R.array.n46)),
                new CharadaData(res.getStringArray(R.array.n47)),
                new CharadaData(res.getStringArray(R.array.n48)),
                new CharadaData(res.getStringArray(R.array.n49)),
                new CharadaData(res.getStringArray(R.array.n50)),
                new CharadaData(res.getStringArray(R.array.n51)),
                new CharadaData(res.getStringArray(R.array.n52)),
                new CharadaData(res.getStringArray(R.array.n53)),
                new CharadaData(res.getStringArray(R.array.n54)),
                new CharadaData(res.getStringArray(R.array.n55)),
                new CharadaData(res.getStringArray(R.array.n56)),
                new CharadaData(res.getStringArray(R.array.n57)),
                new CharadaData(res.getStringArray(R.array.n58)),
                new CharadaData(res.getStringArray(R.array.n59)),
                new CharadaData(res.getStringArray(R.array.n60)),
                new CharadaData(res.getStringArray(R.array.n61)),
                new CharadaData(res.getStringArray(R.array.n62)),
                new CharadaData(res.getStringArray(R.array.n63)),
                new CharadaData(res.getStringArray(R.array.n64)),
                new CharadaData(res.getStringArray(R.array.n65)),
                new CharadaData(res.getStringArray(R.array.n66)),
                new CharadaData(res.getStringArray(R.array.n67)),
                new CharadaData(res.getStringArray(R.array.n68)),
                new CharadaData(res.getStringArray(R.array.n69)),
                new CharadaData(res.getStringArray(R.array.n70)),
                new CharadaData(res.getStringArray(R.array.n71)),
                new CharadaData(res.getStringArray(R.array.n72)),
                new CharadaData(res.getStringArray(R.array.n73)),
                new CharadaData(res.getStringArray(R.array.n74)),
                new CharadaData(res.getStringArray(R.array.n75)),
                new CharadaData(res.getStringArray(R.array.n76)),
                new CharadaData(res.getStringArray(R.array.n77)),
                new CharadaData(res.getStringArray(R.array.n78)),
                new CharadaData(res.getStringArray(R.array.n79)),
                new CharadaData(res.getStringArray(R.array.n80)),
                new CharadaData(res.getStringArray(R.array.n81)),
                new CharadaData(res.getStringArray(R.array.n82)),
                new CharadaData(res.getStringArray(R.array.n83)),
                new CharadaData(res.getStringArray(R.array.n84)),
                new CharadaData(res.getStringArray(R.array.n85)),
                new CharadaData(res.getStringArray(R.array.n86)),
                new CharadaData(res.getStringArray(R.array.n87)),
                new CharadaData(res.getStringArray(R.array.n88)),
                new CharadaData(res.getStringArray(R.array.n89)),
                new CharadaData(res.getStringArray(R.array.n90)),
                new CharadaData(res.getStringArray(R.array.n91)),
                new CharadaData(res.getStringArray(R.array.n92)),
                new CharadaData(res.getStringArray(R.array.n93)),
                new CharadaData(res.getStringArray(R.array.n94)),
                new CharadaData(res.getStringArray(R.array.n95)),
                new CharadaData(res.getStringArray(R.array.n96)),
                new CharadaData(res.getStringArray(R.array.n97)),
                new CharadaData(res.getStringArray(R.array.n98)),
                new CharadaData(res.getStringArray(R.array.n99)),
                new CharadaData(res.getStringArray(R.array.n100))
        };

       /*
       //String[][] palabras = {res.getStringArray(R.array.n1),res.getStringArray(R.array.n2),res.getStringArray(R.array.n3),res.getStringArray(R.array.n4),res.getStringArray(R.array.n5),res.getStringArray(R.array.n6),res.getStringArray(R.array.n7),res.getStringArray(R.array.n8),res.getStringArray(R.array.n9),res.getStringArray(R.array.n10),res.getStringArray(R.array.n11),res.getStringArray(R.array.n12),res.getStringArray(R.array.n13),res.getStringArray(R.array.n14),res.getStringArray(R.array.n15),res.getStringArray(R.array.n16),res.getStringArray(R.array.n17),res.getStringArray(R.array.n18),res.getStringArray(R.array.n19),res.getStringArray(R.array.n20),res.getStringArray(R.array.n21),res.getStringArray(R.array.n22),res.getStringArray(R.array.n23),res.getStringArray(R.array.n24),res.getStringArray(R.array.n25),res.getStringArray(R.array.n26),res.getStringArray(R.array.n27),res.getStringArray(R.array.n28),res.getStringArray(R.array.n29),res.getStringArray(R.array.n30),res.getStringArray(R.array.n31),res.getStringArray(R.array.n32),res.getStringArray(R.array.n33),res.getStringArray(R.array.n34),res.getStringArray(R.array.n35),res.getStringArray(R.array.n36),res.getStringArray(R.array.n37),res.getStringArray(R.array.n38),res.getStringArray(R.array.n39),res.getStringArray(R.array.n40),res.getStringArray(R.array.n41),res.getStringArray(R.array.n42),res.getStringArray(R.array.n43),res.getStringArray(R.array.n44),res.getStringArray(R.array.n45),res.getStringArray(R.array.n46),res.getStringArray(R.array.n47),res.getStringArray(R.array.n48),res.getStringArray(R.array.n49),res.getStringArray(R.array.n50),res.getStringArray(R.array.n51),res.getStringArray(R.array.n52),res.getStringArray(R.array.n53),res.getStringArray(R.array.n54),res.getStringArray(R.array.n55),res.getStringArray(R.array.n56),res.getStringArray(R.array.n57),res.getStringArray(R.array.n58),res.getStringArray(R.array.n59),res.getStringArray(R.array.n60),res.getStringArray(R.array.n61),res.getStringArray(R.array.n62),res.getStringArray(R.array.n63),res.getStringArray(R.array.n64),res.getStringArray(R.array.n65),res.getStringArray(R.array.n66),res.getStringArray(R.array.n67),res.getStringArray(R.array.n68),res.getStringArray(R.array.n69),res.getStringArray(R.array.n70),res.getStringArray(R.array.n71),res.getStringArray(R.array.n72),res.getStringArray(R.array.n73),res.getStringArray(R.array.n74),res.getStringArray(R.array.n75),res.getStringArray(R.array.n76),res.getStringArray(R.array.n77),res.getStringArray(R.array.n78),res.getStringArray(R.array.n79),res.getStringArray(R.array.n80),res.getStringArray(R.array.n81),res.getStringArray(R.array.n82),res.getStringArray(R.array.n83),res.getStringArray(R.array.n84),res.getStringArray(R.array.n85),res.getStringArray(R.array.n86),res.getStringArray(R.array.n87),res.getStringArray(R.array.n88),res.getStringArray(R.array.n89),res.getStringArray(R.array.n90),res.getStringArray(R.array.n91),res.getStringArray(R.array.n92),res.getStringArray(R.array.n93),res.getStringArray(R.array.n94),res.getStringArray(R.array.n95),res.getStringArray(R.array.n96),res.getStringArray(R.array.n97),res.getStringArray(R.array.n98),res.getStringArray(R.array.n99),res.getStringArray(R.array.n100)};

       for (int i = 0; i < palabras.length; i++){
            Log.e(DEBUG_TAG,String.valueOf(i));
            if(binding == null || getActivity() == null)
                break;
            binding.progressbar5.setProgress(i);
            View items = getLayoutInflater().inflate(R.layout.charada_item, null);
            if(items == null)
                break;

            TextView number = items.findViewById(R.id.numero);
            TextView fisrt = items.findViewById(R.id.pripalabra);
            TextView rest = items.findViewById(R.id.restopalabras);

            StringBuilder restwords = new StringBuilder();
            for(int u = 0; u < palabras[i].length; u++){
                if(u == 0){
                    restwords = new StringBuilder(palabra[i][u]);
                }
                if(u > 1){
                    restwords.append(", ").append(palabra[i][u]);
                }
            }

            number.setText(String.valueOf(i+1));
            fisrt.setText(palabra[i][0]);
            rest.setText(restwords.toString());


            binding.linearCharada.addView(items,i);
        }*/

        if(binding != null) {
            RecyclerView recyclerView = (RecyclerView) binding.getRoot().findViewById(R.id.linearCharada);
            CharadaAdapter adapter = new CharadaAdapter(myListData);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        }

        if(binding != null)
            binding.progressbar5.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        //if(binding != null)
            //binding.linearCharada.removeAllViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(binding != null)
            buildCharada();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding.linearCharada.removeAllViews();
        binding = null;
    }
}