/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;

public class AdivinanzaAdapter extends RecyclerView.Adapter<AdivinanzaAdapter.AdivinanzaView>{
    public final PronosticoData[] listdata;

    public AdivinanzaAdapter(PronosticoData[] listData) {
        this.listdata = listData;
    }

    @NonNull
    @Override
    public AdivinanzaView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.fragment_imageviewer, parent, false);
        return new AdivinanzaView(listItem);
    }

    @Override
    public void onBindViewHolder(AdivinanzaView holder, int position) {
        holder.linkAdapter(this);

        //listdata[position].setNumber(String.valueOf(position+1));

        holder.numero.setText(listdata[position].getType());
        holder.primera.setText(listdata[position].getFirstWord());
        holder.resto.setText(listdata[position].getRestWords());
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public PronosticoData[] getListdata(){
        return listdata;
    }

    public static class AdivinanzaView extends RecyclerView.ViewHolder {

        public AdivinanzaAdapter adapter;

        public TextView numero;
        public TextView primera;
        public TextView resto;

        public AdivinanzaView(View itemView) {
            super(itemView);
            this.numero = itemView.findViewById(R.id.numero);
            this.primera = itemView.findViewById(R.id.pripalabra);
            this.resto = itemView.findViewById(R.id.restopalabras);
        }

        public AdivinanzaView linkAdapter(AdivinanzaAdapter charadaAdapter){
            this.adapter = charadaAdapter;
            return this;
        }

        public AdivinanzaAdapter getAdapter() {
            return adapter;
        }
    }
}
