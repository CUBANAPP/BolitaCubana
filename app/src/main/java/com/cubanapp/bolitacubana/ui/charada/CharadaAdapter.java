/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.charada;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;

public class CharadaAdapter extends RecyclerView.Adapter<CharadaAdapter.CharadaView>{
    public final CharadaData[] listdata;

    public CharadaAdapter(CharadaData[] listData) {
        this.listdata = listData;
    }

    @NonNull
    @Override
    public CharadaView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.charada_item, parent, false);
        return new CharadaView(listItem);
    }

    @Override
    public void onBindViewHolder(CharadaView holder, int position) {
        holder.linkAdapter(this);

        //listdata[position].setNumber(String.valueOf(position+1));

        holder.numero.setText(listdata[position].getNumber());
        holder.primera.setText(listdata[position].getFirstWord());
        holder.resto.setText(listdata[position].getRestWords());
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public CharadaData[] getListdata(){
        return listdata;
    }

    public static class CharadaView extends RecyclerView.ViewHolder {

        public CharadaAdapter adapter;

        public TextView numero;
        public TextView primera;
        public TextView resto;

        public CharadaView(View itemView) {
            super(itemView);
            this.numero = itemView.findViewById(R.id.numero);
            this.primera = itemView.findViewById(R.id.pripalabra);
            this.resto = itemView.findViewById(R.id.restopalabras);
        }

        public CharadaView linkAdapter(CharadaAdapter charadaAdapter){
            this.adapter = charadaAdapter;
            return this;
        }

        public CharadaAdapter getAdapter() {
            return adapter;
        }
    }
}
