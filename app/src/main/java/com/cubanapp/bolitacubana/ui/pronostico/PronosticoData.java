/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

public class PronosticoData {

    private String type;
    private String nameid;

    private byte[] base64;


    public PronosticoData(String name, String tipo, byte[] bytes) {
        this.base64 = bytes;
        this.type = tipo;
        this.nameid = name;
    }

    public String getType() {
        return type;
    }

    public String getNameid() {
        return nameid;
    }

    public byte[] getBytes() {
        return base64;
    }
}
