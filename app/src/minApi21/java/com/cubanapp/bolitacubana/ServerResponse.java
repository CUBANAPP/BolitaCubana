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

