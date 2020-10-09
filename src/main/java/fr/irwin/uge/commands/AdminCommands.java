package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.config.Config;
import fr.irwin.uge.internals.AutoRole;
import fr.irwin.uge.managers.TrafficNotificationManager;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.regex.Pattern;

public class AdminCommands {

    @Command(name = "autorole")
    private void autoRole(Guild guild, TextChannel textChannel, Member member, Message message, String[] args) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;
        if (!RolesUtils.isAdmin(member)) return;

        if (args.length < 1 || !UGEBot.config().guilds.get(guild.getId()).autoRoles.containsKey(args[0])) {
            MessageUtils.sendErrorMessage(textChannel, "Vous devez spécifier le nom d'un AutoRole parmi la liste suivante :\n" +
                    "```\n" + String.join(", ", UGEBot.config().guilds.get(guild.getId()).autoRoles.keySet()) + "\n```");
            return;
        }

        final Config.Guild.AutoRole autoRoleConfig = UGEBot.config().guilds.get(guild.getId()).autoRoles.get(args[0]);
        final AutoRole autoRole = new AutoRole(textChannel, autoRoleConfig);

        if (args.length == 2) {
            String messageId;
            if (Pattern.matches("\\d{18}-\\d{18}", args[1])) {
                messageId = args[1].split("-")[1];
            } else {
                messageId = args[1];
            }
            if (autoRole.restore(messageId)) {
                message.addReaction(EmotesUtils.YES.replace(">", "")).queue();
            } else {
                message.addReaction(EmotesUtils.NO.replace(">", "")).queue();
            }
        } else {
            autoRole.send();
        }
    }

    @Command(name = "traffic")
    private void traffic(Guild guild, TextChannel textChannel, Member member, String[] args) {
        if (guild == null) return;
        if (!RolesUtils.isAdmin(member)) return;

        if (args.length == 0) {
            TrafficNotificationManager.registerTextChannel(guild, textChannel.getIdLong());
            return;
        }

        if (args[0].equals("off")) {
            TrafficNotificationManager.unregisterTextChannel(guild, textChannel.getIdLong());
        }
    }

}
