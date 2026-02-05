package com.ticketing.entrainement.domain;

import java.text.Normalizer;
import java.util.Locale;

public class TicketText {
    private TicketText() {};

    public static String normalize(String input) {
        if (input == null) return "";

        String s = input.trim().toLowerCase(Locale.ROOT);

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{M}+", ""); // é -> e, ç -> c, ñ -> n

        s = s.replaceAll("[^\\p{Alnum}]+", " "); // ponctuation -> espace

        s = s.replaceAll("\\s+", " ").trim(); // compacte les espaces

        return s;
    }
}
