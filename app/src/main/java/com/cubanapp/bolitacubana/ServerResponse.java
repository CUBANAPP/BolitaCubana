/*
 * Copyright (c) CUBANAPP LLC 2019-2024 .
 */

package com.cubanapp.bolitacubana;

public class ServerResponse {
    private boolean error;
    private String msg;
    private String fecha;
    private String hora;

    private String cert_msg;
    private String cert;

    private String certFinger;

    private String cert_verify;

    // Getters and setters
    public boolean isError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }
    public String getCert_msg() {
        return cert_msg;
    }
    public String getCert() {
        return cert;
    }

    public String getCert_verify() {
        return cert_verify;
    }
    public String getcertFinger() {
        return certFinger;
    }

    // toString() method for logging
    /*@Override
    public String toString() {
        return "{" +
                "error=" + error +
                ", msg='" + msg + '\'' +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                ", cert='" + hora + '\'' +
                ", hora='" + hora + '\'' +
                ", hora='" + hora + '\'' +
                '}';
    }*/
}

