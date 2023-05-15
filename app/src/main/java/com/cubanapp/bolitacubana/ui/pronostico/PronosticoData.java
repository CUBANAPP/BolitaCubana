/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

public class PronosticoData {

    private String type;

    private byte[] base64;


    public PronosticoData(String tipo, byte[] bytes) {
        this.base64 = bytes;
        this.type = tipo;
    }

    public String getType() {
        return type;
    }

    public byte[] getBytes() {
        return base64;
    }

    public void setBase64(byte[] base64) {
        this.base64 = base64;
    }
    public void setType(String type) {
        this.type = type;
    }
}
