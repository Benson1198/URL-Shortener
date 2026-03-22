package com.urlshortener.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;
    private static final int SHORT_CODE_LENGTH = 7;

    public String encode(long id){
        StringBuilder sb = new StringBuilder();

        while(id > 0){
            sb.append(CHARACTERS.charAt((int)(id%BASE)));
            id /= BASE;
        }

        while(sb.length() < SHORT_CODE_LENGTH){
            sb.append('0');
        }

        return sb.reverse().toString();
    }
}
