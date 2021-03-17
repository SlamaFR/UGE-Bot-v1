package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.managers.CallManager;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class TeacherCommands
{
    @Command(name = "call", aliases = {"appel"})
    private void call(Guild guild, TextChannel textChannel, Member member, Message message, String[] args) {
        if (guild == null) {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId())) {
            return;
        }
        if (!RolesUtils.isTeacher(member)) {
            return;
        }

        int time = 5;
        try {
            if (Integer.parseInt(args[0]) <= 0) {
                throw new NumberFormatException();
            }
            time = Integer.parseInt(args[0]);
            throw new NumberFormatException();
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            if (message.getMentionedRoles().isEmpty()) {
                new CallManager(textChannel, member, time);
            } else {
                new CallManager(textChannel, message.getMentionedRoles().get(0), member, time);
            }
        }
    }

    @Command(name = "queue")
    private void queue(Guild guild, TextChannel textChannel, Member member) {
        if (guild == null) {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId())) {
            return;
        }
        if (!RolesUtils.isTeacher(member)) {
            return;
        }

        if (TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Une file d'attente est déjà ouverte dans ce salon !");
            return;
        }

        TicketManager.getTicketManager(textChannel);
    }

    @Command(name = "next")
    private void next(Guild guild, TextChannel textChannel, Member member) {
        if (guild == null) {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId())) {
            return;
        }
        if (!RolesUtils.isTeacher(member)) {
            return;
        }

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        TicketManager.getTicketManager(textChannel).takeNextTicket(member);
    }

    @Command(name = "close")
    private void close(Guild guild, TextChannel textChannel, Member member) {
        if (guild == null) {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId())) {
            return;
        }
        if (!RolesUtils.isTeacher(member)) {
            return;
        }

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        TicketManager.getTicketManager(textChannel).close();
    }

    @Command(name = "poll")
    private void poll(Guild guild, TextChannel textChannel, Member member, String[] args) {
        if (guild == null) {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId())) {
            return;
        }
        if (!RolesUtils.isTeacher(member)) {
            return;
        }

        textChannel.sendMessage(
                new EmbedBuilder().setTitle(String.format("Sondage demandé par %s", member.getEffectiveName()))
                        .setDescription(String.join(" ", args)).setColor(0x9b59b6).build()).queue(message -> {
            message.addReaction("\uD83D\uDC4D").queue();
            message.addReaction("\uD83E\uDD74").queue();
            message.addReaction("\uD83D\uDC4E").queue();
        });
    }
}

