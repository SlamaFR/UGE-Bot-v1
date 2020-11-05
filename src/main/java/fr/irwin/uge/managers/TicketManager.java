package fr.irwin.uge.managers;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.internals.EventWaiter;
import fr.irwin.uge.internals.TaskScheduler;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Queue;
import java.util.*;

public class TicketManager extends ListenerAdapter
{
    public static final int QUEUE_LIST_MAX_LINES = 5;
    public static final int COOLDOWN = 180;
    public static final int QUEUE_TIMEOUT = 3600;
    public static final Map<Long, TicketManager> managers = new HashMap<>();

    private final JDA jda;
    private final long guildId;
    private final long textChannelId;
    private final Queue<Long> queue;
    private final Set<Long> coolingDown;

    private long ticketManagementMessageId = 0L;
    private long currentTicketMemberId = 0L;
    private TaskScheduler timeoutTask;

    private TicketManager(TextChannel textChannel)
    {
        this.jda = textChannel.getJDA();
        this.guildId = textChannel.getGuild().getIdLong();
        this.textChannelId = textChannel.getIdLong();
        this.queue = new LinkedList<>();
        this.coolingDown = new HashSet<>();
        this.timeoutTask = getTimeoutTask();
        UGEBot.JDA().addEventListener(this);
        updateEmbed();
    }

    public static TicketManager getTicketManager(TextChannel textChannel)
    {
        if (hasOpenTickedManager(textChannel))
            return managers.get(textChannel.getIdLong());
        return managers.put(textChannel.getIdLong(), new TicketManager(textChannel));
    }

    public static boolean hasOpenTickedManager(TextChannel textChannel)
    {
        return managers.containsKey(textChannel.getIdLong());
    }

    public boolean openTicket(Member member)
    {
        if (isAwaiting(member)) return false;
        if (isCoolingDown(member)) return false;

        queue.offer(member.getIdLong());
        coolingDown.add(member.getIdLong());
        updateEmbed();

        TaskScheduler.scheduleDelayed(() -> coolingDown.remove(member.getIdLong()), COOLDOWN * 1000);
        this.timeoutTask.stop();
        this.timeoutTask = getTimeoutTask();
        return true;
    }

    public void takeNextTicket(Member teacher)
    {
        final Guild guild = jda.getGuildById(guildId);
        if (guild == null) return;

        if (queue.isEmpty())
        {
            currentTicketMemberId = 0;
            updateEmbed();
            return;
        }
        currentTicketMemberId = queue.poll();

        final Member student = guild.getMemberById(currentTicketMemberId);
        if (student == null)
        {
            currentTicketMemberId = 0;
            return;
        }
        updateEmbed();
        this.timeoutTask.stop();
        this.timeoutTask = getTimeoutTask();

        final GuildVoiceState teacherVoiceState = teacher.getVoiceState();
        final GuildVoiceState studentVoiceState = student.getVoiceState();

        if (teacherVoiceState == null || studentVoiceState == null) return;
        if (!teacherVoiceState.inVoiceChannel() || !studentVoiceState.inVoiceChannel()) return;

        teacher.getGuild().moveVoiceMember(student, teacherVoiceState.getChannel()).queue();
    }

    public void close()
    {
        final Guild guild = jda.getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        try
        {
            final Message message = textChannel.retrieveMessageById(ticketManagementMessageId).complete();
            message.delete().queue();
        } catch (ErrorResponseException ignored)
        {
        }

        managers.remove(textChannelId);
        UGEBot.JDA().removeEventListener(this);
    }

    private String getStudentList()
    {
        if (queue.isEmpty())
        {
            return "*Aucune*";
        }

        final StringBuilder stringBuilder = new StringBuilder();

        int i = 0;
        for (Long l : queue)
        {
            if (i == QUEUE_LIST_MAX_LINES) break;
            if (stringBuilder.length() > 0) stringBuilder.append("\n");
            stringBuilder.append(String.format("• <@%d>", l));
            i++;
        }

        return stringBuilder.toString();
    }

    private MessageEmbed getEmbed()
    {
        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle("File d'attente")
                .setDescription("Cliquez sur ⏭ ou envoyez `!next` pour passer à la personne suivante.\n" +
                        "Cliquez sur " + EmotesUtils.NO + " ou envoyez `!close` pour fermer cette file d'attente.\n\n" +
                        "Cliquez sur 🙋 ou envoyez `!ticket` pour rejoindre la file d'attente.")
                .setColor(new Color(0x3498db));

        if (currentTicketMemberId > 0)
            builder.addField("Tour actuel", String.format("<@%d>", currentTicketMemberId), false);

        builder.addField("Personnes en attente", getStudentList(), false);
        if (queue.size() > 5)
        {
            int rest = queue.size() - 5;
            builder.setFooter(String.format("Et %d autre%s", rest, rest > 1 ? "s" : ""));
        }
        return builder.build();
    }

    private void updateEmbed()
    {
        final Guild guild = jda.getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        try
        {
            final Message message = textChannel.retrieveMessageById(ticketManagementMessageId).complete();
            message.editMessage(getEmbed()).queue();
        } catch (ErrorResponseException e)
        {
            sendEmbed();
        }
    }

    private void sendEmbed()
    {
        final Guild guild = jda.getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        final Message message = textChannel.sendMessage(getEmbed()).complete();
        message.pin().queue();

        Arrays.asList("🙋", "⏭", EmotesUtils.NO).forEach(e -> {
            message.addReaction(e.replace(">", "")).queue();
        });

        new EventWaiter.Builder(GuildMessageReactionAddEvent.class, e -> {
            return e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot();
        }, (e, ew) -> {
            switch (EmotesUtils.getEmote(e.getReactionEmote()))
            {
                case "🙋":
                    openTicket(e.getMember());
                    e.getReaction().removeReaction(e.getUser()).queue();
                    break;
                case "⏭":
                    if (!RolesUtils.isTeacher(e.getMember())) return;
                    e.getReaction().removeReaction(e.getUser()).queue();
                    takeNextTicket(e.getMember());
                    break;
                case EmotesUtils.NO:
                    if (!RolesUtils.isTeacher(e.getMember())) return;
                    close();
                    break;
            }
        }).autoClose(false).build();

        ticketManagementMessageId = message.getIdLong();
    }

    private boolean isAwaiting(Member member)
    {
        return queue.contains(member.getIdLong());
    }

    private boolean isCoolingDown(Member member)
    {
        return coolingDown.contains(member.getIdLong());
    }

    private TaskScheduler getTimeoutTask()
    {
        return TaskScheduler.scheduleDelayed(this::close, QUEUE_TIMEOUT * 1000);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
    {
        super.onGuildMessageReceived(event);
        final User user = UGEBot.JDA().getSelfUser();

        if (event.getMessage().getType().equals(MessageType.CHANNEL_PINNED_ADD) && event.getAuthor().equals(user))
        {
            event.getMessage().delete().queue();
        }
    }
}
