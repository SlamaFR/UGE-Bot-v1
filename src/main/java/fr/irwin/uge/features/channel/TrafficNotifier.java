package fr.irwin.uge.features.channel;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.internals.JSONFetcher;
import fr.irwin.uge.internals.TaskScheduler;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.RedisUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrafficNotifier {

    private static TrafficNotifier manager;

    private final Set<Long> textChannelsIds;
    private final Map<String, List<String>> lastMessages;
    private final Map<String, String> veryLastMessage;
    private final Map<String, String[]> linesMeta;

    private TrafficNotifier() {
        this.lastMessages = new HashMap<>();
        this.textChannelsIds = new HashSet<>();
        this.veryLastMessage = new HashMap<>();
        this.linesMeta = Stream.of(new Object[][]{
                {"RA", new String[]{"RER A", EmotesUtils.RER, EmotesUtils.RER_A}},
                {"RE", new String[]{"RER E", EmotesUtils.RER, EmotesUtils.RER_E}}
        }).collect(Collectors.toMap(icons -> (String) icons[0], icons -> (String[]) icons[1]));

        TaskScheduler.scheduleRepeating(this::refreshData, 20000);
        TaskScheduler.scheduleRepeating(lastMessages::clear, 7200000);
    }

    public static TrafficNotifier instance() {
        if (manager == null) manager = new TrafficNotifier();
        return manager;
    }

    public static void instance(TrafficNotifier m) {
        manager = m;
    }

    public void registerTextChannel(long textChannelId) {
        manager.textChannelsIds.add(textChannelId);
        RedisUtils.setObject(this);
    }

    public void unregisterTextChannel(long textChannelId) {
        manager.textChannelsIds.remove(textChannelId);
        RedisUtils.setObject(this);
    }

    private void refreshData() {
        try {
            JSONObject ratpObject = JSONFetcher.readJsonFromUrl("https://api.slama.io/siege/v2/traffic/");
            JSONObject sncfObject = JSONFetcher.readJsonFromUrl("https://api-ratp.pierre-grimaud.fr/v4/traffic/rers/E");

            for (String s : ratpObject.keySet()) {
                if (ratpObject.getJSONArray(s).length() == 0) continue;
                String message = ratpObject.getJSONArray(s).getString(0).split("</b>")[1];
                message = message.substring(0, 1).toUpperCase() + message.substring(1);
                checkAndSendMessage(s, message);
            }

            if (!sncfObject.isEmpty()) {
                try {
                    String message = sncfObject.getJSONObject("result").getString("message");
                    String s = "RE";
                    checkAndSendMessage(s, message);
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkAndSendMessage(String s, String message) {
        if (lastMessages.containsKey(s) && lastMessages.get(s).contains(message)) return;
        if (!lastMessages.containsKey(s)) lastMessages.put(s, new ArrayList<>());
        lastMessages.get(s).add(message);

        if (veryLastMessage.getOrDefault(s, "").equals(message)) return;
        veryLastMessage.put(s, message);

        sendNotification(getMode(s), getLineIcon(s), "Info trafic " + getName(s), message);
    }

    private void sendNotification(String modeEmote, String lineEmote, String title, String message) {
        for (Long textChannelId : textChannelsIds) {
            TextChannel textChannel = UGEBot.JDA().getTextChannelById(textChannelId);
            if (textChannel == null) return;

            textChannel.sendMessage(
                    new EmbedBuilder()
                            .addField(String.format("%s %s %s", modeEmote, lineEmote, title), message, false)
                            .setFooter("Plus d'informations sur ratp.fr ou transilien.com")
                            .setTimestamp(Instant.now())
                            .setColor(getColor(message))
                            .build()
            ).queue();
        }
    }

    private Color getColor(String message) {
        if (Pattern.matches(".*travaux.*", message)) return new Color(0xe67e22);
        if (Pattern.matches(".*(perturb|panne|interrompu|accident|sécurité).*", message)) return new Color(0xe74c3c);
        return null;
    }

    private String getName(String lineId) {
        return linesMeta.get(lineId)[0];
    }

    private String getMode(String lineId) {
        return linesMeta.get(lineId)[1];
    }

    private String getLineIcon(String lineId) {
        return linesMeta.get(lineId)[2];
    }
}
