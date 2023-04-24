/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.notifications;

public class CharadaData {

    private String number;
    private String firstWord;
    private String restWords;
    private String[] palabras;

    public CharadaData(String[] palabra, int numero) {
        StringBuilder restwords = new StringBuilder();
        for(int i = 0; i < palabra.length; i++){
            if(i == 1){
                restwords.append(palabra[i]);
            }
            if(i> 2){
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
