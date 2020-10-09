package fr.irwin.uge.managers;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.internals.EventWaiter;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CallManager {

    public static final String CALL_PRESENT_EMOTE = "🙋";

    private final long guildId;
    private final long textChannelId;
    private final long messageId;
    private final long teacherUserId;
    private final int timeout;
    private final Set<String> presents;
    private final File resultFile;

    public CallManager(TextChannel textChannel, Member member, int timeout) {
        this.guildId = textChannel.getGuild().getIdLong();
        this.textChannelId = textChannel.getIdLong();
        this.teacherUserId = member.getIdLong();
        this.timeout = timeout;
        this.messageId = sendMessage(member.getEffectiveName());
        this.presents = new HashSet<>();

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy@HH-mm");
        this.resultFile = new File(String.format("%s-#%s-%s.txt", member.getEffectiveName().replace(' ', '_'), textChannel.getName(), df.format(date)));
    }

    private long sendMessage(String teacherName) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Appel demandé par " + teacherName)
                .setDescription(String.format("Vous avez %d minute%s pour cliquer sur %s afin confirmer votre présence.",
                        timeout, (timeout > 1 ? "s" : ""), CALL_PRESENT_EMOTE))
                .setColor(new Color(0x2ecc71))
                .build();

        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return 0L;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return 0L;

        Message message = textChannel.sendMessage(embed).complete();
        message.addReaction(CALL_PRESENT_EMOTE).queue();

        new EventWaiter.Builder(GuildMessageReactionAddEvent.class, e -> {
            boolean messageCheck = e.getMessageIdLong() == message.getIdLong();
            boolean roleCheck = RolesUtils.isStudent(e.getMember());
            boolean emoteCheck = e.getReactionEmote().getEmoji().equals(CALL_PRESENT_EMOTE);
            boolean userCheck = !e.getUser().isBot();
            return messageCheck && roleCheck && emoteCheck && userCheck;
        }, (e, ew) -> presents.add(e.getMember().getEffectiveName()))
                .autoClose(false)
                .timeout(timeout, TimeUnit.MINUTES)
                .timeoutAction(() -> close(teacherName))
                .build();

        return message.getIdLong();
    }

    private void close(String teacherName) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Appel demandé par " + teacherName)
                .setDescription(String.format("L'appel est terminé. %d personnes étaient présentes.", presents.size()))
                .setColor(new Color(0xe67e22))
                .build();

        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        try {
            final Message message = textChannel.retrieveMessageById(messageId).complete();
            message.editMessage(embed).queue();
            message.removeReaction(CALL_PRESENT_EMOTE).queue();
        } catch (ErrorResponseException e) {
            return;
        }

        final Member member = guild.getMemberById(teacherUserId);
        if (member == null) return;

        try {
            fillFile(teacherName, textChannel.getName());

            member.getUser().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Résultat de l'appel dans #" + textChannel.getName()).queue();
                privateChannel.sendFile(resultFile).queue();
            });
        } catch (IOException e) {
            textChannel.sendMessage("Une erreur est survenue lors de la fin de l'appel. Contacter votre administrateur.").queue();

        }
    }

    private void fillFile(String teacherName, String channelName) throws IOException {
        FileWriter fw = new FileWriter(resultFile);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -timeout);
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy à HH:mm");

        fw.write(String.format("Appel effectué le %s par %s dans le salon #%s.\n\n%d personnes présentes :\n",
                df.format(date), teacherName, channelName, presents.size()));

        for (String name : presents) fw.write(" - " + name + "\n");

        fw.close();
    }

}
