package fr.irwin.uge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
    public static boolean isCapitalized(String string)
    {
        return Pattern.matches("([A-ZÀ-Ÿ]([a-zà-ÿ]|-)+( |)){2,}", string);
    }

    public static String capitalizeString(String string)
    {
        if (string == null) return null;
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++)
        {
            if (!found && Character.isLetter(chars[i]))
            {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '-')
            {
                found = false;
            } else if (Character.isLetter(chars[i]))
            {
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }
        return String.valueOf(chars);
    }

    public static String trim(String string)
    {
        return Pattern.compile("[ \r\t\n]{3,}", Pattern.MULTILINE).matcher(string).replaceAll("\n\n");
    }

    /*
     * Code from Jan Goyvaerts on StackOverflow
     * https://stackoverflow.com/questions/366202
     */
    public static List<String> splitArgs(String string)
    {
        List<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");
        Matcher regexMatcher = regex.matcher(string);
        while (regexMatcher.find())
        {
            if (regexMatcher.group(1) != null)
            {
                matchList.add(regexMatcher.group(1));
            }
            else
            {
                matchList.add(regexMatcher.group());
            }
        }
        return matchList;
    }
}
