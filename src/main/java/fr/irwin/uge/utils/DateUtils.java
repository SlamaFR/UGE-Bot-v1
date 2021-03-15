package fr.irwin.uge.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    public static String dateWithFormat(Date date, String format) {
        return new SimpleDateFormat(format, Locale.FRENCH).format(date);
    }

    public static String formatDateFull(Date date) {
        return dateWithFormat(date, "EEEE d MMMM yyyy");
    }

    public static String formatDateCompact(Date date) {
        return dateWithFormat(date, "dd/MM/yyyy");
    }

    public static String formatDate(Date date) {
        return dateWithFormat(date, "d MMMM yyyy");
    }
}
