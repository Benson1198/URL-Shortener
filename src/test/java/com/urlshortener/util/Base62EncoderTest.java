package com.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Base62EncoderTest {

    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void shouldAlwaysReturn7CharCode(){
        String code = encoder.encode(1L);
        assertEquals(7,code.length());
    }

    @Test
    void shouldReturnDifferentCodesForDifferentIds(){
        String code1 = encoder.encode(1L);
        String code2 = encoder.encode(2L);
        assertNotEquals(code2,code1);
    }

    @Test
    void shouldHandleLargeIds(){
        String code = encoder.encode(999_999_999L);
        assertEquals(7,code.length());
    }

    @Test
    void shouldOnlyContainBase62Characters(){
        String validChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String code = encoder.encode(12345L);
        for(char c: code.toCharArray()){
            assertTrue(validChars.indexOf(c) >= 0, "Invalid char: " + c);
        }
    }
}
