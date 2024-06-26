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

package com.cubanapp.bolitacubana.ui.home.sevendays;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;

import java.util.Objects;

public class SevenDaysAdapter extends RecyclerView.Adapter<SevenDaysAdapter.SevenDaysView> {
    public final SevenData[] listdataSeven;

    public SevenDaysAdapter(SevenData[] listData) {
        this.listdataSeven = listData;
    }


    @NonNull
    @Override
    public SevenDaysView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.sevenday_item, parent, false);
        return new SevenDaysView(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SevenDaysView holder, int position) {

        holder.sm.setText(listdataSeven[position].getSemana());
        holder.d.setText(listdataSeven[position].getDia());
        holder.f1.setText(listdataSeven[position].getFijo1());
        holder.f2.setText(listdataSeven[position].getFijo2());
        holder.c1.setText(listdataSeven[position].getCorrido1());
        holder.c2.setText(listdataSeven[position].getCorrido2());

        if (Objects.equals(listdataSeven[position].getType(), 1)) {
            // rojo 146
            holder.sm.setTextColor(Color.argb(255,255,146,146));
            holder.d.setTextColor(Color.argb(255,255,146,146));
        } else if (Objects.equals(listdataSeven[position].getType(), 2)) {
            // blue 146 172
            holder.sm.setTextColor(Color.argb(255,146,172,255));
            holder.d.setTextColor(Color.argb(255,146,172,255));
        } else {
            // yellow 250 164
            holder.sm.setTextColor(Color.argb(255,255,250,164));
            holder.d.setTextColor(Color.argb(255,255,250,164));
        }
    }


    @Override
    public int getItemCount() {
        return listdataSeven.length;
    }

    public static class SevenDaysView extends RecyclerView.ViewHolder{

        private TextView sm;
        private TextView d;
        private TextView f1;
        private TextView f2;
        private TextView c1;
        private TextView c2;

        public SevenDaysView(View itemView) {
            super(itemView);
            this.sm = itemView.findViewById(R.id.sFecha);
            this.d = itemView.findViewById(R.id.sD);
            this.f1 = itemView.findViewById(R.id.sF1_0);
            this.f2 = itemView.findViewById(R.id.sF1_1);
            this.c1 = itemView.findViewById(R.id.sC1_1);
            this.c2 = itemView.findViewById(R.id.sC1_2);
        }
    }
}
