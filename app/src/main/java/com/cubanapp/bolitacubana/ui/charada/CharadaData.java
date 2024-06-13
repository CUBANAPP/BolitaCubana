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

package com.cubanapp.bolitacubana.ui.charada;

public class CharadaData {

    private String number;
    private String firstWord;
    private String restWords;
    private String[] palabras;

    public CharadaData(String[] palabra, int numero) {
        StringBuilder restwords = new StringBuilder();
        for (int i = 0; i < palabra.length; i++) {
            if (i == 1) {
                restwords.append(palabra[i]);
            }
            if (i > 2) {
                restwords.append(", ").append(palabra[i]);
            }
        }

        this.palabras = palabra;
        this.number = String.valueOf(numero);
        this.firstWord = palabra[0];
        this.restWords = restwords.toString();
    }

    public String[] getPalabras() {
        return palabras;
    }

    public String getNumber() {
        return number;
    }

    public String getFirstWord() {
        return firstWord;
    }

    public String getRestWords() {
        return restWords;
    }
}
