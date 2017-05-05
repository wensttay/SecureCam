package com.example.wensttay.securecam_project.inputFilter;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by wensttay on 30/04/17.
 */
public class CamNumberInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String resp = "";
        int actualLength = dest.length();

        for( int i = 0; i < source.toString().length(); i++ ) {

            switch (actualLength + i){
                case 0: resp += "C"; continue;
                case 1: resp += "A"; continue;
                case 2: resp += "M"; continue;
                case 3: resp += " "; continue;
                case 10: resp += "-"; continue;
            }

            if((Character.isLetterOrDigit(source.charAt( i )) && (actualLength + i) < 11)
                    || (Character.isDigit(source.charAt(i)) && (actualLength + i) > 10)) {
                resp += source.charAt( i );
            }
        }

        return resp;
    }
}
