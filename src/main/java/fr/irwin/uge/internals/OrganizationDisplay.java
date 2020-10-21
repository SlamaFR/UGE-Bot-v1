package fr.irwin.uge.internals;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.irwin.uge.UGEBot;
import fr.irwin.uge.utils.DateUtils;
import fr.irwin.uge.utils.RedisUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class OrganizationDisplay {

    private final long guildId;
    private final long textChannelId;
    private final String title;
    private final Map<String, Field> fields;

    private long messageId;
    private Date start;
    private Date end;

    public OrganizationDisplay(long guildId, long textChannelId, String title, Map<String, Field> fields) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.title = title;
        this.fields = fields;
        setDates();
    }

    /* Don't mind this atrocity, only for deserialization purpose. */
    @JsonCreator
    public OrganizationDisplay(@JsonProperty("guildId") long guildId, @JsonProperty("textChannelId") long textChannelId,
                               @JsonProperty("title") String title, @JsonProperty("fields") Map<String, Field> fields,
                               @JsonProperty("messageId") long messageId, @JsonProperty("start") Date start,
                               @JsonProperty("end") Date end) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.title = title;
        this.fields = fields;
        this.messageId = messageId;
        this.start = start;
        this.end = end;
    }

    public void start(Message m) {
        fields.keySet().forEach(emote -> m.addReaction(emote.replace(">", "")).queue());
        new EventWaiter.Builder(GuildMessageReactionAddEvent.class,
                e -> e.getMessageIdLong() == messageId && RolesUtils.isTeacher(e.getMember()),
                (e, ew) -> {
                    String emote;
                    try {
                        emote = e.getReactionEmote().getEmoji();
                    } catch (IllegalStateException ex) {
                        emote = "<:" + e.getReactionEmote().getAsReactionCode() + '>';
                    }

                    if (fields.containsKey(emote)) {
                        fields.get(emote).cycleState();
                        update();
                        e.getReaction().removeReaction(e.getUser()).queue();
                    } else if (emote.equals("❌")) {
                        ew.close();
                        RedisUtils.removeDisplay(e.getGuild(), messageId);
                    }
                })
                .autoClose(false)
                .build();
    }

    public boolean restore(String messageId) {
        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return false;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return false;

        try {
            final Message message = textChannel.retrieveMessageById(messageId).complete();
            this.start(message);
            RedisUtils.addDisplay(textChannel.getGuild(), message.getIdLong(), this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void send() {
        final MessageEmbed messageEmbed = getEmbed();

        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        Message message = textChannel.sendMessage(messageEmbed).complete();
        messageId = message.getIdLong();
        start(message);
        RedisUtils.addDisplay(guild, messageId, this);
    }

    private void update() {
        setDates();
        final MessageEmbed messageEmbed = getEmbed();

        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        final Message message = textChannel.retrieveMessageById(messageId).complete();
        if (message == null) return;
        message.editMessage(messageEmbed).queue();
    }

    private MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(String.format("Semaine du **%s** au **%s**",
                        DateUtils.formatDate(start), DateUtils.formatDate(end)));

        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            Field field = entry.getValue();
            builder.addField(field.name, field.state.value, field.inline);
        }

        return builder.build();
    }

    private void setDates() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, 0);
        this.start = calendar.getTime();
        calendar.set(Calendar.DAY_OF_WEEK, 7);
        this.end = calendar.getTime();
    }

    public static class Field {

        private final String name;
        private final boolean inline;
        private State state;

        @JsonCreator
        public Field(@JsonProperty("name") String name, @JsonProperty("inline") boolean inline) {
            this.name = name;
            this.inline = inline;
            this.state = State.UNKNOWN;
        }

        private void cycleState() {
            state = State.values()[(state.ordinal() + 1) % State.values().length];
        }
    }

    public enum State {
        UNKNOWN("*Information à venir*"),
        ON_SITE("Présentiel"),
        HYBRID("Hybride"),
        REMOTE("À distance");

        private final String value;

        State(String value) {
            this.value = value;
        }
    }
}
