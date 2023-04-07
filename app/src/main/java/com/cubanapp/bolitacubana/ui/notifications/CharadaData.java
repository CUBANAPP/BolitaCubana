/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.notifications;

public class CharadaData {

    private String number;
    private String firstWord;
    private String restWords;
    private String[] palabras;

    public CharadaData(String[] palabra) {

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
        this.number = "0";
        this.firstWord = palabra[0];
        this.restWords = restwords.toString();
    }

    public String[] getPalabras() {
        return palabras;
    }

    public void setPalabras(String[] palabra) {
        this.palabras = palabra;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getFirstWord() {
        return firstWord;
    }
    public void setFirstWord(String firstwd) {
        this.firstWord = firstwd;
    }
    public String getRestWords() {
        return restWords;
    }
    public void setRestWords(String restwords) {
        this.restWords = restwords;
    }
}