package fr.irwin.uge.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    public static String formatDateFull(Date date) {
        return new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(date);
    }

    public static String formatDateCompact(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(date);
    }

    public static String formatDate(Date date)
    {
        return new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH).format(date);
    }
}
