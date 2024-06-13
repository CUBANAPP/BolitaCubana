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

public class SevenData {

    private String semana;
    private String dia;
    private String fijo1;
    private String fijo2;
    private String corrido1;
    private String corrido2;

    private int type;


    public SevenData(String s1, String s2, String s3, String s4, String s5, String s6, int s7) {
        this.semana = s1;
        this.dia = s2;
        this.fijo1 = s3;
        this.fijo2 = s4;
        this.corrido1 = s5;
        this.corrido2 = s6;
        this.type = s7;
    }

    public String getSemana(){
        return semana;
    }
    public String getDia(){
        return dia;
    }
    public String getFijo1(){
        return fijo1;
    }
    public String getFijo2(){
        return fijo2;
    }
    public String getCorrido1(){
        return corrido1;
    }
    public String getCorrido2(){
        return corrido2;
    }

    public int getType(){
        return type;
    }
}
