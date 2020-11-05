package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.config.Config;
import fr.irwin.uge.features.MessageFeature;
import fr.irwin.uge.features.channel.TrafficNotifier;
import fr.irwin.uge.features.message.AutoRole;
import fr.irwin.uge.features.message.OrganizationDisplay;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.regex.Pattern;

public class AdminCommands
{
    @Command(name = "autorole")
    private void autoRole(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId()))
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length < 1 || !UGEBot.config().guilds.get(guild.getId()).autoRoles.containsKey(args[0]))
        {
            MessageUtils.sendErrorMessage(textChannel,
                    "Vous devez spécifier le nom d'un AutoRole parmi la liste suivante :\n" + "```\n" +
                    String.join(", ", UGEBot.config().guilds.get(guild.getId()).autoRoles.keySet()) + "\n```");
            return;
        }

        final Config.Guild.AutoRole autoRoleConfig = UGEBot.config().guilds.get(guild.getId()).autoRoles.get(args[0]);
        final AutoRole autoRole = new AutoRole(textChannel, autoRoleConfig);
        restoreOrStartMessageFeature(autoRole, message, args);

        message.delete().queue();
    }

    @Command(name = "traffic")
    private void traffic(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length == 0)
        {
            TrafficNotifier.instance().registerTextChannel(textChannel.getIdLong());
            return;
        }

        if (args[0].equals("off"))
        {
            TrafficNotifier.instance().unregisterTextChannel(textChannel.getIdLong());
        }
        message.delete().queue();
    }

    @Command(name = "display")
    private void display(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId()))
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length < 1 || !UGEBot.config().guilds.get(guild.getId()).organizationDisplays.containsKey(args[0]))
        {
            MessageUtils.sendErrorMessage(textChannel,
                    "Vous devez spécifier le nom d'un Display parmi la liste suivante :\n" + "```\n" +
                    String.join(", ", UGEBot.config().guilds.get(guild.getId()).organizationDisplays.keySet()) +
                    "\n```");
            return;
        }

        Config.Guild.OrganizationDisplay config = UGEBot.config().guilds.get(guild.getId()).organizationDisplays.get(args[0]);
        final OrganizationDisplay display = new OrganizationDisplay(guild.getIdLong(), textChannel.getIdLong(), config.title, config.fields);
        restoreOrStartMessageFeature(display, message, args);

        message.delete().queue();
    }

    private void restoreOrStartMessageFeature(MessageFeature feature, Message message, String[] args)
    {
        if (args.length == 2)
        {
            String messageId;
            if (Pattern.matches("\\d{18}-\\d{18}", args[1]))
            {
                messageId = args[1].split("-")[1];
            }
            else
            {
                messageId = args[1];
            }
            feature.restore(messageId, null);
        }
        else
        {
            feature.send();
        }
    }
}
