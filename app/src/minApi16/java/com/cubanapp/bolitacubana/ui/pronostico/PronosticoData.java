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
