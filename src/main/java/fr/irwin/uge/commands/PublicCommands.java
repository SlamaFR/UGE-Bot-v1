package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.MessageUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PublicCommands {

    @Command(name = "ticket")
    private void ticket(Guild guild, TextChannel textChannel, Member member, Message message) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        if (TicketManager.getTicketManager(textChannel).openTicket(member)) {
            message.addReaction(EmotesUtils.YES.replace(">", "")).queue();
        } else {
            message.addReaction(EmotesUtils.NO.replace(">", "")).queue();
        }
    }

}
