package com.ticketing.entrainement.domain;

public class TicketText {
    private TicketText() {};

    public static String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase()
                .replace("\\s+", " ");
    }
}
