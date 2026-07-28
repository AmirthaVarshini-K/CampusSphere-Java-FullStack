package com.campussphere.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private DateTimeUtil() {
    }

    public static String formatInstant(Instant instant) {
        return ISO_INSTANT.format(instant);
    }
}
