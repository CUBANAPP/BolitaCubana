/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubanapp.bolitacubana.R;

import java.util.Objects;

public class AdivinanzaAdapter extends RecyclerView.Adapter<AdivinanzaAdapter.AdivinanzaView>{
    public final PronosticoData[] listdata;

    public AdivinanzaView.PhotoListener photoList;

    public AdivinanzaAdapter(PronosticoData[] listData, AdivinanzaView.PhotoListener photoLists) {
        this.listdata = listData;
        this.photoList = photoLists;
    }

    @NonNull
    @Override
    public AdivinanzaView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.adivinanza_item, parent, false);
        return new AdivinanzaView(listItem, photoList);
    }

    @Override
    public void onBindViewHolder(AdivinanzaView holder, int position) {
        holder.linkAdapter(this);
        holder.type.setText(listdata[position].getType());
        holder.name.setText(listdata[position].getNameid());
        holder.setBytes(listdata[position].getBytes());

        if(Objects.equals(listdata[position].getType(), "jpg")){
            Bitmap decodedByte = BitmapFactory.decodeByteArray(holder.getBytes(), 0, holder.getBytes().length);
            holder.image.setImageBitmap(decodedByte);
        }
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public PronosticoData[] getListdata(){
        return listdata;
    }

    public static class AdivinanzaView extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AdivinanzaAdapter adapter;

        private ImageView image;
        private TextView type;
        private TextView name;

        private byte[] bytes;


        private PhotoListener photoListener;

        public AdivinanzaView(View itemView, PhotoListener photo) {
            super(itemView);
            this.photoListener = photo;
            this.name = itemView.findViewById(R.id.nameid);
            this.type = itemView.findViewById(R.id.typ);
            this.image = itemView.findViewById(R.id.imageshort);
            itemView.setOnClickListener(this);
        }

        public AdivinanzaView linkAdapter(AdivinanzaAdapter adivinanzaLink){
            this.adapter = adivinanzaLink;
            return this;
        }

        public AdivinanzaAdapter getAdapter() {
            return adapter;
        }

        public String getName() {
            return (String) name.getText();
        }

        public String getType() {
            return (String) type.getText();
        }

        @Override
        public void onClick(View v) {
            //onItemClick();
           /* LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout myRoot = new LinearLayout(itemView.getContext());
            View itemView2 = inflater.inflate(R.layout.fragment_imageviewer, myRoot,false);
            //View list = layoutInflater.inflate(R.layout.fragment_imageviewer, null,false);
            ImageView imageView = (ImageView) itemView2.findViewById(R.id.imageFragmentViewer);
            Bitmap f = image.getDrawingCache();
            imageView.setImageBitmap(f);
*/
            photoListener.onItemClick(getAdapterPosition(), getBytes(), getName(), getType());

            Log.e("Adapter", getAdapterPosition() + " CLICKED: " + getName());
        }

        public interface PhotoListener{
            void onItemClick(int position, byte[] map, String name, String type);
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
