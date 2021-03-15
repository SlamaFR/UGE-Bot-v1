package fr.irwin.uge.utils;

import fr.irwin.uge.managers.MailManager;
import org.simplejavamail.internal.util.MimeMessageParser;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MailUtils {
    public static String decodeRFC_2047(String string) {
        if (string == null) {
            return null;
        }
        return Arrays.stream(string.replaceAll("[\n\r\t]", "").split(" ")).map(s -> {
            try {
                return MimeUtility.decodeWord(s);
            }
            catch (ParseException | UnsupportedEncodingException e)
            {
                return s + ' ';
            }
        }).collect(Collectors.joining()).trim();
    }

    public static String extractContent(Message message) throws IOException, MessagingException
    {
        String content;
        if (message.getContent() instanceof Multipart)
        {
            content = new MimeMessageParser((MimeMessage) message).parse().getPlainContent();
        }
        else
        {
            content = (String) message.getContent();
        }

        String[] split = content.split(MailManager.SEPARATOR);
        if (split.length < 2)
        {
            return content.trim();
        }
        return split[1].trim();
    }

    public static boolean isFromMoodle(Message message) throws MessagingException
    {
        String[] courseHeader = message.getHeader("X-Course-Name");
        if (courseHeader == null)
        {
            return false;
        }

        String[] listHeader = message.getHeader("List-Id");
        return listHeader != null &&
               Arrays.stream(listHeader).anyMatch(s -> Pattern.matches("(.*annonces?.*)", s.toLowerCase()));
    }

    public static String getSenderName(Message message) throws MessagingException
    {
        String fromHeader = String.join(" ", message.getHeader("From")).replaceAll("[\"\t\r\n]", "");
        String name = decodeRFC_2047(fromHeader).split("( \\(via| <)")[0].replace("\"", "");

        if (name.contains(" "))
        {
            String[] s = name.split(" ");
            if (s[0].equals(s[0].toUpperCase()))
            {
                return StringUtils.capitalizeString(s[1] + " " + s[0]);
            }
        }
        return name;
    }

    public static String getCourseName(Message message) throws MessagingException
    {
        String h = decodeRFC_2047(String.join(" ", message.getHeader("Subject")));
        String[] split = h.split(":");
        h = String.join(":", Arrays.copyOfRange(split, 1, split.length));
        h = h.replaceAll("[\"\t\r\n]", "");
        return (h.isEmpty()) ? null : h.trim();
    }

    public static String getCourseId(Message message) throws MessagingException
    {
        String[] header = message.getHeader("X-Course-Id");
        return (header == null) ? null : header[0];
    }
}
