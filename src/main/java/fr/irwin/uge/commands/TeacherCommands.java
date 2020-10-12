package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.managers.CallManager;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class TeacherCommands {

    @Command(name = "call")
    private void call(Guild guild, TextChannel textChannel, Member member, String[] args) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;
        if (!RolesUtils.isTeacher(member)) return;

        int time = 5;
        if (args.length == 1) {
            try {
                time = Integer.parseInt(args[0]);
                if (time < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(textChannel, "Veuillez saisir une durée en minutes valide.");
                return;
            }
        }

        new CallManager(textChannel, member, time);
    }

    @Command(name = "queue")
    private void queue(Guild guild, TextChannel textChannel, Member member) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;
        if (!RolesUtils.isTeacher(member)) return;

        if (TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Une file d'attente est déjà ouverte dans ce salon !");
            return;
        }

        TicketManager.getTicketManager(textChannel);
    }

    @Command(name = "next")
    private void next(Guild guild, TextChannel textChannel, Member member) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;
        if (!RolesUtils.isTeacher(member)) return;

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        TicketManager.getTicketManager(textChannel).takeNextTicket(member);
    }

}

