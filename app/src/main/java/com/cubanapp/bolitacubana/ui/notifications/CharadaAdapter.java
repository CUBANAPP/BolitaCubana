/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;

public class CharadaAdapter extends RecyclerView.Adapter<CharadaAdapter.ViewHolder>{
    private CharadaData[] listdata;

    // RecyclerView recyclerView;
    public CharadaAdapter(CharadaData[] listdata) {
        this.listdata = listdata;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.charada_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CharadaData myListData = listdata[position];
        listdata[position].setNumber(String.valueOf(position+1));

        holder.numero.setText(listdata[position].getNumber());
        holder.primera.setText(listdata[position].getFirstWord());
        holder.resto.setText(listdata[position].getRestWords());

        //holder.relativeLayout.setOnClickListener(view -> Toast.makeText(view.getContext(),"click on item: "+myListData.getNumber(),Toast.LENGTH_SHORT).show());
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView numero;
        public TextView primera;
        public TextView resto;
        public LinearLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.numero = (TextView) itemView.findViewById(R.id.numero);
            this.primera = (TextView) itemView.findViewById(R.id.pripalabra);
            this.resto = (TextView) itemView.findViewById(R.id.restopalabras);
            relativeLayout = (LinearLayout) itemView.findViewById(R.id.charada_linear);
        }
    }
}
