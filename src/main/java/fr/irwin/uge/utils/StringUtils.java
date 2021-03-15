package fr.irwin.uge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isCapitalized(String string) {
        return Pattern.matches("([A-ZÀ-Ÿ]([a-zà-ÿ]|-)+( |)){2,}", string);
    }

    public static String capitalizeString(String string) {
        if (string == null) return null;
        return Pattern.compile("(^|\\s|-)+.")
            .matcher(string)
            .replaceAll(match -> {
                String s = match.group();
                return s.substring(0, s.length() - 1) + Character.toUpperCase(s.charAt(s.length() - 1));
            });
    }

    public static String trim(String string) {
        return Pattern.compile("[ \r\t\n]{3,}", Pattern.MULTILINE).matcher(string).replaceAll("\n\n");
    }

    /*
     * Code from Jan Goyvaerts on StackOverflow
     * https://stackoverflow.com/questions/366202
     *
     * Regex to ignore escaped quotes from Kobi on StackOverflow + self work
     * https://stackoverflow.com/questions/4031900
     */
    public static List<String> splitArgs(String string) {
        final var matchList = new ArrayList<String>();
        final var regex = Pattern.compile("(?:\\\\\"|[^\\s\"])+|\"(?:\\\\\"|[^\"])*\"");
        final var regexMatcher = regex.matcher(string);
        while (regexMatcher.find()) {
            matchList.add(
                regexMatcher
                    .group(0)
                    .replaceAll("(?<!\\\\)\"", "")
                    .replaceAll("\\\\\"", "\"")
            );
        }
        return matchList;
    }
}
