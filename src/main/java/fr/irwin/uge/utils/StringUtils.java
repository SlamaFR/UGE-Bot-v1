package fr.irwin.uge.utils;

import java.util.regex.Pattern;

public class StringUtils {

    public static boolean isCapitalized(String string) {
        return Pattern.matches("([A-ZÀ-Ÿ]([a-zà-ÿ]|-)+( |)){2,}", string);
    }

    public static String capitalizeString(String string) {
        if (string == null) return null;
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '-') {
                found = false;
            } else if (Character.isLetter(chars[i])) {
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }
        return String.valueOf(chars);
    }

    public static String trim(String string) {
        return Pattern.compile("[ \r\t\n]{3,}", Pattern.MULTILINE).matcher(string).replaceAll("\n\n");
    }

}
